package com.zyb.apiInterface.controller;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.zyb.apiInterface.common.ErrorResponse.buildErrorResponse;

@Slf4j
@RestController
@RequestMapping("/history")
public class HistoryController {

    @GetMapping("/today")
    public String getHistoryOfToday() {
        String apiUrl = "https://api.oick.cn/lishi/api.php";
        String response = HttpUtil.get(apiUrl);
        
        try {
            JSONObject jsonObject = JSON.parseObject(response);
            return jsonObject.getJSONArray("result").toJSONString();
        } catch (Exception e) {
            log.error("解析字段失败", e);
            return buildErrorResponse("解析字段失败");
        }
    }
}