package com.zhongbenshuo.bulletinboard.bean;

import java.util.ArrayList;
import java.util.List;

public class AllUserInfoStatus {

    private int position_id;

    private String position;

    private int priority;

    private List<ProjectAnnouncement> projectAnnouncementList;

    private List<UserInfoStatus> users = new ArrayList<>();

    public int getPosition_id() {
        return position_id;
    }

    public void setPosition_id(int position_id) {
        this.position_id = position_id;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public List<UserInfoStatus> getUsers() {
        return users;
    }

    public void setUsers(List<UserInfoStatus> users) {
        this.users = users;
    }

    public List<ProjectAnnouncement> getProjectAnnouncementList() {
        return projectAnnouncementList;
    }

    public void setProjectAnnouncementList(List<ProjectAnnouncement> projectAnnouncementList) {
        this.projectAnnouncementList = projectAnnouncementList;
    }
}
