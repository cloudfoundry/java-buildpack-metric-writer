#!/usr/bin/env sh

set -e

pushd java-buildpack-metric-writer
  ./mvnw -q -Dmaven.test.skip=true deploy
popd
