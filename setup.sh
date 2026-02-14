#!/bin/bash

# Build reference solution
cd reference_solution
gradle build
cd ..

# Create autograder config
mkdir -p /autograder/results

# Set permissions
chmod +x autograder/run_autograder.sh

echo "Setup complete"
