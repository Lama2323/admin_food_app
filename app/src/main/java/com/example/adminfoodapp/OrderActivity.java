package com.example.adminfoodapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.example.adminfoodapp.classes.Order;

import java.util.ArrayList;
import java.util.List;

public class OrderActivity extends AppCompatActivity {

    private RecyclerView rvOrders;
    private OrderAdapter orderAdapter;
    private List<Order> orderList;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        rvOrders = findViewById(R.id.rvOrders);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        orderList = new ArrayList<>();
        orderAdapter = new OrderAdapter(orderList, this);
        rvOrders.setAdapter(orderAdapter);

        loadOrders();

        swipeRefreshLayout.setOnRefreshListener(this::loadOrders);
    }

    private void loadOrders() {
        String whereClause = "is_done = false";
        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setWhereClause(whereClause);
        queryBuilder.setSortBy("created DESC");

        Backendless.Data.of(Order.class).find(queryBuilder, new AsyncCallback<List<Order>>() {
            @Override
            public void handleResponse(List<Order> response) {
                orderList.clear();
                orderList.addAll(response);
                orderAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(OrderActivity.this, "Error: " + fault.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("OrderActivity", "Error loading orders: " + fault.getMessage());
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_product) {
            Intent intent = new Intent(this, ProductActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menu_profile) {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menu_order) {
            return true;
        } else if (id == R.id.menu_report) {
            Intent intent = new Intent(this, ReportActivity.class);
            startActivity(intent);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}