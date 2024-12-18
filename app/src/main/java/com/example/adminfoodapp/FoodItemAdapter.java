package com.example.adminfoodapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adminfoodapp.classes.FoodItem;

import java.util.List;

public class FoodItemAdapter extends RecyclerView.Adapter<FoodItemAdapter.FoodItemViewHolder> {

    private List<FoodItem> foodItemList;

    public FoodItemAdapter(List<FoodItem> foodItemList) {
        this.foodItemList = foodItemList;
    }

    @NonNull
    @Override
    public FoodItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.food_item, parent, false);
        return new FoodItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodItemViewHolder holder, int position) {
        FoodItem foodItem = foodItemList.get(position);
        holder.tvFoodName.setText(foodItem.getName());
        holder.tvQuantity.setText(String.valueOf(foodItem.getQuantity()));
        holder.tvPrice.setText(String.valueOf(foodItem.getPrice()));
    }

    @Override
    public int getItemCount() {
        return foodItemList.size();
    }

    static class FoodItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvFoodName, tvQuantity, tvPrice;

        public FoodItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFoodName = itemView.findViewById(R.id.tvFoodName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }
    }
}
