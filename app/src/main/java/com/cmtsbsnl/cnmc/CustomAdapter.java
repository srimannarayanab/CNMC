package com.cmtsbsnl.cnmc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;

public class CustomAdapter extends ArrayAdapter<DataModel> {
    Context mContext;
    public static ArrayList<DataModel> dataSet;

    // View lookup cache
    private static class ViewHolder {
        TextView txtName;
        CheckBox checkBox;
    }

    public CustomAdapter(Context context, ArrayList<DataModel> data) {
        super(context, R.layout.row_item, data);
        dataSet = data;
        this.mContext = context;
    }
    @Override
    public int getCount() {
        return dataSet.size();
    }

    @Override
    public DataModel getItem(int position) {
        return dataSet.get(position);
    }

    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;
        final View result;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item, parent, false);
            viewHolder.txtName = convertView.findViewById(R.id.txtName);
            viewHolder.checkBox = convertView.findViewById(R.id.checkBox);
            result=convertView;
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

        DataModel item = getItem(position);

        viewHolder.txtName.setText(mContext.getString(R.string.name_value_hypen,item.bts_name,item.bts_type));
        viewHolder.checkBox.setChecked(item.checked);

        return result;
    }

    public void updateList(ArrayList<DataModel> filteredList){
        dataSet = filteredList;
        notifyDataSetChanged();
    }
}

