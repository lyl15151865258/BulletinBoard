package com.zhongbenshuo.bulletinboard.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zhongbenshuo.bulletinboard.R;
import com.zhongbenshuo.bulletinboard.bean.RealData;
import com.zhongbenshuo.bulletinboard.widget.ClockView;

import java.util.List;

/**
 * 实时数据图表
 * Created at 2019/9/24 19:36
 *
 * @author LiYuliang
 * @version 1.0
 */
public class RealDataAdapter extends RecyclerView.Adapter<RealDataAdapter.ListViewHolder> {

    private Context mContext;
    private List<RealData> list;

    public RealDataAdapter(Context context, List<RealData> lv) {
        mContext = context;
        list = lv;
    }

    @Override
    public ListViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_real_data, viewGroup, false);
        ListViewHolder listViewHolder = new ListViewHolder(view);
        listViewHolder.cvRealData = view.findViewById(R.id.cvRealData);
        listViewHolder.tvStatus = view.findViewById(R.id.tvStatus);
        return listViewHolder;
    }

    @Override
    public void onBindViewHolder(ListViewHolder holder, int position) {
        RealData realData = list.get(position);
        holder.cvRealData.setCompleteDegree(realData.getValue(), realData.getUnit());
        switch (realData.getDataType()) {
            case TYPE_TEMP:
                // 温度
                holder.cvRealData.setColor(mContext.getResources().getColor(R.color.value_low), mContext.getResources().getColor(R.color.value_normal), mContext.getResources().getColor(R.color.value_high));
                holder.cvRealData.setValue(-10, 40, 5, 30);
                if (realData.getValue() >= 5 && realData.getValue() <= 30) {
                    holder.tvStatus.setText("舒适");
                    holder.tvStatus.setBackgroundResource(R.drawable.background_normal);
                } else if (realData.getValue() < 5) {
                    holder.tvStatus.setText("寒冷");
                    holder.tvStatus.setBackgroundResource(R.drawable.background_temperature_low);
                } else {
                    holder.tvStatus.setText("炎热");
                    holder.tvStatus.setBackgroundResource(R.drawable.background_abnormal);
                }
                break;
            case TYPE_HUMIDITY:
                // 湿度
                holder.cvRealData.setColor(mContext.getResources().getColor(R.color.value_dry), mContext.getResources().getColor(R.color.value_normal), mContext.getResources().getColor(R.color.value_high));
                holder.cvRealData.setValue(0, 100, 30, 70);
                if (realData.getValue() >= 30 && realData.getValue() <= 70) {
                    holder.tvStatus.setText("舒适");
                    holder.tvStatus.setBackgroundResource(R.drawable.background_normal);
                } else if (realData.getValue() < 30) {
                    holder.tvStatus.setText("干燥");
                    holder.tvStatus.setBackgroundResource(R.drawable.background_dry);
                } else {
                    holder.tvStatus.setText("潮湿");
                    holder.tvStatus.setBackgroundResource(R.drawable.background_abnormal);
                }
                break;
            case TYPE_PM25:
                // PM2.5
                holder.cvRealData.setColor(mContext.getResources().getColor(R.color.value_normal), mContext.getResources().getColor(R.color.orange_600), mContext.getResources().getColor(R.color.value_high));
                holder.cvRealData.setValue(0, 300, 75, 150);
                if (realData.getValue() <= 35) {
                    holder.tvStatus.setText("低");
                    holder.tvStatus.setBackgroundResource(R.drawable.background_normal);
                } else if (realData.getValue() > 35 && realData.getValue() <= 75) {
                    holder.tvStatus.setText("正常");
                    holder.tvStatus.setBackgroundResource(R.drawable.background_normal);
                } else if (realData.getValue() > 75 && realData.getValue() <= 115) {
                    holder.tvStatus.setText("轻度污染");
                    holder.tvStatus.setBackgroundResource(R.drawable.background_normal2);
                } else if (realData.getValue() > 115 && realData.getValue() <= 150) {
                    holder.tvStatus.setText("中度污染");
                    holder.tvStatus.setBackgroundResource(R.drawable.background_normal2);
                } else if (realData.getValue() > 150 && realData.getValue() <= 250) {
                    holder.tvStatus.setText("重度污染");
                    holder.tvStatus.setBackgroundResource(R.drawable.background_abnormal);
                } else {
                    holder.tvStatus.setText("严重污染");
                    holder.tvStatus.setBackgroundResource(R.drawable.background_abnormal);
                }
                break;
            case TYPE_PM10:
                // PM10
                holder.cvRealData.setColor(mContext.getResources().getColor(R.color.value_normal), mContext.getResources().getColor(R.color.value_normal), mContext.getResources().getColor(R.color.value_high));
                holder.cvRealData.setValue(0, 300, 50, 150);
                if (realData.getValue() <= 50) {
                    holder.tvStatus.setText("低");
                    holder.tvStatus.setBackgroundResource(R.drawable.background_normal);
                } else if (realData.getValue() > 50 && realData.getValue() <= 150) {
                    holder.tvStatus.setText("正常");
                    holder.tvStatus.setBackgroundResource(R.drawable.background_normal);
                } else {
                    holder.tvStatus.setText("高");
                    holder.tvStatus.setBackgroundResource(R.drawable.background_abnormal);
                }
                break;
            case TYPE_HCHO:
                // HCHO
                holder.cvRealData.setColor(mContext.getResources().getColor(R.color.value_normal), mContext.getResources().getColor(R.color.value_normal), mContext.getResources().getColor(R.color.value_high));
                holder.cvRealData.setValue(0, 0.2f, 0.08f, 0.08f);
                if (realData.getValue() <= 0.08) {
                    holder.tvStatus.setText("正常");
                    holder.tvStatus.setBackgroundResource(R.drawable.background_normal);
                } else {
                    holder.tvStatus.setText("异常");
                    holder.tvStatus.setBackgroundResource(R.drawable.background_abnormal);
                }
                break;
            case TYPE_CO2:
                // CO2
                holder.cvRealData.setColor(mContext.getResources().getColor(R.color.value_normal), mContext.getResources().getColor(R.color.orange_600), mContext.getResources().getColor(R.color.value_high));
                holder.cvRealData.setValue(0, 2500, 1000, 2000);
                if (realData.getValue() <= 450) {
                    holder.tvStatus.setText("正常");
                    holder.tvStatus.setBackgroundResource(R.drawable.background_normal);
                } else if (realData.getValue() > 450 && realData.getValue() <= 1000) {
                    holder.tvStatus.setText("略高");
                    holder.tvStatus.setBackgroundResource(R.drawable.background_normal);
                } else if (realData.getValue() > 1000 && realData.getValue() <= 2000) {
                    holder.tvStatus.setText("高");
                    holder.tvStatus.setBackgroundResource(R.drawable.background_normal2);
                } else {
                    holder.tvStatus.setText("异常");
                    holder.tvStatus.setBackgroundResource(R.drawable.background_abnormal);
                }
                break;
            default:
                break;
        }
        holder.cvRealData.setTitle(realData.getChartName());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ListViewHolder extends RecyclerView.ViewHolder {

        private ClockView cvRealData;
        private TextView tvStatus;

        private ListViewHolder(View itemView) {
            super(itemView);
        }
    }

}
