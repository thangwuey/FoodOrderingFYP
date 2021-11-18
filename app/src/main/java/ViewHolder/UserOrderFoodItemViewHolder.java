package ViewHolder;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.foodorderingfyp.R;

import Interface.foodItemClickListener;

public class UserOrderFoodItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView txtAdminOrderFoodName, txtAdminOrderFoodQuantity, txtAdminOrderFoodSubtotal;
    public foodItemClickListener listener;

    public UserOrderFoodItemViewHolder(View itemView)
    {
        super(itemView);

        txtAdminOrderFoodName = itemView.findViewById(R.id.user_order_food_item_name);
        txtAdminOrderFoodQuantity = itemView.findViewById(R.id.user_order_food_item_quantity);
        txtAdminOrderFoodSubtotal = itemView.findViewById(R.id.user_order_food_item_subtotal);
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
