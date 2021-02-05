#!/bin/bash

# Fail the whole script if any command fails
set -e

export JSR308=$(cd $(dirname "$0")/.. && pwd)

if [ "$(uname)" == "Darwin" ] ; then
  export JAVA_HOME=${JAVA_HOME:-$(/usr/libexec/java_home)}
else
  export JAVA_HOME=${JAVA_HOME:-$(dirname $(dirname $(readlink -f $(which javac))))}
fi

# Default value is opprop. REPO_SITE may be set to other value for travis test purpose.
export REPO_SITE="${REPO_SITE:-txiang61}"

echo "------ Downloading everything from REPO_SITE: $REPO_SITE ------"

# Build checker-framework
if [ -d $JSR308/checker-framework ] ; then
    (cd $JSR308/checker-framework && git pull)
else
    BRANCH=master
    (cd $JSR308 && git clone -b $BRANCH --depth 1 https://github.com/"$REPO_SITE"/checker-framework.git)
fi

# Build checker-framework-inference
if [ -d $JSR308/checker-framework-inference ] ; then
    (cd $JSR308/checker-framework-inference && git pull)
else
    BRANCH=master
    (cd $JSR308 && git clone -b $BRANCH --depth 1 https://github.com/"$REPO_SITE"/checker-framework-inference.git)
fi

# This also builds annotation-tools
(cd $CHECKERFRAMEWORK && checker/bin-devel/build.sh downloadjdk)

(cd $JSR308/checker-framework-inference && ./gradlew assemble dist && ./gradlew testLibJar)

echo "Fetching DLJC"

if [ -d $JSR308/do-like-javac ] ; then
    (cd $JSR308/do-like-javac && git pull)
else
    BRANCH=master
    (cd $JSR308 && git clone -b $BRANCH --depth 1 https://github.com/"$REPO_SITE"/do-like-javac.git)
fi

echo "Building value-inference without testing"

(cd $JSR308/value-inference && ./gradlew build -x test --console=plain)
