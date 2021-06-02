package com.zhongbenshuo.bulletinboard.bean;

import java.util.List;

public class AllUserInfoStatusResult {

    private List<AllUserInfoStatus> employeeStatusList;
    private List<ProjectAnnouncement> projectAnnouncementList;

    public List<AllUserInfoStatus> getEmployeeStatusList() {
        return employeeStatusList;
    }

    public void setEmployeeStatusList(List<AllUserInfoStatus> employeeStatusList) {
        this.employeeStatusList = employeeStatusList;
    }

    public List<ProjectAnnouncement> getProjectAnnouncementList() {
        return projectAnnouncementList;
    }

    public void setProjectAnnouncementList(List<ProjectAnnouncement> projectAnnouncementList) {
        this.projectAnnouncementList = projectAnnouncementList;
    }
}
