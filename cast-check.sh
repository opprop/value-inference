#!/bin/bash

WORKING_DIR=$(pwd)
ROOT=$(cd $(dirname "$0")/.. && pwd)
JAVAC=$ROOT/checker-framework/checker/bin-devel/javac

CAST_CHECKER=$ROOT/cast_checker

cd $WORKING_DIR

java_files=$1
shift
while [ $# -gt 0 ]
do
    java_files="$java_files $1"
    shift
done

# -Acfgviz=org.checkerframework.dataflow.cfg.DOTCFGVisualizer,verbose,outdir=dotfile
$JAVAC -processor cast.CastChecker -cp $CAST_CHECKER/bin:$CAST_CHECKER/lib $java_files
