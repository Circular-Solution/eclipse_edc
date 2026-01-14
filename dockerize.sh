#!/bin/bash

docker build -t edc-controlplane:latest -f launchers/controlplane/Dockerfile launchers/controlplane
docker build -t edc-dataplane:latest -f launchers/dataplane/Dockerfile launchers/dataplane

echo "Docker images built successfully:"
