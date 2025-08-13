package com.zyb.apiInterface.model.dto;

import lombok.Data;

@Data
public class CityInfo {

    /**
     * 地区/城市ID
     */
    private String id;

    /**
     * 地区/城市名称
     */
    private String name;

    /**
     * 城市所属一级行政区域
     */
    private String adm1;

    /**
     * 城市的上级行政区划名称
     */
    private String adm2;

    /**
     * 所属国家名称
     */
    private String country;

    /**
     * 地区评分，用于排序
     */
    private int rank;
} 