package com.cmtsbsnl.cnmc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MyBtsUserAdapter extends RecyclerView.Adapter<MyBtsUserAdapter.MyViewHolder> {
    private final Context context;
    private final List<MyBtsUserModel> myBtsUserModelList;
    public MyBtsUserAdapter(Context context, List<MyBtsUserModel> myBtsUserModelList) {
        this.context = context;
        this.myBtsUserModelList = myBtsUserModelList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemview = LayoutInflater.from(parent.getContext()).inflate(R.layout.mybtsuser_model,parent, false);
        return new MyViewHolder(itemview);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final MyBtsUserModel myBtsModel = myBtsUserModelList.get(position);
        holder.name.setText(myBtsModel.getName());
        holder.circle_name.setText(myBtsModel.getCircle_name());
        holder.desg.setText(myBtsModel.getDesg());
        holder.email.setText(myBtsModel.getEmail());
        holder.site_count.setText(myBtsModel.getSite_count());
        holder.msisdn.setText(myBtsModel.getMsisdn());
    }

    @Override
    public int getItemCount() {
        return myBtsUserModelList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name, circle_name, desg, email, site_count , msisdn;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            circle_name = itemView.findViewById(R.id.circle_name);
            desg = itemView.findViewById(R.id.desg);
            email = itemView.findViewById(R.id.email);
            site_count= itemView.findViewById(R.id.site_count);
            msisdn=itemView.findViewById(R.id.msisdn);
        }
    }
}
