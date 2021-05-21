package com.zhongbenshuo.bulletinboard.bean;

/**
 * 显示在图表数据的实体类
 * Created at 2019/9/24 19:29
 *
 * @author LiYuliang
 * @version 1.0
 */

public class RealData {

    public enum DATA_TYPE {
        TYPE_TEMP,
        TYPE_HUMIDITY,
        TYPE_PM25,
        TYPE_PM10,
        TYPE_HCHO,
        TYPE_CO2,
        TYPE_ILLUMINANCE
    }

    private String chartName;

    private DATA_TYPE dataType;

    private float value;

    private String unit;

    public RealData(String chartName, DATA_TYPE dataType, float value, String unit) {
        this.chartName = chartName;
        this.dataType = dataType;
        this.value = value;
        this.unit = unit;
    }

    public String getChartName() {
        return chartName;
    }

    public void setChartName(String chartName) {
        this.chartName = chartName;
    }

    public DATA_TYPE getDataType() {
        return dataType;
    }

    public void setDataType(DATA_TYPE dataType) {
        this.dataType = dataType;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
