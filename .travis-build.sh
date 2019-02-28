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

echo "Starting security-demo tests"

. ./test-cast.sh travis