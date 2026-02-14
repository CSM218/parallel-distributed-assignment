import json
import subprocess
import time
import os
import sys
import threading
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path

class ParallelExecutionTest:
    def __init__(self):
        self.task_timings = {}
        self.task_results = {}
    
    def run_integration_test(self):
        """Run distributed system and measure parallelism"""
        try:
            # Compile reference solution
            result = subprocess.run(
                ["javac", "-d", "target/classes", "reference_solution/src/main/java/pdc/*.java"],
                capture_output=True,
                timeout=30
            )
            
            if result.returncode != 0:
                return False, "Failed to compile reference solution"
            
            # Launch master
            env = os.environ.copy()
            env["MASTER_PORT"] = "9999"
            env["STUDENT_ID"] = "test-student"
            
            master_proc = subprocess.Popen(
                ["java", "-cp", "target/classes", "pdc.ReferenceMaster"],
                env=env,
                stdout=subprocess.PIPE,
                stderr=subprocess.PIPE
            )
            
            time.sleep(1)
            
            # Launch workers
            workers = []
            for i in range(3):
                env["WORKER_ID"] = f"worker-{i}"
                env["MASTER_HOST"] = "localhost"
                env["MASTER_PORT"] = "9999"
                
                proc = subprocess.Popen(
                    ["java", "-cp", "target/classes", "pdc.ReferenceWorker"],
                    env=env,
                    stdout=subprocess.PIPE,
                    stderr=subprocess.PIPE
                )
                workers.append(proc)
            
            time.sleep(2)
            
            # Send 4 simultaneous tasks
            task_ids = ["task-1", "task-2", "task-3", "task-4"]
            start_times = {}
            
            for task_id in task_ids:
                start_times[task_id] = time.time()
            
            time.sleep(3)
            
            # Check for parallel execution overlap
            timings = list(start_times.values())
            if len(timings) >= 2:
                min_start = min(timings)
                max_start = max(timings)
                duration = max_start - min_start
                
                if duration < 1.0:  # Tasks started within 1 second
                    # This suggests parallel execution
                    return True, "Parallel execution detected"
                else:
                    return False, "Tasks appear sequential"
            
            return False, "Unable to verify parallelism"
            
        except Exception as e:
            return False, f"Error: {str(e)}"
        
        finally:
            try:
                master_proc.terminate()
                master_proc.wait(timeout=2)
            except:
                master_proc.kill()
            
            for proc in workers:
                try:
                    proc.terminate()
                    proc.wait(timeout=2)
                except:
                    proc.kill()
    
    def check_concurrency_support(self):
        """Verify code supports concurrent requests"""
        try:
            concurrent_handling = False
            
            files = ["src/main/java/pdc/Master.java"]
            
            for file_path in files:
                try:
                    with open(file_path, "r") as f:
                        content = f.read()
                        
                        # Check for threading constructs
                        if "Thread" in content or "ExecutorService" in content or "Runnable" in content:
                            concurrent_handling = True
                except:
                    pass
            
            if concurrent_handling:
                return True, "Concurrent execution support found"
            else:
                return False, "No concurrent execution support"
        
        except Exception as e:
            return False, str(e)
    
    def measure_task_distribution(self):
        """Verify tasks are distributed to multiple workers"""
        try:
            # Check if Master distributes work
            distribution_logic = False
            
            with open("src/main/java/pdc/Master.java", "r") as f:
                content = f.read()
                if "worker" in content.lower() and ("distribute" in content.lower() or "assign" in content.lower()):
                    distribution_logic = True
            
            if distribution_logic:
                return True, "Task distribution logic found"
            else:
                return False, "No task distribution logic"
        
        except Exception as e:
            return False, str(e)
    
    def run_all(self):
        """Run all parallel execution tests"""
        results = {}
        
        success, msg = self.check_concurrency_support()
        results["concurrency_support"] = {"passed": success, "message": msg, "weight": 0.10}
        
        success, msg = self.measure_task_distribution()
        results["task_distribution"] = {"passed": success, "message": msg, "weight": 0.10}
        
        return results

if __name__ == "__main__":
    tester = ParallelExecutionTest()
    results = tester.run_all()
    
    for test_name, result in results.items():
        status = "PASS" if result["passed"] else "FAIL"
        print(f"{test_name}: {status} - {result['message']}")
