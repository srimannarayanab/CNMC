package com.cmtsbsnl.cnmc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CircleUserAdapter extends RecyclerView.Adapter<CircleUserAdapter.MyViewHolder> {
    private final Context context;
    public static List<CircleUserModel> circleUserModelList;

    public CircleUserAdapter(Context context, List<CircleUserModel> circleUserModelList) {
        this.context = context;
        CircleUserAdapter.circleUserModelList = circleUserModelList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemview = LayoutInflater.from(parent.getContext()).inflate(R.layout.circle_user_list, parent, false);
        return new CircleUserAdapter.MyViewHolder(itemview);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final CircleUserModel circleUserModel = circleUserModelList.get(position);
        holder.msisdn.setText(circleUserModel.getMsisdn());
        holder.circle_name.setText(context.getString(R.string.name_value_hypen,circleUserModel.getCircle(),circleUserModel.getCircle_id()));
        holder.name.setText(context.getString(R.string.name_value_colon,"Name",circleUserModel.getName()));
        holder.desg.setText(context.getString(R.string.name_value_colon,"Desg",circleUserModel.getDesg()));
        holder.email.setText(context.getString(R.string.name_value_colon,"Email",circleUserModel.getEmail()));
        holder.hrms_no.setText(context.getString(R.string.name_value_colon,"HrmsNo",circleUserModel.getHrms_no()));
        holder.last_login.setText(context.getString(R.string.name_value_colon,"LastLogin",circleUserModel.getLast_login()));
        holder.app_version.setText(context.getString(R.string.name_value_colon,"AppVersion",circleUserModel.getApp_version()));
        holder.lvl.setText(context.getString(R.string.name_value_colon,"Level",circleUserModel.getLvl()));
        holder.lvl2.setText(context.getString(R.string.name_value_colon,"Level2",circleUserModel.getLvl2()));
        holder.lvl3.setText(context.getString(R.string.name_value_colon,"Level3",circleUserModel.getLvl3()));
    }

    @Override
    public int getItemCount() {
        return circleUserModelList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView msisdn, name, circle_name, desg, email, hrms_no, last_login, app_version, lvl,lvl2, lvl3;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            msisdn = itemView.findViewById(R.id.msisdn);
            name = itemView.findViewById(R.id.name);
            circle_name = itemView.findViewById(R.id.circle_name);
            desg = itemView.findViewById(R.id.desg);
            email = itemView.findViewById(R.id.email);
            hrms_no = itemView.findViewById(R.id.hrms_no);
            last_login = itemView.findViewById(R.id.last_login);
            app_version = itemView.findViewById(R.id.app_version);
            lvl = itemView.findViewById(R.id.lvl);
            lvl2 = itemView.findViewById(R.id.lvl2);
            lvl3 = itemView.findViewById(R.id.lvl3);

        }
    }

    public void updateList(List<CircleUserModel> filteredList) {
        circleUserModelList = filteredList;
        notifyDataSetChanged();
    }
}
