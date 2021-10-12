package ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.foodorderingfyp.R;

import Interface.foodItemClickListener;

public class AdminFoodViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView txtAdminFoodName, txtAdminFoodPrice;
    public ImageView adminImageView;
    public foodItemClickListener listener;

    public AdminFoodViewHolder(View itemView)
    {
        super(itemView);


        adminImageView = (ImageView) itemView.findViewById(R.id.admin_food_image);
        txtAdminFoodName = (TextView) itemView.findViewById(R.id.admin_food_name);
        //txtAdminFoodPrice = (TextView) itemView.findViewById(R.id.admin_food_price);
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
