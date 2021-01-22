#!/bin/bash

JSR308=$(cd $(dirname "$0")/.. && pwd)

echo "$JSR308"/value-inference

CFI=$JSR308/checker-framework-inference
VI=$JSR308/value-inference
VIPATH=$VI/build/classes/java/main:$VI/build/resources/main:$VI/build/libs/value-inference.jar

export AFU=$JSR308/annotation-tools/annotation-file-utilities
export PATH=$AFU/scripts:$PATH

CHECKER=value.ValueChecker

SOLVER=value.solver.ValueSolverEngine
DEBUG_SOLVER=checkers.inference.solver.DebugSolver
IS_HACK=true

# SOLVER="$DEBUG_SOLVER"
# IS_HACK=false
# DEBUG_CLASSPATH=""

if [ -n "$1" ] && [ $1 = "true" ]; then
    SOLVERARGS=solver=Z3smt,optimizingMode=true,collectStatistics=true,writeSolutions=true,noAppend=true
else
    SOLVERARGS=solver=Z3smt,collectStatistics=true,writeSolutions=true,noAppend=true
fi

export CLASSPATH=$VIPATH:$DEBUG_CLASSPATH:.
export external_checker_classpath=$VIPATH

CFI_LIB=$CFI/lib
export DYLD_LIBRARY_PATH=$CFI_LIB
export LD_LIBRARY_PATH=$CFI_LIB
export JAVA_LIBRARY_PATH=$CFI_LIB

# NOTE: ROUNDTRIP mode actually writes out files to annotated, INFER mode only
# performs inference without writing to annotated

# Inference
if [ -n "$1" ] && [ $1 = "true" ]; then
    $CFI/scripts/inference-dev -m ROUNDTRIP_TYPECHECK --checker "$CHECKER" \
        --solver "$SOLVER" --solverArgs="$SOLVERARGS" \
        --hacks="$IS_HACK" -afud ./annotated "${@:2}"
else
    # Logging level set to SEVERE to hide output spam
    # --logLevel "SEVERE" \
    # see java.util.logging.Level
    $CFI/scripts/inference-dev -m ROUNDTRIP --checker "$CHECKER" \
        --solver "$SOLVER" --solverArgs="$SOLVERARGS" --debug=5005\
        --logLevel "INFO" \
        --hacks="$IS_HACK" -afud ./annotated "$@"

            # --cfArgs "-doe" \
fi
