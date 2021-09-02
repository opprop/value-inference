#!/bin/bash

# Fail the whole script if any command fails
set -e

export JSR308=$(cd $(dirname "$0")/.. && pwd)

if [ "$(uname)" == "Darwin" ] ; then
  export JAVA_HOME=${JAVA_HOME:-$(/usr/libexec/java_home)}
else
  export JAVA_HOME=${JAVA_HOME:-$(dirname $(dirname $(readlink -f $(which javac))))}
fi

if [ -d "/tmp/plume-scripts" ] ; then
  git -C /tmp/plume-scripts pull -q
else
  git -C /tmp clone --depth 1 -q https://github.com/plume-lib/plume-scripts.git
fi

# Default value is opprop. REPO_SITE may be set to other value for travis test purpose.
export REPO_SITE="${REPO_SITE:-opprop}"

echo "------ Downloading everything from REPO_SITE: $REPO_SITE ------"

# Build checker-framework
if [ -d $JSR308/checker-framework ] ; then
    (cd $JSR308/checker-framework && git pull)
else
    /tmp/plume-scripts/git-clone-related $REPO_SITE checker-framework $JSR308/checker-framework
fi

# Build checker-framework-inference
if [ -d $JSR308/checker-framework-inference ] ; then
    (cd $JSR308/checker-framework-inference && git pull)
else
    /tmp/plume-scripts/git-clone-related $REPO_SITE checker-framework-inference $JSR308/checker-framework-inference
fi

# This also builds annotation-tools
(cd $JSR308/checker-framework && checker/bin-devel/build.sh downloadjdk)

(cd $JSR308/checker-framework-inference && ./gradlew assemble dist && ./gradlew testLibJar)

echo "Fetching DLJC"

if [ -d $JSR308/do-like-javac ] ; then
    (cd $JSR308/do-like-javac && git pull)
else
    /tmp/plume-scripts/git-clone-related $REPO_SITE do-like-javac $JSR308/do-like-javac
fi

echo "Building value-inference without testing"

(cd $JSR308/value-inference && ./gradlew build -x test --console=plain)
