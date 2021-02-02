#!/bin/bash

# builds & test units-inference, runs it on the corpus, and grabs the results

# Split $TRAVIS_REPO_SLUG into the owner and repository parts
OIFS=$IFS
IFS='/'
read -r -a slugarray <<< "$TRAVIS_REPO_SLUG"
SLUGOWNER=${slugarray[0]}
SLUGREPO=${slugarray[1]}
IFS=$OIFS

export REPO_SITE=$SLUGOWNER

# Build dependencies
. ./setup.sh

echo "Starting value-inference tests"

# Currently we only run basic tests in value-inference.
# TODO run with corpus
./gradlew test
