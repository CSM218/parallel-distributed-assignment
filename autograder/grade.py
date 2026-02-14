#!/usr/bin/env python3

import json
import sys
import os
import subprocess
from pathlib import Path

# Import test modules
sys.path.insert(0, os.path.join(os.path.dirname(__file__), 'tests'))

from test_rpc_basic import AutograderTest
from test_parallel_execution import ParallelExecutionTest
from test_failure_handling import FailureHandlingTest
from test_protocol_structure import ProtocolStructureTest
from test_concurrency import ConcurrencyTest

class Grader:
    def __init__(self):
        self.results = {}
        self.total_score = 0.0
        self.output_path = "/autograder/results/results.json"
        
    def compile_code(self):
        """Compile student and reference code"""
        try:
            # Compile student code
            result = subprocess.run(
                ["./gradlew", "build"],
                cwd="/submission",
                capture_output=True,
                timeout=120
            )
            
            if result.returncode != 0:
                print("COMPILATION FAILED")
                print(result.stderr.decode())
                return False
            
            print("✓ Compilation successful")
            return True
        except Exception as e:
            print(f"✗ Compilation error: {e}")
            return False
    
    def run_tests(self):
        """Run all test suites"""
        test_suites = [
            ("RPC Basic Tests", AutograderTest()),
            ("Parallel Execution Tests", ParallelExecutionTest()),
            ("Failure Handling Tests", FailureHandlingTest()),
            ("Protocol Structure Tests", ProtocolStructureTest()),
            ("Concurrency Tests", ConcurrencyTest()),
        ]
        
        all_results = {}
        all_weights = {}
        
        for suite_name, tester in test_suites:
            print(f"\n--- {suite_name} ---")
            results = tester.run_all()
            
            for test_name, test_result in results.items():
                full_name = f"{suite_name}::{test_name}"
                status = "✓" if test_result["passed"] else "✗"
                print(f"{status} {test_name}: {test_result['message']}")
                
                all_results[full_name] = test_result["passed"]
                all_weights[full_name] = test_result.get("weight", 0)
        
        return all_results, all_weights
    
    def calculate_score(self, results, weights):
        """Calculate weighted score"""
        total_weight = sum(weights.values())
        weighted_score = 0.0
        
        for test_name, passed in results.items():
            weight = weights.get(test_name, 0)
            if passed:
                weighted_score += weight
        
        # Normalize to 100
        score = (weighted_score / total_weight * 100) if total_weight > 0 else 0
        return score
    
    def run(self):
        """Execute full autograding pipeline"""
        print("=== CSM218 Autograder ===\n")
        
        # Compile code
        if not self.compile_code():
            score = 0.0
            results = {
                "score": score,
                "status": "FAILED",
                "message": "Compilation failed"
            }
            self.output_results(results)
            return
        
        # Run tests
        try:
            test_results, test_weights = self.run_tests()
        except Exception as e:
            print(f"Error running tests: {e}")
            test_results = {}
            test_weights = {}
        
        # Calculate score
        final_score = self.calculate_score(test_results, test_weights)
        
        # Determine status
        status = "PASS" if final_score >= 40.0 else "FAIL"
        
        results = {
            "score": round(final_score, 2),
            "status": status,
            "test_results": test_results,
            "test_weights": test_weights,
            "message": f"Final Score: {final_score:.2f}%"
        }
        
        self.output_results(results)
        print(f"\n=== Final Score: {final_score:.2f}% ===")
    
    def output_results(self, results):
        """Output results as JSON"""
        os.makedirs("/autograder/results", exist_ok=True)
        
        with open(self.output_path, 'w') as f:
            json.dump(results, f, indent=2)
        
        with open(self.output_path, 'w') as f:
            json.dump(results, f, indent=2)
        
        print(f"Results written to {self.output_path}")

if __name__ == "__main__":
    grader = Grader()
    grader.run()
