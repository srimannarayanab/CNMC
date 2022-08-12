package com.cmtsbsnl.cnmc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class TodayLoginsAdapter extends RecyclerView.Adapter<TodayLoginsAdapter.MyViewHolder> {

  private final Context context;
  private final List<TodayLoginsModel> todayLoginsModelList;
  public TodayLoginsAdapter(Context context, List<TodayLoginsModel> todayLoginsModelList) {
    this.context = context;
    this.todayLoginsModelList = todayLoginsModelList;
  }

  @NonNull
  @Override
  public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View itemview = LayoutInflater.from(parent.getContext()).inflate(R.layout.todaylogins_model, parent, false);
    return new MyViewHolder(itemview);
  }

  @Override
  public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
    final TodayLoginsModel todayLoginsModel = todayLoginsModelList.get(position);
    holder.name.setText(todayLoginsModel.getName());
    holder.circle_name.setText(todayLoginsModel.getCircle_name());
    holder.desg.setText(todayLoginsModel.getDesg());
    holder.email.setText(todayLoginsModel.getEmail());
    holder.msisdn.setText(todayLoginsModel.getMsisdn());
    holder.last_login.setText(todayLoginsModel.getLast_login());
  }

  @Override
  public int getItemCount() {
    return todayLoginsModelList.size();
  }


  public class MyViewHolder extends RecyclerView.ViewHolder {

    TextView name, circle_name, desg, email, site_count , msisdn, last_login;
    public MyViewHolder(@NonNull View itemView) {
      super(itemView);
      name = itemView.findViewById(R.id.name);
      circle_name = itemView.findViewById(R.id.circle_name);
      desg = itemView.findViewById(R.id.desg);
      email = itemView.findViewById(R.id.email);
      site_count= itemView.findViewById(R.id.site_count);
      msisdn=itemView.findViewById(R.id.msisdn);
      last_login = itemView.findViewById(R.id.last_login);
    }
  }
}
