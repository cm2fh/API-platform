package com.zyb.apiInterface.controller;

import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

import static com.zyb.apiInterface.common.ErrorResponse.buildErrorResponse;

@Slf4j
@RestController
@RequestMapping("/ip")
public class IpController {

    @PostMapping("/get")
    public String getIpLocation(@RequestParam String ip) {
        String url = "http://ip-api.com/json/" + ip;
        
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("lang", "zh-CN");
        
        try {
            return HttpUtil.get(url, paramMap);
        } catch (Exception e) {
            log.error("请求失败", e);
            return buildErrorResponse("请求失败");
        }
    }
}