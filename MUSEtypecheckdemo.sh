#!/bin/bash

# TESTFILE=testinput/typecheck/CopyToCast.java
# TESTFILE=testinput/typecheck/ImmutableListProblem.java
TESTFILEREAD=FooProject/src/JavaRead.java
TESTFILEWRITE=FooProject/src/JavaWrite.java


subl $TESTFILEREAD
subl $TESTFILEWRITE

./cast-check.sh $TESTFILEREAD
./cast-check.sh $TESTFILEWRITE
