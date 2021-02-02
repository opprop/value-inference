#!/bin/bash

# Fail the whole script if any command fails
set -e

export JAVA_HOME=${JAVA_HOME:-$(dirname $(dirname $(dirname $(readlink -f $(/usr/bin/which java)))))} 
export JSR308=$(cd $(dirname "$0")/.. && pwd)

# export SHELLOPTS

#default value is opprop. REPO_SITE may be set to other value for travis test purpose.
export REPO_SITE="${REPO_SITE:-txiang61}"

echo "------ Downloading everything from REPO_SITE: $REPO_SITE ------"

##### build checker-framework-inference
if [ -d $JSR308/checker-framework-inference ] ; then
    (cd $JSR308/checker-framework-inference && git pull)
else
    BRANCH=master
    (cd $JSR308 && git clone -b $BRANCH --depth 1 https://github.com/"$REPO_SITE"/checker-framework-inference.git)
fi

(cd $JSR308/checker-framework-inference && ./.travis-build-without-test.sh)


echo "Fetching DLJC"

if [ -d $JSR308/do-like-javac ] ; then
    (cd $JSR308/do-like-javac && git pull)
else
    BRANCH=master
    (cd $JSR308 && git clone -b $BRANCH --depth 1 https://github.com/"$REPO_SITE"/do-like-javac.git)
fi


echo "Building value-inference without testing"

(cd $JSR308/value-inference && ./gradlew build -x test --console=plain)
