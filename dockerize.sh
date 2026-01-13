#!/bin/bash

docker build --platform linux/amd64 -t edc-minimal:latest -f launchers/minimal/Dockerfile launchers/minimal

echo "Docker images built successfully:"
