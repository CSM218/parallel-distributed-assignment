#!/usr/bin/env python3
"""
Verification script for autograder infrastructure
Ensures all required files are in place
"""

import os
import json
from pathlib import Path

REQUIRED_FILES = {
    "Core Autograder": [
        "autograder/grade.py",
        "autograder/run_autograder.sh",
        "autograder/config.json",
    ],
    "Test Suites": [
        "autograder/tests/test_rpc_basic.py",
        "autograder/tests/test_protocol_structure.py",
        "autograder/tests/test_concurrency.py",
        "autograder/tests/test_parallel_execution.py",
        "autograder/tests/test_failure_handling.py",
        "autograder/tests/test_advanced_protocol.py",
    ],
    "Test Harness": [
        "autograder/harness/ProcessLauncher.java",
        "autograder/harness/DistributedSystemRunner.java",
        "autograder/harness/integration_test.py",
    ],
    "Reference Solution": [
        "reference_solution/src/main/java/pdc/Message.java",
        "reference_solution/src/main/java/pdc/ReferenceMaster.java",
        "reference_solution/src/main/java/pdc/ReferenceWorker.java",
        "reference_solution/build.gradle",
    ],
    "GitHub Actions": [
        ".github/workflows/classroom.yml",
    ],
    "Documentation": [
        "README_AUTOGRADER.md",
        "ASSIGNMENT.md",
        "rubric.json",
    ],
    "Setup Scripts": [
        "setup.sh",
        "classroom_setup.sh",
    ]
}

def verify():
    """Verify all required files exist"""
    print("=" * 60)
    print("CSM218 AUTOGRADER INFRASTRUCTURE VERIFICATION")
    print("=" * 60 + "\n")
    
    total_files = 0
    found_files = 0
    
    for category, files in REQUIRED_FILES.items():
        print(f"[{category}]")
        for filepath in files:
            total_files += 1
            exists = os.path.exists(filepath)
            status = "✓" if exists else "✗"
            print(f"  {status} {filepath}")
            if exists:
                found_files += 1
        print()
    
    print("=" * 60)
    print(f"Summary: {found_files}/{total_files} files present")
    
    if found_files == total_files:
        print("Status: ✓ COMPLETE")
        return True
    else:
        print(f"Status: ✗ INCOMPLETE ({total_files - found_files} missing)")
        return False

if __name__ == "__main__":
    success = verify()
    exit(0 if success else 1)
