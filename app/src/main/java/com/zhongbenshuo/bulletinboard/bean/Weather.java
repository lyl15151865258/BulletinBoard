package com.zhongbenshuo.bulletinboard.bean;

import java.util.List;

/**
 * 高德天气实体类
 * Created at 2019/9/26 20:24
 *
 * @author LiYuliang
 * @version 1.0
 */

public class Weather {
    private String status;
    private String count;
    private String info;
    private String infocode;
    private List<Lives> lives;

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public String getCount() {
        return count;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getInfo() {
        return info;
    }

    public void setInfocode(String infocode) {
        this.infocode = infocode;
    }

    public String getInfocode() {
        return infocode;
    }

    public void setLives(List<Lives> lives) {
        this.lives = lives;
    }

    public List<Lives> getLives() {
        return lives;
    }

    public class Lives {

        private String province;
        private String city;
        private String adcode;
        private String weather;
        private String temperature;
        private String winddirection;
        private String windpower;
        private String humidity;
        private String reporttime;

        public void setProvince(String province) {
            this.province = province;
        }

        public String getProvince() {
            return province;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getCity() {
            return city;
        }

        public void setAdcode(String adcode) {
            this.adcode = adcode;
        }

        public String getAdcode() {
            return adcode;
        }

        public void setWeather(String weather) {
            this.weather = weather;
        }

        public String getWeather() {
            return weather;
        }

        public void setTemperature(String temperature) {
            this.temperature = temperature;
        }

        public String getTemperature() {
            return temperature;
        }

        public void setWinddirection(String winddirection) {
            this.winddirection = winddirection;
        }

        public String getWinddirection() {
            return winddirection;
        }

        public void setWindpower(String windpower) {
            this.windpower = windpower;
        }

        public String getWindpower() {
            return windpower;
        }

        public void setHumidity(String humidity) {
            this.humidity = humidity;
        }

        public String getHumidity() {
            return humidity;
        }

        public void setReporttime(String reporttime) {
            this.reporttime = reporttime;
        }

        public String getReporttime() {
            return reporttime;
        }

    }
}
