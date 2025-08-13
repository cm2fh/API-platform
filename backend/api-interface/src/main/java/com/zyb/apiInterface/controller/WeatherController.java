package com.zyb.apiInterface.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.zyb.apiInterface.model.dto.CityInfo;
import com.zyb.apiInterface.model.dto.WeatherInfo;
import com.zyb.apiInterface.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static com.zyb.apiInterface.common.ErrorResponse.buildErrorResponse;

/**
 * 天气 API
 */
@RestController
@Slf4j
@RequestMapping("/weather")
public class WeatherController {

    @Value("${api.weather.api-host}")
    private String weatherApiHost;

    private static String jwt;

    @GetMapping("/get")
    public String getWeatherByGet(String city) throws Exception {
        if (StrUtil.isBlank(city)) {
            return buildErrorResponse("城市名称不能为空");
        }

        String locationId = getCityLocationId(city);
        if (locationId == null) {
            return buildErrorResponse(String.format("未查询到城市 %s 的信息", city));
        }

        WeatherInfo weatherInfo = getRealtimeWeather(locationId);
        if (weatherInfo == null) {
            return buildErrorResponse("获取实时天气失败");
        }

        return JSON.toJSONString(translate(weatherInfo));
    }

    /**
     * 将 WeatherInfo 对象的字段名转换为中文
     */
    private JSONObject translate(WeatherInfo weatherInfo) {
        JSONObject chineseKeyJson = new JSONObject();
        chineseKeyJson.put("数据观测时间", weatherInfo.getObsTime());
        chineseKeyJson.put("温度", weatherInfo.getTemp());
        chineseKeyJson.put("体感温度", weatherInfo.getFeelsLike());
        chineseKeyJson.put("天气状况", weatherInfo.getText());
        chineseKeyJson.put("风向", weatherInfo.getWindDir());
        chineseKeyJson.put("风力等级", weatherInfo.getWindScale());
        chineseKeyJson.put("相对湿度", weatherInfo.getHumidity());
        chineseKeyJson.put("当前小时累计降水量", weatherInfo.getPrecip());
        chineseKeyJson.put("大气压强", weatherInfo.getPressure());
        chineseKeyJson.put("能见度", weatherInfo.getVis());
        return chineseKeyJson;
    }

    /**
     * 获取城市 ID
     */
    private String getCityLocationId(String city) throws Exception {
        String url = "https://" + weatherApiHost + "/geo/v2/city/lookup?location=" + city;
        jwt = JwtUtil.getJwt();

        try {
            String result = HttpRequest.get(url)
                    .header("Authorization", "Bearer " + jwt)
                    .header("Accept-Encoding", "gzip, deflate")
                    .execute()
                    .body();

            log.info("查询城市ID: {}, 返回: {}", city, result);

            JSONObject jsonObject = JSON.parseObject(result);
            if ("200".equals(jsonObject.getString("code"))) {
                JSONArray locationArray = jsonObject.getJSONArray("location");
                if (locationArray == null || locationArray.isEmpty()) {
                    return null;
                }
                // 解析返回的城市列表
                List<CityInfo> cityList = JSON.parseArray(locationArray.toJSONString(), CityInfo.class);

                // 根据 rank 字段找到最优匹配
                Optional<CityInfo> bestMatch = cityList.stream()
                        .min(Comparator.comparingInt(CityInfo::getRank));

                return bestMatch.map(CityInfo::getId).orElse(null);
            }
        } catch (Exception e) {
            log.error("获取城市ID失败", e);
        }
        return null;
    }

    /**
     * 获取实时天气
     */
    private WeatherInfo getRealtimeWeather(String locationId) {
        String url = "https://" + weatherApiHost + "/v7/weather/now?location=" + locationId;

        try {
            String result = HttpRequest.get(url)
                    .header("Authorization", "Bearer " + jwt)
                    .header("Accept-Encoding", "gzip, deflate")
                    .execute()
                    .body();

            log.info("查询实时天气: {}, 返回: {}", locationId, result);

            JSONObject jsonObject = JSON.parseObject(result);
            if ("200".equals(jsonObject.getString("code"))) {
                JSONObject now = jsonObject.getJSONObject("now");
                return JSON.parseObject(now.toJSONString(), WeatherInfo.class);
            }
        } catch (Exception e) {
            log.error("获取实时天气失败", e);
        }
        return null;
    }
} 