package com.example.adminfoodapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adminfoodapp.R;
import com.example.adminfoodapp.classes.Order;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<Order> orderList;
    private Context context;

    public OrderAdapter(List<Order> orderList, Context context) {
        this.orderList = orderList;
        this.context = context;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_item, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.tvPhoneNumber.setText("Số điện thoại: " + order.getPhone_number());
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        holder.tvCreated.setText("Đặt lúc: " + formatter.format(order.getCreated()));
        holder.tvTotal.setText("Tổng tiền: " + order.getTotal());
        holder.tvAddress.setText("Địa chỉ: " + order.getAddress());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, OrderDetailActivity.class);
            intent.putExtra("orderId", order.getObjectId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public void setOrders(List<Order> orders) {
        this.orderList = orders;
        notifyDataSetChanged();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvPhoneNumber, tvCreated, tvTotal, tvAddress;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPhoneNumber = itemView.findViewById(R.id.tvPhoneNumber);
            tvCreated = itemView.findViewById(R.id.tvCreated);
            tvTotal = itemView.findViewById(R.id.tvTotal);
            tvAddress = itemView.findViewById(R.id.tvAddress);
        }
    }
}