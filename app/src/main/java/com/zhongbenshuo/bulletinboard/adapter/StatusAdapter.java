package com.zhongbenshuo.bulletinboard.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.zhongbenshuo.bulletinboard.R;
import com.zhongbenshuo.bulletinboard.bean.userstatus.ShowData;
import com.zhongbenshuo.bulletinboard.bean.userstatus.UserInfoStatus;
import com.zhongbenshuo.bulletinboard.utils.GsonUtils;
import com.zhongbenshuo.bulletinboard.utils.LogUtils;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 人员状态；列表适配器
 * Created at 2019/9/30 9:57
 *
 * @author LiYuliang
 * @version 1.0
 */

public class StatusAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ShowData> showDataList;

    public StatusAdapter(Context context, List<ShowData> lv) {
        showDataList = lv;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view;
        if (viewType == 1) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_department, viewGroup, false);
            DepartmentViewHolder departmentViewHolder = new DepartmentViewHolder(view);
            departmentViewHolder.tvDepartment = view.findViewById(R.id.tvPosition);
            departmentViewHolder.bottomDivider = view.findViewById(R.id.bottomDivider);
            return departmentViewHolder;
        } else {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_employee, viewGroup, false);
            EmployeeViewHolder employeeViewHolder = new EmployeeViewHolder(view);
            employeeViewHolder.tvName = view.findViewById(R.id.tvName);
            employeeViewHolder.tvRemark = view.findViewById(R.id.tvRemark);
            employeeViewHolder.tvStatus = view.findViewById(R.id.tvStatus);
            employeeViewHolder.topDivider = view.findViewById(R.id.topDivider);
            return employeeViewHolder;
        }
    }

    @Override
    public void onBindViewHolder(@NotNull RecyclerView.ViewHolder holder, int position) {
        ShowData showData = showDataList.get(position);
        LogUtils.d("onBindViewHolder:" + GsonUtils.convertJSON(showData));
        if (showData.getType() == ShowData.POSITION) {
            DepartmentViewHolder departmentViewHolder = (DepartmentViewHolder) holder;
            departmentViewHolder.tvDepartment.setText("【" + showData.getObject() + "】");
            if (position == getItemCount() - 1) {
                departmentViewHolder.bottomDivider.setVisibility(View.GONE);
            } else {
                departmentViewHolder.bottomDivider.setVisibility(View.VISIBLE);
            }
        } else if (showData.getType() == ShowData.EMPLOYEE) {
            EmployeeViewHolder employeeViewHolder = (EmployeeViewHolder) holder;
            UserInfoStatus userInfoStatus = GsonUtils.parseJSON(GsonUtils.convertJSON(showData.getObject()), UserInfoStatus.class);
            employeeViewHolder.tvName.setText(userInfoStatus.getUser_name());
            employeeViewHolder.tvRemark.setText(userInfoStatus.getRemarks());
            employeeViewHolder.tvStatus.setText(userInfoStatus.getStatus());
            try {
                employeeViewHolder.tvName.setTextColor(Color.parseColor(userInfoStatus.getColor()));
                employeeViewHolder.tvRemark.setTextColor(Color.parseColor(userInfoStatus.getColor()));
                employeeViewHolder.tvStatus.setTextColor(Color.parseColor(userInfoStatus.getColor()));
            } catch (Exception e) {
                e.printStackTrace();
            }
//            if (position == 0) {
//                employeeViewHolder.topDivider.setVisibility(View.VISIBLE);
//            } else {
//                employeeViewHolder.topDivider.setVisibility(View.GONE);
//            }
        }
    }

    @Override
    public int getItemCount() {
        return showDataList.size();
    }

    @Override
    public int getItemViewType(int position) {
        LogUtils.d("ShowData：" + GsonUtils.convertJSON(showDataList.get(position)));
        return showDataList.get(position).getType();
    }

    private class DepartmentViewHolder extends RecyclerView.ViewHolder {

        private TextView tvDepartment;
        private View bottomDivider;

        private DepartmentViewHolder(View itemView) {
            super(itemView);
        }
    }

    private class EmployeeViewHolder extends RecyclerView.ViewHolder {

        private TextView tvName, tvRemark, tvStatus;
        private View topDivider;

        private EmployeeViewHolder(View itemView) {
            super(itemView);
        }
    }

}
