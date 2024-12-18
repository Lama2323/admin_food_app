package com.example.adminfoodapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.files.BackendlessFile;
import com.backendless.persistence.DataQueryBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.adminfoodapp.classes.Product;
import com.example.adminfoodapp.utils.ImageCompressor;
import com.example.adminfoodapp.utils.VietnameseUtils;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

public class UpdateProductActivity extends BaseNetworkActivity {

    private static final String TAG = "UpdateProductActivity";
    private static final int PICK_IMAGE_REQUEST = 1;

    // Views
    private EditText productNameEditText;
    private EditText productDescriptionEditText;
    private ImageView productImageView;
    private Button chooseImageButton;
    private EditText productPriceEditText;
    private EditText productQuantityEditText;
    private Button deleteProductButton;
    private Button updateProductButton;

    // Product data
    private Product currentProduct;
    private String objectId;
    private String currentImageUrl;

    // Image
    private Uri imageUri;
    private Bitmap selectedImageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_product);

        objectId = getIntent().getStringExtra("objectId");

        initializeViews();
        setupClickListeners();
        loadProductData();
    }

    private void initializeViews() {
        productNameEditText = findViewById(R.id.productNameEditText);
        productDescriptionEditText = findViewById(R.id.productDescriptionEditText);
        productImageView = findViewById(R.id.productImageView);
        chooseImageButton = findViewById(R.id.chooseImageButton);
        productPriceEditText = findViewById(R.id.productPriceEditText);
        productQuantityEditText = findViewById(R.id.productQuantityEditText);
        updateProductButton = findViewById(R.id.updateProductButton);
        deleteProductButton = findViewById(R.id.deleteProductButton);
    }

    private void setupClickListeners() {
        chooseImageButton.setOnClickListener(v -> openFileChooser());
        updateProductButton.setOnClickListener(v -> checkForDuplicateNameAndUpdate());
        deleteProductButton.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();

            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                selectedImageBitmap = BitmapFactory.decodeStream(inputStream);
                productImageView.setImageBitmap(selectedImageBitmap);
                currentImageUrl = null; // Reset current image URL if a new image is selected
            } catch (FileNotFoundException e) {
                Log.e(TAG, "Error loading image: " + e.getMessage());
                showError("Error loading image");
            }
        }
    }

    private void loadProductData() {
        showProgressDialog("Loading product data...");

        Backendless.Data.of(Product.class).findById(objectId, new AsyncCallback<Product>() {
            @Override
            public void handleResponse(Product response) {
                currentProduct = response;
                currentImageUrl = currentProduct.getImage_source();
                runOnUiThread(() -> {
                    productNameEditText.setText(currentProduct.getName());
                    productDescriptionEditText.setText(currentProduct.getDescription());
                    productPriceEditText.setText(String.valueOf(currentProduct.getPrice()));
                    productQuantityEditText.setText(String.valueOf(currentProduct.getQuantity()));

                    // Load image using Glide with placeholder
                    if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
                        Glide.with(UpdateProductActivity.this)
                                .asBitmap()
                                .load(currentImageUrl)
                                .listener(new RequestListener<Bitmap>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                        Log.e(TAG, "Error loading image with Glide: " + e.getMessage());
                                        runOnUiThread(() -> hideProgressDialog());
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                        runOnUiThread(() -> hideProgressDialog());
                                        return false;
                                    }
                                })
                                .into(productImageView);
                    } else {
                        // Use ic_food as placeholder if no image is available
                        Glide.with(UpdateProductActivity.this)
                                .load(R.drawable.ic_food)
                                .into(productImageView);
                        hideProgressDialog();
                    }
                });
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                hideProgressDialog();
                Log.e(TAG, "Error loading product data: " + fault.getMessage());
                showError("Error loading product data: " + fault.getMessage());
                finish();
            }
        });
    }

    private void checkForDuplicateNameAndUpdate() {
        showProgressDialog("Đang cập nhật sản phẩm...");
        String name = productNameEditText.getText().toString().trim();

        // Check for duplicate name (excluding the current product) using LOWER and WHERE clause
        String whereClause = "LOWER(name) = '" + VietnameseUtils.removeAccents(name.toLowerCase()) + "' and objectId != '" + objectId + "'";
        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setWhereClause(whereClause);

        Backendless.Data.of(Product.class).find(queryBuilder, new AsyncCallback<List<Product>>() {
            @Override
            public void handleResponse(List<Product> response) {
                if (!response.isEmpty()) {
                    hideProgressDialog();
                    showError("Tên sản phẩm đã tồn tại");
                } else {
                    updateProduct();
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                hideProgressDialog();
                Log.e(TAG, "Error checking for duplicate product name: " + fault.getMessage());
                showError("Lỗi khi kiểm tra trùng tên sản phẩm: " + fault.getMessage());
            }
        });
    }

    private void updateProduct() {
        String name = productNameEditText.getText().toString().trim();
        String description = productDescriptionEditText.getText().toString().trim();
        String priceString = productPriceEditText.getText().toString().trim();
        String quantityString = productQuantityEditText.getText().toString().trim();

        // Validation
        if (name.isEmpty() || description.isEmpty() || priceString.isEmpty() || quantityString.isEmpty()) {
            hideProgressDialog();
            showError("Please fill in all fields");
            return;
        }

        int price, quantity;
        try {
            price = Integer.parseInt(priceString);
            quantity = Integer.parseInt(quantityString);
        } catch (NumberFormatException e) {
            hideProgressDialog();
            showError("Price and quantity must be numbers");
            return;
        }

        if (price <= 0 || quantity <= 0) {
            hideProgressDialog();
            showError("Price and quantity must be greater than 0");
            return;
        }

        // Update currentProduct object
        currentProduct.setName(name);
        currentProduct.setDescription(description);
        currentProduct.setPrice(price);
        currentProduct.setQuantity(quantity);

        // Upload new image if selected
        if (selectedImageBitmap != null) {
            // Compress image
            Bitmap compressedBitmap = ImageCompressor.compressBitmap(selectedImageBitmap, 50);

            // Upload image to Backendless
            Backendless.Files.Android.upload(
                    compressedBitmap,
                    Bitmap.CompressFormat.JPEG,
                    80,
                    System.currentTimeMillis() + ".jpg",
                    "product_image",
                    new AsyncCallback<BackendlessFile>() {
                        @Override
                        public void handleResponse(BackendlessFile response) {
                            currentProduct.setImage_source(response.getFileURL());
                            saveProductChanges();
                        }

                        @Override
                        public void handleFault(BackendlessFault fault) {
                            hideProgressDialog();
                            Log.e(TAG, "Error uploading image: " + fault.getMessage());
                            showError("Error uploading image: " + fault.getMessage());
                        }
                    }
            );
        } else {
            // Keep the existing image URL if no new image is selected
            // If there was no image previously, set image_source to null
            currentProduct.setImage_source(currentImageUrl);
            saveProductChanges();
        }
    }

    private void saveProductChanges() {
        Backendless.Data.of(Product.class).save(currentProduct, new AsyncCallback<Product>() {
            @Override
            public void handleResponse(Product response) {
                hideProgressDialog();
                showToast("Product updated successfully");
                finish(); // Close UpdateProductActivity
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                hideProgressDialog();
                Log.e(TAG, "Error updating product: " + fault.getMessage());
                showError("Error updating product: " + fault.getMessage());
            }
        });
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa sản phẩm này?")
                .setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteProduct();
                    }
                })
                .setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void deleteProduct() {
        showProgressDialog("Deleting product...");

        Backendless.Data.of(Product.class).remove(currentProduct, new AsyncCallback<Long>() {
            @Override
            public void handleResponse(Long response) {
                hideProgressDialog();
                showToast("Product deleted successfully");
                finish(); // Close UpdateProductActivity
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                hideProgressDialog();
                Log.e(TAG, "Error deleting product: " + fault.getMessage());
                showError("Error deleting product: " + fault.getMessage());
            }
        });
    }

}