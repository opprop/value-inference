import sys
import os
import subprocess
import yaml
import shlex
import argparse
import time
import multiprocessing
import signal

SCRIPTS_DIR = os.path.dirname(os.path.realpath(__file__))
UNITS_INFERENCE_DIR = os.path.join(SCRIPTS_DIR)
INFER_SCRIPT = os.path.join(SCRIPTS_DIR, "run-dljc-infer.sh")
LOG_FILENAME = "inferTiming.log"

# Returns number of jobs completed by the workers
def jobs_completed(results):
    count = 0
    for result in results:
        if result.ready():
            count += 1

    return count

# Custom sleep delay
SLEEP_DELAYS = [1, 1, 2, 3, 5, 8, 13, 21, 34, 55]
def sleep_delay(n):
    return SLEEP_DELAYS[n] if n < 10 else 60

# Used to redirect a Ctrl + C from a worker to the parent try catch
class KeyboardInterruptException(Exception): pass

# Run a single experiment and return the final exit code
def run_worker(project_dir, project_name, project_attrs, optimizing_mode):
    print("Running {} on worker process (pid = {})".format(project_name, os.getpid()))

    try:
        os.chdir(project_dir)

        log_file = os.path.join(project_dir, LOG_FILENAME)

        print("Timing log: {}".format(log_file))

        with open(log_file, "w") as log:
            log.write("Enter directory: {}\n".format(project_dir))

            if project_attrs["clean"] == '' or project_attrs["build"] == '':
                log.write(
                    "Skip project {}, as there were no build/clean cmd.\n".format(project_name))
                return 1

            try:
                cmd = shlex.split(project_attrs["clean"])
                log.write("Cleaning project with command {}\n".format(cmd))
                log.flush()   # manual flush required to sequence subprocess content
                proc = subprocess.Popen(cmd, stdout=log, stderr=log)
                proc.wait()
                log.write("Cleaning done.\n")

                cmd = [INFER_SCRIPT, optimizing_mode, project_attrs["build"]]
                log.write("Running command: {}\n".format(cmd))
                log.flush()   # manual flush required to sequence subprocess content
                start = time.time()
                proc = subprocess.Popen(cmd, stdout=log, stderr=log)
                proc.wait()
                end = time.time()
                rtn_code = proc.returncode
                log.write("Return code is {}.\n".format(rtn_code))

                log.write("Time taken by {}: \t{}\t seconds\n".format(project_name, end - start))

                print("Completed job for {}".format(project_name))
                return rtn_code

            except SystemExit:  # triggered by pool.terminate()
                if proc is not None:
                    print("Killing {}".format(project_name))
                    proc.kill()
                return 1

    except KeyboardInterrupt:
        raise KeyboardInterruptException()

# Calls git command with given args, returns git command exit code
def git(*args):
    git_command = ['git'] + list(args)
    print("Running " + " ".join(git_command))
    return subprocess.check_call(git_command)


# Main program
def main(argv):
    parser = argparse.ArgumentParser()
    parser.add_argument('--corpus-file', dest='corpus_file', required=True)
    parser.add_argument('--corpus', dest='corpus')
    parser.add_argument('--optimizing-mode', dest='is_optimizing_mode', action='store_true')
    parser.add_argument('--no-optimizing-mode', dest='is_optimizing_mode', action='store_false')
    parser.set_defaults(is_optimizing_mode=False)
    parser.add_argument('--run-in-parallel', dest='run_in_parallel', action='store_true')
    parser.add_argument('--no-run-in-parallel', dest='run_in_parallel', action='store_false')
    parser.set_defaults(run_in_parallel=True)
    parser.add_argument('--is-travis-build', dest='is_travis_build', action='store_true')
    args = parser.parse_args()

    print("debug: {}".format(args.run_in_parallel))

    corpus_name = args.corpus if not args.corpus == None else os.path.splitext(args.corpus_file)[0]
    optimizing_mode = 'true' if args.is_optimizing_mode else 'false'

    print("optimizing-mode: {}".format(optimizing_mode))
    print("run-in-parallel: {}".format(args.run_in_parallel))
    print("is-travis-build: {}".format(args.is_travis_build))

    BENCHMARK_DIR = os.path.join(UNITS_INFERENCE_DIR, "benchmarks", corpus_name)

    print("----- Fetching corpus... -----")
    if not os.path.exists(BENCHMARK_DIR):
        print("Creating corpus dir {}.".format(BENCHMARK_DIR))
        os.makedirs(BENCHMARK_DIR)
        print("Corpus dir {} created.".format(BENCHMARK_DIR))

    print("Enter corpus dir {}.".format(BENCHMARK_DIR))
    os.chdir(BENCHMARK_DIR)

    projects = None
    with open (os.path.join(UNITS_INFERENCE_DIR, "benchmarks", args.corpus_file)) as projects_file:
        projects = yaml.load(projects_file)["projects"]

    for project_name, project_attrs in projects.iteritems():
        project_dir = os.path.join(BENCHMARK_DIR, project_name)
        if not os.path.exists(project_dir):
            # clone the branch if it is defined
            if "branch" in project_attrs and project_attrs["branch"]:
                git("clone", project_attrs["giturl"], "--depth", "1", "--branch", project_attrs["branch"])
            else:
                git("clone", project_attrs["giturl"], "--depth", "1")

    print("----- Fetching corpus done. -----")

    print("----- Running Units Inference on corpus... -----")

    successful_projects = list()
    failed_projects = list()

    num_cpu = multiprocessing.cpu_count()
    num_workers = 1
    if args.run_in_parallel:
        num_workers = max(num_cpu - 1, 1)

    # TODO: with 8gb ram, might be able to run 2 parallel z3 instances

    print("Available CPUs in System = " + str(num_cpu))
    print("Creating " + str(num_workers) + " worker(s) in the pool")

    pool = multiprocessing.Pool(num_workers)

    # try block for handling Ctrl + C keyboard interrupts
    try:
        project_names = []
        results = []
        for project_name, project_attrs in projects.iteritems():
            project_dir = os.path.join(BENCHMARK_DIR, project_name)
            project_names.append(project_name)
            # add the jobs into the job queue, execute asynchronously from this process
            # each job is a call to run_worker(project_dir, project_name, project_attrs, optimizing_mode)
            results.append(pool.apply_async(run_worker,
                (project_dir, project_name, project_attrs, optimizing_mode)))

        total_jobs = len(results)
        print("Queued {} jobs".format(total_jobs))

        # Set up a wait loop for all async processes to finish
        initial_sleep_index = 0
        sleep_index = initial_sleep_index
        sleep_time = sleep_delay(sleep_index)

        last_completed = 0  # most recent number of completed jobs
        completed = 0   # current number of completed jobs

        while completed < total_jobs:
            # print("sleeping for " + str(sleep_time))
            time.sleep(sleep_time)
            completed = jobs_completed(results)

            # if any new jobs is completed, reset sleep index as the next job
            # worked on might not take as long as the previous to complete
            if last_completed < completed:
                last_completed = completed
                sleep_index = initial_sleep_index
            else:
                sleep_index += 1

            # progressively sleep longer
            sleep_time = sleep_delay(sleep_index)

    except KeyboardInterrupt:
        print("Caught KeyboardInterrupt, terminating workers")
        pool.terminate()
        pool.join()
        # TODO: kill the relevant z3 processes too, via matching project names
        sys.exit(1)

    else:
        print("All jobs completed")
        pool.close()
        pool.join()

        print("Final results:")
        for i in range(0, len(project_names)):
            project_name = project_names[i]
            project_rtn_code = results[i].get()
            if not project_rtn_code == 0:
                failed_projects.append(project_name)
            else:
                successful_projects.append(project_name)

        if len(failed_projects) > 0:
            print("----- Inference failed on {} out of {} projects. -----".format(len(failed_projects), len(projects)))
            print("  Successful projects are: {}.".format(successful_projects))
            print("  Failed projects are: {}.".format(failed_projects))
        else:
            print("----- Inference successfully inferred all {} projects. -----".format(len(projects)))

        print("----- Running Units Inference on corpus done. -----")

        rtn_code = 1 if len(failed_projects) > 0 else 0

        # DEBUGGING FOR TRAVIS
        if args.is_travis_build:
            print("----- Log file contents of failed projects: -----" + '\n')

            for project_name in failed_projects:
                log_file = os.path.join(BENCHMARK_DIR, project_name, "logs", "infer.log")
                print("------------------------------------------------------------")
                print(log_file)
                print("------------------------------------------------------------")

                try:
                    log_file_content = open(log_file, "r")
                    print(log_file_content.read())
                except IOError:
                    print("log file does not exist")

                print("------------------------------------------------------------")
                print("end of " + log_file)
                print("------------------------------------------------------------")

        sys.exit(rtn_code)

if __name__ == "__main__":
    main(sys.argv)
