#!/usr/bin/env sh

set -e

pushd java-buildpack-metric-writer
  ./mvnw -q package
popd
