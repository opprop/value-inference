import yaml
import os, sys
import subprocess
import shlex
import argparse

# python run-corpus.py 
# --corpus-file <absPath> --executable <absPath>
ONTOLOGY_DIR = os.path.dirname(os.path.realpath(__file__))

def main(argv):
    parser = argparse.ArgumentParser()
    parser.add_argument('--corpus-file', dest='corpus_file', required=True)
    parser.add_argument('--executable', dest='executable', required=True)
    args = parser.parse_args()

    tool_excutable = os.path.abspath(args.executable)

    corpus_name = os.path.splitext(os.path.basename(args.corpus_file))[0]
    corpus_dir = os.path.dirname(os.path.abspath(args.corpus_file))

    BENCHMARK_DIR = os.path.join(corpus_dir, corpus_name)

    print "----- Fetching corpus... -----"
    if not os.path.exists(BENCHMARK_DIR):
        print "Creating corpus dir {}.".format(BENCHMARK_DIR)
        os.makedirs(BENCHMARK_DIR)
        print "Corpus dir {} created.".format(BENCHMARK_DIR)

    print "Loading corpus file..."
    projects = None
    with open (args.corpus_file) as projects_file:
        projects = yaml.load(projects_file)["projects"]
    print "Loading corpus file done."

    print projects

    print "Enter corpus dir {}.".format(BENCHMARK_DIR)
    os.chdir(BENCHMARK_DIR)

    for project_name, project_attrs in projects.iteritems():
        project_dir = os.path.join(BENCHMARK_DIR, project_name)
        if not os.path.exists(project_dir):
            git("clone", project_attrs["giturl"], "--depth", "1")

    print "----- Fetching corpus done. -----"

    print "----- Runnning Executable on corpus... -----"

    failed_projects = list()

    for project_name, project_attrs in projects.iteritems():
        project_dir = os.path.join(BENCHMARK_DIR, project_name)
        os.chdir(project_dir)
        print "Enter directory: {}".format(project_dir)
        if project_attrs["clean"] == '' or project_attrs["build"] == '':
            print "Skip project {}, as there were no build/clean cmd.".format(project_name)
        print "Cleaning project..."
        subprocess.call(shlex.split(project_attrs["clean"]))
        print "Cleaning done."
        print "Running command: {}".format(tool_excutable + " " + project_attrs["build"])
        rtn_code = subprocess.call([tool_excutable, project_attrs["build"]])
        print "Return code is {}.".format(rtn_code)
        if not rtn_code == 0:
            failed_projects.append(project_name)

    if len(failed_projects) > 0:
        print "----- Executable failed on {} out of {} projects. Failed projects are: {} -----".format(len(failed_projects), len(projects), failed_projects)
    else:
        print "----- Executable succeed infer all {} projects. -----".format(len(projects))

    print "----- Runnning Executable on corpus done. -----"

    rtn_code = 1 if len(failed_projects) > 0 else 0

    sys.exit(rtn_code)

def git(*args):
    return subprocess.check_call(['git'] + list(args))

if __name__ == "__main__":
    main(sys.argv)