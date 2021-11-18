package ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.foodorderingfyp.R;

import Interface.foodItemClickListener;

public class DriverListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView txtDriverName, txtDriverPhone;
    public ImageView driverImageView;
    public foodItemClickListener listener;

    public DriverListViewHolder(View itemView)
    {
        super(itemView);


        driverImageView = itemView.findViewById(R.id.driver_list_image);
        txtDriverName = itemView.findViewById(R.id.driver_list_name);
        txtDriverPhone = itemView.findViewById(R.id.driver_list_phone);
    }

    public void setItemClickListener(foodItemClickListener listener)
    {
        this.listener = listener;
    }

    @Override
    public void onClick(View view) {
        listener.onClick(view, getAdapterPosition(), false);
    }
}
