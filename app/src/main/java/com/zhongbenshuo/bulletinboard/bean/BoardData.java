package com.zhongbenshuo.bulletinboard.bean;

import java.util.List;

/**
 * 人员状态板真正展示的内容
 * Created at 2019/9/30 9:30
 *
 * @author LiYuliang
 * @version 1.0
 */

public class BoardData {

    private List<ShowData> showDataList;
    private List<ProjectAnnouncement> projectAnnouncementList;

    public List<ShowData> getShowDataList() {
        return showDataList;
    }

    public void setShowDataList(List<ShowData> showDataList) {
        this.showDataList = showDataList;
    }

    public List<ProjectAnnouncement> getProjectAnnouncementList() {
        return projectAnnouncementList;
    }

    public void setProjectAnnouncementList(List<ProjectAnnouncement> projectAnnouncementList) {
        this.projectAnnouncementList = projectAnnouncementList;
    }
}
