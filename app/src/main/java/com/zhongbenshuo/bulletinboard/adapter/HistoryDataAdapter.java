package com.zhongbenshuo.bulletinboard.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zhongbenshuo.bulletinboard.R;
import com.zhongbenshuo.bulletinboard.bean.Environment;
import com.zhongbenshuo.bulletinboard.utils.TimeUtils;

import java.util.List;

/**
 * 环境监测点列表适配器
 * Created at 2019/9/24 16:08
 *
 * @author LiYuliang
 * @version 1.0
 */

public class HistoryDataAdapter extends RecyclerView.Adapter<HistoryDataAdapter.ListViewHolder> {

    private Context mContext;
    private List<Environment> list;

    public HistoryDataAdapter(Context context, List<Environment> lv) {
        mContext = context;
        list = lv;
    }

    @Override
    public ListViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_environment, viewGroup, false);
        ListViewHolder listViewHolder = new ListViewHolder(view);
        listViewHolder.tvTime = view.findViewById(R.id.tvTime);
        listViewHolder.tvTemperature = view.findViewById(R.id.tvTemperature);
        listViewHolder.tvHumidity = view.findViewById(R.id.tvHumidity);
        listViewHolder.tvPM25 = view.findViewById(R.id.tvPM25);
        listViewHolder.tvPM10 = view.findViewById(R.id.tvPM10);
        listViewHolder.tvFormaldehyde = view.findViewById(R.id.tvFormaldehyde);
        listViewHolder.tvCarbonDioxide = view.findViewById(R.id.tvCarbonDioxide);
        return listViewHolder;
    }

    @Override
    public void onBindViewHolder(ListViewHolder holder, int position) {
        Environment environment = list.get(position);
        holder.tvTime.setText(TimeUtils.normalFormat(environment.getCreateTime(), "yyyy-MM-dd HH:mm:ss", "HH:mm:ss"));
        // 温度
        holder.tvTemperature.setText(environment.getTemperature() + "℃");
        if (environment.getTemperature() >= 5 && environment.getTemperature() <= 30) {
            holder.tvTemperature.setTextColor(mContext.getResources().getColor(R.color.value_normal));
        } else if (environment.getTemperature() < 5) {
            holder.tvTemperature.setTextColor(mContext.getResources().getColor(R.color.value_low));
        } else {
            holder.tvTemperature.setTextColor(mContext.getResources().getColor(R.color.value_high));
        }
        // 湿度
        holder.tvHumidity.setText(environment.getHumidity() + "%");
        if (environment.getHumidity() >= 30 && environment.getHumidity() <= 70) {
            holder.tvHumidity.setTextColor(mContext.getResources().getColor(R.color.value_normal));
        } else if (environment.getHumidity() < 30) {
            holder.tvHumidity.setTextColor(mContext.getResources().getColor(R.color.value_dry));
        } else {
            holder.tvHumidity.setTextColor(mContext.getResources().getColor(R.color.value_high));
        }
        // PM2.5
        holder.tvPM25.setText(environment.getPm25() + "μg/m³");
        if (environment.getPm25() <= 75) {
            holder.tvPM25.setTextColor(mContext.getResources().getColor(R.color.value_normal));
        } else if (environment.getPm25() > 75 && environment.getPm25() <= 150) {
            holder.tvPM25.setTextColor(mContext.getResources().getColor(R.color.orange_600));
        } else {
            holder.tvPM25.setTextColor(mContext.getResources().getColor(R.color.value_high));
        }
        // PM10
        holder.tvPM10.setText(environment.getPm10() + "μg/m³");
        if (environment.getPm10() <= 150) {
            holder.tvPM10.setTextColor(mContext.getResources().getColor(R.color.value_normal));
        } else {
            holder.tvPM10.setTextColor(mContext.getResources().getColor(R.color.value_high));
        }
        // 甲醛
        holder.tvFormaldehyde.setText(environment.getFormaldehyde() + "mg/m³");
        if (environment.getFormaldehyde() <= 0.08) {
            holder.tvFormaldehyde.setTextColor(mContext.getResources().getColor(R.color.value_normal));
        } else {
            holder.tvFormaldehyde.setTextColor(mContext.getResources().getColor(R.color.value_high));
        }
        // 二氧化碳
        holder.tvCarbonDioxide.setText(environment.getCarbonDioxide() + "ppm");
        if (environment.getCarbonDioxide() <= 1000) {
            holder.tvCarbonDioxide.setTextColor(mContext.getResources().getColor(R.color.value_normal));
        } else if (environment.getCarbonDioxide() > 1000 && environment.getCarbonDioxide() <= 2000) {
            holder.tvCarbonDioxide.setTextColor(mContext.getResources().getColor(R.color.orange_600));
        } else {
            holder.tvCarbonDioxide.setTextColor(mContext.getResources().getColor(R.color.value_high));
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ListViewHolder extends RecyclerView.ViewHolder {

        private TextView tvTime, tvTemperature, tvHumidity, tvPM25, tvPM10, tvFormaldehyde, tvCarbonDioxide;

        private ListViewHolder(View itemView) {
            super(itemView);
        }
    }

}
