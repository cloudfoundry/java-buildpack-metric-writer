#!/usr/bin/env sh

set -e

cd java-buildpack-metric-writer

./mvnw -q package
