#!/bin/bash

# Classroom-specific setup
mkdir -p /autograder/source
mkdir -p /autograder/submission
mkdir -p /autograder/results

# Copy submission to grading location
if [ -d "/submission" ]; then
    cp -r /submission/* /autograder/submission/
fi

# Install dependencies
apt-get update
apt-get install -y openjdk-11-jdk

# Run setup
bash setup.sh

# Run autograder
bash autograder/run_autograder.sh
