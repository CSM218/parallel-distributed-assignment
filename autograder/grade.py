#!/usr/bin/env python3

import json
import sys
import os
import shutil
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
        
        # Determine repository root and environment
        script_dir = os.path.dirname(os.path.abspath(__file__))
        repo_root = os.path.abspath(os.path.join(script_dir, os.pardir))

        if os.path.exists("/submission"):
            # GitHub Classroom environment: submission mounted at /submission
            self.submission_dir = "/submission"
            self.output_dir = "/autograder/results"
        else:
            # Regular GitHub Actions or local runs: use repository root
            self.submission_dir = repo_root
            self.output_dir = os.path.join(repo_root, "autograder", "results")

        self.output_path = os.path.join(self.output_dir, "results.json")
        
    def compile_code(self):
        """Compile student and reference code"""
        repo_root = self.submission_dir
        gradle_log_path = os.path.join(self.output_dir, 'gradle_build.log')
        os.makedirs(self.output_dir, exist_ok=True)

        # Choose wrapper based on platform and availability
        is_windows = sys.platform.startswith('win')
        wrapper_sh = os.path.join(repo_root, 'gradlew')
        wrapper_bat = os.path.join(repo_root, 'gradlew.bat')

        cmd = None
        shell = False
        if is_windows and os.path.exists(wrapper_bat):
            cmd = [wrapper_bat, 'build']
            shell = True
        elif (not is_windows) and os.path.exists(wrapper_sh):
            cmd = [wrapper_sh, 'build']
            shell = False
        elif shutil.which('gradle'):
            cmd = ['gradle', 'build']
            shell = False
        else:
            # nothing to run
            with open(gradle_log_path, 'w', encoding='utf-8') as f:
                f.write('No gradle wrapper or gradle executable found in PATH.')
            print('COMPILATION FAILED: no gradle wrapper or gradle found')
            return False

        try:
            proc = subprocess.run(cmd, cwd=repo_root, capture_output=True, timeout=300, text=True, shell=shell)
            # write log always for visibility
            with open(gradle_log_path, 'w', encoding='utf-8') as f:
                f.write(proc.stdout or '')
                f.write('\n')
                f.write(proc.stderr or '')

            if proc.returncode != 0:
                print('COMPILATION FAILED')
                print(proc.stderr)
                return False

            print('✓ Compilation successful')
            return True
        except Exception as e:
            # write exception to log
            with open(gradle_log_path, 'w', encoding='utf-8') as f:
                f.write(str(e))
            print(f'✗ Compilation error: {e}')
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
        status = "PASS" if final_score >= 60.0 else "FAIL"
        
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
        os.makedirs(self.output_dir, exist_ok=True)
        
        with open(self.output_path, 'w') as f:
            json.dump(results, f, indent=2)
        
        print(f"Results written to {self.output_path}")

if __name__ == "__main__":
    grader = Grader()
    grader.run()
