package ViewHolder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodorderingfyp.R;

import org.jetbrains.annotations.NotNull;

import Interface.foodItemClickListener;

public class CartViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    //Video 24
    public TextView txtFoodName, txtFoodPrice, txtFoodQuantity;
    private foodItemClickListener itemClickListener;

    public CartViewHolder(@NonNull @NotNull View itemView)
    {
        super(itemView);

        txtFoodName = itemView.findViewById(R.id.cart_food_name);
        txtFoodPrice = itemView.findViewById(R.id.cart_food_price);
        txtFoodQuantity = itemView.findViewById(R.id.cart_food_quantity);
    }

    @Override
    public void onClick(View v)
    {
        itemClickListener.onClick(v,getAdapterPosition(),false);
    }

    public void setItemClickListener(foodItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }
}
