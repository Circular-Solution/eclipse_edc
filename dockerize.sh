#!/bin/bash

docker build -t eclipse-edc-minimal:latest -f launchers/minimal/Dockerfile launchers/minimal

echo "Docker images built successfully:"
