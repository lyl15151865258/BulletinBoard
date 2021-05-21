package com.zhongbenshuo.bulletinboard.bean;

public class Station implements Comparable<Station> {

    private int stationId;

    private String stationName;

    private boolean online;

    private int type;

    public static final int TYPE_AIR = 1;
    public static final int TYPE_CAMERA = 2;

    public Station(int stationId, String stationName, boolean online, int type) {
        this.stationId = stationId;
        this.stationName = stationName;
        this.online = online;
        this.type = type;
    }

    public int getStationId() {
        return stationId;
    }

    public void setStationId(int stationId) {
        this.stationId = stationId;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Station)) {
            return false;
        } else {
            try {
                Station that = (Station) o;
                return stationId == that.stationId;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    @Override
    public int compareTo(Station o) {
        return this.stationId - o.stationId;
    }
}
