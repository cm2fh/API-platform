package com.zyb.apiInterface.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/name")
public class NameController {

    @GetMapping("/get")
    public String getNameByGet(String name) {
        return "你的名字是" + name;
    }

    @PostMapping("/post")
    public String getNameByPost(@RequestBody Map<String, Object> requestBody) {
        String name = (String) requestBody.get("name");
        if (name == null) {
            return "参数不完整";
        }
        return "你的名字是" + name;
    }
}
