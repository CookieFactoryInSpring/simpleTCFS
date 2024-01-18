#!/usr/bin/env bash

echo "Compiling the TCF Spring BACKEND within a multi-stage docker build"

docker build --build-arg JAR_FILE=simpleTCFS-0.0.1-SNAPSHOT.jar -t pcollet/tcf-spring-backend .
