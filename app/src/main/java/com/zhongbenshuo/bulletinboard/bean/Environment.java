package com.zhongbenshuo.bulletinboard.bean;

public class Environment implements Comparable<Environment> {
    private int station;                //站号
    private float temperature;          //温度（℃）
    private float humidity;             //湿度（%）
    private float pm25;                 //PM2.5（μg/m³）
    private float pm10;                 //PM10（μg/m³）
    private int carbonDioxide;          //CO2（ppm）
    private float formaldehyde;         //甲醛（mg/m3）
    private int illuminance;            //光照度（lux）
    private String stationRemarks;      //站号备注
    private String name;                //别名
    private String createTime;          //创建时间
    private String station_name;        //环境检测仪别名
    private String station_remarks;     //环境检测仪备注
    private boolean state;              //环境检测仪的状态

    public Environment() {
    }

    public int getStation() {
        return station;
    }

    public void setStation(int station) {
        this.station = station;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public float getHumidity() {
        return humidity;
    }

    public void setHumidity(float humidity) {
        this.humidity = humidity;
    }

    public float getPm25() {
        return pm25;
    }

    public void setPm25(float pm25) {
        this.pm25 = pm25;
    }

    public float getPm10() {
        return pm10;
    }

    public void setPm10(float pm10) {
        this.pm10 = pm10;
    }

    public int getCarbonDioxide() {
        return carbonDioxide;
    }

    public void setCarbonDioxide(int carbonDioxide) {
        this.carbonDioxide = carbonDioxide;
    }

    public float getFormaldehyde() {
        return formaldehyde;
    }

    public void setFormaldehyde(float formaldehyde) {
        this.formaldehyde = formaldehyde;
    }

    public int getIlluminance() {
        return illuminance;
    }

    public void setIlluminance(int illuminance) {
        this.illuminance = illuminance;
    }

    public String getStationRemarks() {
        return stationRemarks;
    }

    public void setStationRemarks(String stationRemarks) {
        this.stationRemarks = stationRemarks;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getStation_name() {
        return station_name;
    }

    public void setStation_name(String station_name) {
        this.station_name = station_name;
    }

    public String getStation_remarks() {
        return station_remarks;
    }

    public void setStation_remarks(String station_remarks) {
        this.station_remarks = station_remarks;
    }

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Environment)) {
            return false;
        } else {
            try {
                Environment that = (Environment) o;
                return station == that.station;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    @Override
    public int compareTo(Environment o) {
        return this.station - o.station;
    }
}
