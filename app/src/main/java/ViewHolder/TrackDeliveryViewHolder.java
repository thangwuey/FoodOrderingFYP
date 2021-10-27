package ViewHolder;

import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodorderingfyp.R;

import org.jetbrains.annotations.NotNull;

import Interface.foodItemClickListener;

public class TrackDeliveryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView txtTrackOrderID, txtTrackOrderTime, txtTrackOrderAmount;
    public Button btnArrow, btnTrack;
    public RelativeLayout rlExpandableLayout;
    public CardView cvCardView;
    private foodItemClickListener itemClickListener;

    public TrackDeliveryViewHolder(@NonNull @NotNull View itemView)
    {
        super(itemView);

        txtTrackOrderID = itemView.findViewById(R.id.td_order_id);
        txtTrackOrderTime = itemView.findViewById(R.id.td_order_time);
        txtTrackOrderAmount = itemView.findViewById(R.id.td_order_amount);
        btnArrow = itemView.findViewById(R.id.td_arrow_button);
        btnTrack = itemView.findViewById(R.id.td_order_button);
        rlExpandableLayout = itemView.findViewById(R.id.td_expandable_layout);
        cvCardView = itemView.findViewById(R.id.td_card_view);

        cvCardView.setOnClickListener((view) -> {
            if (rlExpandableLayout.getVisibility()==View.GONE) {
                TransitionManager.beginDelayedTransition(cvCardView, new AutoTransition());
                rlExpandableLayout.setVisibility(View.VISIBLE);
                btnArrow.setBackgroundResource(R.drawable.ic_show_less);
            } else {
                TransitionManager.beginDelayedTransition(cvCardView, new AutoTransition());
                rlExpandableLayout.setVisibility(View.GONE);
                btnArrow.setBackgroundResource(R.drawable.ic_show_more);
            }
        });

        btnArrow.setOnClickListener((view) -> {
            if (rlExpandableLayout.getVisibility()==View.GONE) {
                TransitionManager.beginDelayedTransition(cvCardView, new AutoTransition());
                rlExpandableLayout.setVisibility(View.VISIBLE);
                btnArrow.setBackgroundResource(R.drawable.ic_show_less);
            } else {
                TransitionManager.beginDelayedTransition(cvCardView, new AutoTransition());
                rlExpandableLayout.setVisibility(View.GONE);
                btnArrow.setBackgroundResource(R.drawable.ic_show_more);
            }
        });
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
