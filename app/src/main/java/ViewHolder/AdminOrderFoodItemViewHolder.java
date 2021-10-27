package ViewHolder;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.foodorderingfyp.R;

import Interface.foodItemClickListener;

public class AdminOrderFoodItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView txtAdminOrderFoodName, txtAdminOrderFoodQuantity, txtAdminOrderFoodSubtotal;
    public foodItemClickListener listener;

    public AdminOrderFoodItemViewHolder(View itemView)
    {
        super(itemView);

        txtAdminOrderFoodName = (TextView) itemView.findViewById(R.id.order_food_item_name);
        txtAdminOrderFoodQuantity = (TextView) itemView.findViewById(R.id.order_food_item_quantity);
        txtAdminOrderFoodSubtotal = (TextView) itemView.findViewById(R.id.order_food_item_subtotal);
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