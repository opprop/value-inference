#!/bin/bash

WORKING_DIR=$(pwd)
ROOT=$(cd $(dirname "$0")/.. && pwd)
JAVAC=$ROOT/checker-framework/checker/bin-devel/javac

CAST_CHECKER=$ROOT/cast_checker

cd $WORKING_DIR

files=$1
java_files=""
shift
while [ $# -gt 0 ]
do
    files="$files $1"
    shift
done

for entry in $(find $files -name '*.java' -or -name '*.doc')
do
    java_files="$java_files $entry"
done

echo $java_files

# no cast checker invocation:
# $JAVAC $java_files

# has cast checker invocation:
# -Acfgviz=org.checkerframework.dataflow.cfg.DOTCFGVisualizer,verbose,outdir=dotfile
$JAVAC -processor cast.CastChecker -cp $CAST_CHECKER/bin:$CAST_CHECKER/lib -Aflowdotdir=someDirectory $java_files
