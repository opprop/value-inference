#!/bin/bash

if ! [ -n "$1" ]; then
    echo "This script gives the overall inference summary statistics from a corpus in a tabular format"
    echo "usage: $0 <corpus-root-folder-name>"
    exit 1
fi

cd $1

declare -a statsTimeKeys=(
    "smt_serialization_time(millisec)" \
    "smt_solving_time(millisec)" \
    "smt_unsat_serialization_time(millisec)" \
    "smt_unsat_solving_time(millisec)")


 declare -a statsKeys=(
    "total_slots" "total_constraints" \
    "constantslot" "total_variable_slots" \
    "subtypeconstraint" "equalityconstraint" "arithmeticconstraint" \
    "comparableconstraint" "existentialconstraint" "preferenceconstraint")

declare -a constantSlotsNameKeys=(
    "Top" "IntRange" "Bottom")

declare -a constantSlotsOutputKeys=(
    "UnknownVal" \
    "IntRange" "BottomVal")

declare -a projects=($(ls -d */ | sort))

pad=$(printf '%0.1s' " "{1..60})
padlength=30

# Print header row =============================================================
declare -a headers=(
    'Project' \
    'inference-failed' \
#    'expected-subtargets' \
#    'successful-subtargets' \
    'serialization-time(ms)' \
    'solving-time(ms)' \
    'unsat-serialization-time(ms)' \
    'unsat-solving-time(ms)' \
    'process-time(sec)'\
    'z3-bools' \
    'z3-ints' \
    'z3-asserts' \
    'z3-assert-softs')

for key in "${headers[@]}"; do
    printf '%s\t' "$key"
done
# for key in "${statsKeys[@]}"; do
#    printf '%s\t' "$key"
# done
for key in "${constantSlotsNameKeys[@]}"; do
    printf '%s\t' "$key"
done

printf '%s\t' "inserted-annotations"
for key in "${constantSlotsOutputKeys[@]}"; do
    printf '%s\t' "$key"
done

# printf '%s\t' "prefix-encoded"
# printf '%s\t' "baseunits-encoded"

printf '\n'

# Helper functions =============================================================

# count_basic key file [file2 ...]
function count_basic() {
    key="$1"
    shift
    files=$@

    count=$(grep -w "$key" $files | wc -l)
    printf '%s\t' "$count"
}

# Print each project ===========================================================
for project in "${projects[@]}"; do
    # remove trailing slash in project name
    project=$(printf '%*.*s' 0 $((${#project} - 1)) "$project")

    printf '%s\t' "$project"

    InferenceLogFile=$project/logs/infer.log
    if [ -f $InferenceLogFile ]; then
        # inference success?
        count_basic "!!! The set of constraints is unsatisfiable! !!!" "$InferenceLogFile"
        # number of sub-projects
#        count_basic "Running java" "$InferenceLogFile"
        # number of successful sub-projects
#        count_basic "Statistics have been written to" "$InferenceLogFile"
    else
        printf '%s\t' "1"
#        printf '%s\t' "0"
#        printf '%s\t' "0"
    fi

    InferenceStatsFile=$project/statistics.txt
    if [ -f $InferenceStatsFile ]; then
        for key in "${statsTimeKeys[@]}"; do
            # stats file might have more than 1 matching row, sum them up and print it
            grep -w "$key" "$InferenceStatsFile" | cut -d ':' -f 2 | \
                awk -v tab="\t" '{sum += $1} END {printf sum+0 tab}'
        done
    else
        for key in "${statsTimeKeys[@]}"; do
            printf '%s\t' "0"
        done
    fi

    InferenceTimingFile=$project/inferTiming.log
    if [ -f $InferenceTimingFile ]; then
        grep "Time taken by" "$InferenceTimingFile" | \
            cut -d $'\t' -f 2 | \
            xargs printf '%s\t'
    else
        printf '%s\t' "0"
    fi

    ConstraintsStatsFile=$project/z3ConstraintsGlob.smt
    if [ -f $ConstraintsStatsFile ]; then
        # number of z3 bools
        count_basic "declare-fun.*Bool" "$ConstraintsStatsFile"
        # number of z3 ints
        count_basic "declare-fun.*Int" "$ConstraintsStatsFile"
        # number of z3 asserts
        count_basic "assert" "$ConstraintsStatsFile"
        # number of z3 assert-softs
        count_basic "assert-soft" "$ConstraintsStatsFile"
    else
        printf '%s\t' "0"
        printf '%s\t' "0"
        printf '%s\t' "0"
        printf '%s\t' "0"
    fi

    # if [ -f $InferenceStatsFile ]; then
    #    for key in "${statsKeys[@]}"; do
    #        # stats file might have more than 1 matching row, sum them up and print it
    #        grep -w "$key" "$InferenceStatsFile" | cut -d ':' -f 2 | \
    #            awk -v tab="\t" '{sum += $1} END {printf sum+0 tab}'
    #    done
    # else
    #    for key in "${statsKeys[@]}"; do
    #         printf '%s\t' "0"
    #    done
    # fi

    InferenceSolutionsFile=$project/solutions.txt
    SOLUTIONSPrefix="Annotation: @"
    if [ -f $InferenceSolutionsFile ]; then
        for key in "${constantSlotsOutputKeys[@]}"; do
            count_basic "$SOLUTIONSPrefix$key" "$InferenceSolutionsFile"
        done
    else
        for key in "${constantSlotsOutputKeys[@]}"; do
            printf '%s\t' "0"
        done
    fi

    INSERTKey=insert-annotation
    QUALPrefix=@value.qual.
    if [ -f "$project/logs/infer_result_0.jaif" ]; then
        # there can be more than 1 result jaif file
        count_basic "$INSERTKey" $project/logs/infer_result_*.jaif

        for key in "${constantSlotsOutputKeys[@]}"; do
            count_basic "$INSERTKey.*$QUALPrefix$key" $project/logs/infer_result_*.jaif
        done
    else
        printf '%s\t' "0"
        for key in "${constantSlotsOutputKeys[@]}"; do
            printf '%s\t' "0"
        done
    fi

    if [ -f $InferenceStatsFile ]; then
        # stats file might have more than 1 matching row, sum them up and print it
        grep -w "serialize_prefix" "$InferenceStatsFile" | cut -d ':' -f 2 | \
            awk '{printf $1 "|"}'
        printf '\t'
        grep -w "serialize_baseunits" "$InferenceStatsFile" | cut -d ':' -f 2 | \
            awk '{printf $1 "|"}'
        printf '\t'
    else
        printf '%s\t' "0"
        printf '%s\t' "0"
    fi

    # InferenceLogFile=$project/logs/infer.log
    # if [ -f $InferenceLogFile ]; then
    #     # prefix encoded?
    #     count_basic "prefix: true" "$InferenceLogFile"
    #     # number of base units encoded
    #     count_basic "bu:" "$InferenceLogFile"
    # else
    #     printf '%s\t' "0"
    #     printf '%s\t' "0"
    # fi

    printf '\n'
done
