#!/usr/bin/env bash

SRC_DIR=vendor/dropsonde-protocol/events
DEST_DIR=src/main/java

protoc -I="${SRC_DIR}" --java_out="${DEST_DIR}" "${SRC_DIR}/metric.proto" "${SRC_DIR}/uuid.proto"
