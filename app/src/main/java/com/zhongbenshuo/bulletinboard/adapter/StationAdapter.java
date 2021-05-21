package com.zhongbenshuo.bulletinboard.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zhongbenshuo.bulletinboard.R;
import com.zhongbenshuo.bulletinboard.bean.Station;

import java.util.List;

/**
 * 环境监测点列表适配器
 * Created at 2019/9/24 16:08
 *
 * @author LiYuliang
 * @version 1.0
 */

public class StationAdapter extends RecyclerView.Adapter<StationAdapter.ListViewHolder> {

    private Context mContext;
    private List<Station> list;
    private int selectedPosition;
    private OnItemClickListener mItemClickListener;

    public StationAdapter(Context context, List<Station> lv, int selectedPosition) {
        mContext = context;
        list = lv;
        this.selectedPosition = selectedPosition;
    }

    @Override
    public ListViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_station, viewGroup, false);
        ListViewHolder listViewHolder = new ListViewHolder(view);
        listViewHolder.tvStation = view.findViewById(R.id.tvStation);
        return listViewHolder;
    }

    @Override
    public void onBindViewHolder(ListViewHolder holder, int position) {
        Station station = list.get(position);
        holder.tvStation.setText(station.getStationName());
        holder.itemView.setSelected(position == selectedPosition);
        holder.itemView.setOnClickListener((v) -> {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ListViewHolder extends RecyclerView.ViewHolder {

        private TextView tvStation;

        private ListViewHolder(View itemView) {
            super(itemView);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mItemClickListener = onItemClickListener;
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
        notifyDataSetChanged();
    }

}
