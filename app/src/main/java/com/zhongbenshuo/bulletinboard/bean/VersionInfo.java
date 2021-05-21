package com.zhongbenshuo.bulletinboard.bean;

/**
 * 版本信息
 * Created at 2019/9/27 18:38
 *
 * @author LiYuliang
 * @version 1.0
 */

public class VersionInfo {

    private int versionId; // 版本编号
    private int apkTypeId; // apk编号
    private int versionCode; // 版本号（第几版本）-- 安卓上传输入
    private String versionName; // 版本名称（文件名） -- 安卓上传输入
    private String versionUrl; // 存放位置
    private String versionType;// 版本类型
    private String versionLog; // 版本日志
    private long versionSize; // 版本大小
    private String md5Value; // md5值
    private int versionCount; // 版本号（第几版本）-- 数据库统计
    private String versionFileName; // 版本名称（文件名） -- 文件名称
    private String versionFileUrl; // 版本完整路径
    private String createTime;// 版本提交时间

    public int getVersionId() {
        return versionId;
    }

    public void setVersionId(int versionId) {
        this.versionId = versionId;
    }

    public int getApkTypeId() {
        return apkTypeId;
    }

    public void setApkTypeId(int apkTypeId) {
        this.apkTypeId = apkTypeId;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getVersionUrl() {
        return versionUrl;
    }

    public void setVersionUrl(String versionUrl) {
        this.versionUrl = versionUrl;
    }

    public String getVersionType() {
        return versionType;
    }

    public void setVersionType(String versionType) {
        this.versionType = versionType;
    }

    public String getVersionLog() {
        return versionLog;
    }

    public void setVersionLog(String versionLog) {
        this.versionLog = versionLog;
    }

    public long getVersionSize() {
        return versionSize;
    }

    public void setVersionSize(long versionSize) {
        this.versionSize = versionSize;
    }

    public String getMd5Value() {
        return md5Value;
    }

    public void setMd5Value(String md5Value) {
        this.md5Value = md5Value;
    }

    public int getVersionCount() {
        return versionCount;
    }

    public void setVersionCount(int versionCount) {
        this.versionCount = versionCount;
    }

    public String getVersionFileName() {
        return versionFileName;
    }

    public void setVersionFileName(String versionFileName) {
        this.versionFileName = versionFileName;
    }

    public String getVersionFileUrl() {
        return versionFileUrl;
    }

    public void setVersionFileUrl(String versionFileUrl) {
        this.versionFileUrl = versionFileUrl;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "VersionInfo [versionId=" + versionId + ", apkTypeId=" + apkTypeId + ", versionCode=" + versionCode
                + ", versionName=" + versionName + ", versionUrl=" + versionUrl + ", versionType=" + versionType
                + ", versionLog=" + versionLog + ", versionSize=" + versionSize + ", md5Value=" + md5Value
                + ", versionCount=" + versionCount + ", versionFileName=" + versionFileName + ", versionFileUrl="
                + versionFileUrl + ", createTime=" + createTime + "]";
    }

}
