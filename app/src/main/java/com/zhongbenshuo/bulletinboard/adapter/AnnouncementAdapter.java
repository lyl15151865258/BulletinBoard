package com.zhongbenshuo.bulletinboard.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.zhongbenshuo.bulletinboard.R;
import com.zhongbenshuo.bulletinboard.bean.ProjectAnnouncement;
import com.zhongbenshuo.bulletinboard.bean.userstatus.ShowData;
import com.zhongbenshuo.bulletinboard.bean.userstatus.UserInfoStatus;
import com.zhongbenshuo.bulletinboard.utils.GsonUtils;
import com.zhongbenshuo.bulletinboard.utils.LogUtils;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 公告栏列表适配器
 * Created at 2021/5/21 17:36
 *
 * @author LiYuliang
 * @version 1.0
 */

public class AnnouncementAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ProjectAnnouncement> projectAnnouncementList;

    public AnnouncementAdapter(Context context, List<ProjectAnnouncement> lv) {
        projectAnnouncementList = lv;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_announcement, viewGroup, false);
        AnnouncementViewHolder announcementViewHolder = new AnnouncementViewHolder(view);
        announcementViewHolder.tvContent = view.findViewById(R.id.tvContent);
        return announcementViewHolder;
    }

    @Override
    public void onBindViewHolder(@NotNull RecyclerView.ViewHolder holder, int position) {
        ProjectAnnouncement announcement = projectAnnouncementList.get(position);
        AnnouncementViewHolder announcementViewHolder = (AnnouncementViewHolder) holder;
        announcementViewHolder.tvContent.setText((position + 1) + "、" + announcement.getQuotation());
    }

    @Override
    public int getItemCount() {
        return projectAnnouncementList.size();
    }

    private class AnnouncementViewHolder extends RecyclerView.ViewHolder {

        private TextView tvContent;

        private AnnouncementViewHolder(View itemView) {
            super(itemView);
        }
    }

}
