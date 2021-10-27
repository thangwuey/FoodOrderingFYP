package ViewHolder;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.foodorderingfyp.R;

import Interface.foodItemClickListener;

public class AdminOrderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView txtAdminSendOrderID, txtAdminSendOrderTime, txtAdminSendOrderState;
    public foodItemClickListener listener;

    public AdminOrderViewHolder(View itemView)
    {
        super(itemView);

        txtAdminSendOrderID = (TextView) itemView.findViewById(R.id.aso_order_id);
        txtAdminSendOrderTime = (TextView) itemView.findViewById(R.id.aso_order_time);
        txtAdminSendOrderState = (TextView) itemView.findViewById(R.id.aso_order_state);
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
