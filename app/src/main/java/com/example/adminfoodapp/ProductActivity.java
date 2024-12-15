package com.example.adminfoodapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.example.adminfoodapp.classes.Product;
import com.example.adminfoodapp.utils.VietnameseUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductActivity extends BaseNetworkActivity {
    private static final String TAG = "ProductActivity";

    // Views
    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private EditText searchEditText;
    private Button addProductButton;

    // Data
    private final List<Product> productList = new ArrayList<>();
    private final List<Product> allProductsList = new ArrayList<>();
    private boolean isInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

        initializeViews();
        setupRecyclerView();
        setupSwipeRefresh();
        setupSearch();
        setupClickListeners();
        loadProducts();
        isInitialized = true; // Thêm dòng này để kích hoạt filterProducts
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        searchEditText = findViewById(R.id.searchEditText);
        addProductButton = findViewById(R.id.add_product_button);
    }

    private void setupRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new ProductAdapter(this, productList, this::onProductClick);
        recyclerView.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );
        swipeRefreshLayout.setOnRefreshListener(this::loadProducts);
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupClickListeners() {
        addProductButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddProductActivity.class);
            startActivity(intent);
        });
    }

    private void loadProducts() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(true);
        }

        Backendless.Data.of(Product.class).find(new AsyncCallback<List<Product>>() {
            @Override
            public void handleResponse(List<Product> response) {
                if (!isFinishing() && !isDestroyed()) {
                    runOnUiThread(() -> {
                        try {
                            updateProductList(response);
                        } catch (Exception e) {
                            Log.e(TAG, "Error updating product list: " + e.getMessage(), e);
                            showError("Error updating products");
                        }
                    });
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                if (!isFinishing() && !isDestroyed()) {
                    runOnUiThread(() -> {
                        Log.e(TAG, "Error loading products: " + fault.getMessage());
                        showError("Error loading products: " + fault.getMessage());
                        if (swipeRefreshLayout != null) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    });
                }
            }
        });
    }

    private void updateProductList(List<Product> response) {
        productList.clear();
        allProductsList.clear();

        if (response != null) {
            productList.addAll(response);
            allProductsList.addAll(response);
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void filterProducts(String searchText) {
        if (!isInitialized) return;

        productList.clear();

        if (searchText.isEmpty()) {
            productList.addAll(allProductsList);
        } else {
            String normalizedSearch = VietnameseUtils.removeAccents(
                    searchText.toLowerCase(Locale.getDefault())
            );

            for (Product product : allProductsList) {
                String normalizedName = VietnameseUtils.removeAccents(
                        product.getName().toLowerCase(Locale.getDefault())
                );

                if (normalizedName.contains(normalizedSearch)) {
                    productList.add(product);
                }
            }
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void onProductClick(Product product) {
        Intent intent = new Intent(this, UpdateProductActivity.class);
        intent.putExtra("objectId", product.getObjectId());
        startActivity(intent);
    }

    private void showError(String message) {
        if (!isFinishing() && !isDestroyed()) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_product) {
            return true;
        } else if (id == R.id.menu_profile) {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menu_report) {
            Intent intent = new Intent(this, ReportActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menu_order) {
            Intent intent = new Intent(this, OrderActivity.class);
            startActivity(intent);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
}