package com.zyb.apiInterface.model.dto;

import lombok.Data;


@Data
public class WeatherInfo {

    /**
     * 数据观测时间
     */
    private String obsTime;

    /**
     * 温度
     */
    private String temp;

    /**
     * 体感温度
     */
    private String feelsLike;

    /**
     * 天气状况的图标代码
     */
    private String icon;

    /**
     * 天气状况的文字描述
     */
    private String text;

    /**
     * 风向
     */
    private String windDir;

    /**
     * 风力等级
     */
    private String windScale;

    /**
     * 相对湿度
     */
    private String humidity;

    /**
     * 过去1小时降水量
     */
    private String precip;

    /**
     * 大气压强
     */
    private String pressure;

    /**
     * 能见度
     */
    private String vis;

} 