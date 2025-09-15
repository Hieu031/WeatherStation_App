/*** --- Function of module --- 
 * Adapter is use for RecyclerView to display the next day weather forecard. 
 */
package com.example.myapplication.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.Domains.FutureDomain;
import com.example.myapplication.Domains.Hourly;
import com.example.myapplication.R;

import java.util.ArrayList;

// Adapter for RecyclerView display list "Future"
public class FutureAdapters extends RecyclerView.Adapter<FutureAdapters.viewHolder> {
    ArrayList<FutureDomain> items;
    Context context;

    // Constructor: Receive data and context from Activity/Fragment
    public FutureAdapters(ArrayList<FutureDomain> items, Context context) {

        this.items = items;
    }

    // Create ViewHolder (Init layout item for each row RecyclerView)
    @NonNull
    @Override
    public FutureAdapters.viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Load viewholder_hourly.xml into a View
        View inflate= LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_future,parent,false);
        context = parent.getContext(); // Retrieve context from parent
        return new viewHolder(inflate);
    }

    // Assign data for each item in list
    @Override
    public void onBindViewHolder(@NonNull FutureAdapters.viewHolder holder, int position) {

        // assign attributes
        holder.dayTxt.setText(items.get(position).getDay());
        holder.statusTxt.setText(items.get(position).getStatus());
        holder.lowTxt.setText(items.get(position).getLowTemp()+"%");
        holder.highTxt.setText(items.get(position).getHighTemp()+"°");

        // Take image's ID from name's file in drawable
        int drawableResourceID=holder.itemView.getResources()
                .getIdentifier(items.get(position).getPicPath(),"drawable",holder.itemView.getContext().getPackageName());
        
        // Load image into Image View
        Glide.with(context)
                .load(drawableResourceID)
                .into(holder.pic);
    }

    // Return count items
    @Override
    public int getItemCount() {
        return items.size();
    }

    // The ViewHolder class maps the Views in the viewholder_future.xml layout
    public class viewHolder extends RecyclerView.ViewHolder{
        TextView dayTxt,statusTxt,lowTxt,highTxt;
        ImageView pic;
        public viewHolder(@NonNull View itemView) {
            super(itemView);

            dayTxt=itemView.findViewById(R.id.dayTxt);
            statusTxt=itemView.findViewById(R.id.statusTxt);
            lowTxt=itemView.findViewById(R.id.lowTxt);
            highTxt=itemView.findViewById(R.id.highTxt);
            pic=itemView.findViewById(R.id.pic);
        }
    }
}
