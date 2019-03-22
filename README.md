# Cast Checker Inference 

[![build-status](https://travis-ci.org/txiang61/cast_checker.svg?branch=master)](https://travis-ci.org/txiang61/cast_checker)

A type system that enforces [CERT-FIO08-J rule](https://www.securecoding.cert.org/confluence/display/java/FIO08-J.+Distinguish+between+characters+or+bytes+read+from+a+stream+and+-1) based on [Checker Framework](http://types.cs.washington.edu/checker-framework/)

## Dependencies

This project is developed based on [Checker Framework](http://types.cs.washington.edu/checker-framework/). To use this checker, below dependencies are required:

- checker framework
- jsr308-langtools
- annotation-tools

I have a `setup.sh` to build these dependencies and also the Cast Checker. This `setup.sh` needs following tools to be ready in your machine before running it:

- [ant](http://ant.apache.org/manual/install.html)
- [mercurial](https://www.mercurial-scm.org/wiki/Download)
- [git](https://git-scm.com/book/en/v2/Getting-Started-Installing-Git)

## Build

First, to have a better file structure, you may want to create a root directory called `jsr308`.

In `jsr308`, clone this project. In the clone, run `./setup.sh`. This script will download and build all neccessary dependencies, followed by building Cast Checker and running test suites of Cast Checker.

It is suggested to further configure `JSR308` environment variable for your convenience:

- In your bash profile file, export `JSR308` as the absolute path of your `jsr308` directory:

  ```bash
  export JSR308=<the absolute path of your jsr308 dir in your machine>
  ```

This `JSR308` environment variable is required for using my version of [do-like-javac](https://github.com/CharlesZ-Chen/do-like-javac) to run Cast Checker on a project with project's build command, and it also allows running Cast Checker with a conciser command.

## How to run Cast Checker to check your Java code

### Foo Project demo

in `cast_checker` clone, I've attached a `FooProject` as a demo of . You can run Cast Checker on this foo project in two ways:

 1. running Cast Checker directly on source files of FooProject

  e.g. In dir `FooProject/` :

  ```bash
  ../cast-check.sh src/Foo.java
  ```
 2. running Cast Checker on FooProject by FooProject's build command (needs configure `$JSR308` environment variable)

  e.g. In dir `FooProject/` :

  ```bash
  ant clean
  ../run-dljc.sh ant
  ```

The subsections below introduce the details of each way of running Cast Checker.

### Running Cast Checker on Java file(s)

I have written a simple script `cast-check.sh` to make this task easier. You could just passing java files to this script, and this script will check all the java files you passing through.

e.g.

```bash
$JSR308/cast_checker/cast-check.sh <your java files>
$JSR308/cast_checker/cast-check.sh aSingleFile.java
$JSR308/cast_checker/cast-check.sh **/*.java
$JSR308/cast_checker/cast-check.sh FileA.java FileB.java ... FileN.java
```

For the detailers, this script just a wrap-up of below command:

```bash
cast_checker/../checker-framework/checker/bin-devel/javac -processor cast.cast_checker -cp cast_checker/bin:cast_checker/lib <your java files>
```

### Running Cast Checker on a project by do-like-javac

In your project, just run `run-dljc.sh` with the build cmd of your project:

```bash
$JSR308/cast_checker/run-dljc.sh <your build cmd, e.g. `ant build` or `mvn install`>
```

Note: 
  1. using `do-like-javac` needs `JSR308` environment variable.
  2. running a Checker by `do-like-javac` on a project needs this project is in a "clean" state. In other words, you should do a `clean` command in your project before runnning Cast Checker on it.

Details of `do-like-javac` could be find [here](https://github.com/SRI-CSL/do-like-javac).

## Notes on useful materials
- [CERT rule FIO08-J](https://www.securecoding.cert.org/confluence/display/java/FIO08-J.+Distinguish+between+characters+or+bytes+read+from+a+stream+and+-1)
- [William Pugh, Defective Java Code: Turning WTF Code into a Learning Experience, JavaOne Conference, 2008.](http://www.oracle.com/technetwork/server-storage/ts-6589-159312.pdf)
- [WTF CodeSOD 20061102](http://thedailywtf.com/articles/Please_Supply_a_Test_Case)
