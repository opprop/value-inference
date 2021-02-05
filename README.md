# Constant Value Inference (Interval Inference)

A type system that enforces [CERT-FIO08-J rule](https://www.securecoding.cert.org/confluence/display/java/FIO08-J.+Distinguish+between+characters+or+bytes+read+from+a+stream+and+-1) based on [Checker Framework](http://types.cs.washington.edu/checker-framework/)

Constant Value Inference is a type inference for the [Constant Value Checker](http://checkerframework.org/manual/#constant-value-checker/). It supports a subtype of the annotations in the Checker. The annotations are @UnknownVal, @BottomVal, @BoolVal, @IntRange(from, to), and @StringVal. The main annotation is @IntRange. @IntRange takes two arguments — a lower bound and an upper bound. Its meaning is that at run time, the expression evaluates to a value between the bounds (inclusive). For example, an expression of type @IntRange(from=0, to=255) evaluates to 0, 1, 2, …, 254, or 255.

## Dependencies

This project is developed based on [Checker Framework Inference](https://github.com/opprop/checker-framework-inference).

A `setup.sh` is provided to build these dependencies and also the Value Inference.

## Build

First, to have a better file structure, you may want to create a root directory called `jsr308`.

In `jsr308`, clone this project. In the clone, run `./setup.sh`. This script will download and build all neccessary dependencies, followed by building Value Inference and running test suites of Value Inference.

It is suggested to further configure `JSR308` environment variable for your convenience:

- In your bash profile file, export `JSR308` as the absolute path of your `jsr308` directory:

  ```bash
  export JSR308=<the absolute path of your jsr308 dir in your machine>
  ```

This `JSR308` environment variable is required for using my version of [do-like-javac](https://github.com/CharlesZ-Chen/do-like-javac) to run Cast Checker on a project with project's build command, and it also allows running Value Inference with a conciser command.

## How to run Value Inference to check your Java code

I have written a simple script `value-inference.sh` to make this task easier. You could just passing java files to this script, and this script will check all the java files you passing through.

e.g.

```bash
$JSR308/value-inference/value-inference.sh <true?> <your java files>
$JSR308/value-inference/value-inference.sh true aSingleFile.java
$JSR308/value-inference/value-inference.sh true **/*.java
$JSR308/value-inference/value-inference.sh true FileA.java FileB.java ... FileN.java
```

For the detailers, this script just a wrap-up of below command:

```bash
value-inference/../checker-framework/checker/bin-devel/javac -processor value.ValueChecker -cp value-inference/bin:value-inference/lib <your java files>
```

### Running Cast Checker on a project by do-like-javac

In your project, just run `run-dljc.sh` with the build cmd of your project:

```bash
$JSR308/value-inference/run-dljc.sh true <your build cmd, e.g. `ant build` or `mvn install`>
```

Note: 
  1. using `do-like-javac` needs `JSR308` environment variable.
  2. running a Checker by `do-like-javac` on a project needs this project is in a "clean" state. In other words, you should do a `clean` command in your project before runnning Cast Checker on it.

Details of `do-like-javac` could be find [here](https://github.com/SRI-CSL/do-like-javac).

## Notes on useful materials
- [CERT rule FIO08-J](https://www.securecoding.cert.org/confluence/display/java/FIO08-J.+Distinguish+between+characters+or+bytes+read+from+a+stream+and+-1)
- [William Pugh, Defective Java Code: Turning WTF Code into a Learning Experience, JavaOne Conference, 2008.](http://www.oracle.com/technetwork/server-storage/ts-6589-159312.pdf)
- [WTF CodeSOD 20061102](http://thedailywtf.com/articles/Please_Supply_a_Test_Case)
