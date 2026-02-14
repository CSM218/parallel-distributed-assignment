#!/bin/bash

set -e

echo "=== CSM218 Autograder Start ==="

# Get the directory where this script is located
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Change to the autograder directory
cd "$SCRIPT_DIR"

# Run Python grading script with forwarded arguments
python3 grade.py "$@"

echo "=== Autograder Complete ==="
