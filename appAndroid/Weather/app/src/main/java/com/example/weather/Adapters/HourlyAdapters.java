package com.example.weather.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.weather.Domains.Hourly;
import com.example.weather.R;

import java.util.ArrayList;

public class HourlyAdapters extends RecyclerView.Adapter<HourlyAdapters.viewHolder> {
    ArrayList<Hourly> items;
    Context context;

    public HourlyAdapters(ArrayList<Hourly> items, Context context) {

        this.items = items;
    }

    @NonNull
    @Override
    public HourlyAdapters.viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate= LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_hourly,parent,false);
        context = parent.getContext();
        return new viewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull HourlyAdapters.viewHolder holder, int position) {
        holder.hourTxT.setText(items.get(position).getHour());
        holder.tempTxT.setText(items.get(position).getTemp()+"°");

        int drawableResourceID=holder.itemView.getResources()
                .getIdentifier(items.get(position).getPicPath(),"drawable",holder.itemView.getContext().getPackageName());
        Glide.with(context)
                .load(drawableResourceID)
                .into(holder.pic);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    public class viewHolder extends RecyclerView.ViewHolder{
        TextView hourTxT,tempTxT;
        ImageView pic;
        public viewHolder(@NonNull View itemView) {
            super(itemView);

            hourTxT=itemView.findViewById(R.id.hourTxT);
            tempTxT=itemView.findViewById(R.id.tempTxT);
            pic=itemView.findViewById(R.id.pic);
        }
    }
}
