package com.example.adminfoodapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.example.adminfoodapp.classes.FoodItem;
import com.example.adminfoodapp.classes.Order;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrderDetailActivity extends AppCompatActivity {

    private TextView tvPhoneNumber, tvCreated, tvAddress, tvPaymentMethod, tvTotal;
    private RecyclerView rvFoodList;
    private Button btnCompleteOrder;
    private Order order;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        tvPhoneNumber = findViewById(R.id.tvPhoneNumber);
        tvCreated = findViewById(R.id.tvCreated);
        tvAddress = findViewById(R.id.tvAddress);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
        tvTotal = findViewById(R.id.tvTotal);
        rvFoodList = findViewById(R.id.rvFoodList);
        btnCompleteOrder = findViewById(R.id.btnCompleteOrder);

        rvFoodList.setLayoutManager(new LinearLayoutManager(this));

        String orderId = getIntent().getStringExtra("orderId");
        loadOrderDetail(orderId);

        btnCompleteOrder.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận")
                    .setMessage("Bạn có chắc chắn muốn hoàn thành đơn hàng này?")
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> completeOrder(order))
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        });
    }

    private void loadOrderDetail(String orderId) {
        Backendless.Data.of(Order.class).findById(orderId, new AsyncCallback<Order>() {
            @Override
            public void handleResponse(Order response) {
                order = response;
                tvPhoneNumber.setText("Số điện thoại: " + order.getPhone_number());
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                tvCreated.setText("Đặt lúc: " + formatter.format(order.getCreated()));
                tvAddress.setText("Địa chỉ: " + order.getAddress());
                tvPaymentMethod.setText("Phương thức thanh toán: Tiền mặt" );
                tvTotal.setText("Tổng tiền: " + order.getTotal());

                // Parse food_list using org.json
                List<FoodItem> foodItemList = parseFoodList(order.getFood_list());

                FoodItemAdapter foodItemAdapter = new FoodItemAdapter(foodItemList);
                rvFoodList.setAdapter(foodItemAdapter);
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(OrderDetailActivity.this, "Error: " + fault.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("OrderDetailActivity", "Error loading order detail: " + fault.getMessage());
            }
        });
    }

    private List<FoodItem> parseFoodList(String foodListJson) {
        List<FoodItem> foodItemList = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(foodListJson);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String name = jsonObject.getString("name");
                int quantity = jsonObject.getInt("quantity");
                int price = jsonObject.getInt("price");
                foodItemList.add(new FoodItem(name, quantity, price));
            }
        } catch (JSONException e) {
            Log.e("OrderDetailActivity", "Error parsing food list: " + e.getMessage());
        }
        return foodItemList;
    }

    private void completeOrder(Order order) {
        order.setIs_done(true);
        Backendless.Data.of(Order.class).save(order, new AsyncCallback<Order>() {
            @Override
            public void handleResponse(Order response) {
                Toast.makeText(OrderDetailActivity.this, "Đơn hàng đã được hoàn thành", Toast.LENGTH_SHORT).show();
                finish(); // Close OrderDetailActivity
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(OrderDetailActivity.this, "Error: " + fault.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("OrderDetailActivity", "Error completing order: " + fault.getMessage());
            }
        });
    }
}