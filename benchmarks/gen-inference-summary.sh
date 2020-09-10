#!/bin/bash

if ! [ -n "$1" ]; then
    echo "This script gives the overall inference summary statistics from a corpus in a human readable format"
    echo "usage: $0 <corpus-root-folder-name>"
    exit 1
fi

declare -a statsKeys=("total_slots" "total_constraints" \
    "constantslot" "total_variable_slots" \
    "existentialvariableslot" "refinementvariableslot" "lubvariableslot" \
    "comparisonvariableslot" "arithmeticvariableslot" \
    "subtypeconstraint" "equalityconstraint" "arithmeticconstraint" \
    "comparisonconstraint" \
    "comparableconstraint" "existentialconstraint" "preferenceconstraint")

cd $1

declare -a projects=($(ls -d */ | sort))

pad=$(printf '%0.1s' " "{1..60})
padlength=30

for project in "${projects[@]}"; do
    # remove trailing slash in project name
    project=$(printf '%*.*s' 0 $((${#project} - 1)) "$project")

    printf '\n%s\n' "$project"

    InferenceLogFile=$project/logs/infer.log

    # number of sub-projects
    countKey="  expected-subtargets"
    padding=$(printf '%*.*s' 0 $((padlength - ${#countKey})) "$pad")
    if [ -f $InferenceLogFile ]; then
        count=$(grep "Running java" "$InferenceLogFile" | wc -l)
    else
        count=0
    fi
    echo -e "$countKey$padding\t$count"
    # number of successful sub-projects
    countKey="  successful-subtargets"
    padding=$(printf '%*.*s' 0 $((padlength - ${#countKey})) "$pad")
    if [ -f $InferenceLogFile ]; then
        count=$(grep "Statistics have been written to" "$InferenceLogFile" | wc -l)
    else
        count=0
    fi
    echo -e "$countKey$padding\t$count"

    for key in "${statsKeys[@]}"; do
        # string consisting of the stats key and the count
        keyArg="  ${key}"
        # string consisting of the stats key, count, and space padding to 30 total characters
        padding=$(printf '%*.*s' 0 $((padlength - ${#keyArg})) "$pad")

        if [ -f $InferenceLogFile ]; then
            # sift through the log files to find all the statistics values, sum them up and print it
            grep "$key" "$InferenceLogFile" | cut -d ':' -f 2 | \
                awk -v p="${keyArg}${padding}\t" '{sum += $1} END {print p sum+0}'
        else
            echo -e "${keyArg}${padding}\t0"
        fi
    done

    InferenceTimingFile=$project/inferTiming.log
    countKey="  process-time(sec)"
    padding=$(printf '%*.*s' 0 $((padlength - ${#countKey})) "$pad")
    if [ -f $InferenceTimingFile ]; then
        count=$(grep "Time taken by" "$InferenceTimingFile" | \
            cut -d $'\t' -f 2)
    else
        count=0
    fi
    echo -e "$countKey$padding\t$count"

done

printf '\n'
