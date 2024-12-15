package com.example.adminfoodapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.adminfoodapp.classes.Product;
import com.example.adminfoodapp.utils.VietnameseUtils;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.badge.ExperimentalBadgeUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductActivity extends BaseAuthenticatedActivity {
    private static final String TAG = "ProductActivity";

    // Views
    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private EditText searchEditText;
    private TextView userNameTextView;
    private ImageView profileIcon;
    private Button addProductButton;

    // Data
    private final List<Product> productList = new ArrayList<>();
    private final List<Product> allProductsList = new ArrayList<>();
    private boolean isInitialized = false;
    private static final RequestOptions profileImageOptions = new RequestOptions()
            .placeholder(R.drawable.ic_profile)
            .error(R.drawable.ic_profile)
            .centerCrop()
            .dontAnimate();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);
    }

    @ExperimentalBadgeUtils
    @Override
    protected void onAuthenticated() {
        try {
            if (!isInitialized) {
                initializeViews();
                setupUserInfo();
                setupRecyclerView();
                setupSwipeRefresh();
                setupSearch();
                setupClickListeners();
                isInitialized = true;
            }
            loadProducts();
        } catch (Exception e) {
            Log.e(TAG, "Error in onAuthenticated: " + e.getMessage(), e);
            Toast.makeText(this, "Error initializing product view", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        searchEditText = findViewById(R.id.searchEditText);
        userNameTextView = findViewById(R.id.userNameTextView);
        profileIcon = findViewById(R.id.profileIcon);
        addProductButton = findViewById(R.id.add_product_button);
    }

    private void setupUserInfo() {
        String userId = Backendless.UserService.loggedInUser();

        Backendless.UserService.findById(userId, new AsyncCallback<BackendlessUser>() {
            @Override
            public void handleResponse(BackendlessUser user) {
                if (!isFinishing() && !isDestroyed()) {
                    runOnUiThread(() -> {
                        String userName = user.getProperty("name") != null ?
                                (String) user.getProperty("name") : "Admin";
                        userNameTextView.setText(userName);

                        String imageUrl = user.getProperty("image_source") != null ?
                                (String) user.getProperty("image_source") : "";
                        if (!imageUrl.isEmpty()) {
                            preloadProfileImage(imageUrl);

                            Glide.with(ProductActivity.this)
                                    .load(imageUrl)
                                    .apply(profileImageOptions)
                                    .into(profileIcon);
                        } else {
                            profileIcon.setImageResource(R.drawable.ic_profile);
                        }
                    });
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                if (!isFinishing() && !isDestroyed()) {
                    Log.e(TAG, "Error fetching user info: " + fault.getMessage());
                    runOnUiThread(() ->
                            Toast.makeText(ProductActivity.this,
                                    "Error loading user info",
                                    Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    private void preloadProfileImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .apply(profileImageOptions)
                    .preload();
        }
    }

    private void setupRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new ProductAdapter(this, productList, this::onProductClick);
        recyclerView.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setColorSchemeResources( // setup màu sắc của swipe refresh indicator
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );
        swipeRefreshLayout.setOnRefreshListener(this::loadProducts);  //setup func sẽ thực thi khi swipe, ở đây là func loadProducts
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
        profileIcon.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        });

        // Thêm sự kiện onClick cho nút "Thêm món"
        addProductButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, UpdateProductActivity.class);
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
    protected void onResume() {
        super.onResume();
        if (isInitialized) {
            if (getIntent().getBooleanExtra("USER_UPDATED", false)) {
                setupUserInfo();
                // Xóa flag để tránh load lại khi activity resume lần sau
                getIntent().removeExtra("USER_UPDATED");
            }
        }
    }

    @Override
    protected void handleAuthenticationError(Exception e) {
        super.handleAuthenticationError(e);
        showError("Authentication error occurred");
    }

    @Override
    public void onBackPressed() {}
}