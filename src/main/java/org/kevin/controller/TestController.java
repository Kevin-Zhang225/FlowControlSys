package org.kevin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class TestController {
    @GetMapping("/getData")
    public ResponseEntity<String> getData() {
        // 模拟处理业务
        return ResponseEntity.ok("OK - GET");
    }

    @PostMapping("/postData")
    public ResponseEntity<String> postData(@RequestBody String body) {
        // 模拟处理业务
        return ResponseEntity.ok("OK - POST, body=" + body);
    }

    @PutMapping("/putData")
    public ResponseEntity<String> putData(@RequestBody String body) {
        // 模拟处理业务
        return ResponseEntity.ok("OK - PUT, body=" + body);
    }
}
