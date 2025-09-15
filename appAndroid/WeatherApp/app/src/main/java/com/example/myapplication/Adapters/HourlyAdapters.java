/*** --- Function of module --- 
 * Adapter is use for RecyclerView to display the next hour weather forecard. 
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

import com.example.myapplication.Domains.Hourly;
import com.example.myapplication.R;

import java.util.ArrayList;
import com.bumptech.glide.Glide;

// Adapter for RecyclerView display list "Hourly"
public class HourlyAdapters extends RecyclerView.Adapter<HourlyAdapters.viewHolder> {
    ArrayList<Hourly> items;    // List data follow hour
    Context context;            // Application context (needed to load images, inflate layout)

    // Constructor: Receive data and context from Activity/Fragment
    public HourlyAdapters(ArrayList<Hourly> items, Context context) {

        this.items   = items;
    }

    // Create ViewHolder (Init layout item for each row RecyclerView)
    @NonNull
    @Override
    public HourlyAdapters.viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Load viewHolder_hourly.xml into a View
        View inflate= LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_hourly,parent,false);
        context = parent.getContext(); // Retrieve context from parent
        return new viewHolder(inflate);
    }

    // Assign data for each item in list
    @Override
    public void onBindViewHolder(@NonNull HourlyAdapters.viewHolder holder, int position) {
        // assign hour and temp
        holder.hourTxT.setText(items.get(position).getHour());
        holder.tempTxT.setText(items.get(position).getTemp()+"°");

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

    // The ViewHolder class maps the Views in the viewholder_hourly.xml layout
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
