import json
import subprocess
import sys
from pathlib import Path

class AdvancedProtocolTest:
    """Additional hidden tests for anti-template protections"""
    
    def test_message_mutation_rejection(self):
        """Verify system rejects mutated message fields"""
        try:
            # Check if Message.parse() validates field names strictly
            with open("src/main/java/pdc/Message.java", "r") as f:
                content = f.read()
                
                # Check for strict field name validation
                if "msgType" in content or "msg_type" in content:
                    return False, "Message class accepts field aliases"
                
                if "validate()" in content and "exception" in content.lower():
                    return True, "Strict validation implemented"
                else:
                    return False, "No strict field validation"
        except Exception as e:
            return False, str(e)
    
    def test_serialization_explicit(self):
        """Verify explicit serialization (not string concat)"""
        try:
            with open("src/main/java/pdc/Message.java", "r") as f:
                content = f.read()
                
                # Check for explicit serialization methods
                has_json_method = "toJson" in content or "serialize" in content
                has_data_stream = "DataOutputStream" in content
                
                if has_json_method or has_data_stream:
                    return True, "Explicit serialization found"
                else:
                    return False, "Missing explicit serialization"
        except Exception as e:
            return False, str(e)
    
    def test_dynamic_port_allocation(self):
        """Verify ports read from environment"""
        try:
            files = ["src/main/java/pdc/Master.java", "src/main/java/pdc/Worker.java"]
            uses_env_port = False
            
            for file_path in files:
                try:
                    with open(file_path, "r") as f:
                        content = f.read()
                        if "CSM218_PORT_BASE" in content or "getenv" in content and "PORT" in content:
                            uses_env_port = True
                except:
                    pass
            
            if uses_env_port:
                return True, "Dynamic port allocation used"
            else:
                return False, "Hardcoded ports or missing env usage"
        except Exception as e:
            return False, str(e)
    
    def test_student_id_protocol_inclusion(self):
        """Verify STUDENT_ID included in protocol messages"""
        try:
            message_uses_student_id = False
            
            with open("src/main/java/pdc/Message.java", "r") as f:
                content = f.read()
                if "studentId" in content and "getenv" in content:
                    message_uses_student_id = True
            
            if message_uses_student_id:
                return True, "STUDENT_ID included in protocol"
            else:
                return False, "STUDENT_ID not in protocol"
        except Exception as e:
            return False, str(e)
    
    def test_heartbeat_mechanism(self):
        """Verify heartbeat for failure detection"""
        try:
            heartbeat_found = False
            timeout_found = False
            
            files = ["src/main/java/pdc/Master.java"]
            
            for file_path in files:
                try:
                    with open(file_path, "r") as f:
                        content = f.read()
                        if "heartbeat" in content.lower():
                            heartbeat_found = True
                        if "timeout" in content.lower() or "TIMEOUT" in content:
                            timeout_found = True
                except:
                    pass
            
            if heartbeat_found and timeout_found:
                return True, "Heartbeat and timeout detected"
            else:
                return False, "Incomplete failure detection"
        except Exception as e:
            return False, str(e)
    
    def test_rpc_request_response_semantics(self):
        """Verify RPC follows request->response pattern"""
        try:
            rpc_semantics = False
            
            files = ["src/main/java/pdc/Master.java", "src/main/java/pdc/Worker.java"]
            
            for file_path in files:
                try:
                    with open(file_path, "r") as f:
                        content = f.read()
                        if "RPC_REQUEST" in content and "RPC_RESPONSE" in content:
                            rpc_semantics = True
                        if "TASK_COMPLETE" in content or "TASK_ERROR" in content:
                            rpc_semantics = True
                except:
                    pass
            
            if rpc_semantics:
                return True, "RPC semantics implemented"
            else:
                return False, "RPC semantics missing"
        except Exception as e:
            return False, str(e)
    
    def test_log_signature_format(self):
        """Verify CSM218 log signatures"""
        try:
            # Just check if System.out.println exists
            # Actual log checking happens at runtime
            with open("src/main/java/pdc/Worker.java", "r") as f:
                content = f.read()
                if "System.out" in content or "System.err" in content or "logger" in content.lower():
                    return True, "Logging mechanism present"
                else:
                    return False, "No logging output"
        except Exception as e:
            return False, str(e)
    
    def run_all(self):
        """Run all advanced protocol tests"""
        results = {}
        
        success, msg = self.test_message_mutation_rejection()
        results["message_mutation_rejection"] = {"passed": success, "message": msg, "weight": 0.02}
        
        success, msg = self.test_serialization_explicit()
        results["serialization_explicit"] = {"passed": success, "message": msg, "weight": 0.02}
        
        success, msg = self.test_dynamic_port_allocation()
        results["dynamic_port_allocation"] = {"passed": success, "message": msg, "weight": 0.02}
        
        success, msg = self.test_student_id_protocol_inclusion()
        results["student_id_protocol"] = {"passed": success, "message": msg, "weight": 0.02}
        
        success, msg = self.test_heartbeat_mechanism()
        results["heartbeat_mechanism"] = {"passed": success, "message": msg, "weight": 0.02}
        
        success, msg = self.test_rpc_request_response_semantics()
        results["rpc_semantics"] = {"passed": success, "message": msg, "weight": 0.02}
        
        success, msg = self.test_log_signature_format()
        results["log_signature"] = {"passed": success, "message": msg, "weight": 0.01}
        
        return results

if __name__ == "__main__":
    tester = AdvancedProtocolTest()
    results = tester.run_all()
    
    for test_name, result in results.items():
        status = "PASS" if result["passed"] else "FAIL"
        print(f"{test_name}: {status} - {result['message']}")
