#!/bin/bash

ROOT=$(cd $(dirname "$0")/.. && pwd)

CFI=$ROOT/checker-framework-inference

AFU=$ROOT/annotation-tools/annotation-file-utilities
export PATH=$AFU/scripts:$PATH

CHECKER=value.ValueChecker

SOLVER=value.solver.ValueSolverEngine
DEBUG_SOLVER=checkers.inference.solver.DebugSolver
IS_HACK=true

# SOLVER="$DEBUG_SOLVER"
# IS_HACK=false
# DEBUG_CLASSPATH=""

if [ -n "$1" ] && [ $1 = "true" ]; then SOLVERARGS=solver=Z3smt,optimizingMode=true,collectStatistics=true,writeSolutions=true,noAppend=true
else  SOLVERARGS=solver=Z3smt,collectStatistics=true,writeSolutions=true,noAppend=true
fi

SECURITYPATH=$ROOT/value-inference/build/classes/java/main
export CLASSPATH=$SECURITYPATH:$DEBUG_CLASSPATH:.
export external_checker_classpath=$SECURITYPATH

CFI_LIB=$CFI/lib
export DYLD_LIBRARY_PATH=$CFI_LIB
export LD_LIBRARY_PATH=$CFI_LIB
export JAVA_LIBRARY_PATH=$CFI_LIB

# TYPE CHECKING
# $CFI/scripts/inference-dev --checker "$CHECKER" --solver "$SOLVER" --solverArgs="collectStatistics=true,solver=z3" --hacks="$IS_HACK" -m TYPECHECK "$@"

# Inference
if [ -n "$1" ] && [ $1 = "true" ]; then
    $CFI/scripts/inference-dev -m ROUNDTRIP --checker "$CHECKER" \
        --solver "$SOLVER" --solverArgs="$SOLVERARGS" \
        --hacks="$IS_HACK" -afud ./annotated "${@:2}"
else
    # Logging level set to SEVERE to hide output spam
    # --logLevel "SEVERE" \
    # see java.util.logging.Level
    $CFI/scripts/inference-dev -m ROUNDTRIP --checker "$CHECKER" \
        --solver "$SOLVER" --solverArgs="$SOLVERARGS" \
        --logLevel "INFO" \
        --hacks="$IS_HACK" -afud ./annotated "$@"

            # --cfArgs "-doe" \
fi
