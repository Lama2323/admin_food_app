package com.example.adminfoodapp;

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

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.files.BackendlessFile;
import com.backendless.persistence.DataQueryBuilder;
import com.example.adminfoodapp.classes.Product;
import com.example.adminfoodapp.utils.ImageCompressor;
import com.example.adminfoodapp.utils.VietnameseUtils;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

public class AddProductActivity extends BaseNetworkActivity {

    private static final String TAG = "AddProductActivity";
    private static final int PICK_IMAGE_REQUEST = 1;

    // Views
    private EditText productNameEditText;
    private EditText productDescriptionEditText;
    private ImageView productImageView;
    private Button chooseImageButton;
    private EditText productPriceEditText;
    private EditText productQuantityEditText;
    private Button addProductButton;

    // Image
    private Uri imageUri;
    private Bitmap selectedImageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        productNameEditText = findViewById(R.id.productNameEditText);
        productDescriptionEditText = findViewById(R.id.productDescriptionEditText);
        productImageView = findViewById(R.id.productImageView);
        chooseImageButton = findViewById(R.id.chooseImageButton);
        productPriceEditText = findViewById(R.id.productPriceEditText);
        productQuantityEditText = findViewById(R.id.productQuantityEditText);
        addProductButton = findViewById(R.id.addProductButton);
    }

    private void setupClickListeners() {
        chooseImageButton.setOnClickListener(v -> openFileChooser());
        addProductButton.setOnClickListener(v -> addProduct());
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
            } catch (FileNotFoundException e) {
                Log.e(TAG, "Error loading image: " + e.getMessage());
                showError("Error loading image");
            }
        }
    }

    private void addProduct() {
        showProgressDialog("Đang thêm sản phẩm...");

        String name = productNameEditText.getText().toString().trim();
        String description = productDescriptionEditText.getText().toString().trim();
        String priceString = productPriceEditText.getText().toString().trim();
        String quantityString = productQuantityEditText.getText().toString().trim();

        // Validation
        if (name.isEmpty() || description.isEmpty() || priceString.isEmpty() || quantityString.isEmpty()) {
            hideProgressDialog();
            showError("Vui lòng nhập đầy đủ thông tin");
            return;
        }

        int price, quantity;
        try {
            price = Integer.parseInt(priceString);
            quantity = Integer.parseInt(quantityString);
        } catch (NumberFormatException e) {
            hideProgressDialog();
            showError("Giá và số lượng phải là số");
            return;
        }

        if (price <= 0 || quantity <= 0) {
            hideProgressDialog();
            showError("Giá và số lượng phải lớn hơn 0");
            return;
        }

        // Check for duplicate name using LOWER and WHERE clause
        String whereClause = "LOWER(name) = '" + VietnameseUtils.removeAccents(name.toLowerCase()) + "'";
        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setWhereClause(whereClause);

        Backendless.Data.of(Product.class).find(queryBuilder, new AsyncCallback<List<Product>>() {
            @Override
            public void handleResponse(List<Product> response) {
                if (!response.isEmpty()) {
                    hideProgressDialog();
                    showError("Tên sản phẩm đã tồn tại");
                } else {
                    if (selectedImageBitmap != null) {
                        uploadImageAndSaveProduct(name, description, price, quantity);
                    } else {
                        saveProduct(name, description, price, quantity, null);
                    }
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

    private void uploadImageAndSaveProduct(String name, String description, int price, int quantity) {
        // Compress image
        Bitmap compressedBitmap = ImageCompressor.compressBitmap(selectedImageBitmap, 50); // Compress to under 50KB

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
                        saveProduct(name, description, price, quantity, response.getFileURL());
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        hideProgressDialog();
                        Log.e(TAG, "Error uploading image: " + fault.getMessage());
                        showError("Lỗi khi tải ảnh lên: " + fault.getMessage());
                    }
                }
        );
    }
    private void saveProduct(String name, String description, int price, int quantity, String imageUrl) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setQuantity(quantity);
        product.setImage_source(imageUrl);

        Backendless.Data.of(Product.class).save(product, new AsyncCallback<Product>() {
            @Override
            public void handleResponse(Product response) {
                hideProgressDialog();
                showToast("Thêm sản phẩm thành công");
                finish();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                hideProgressDialog();
                Log.e(TAG, "Error saving product: " + fault.getMessage());
                showError("Lỗi khi lưu sản phẩm: " + fault.getMessage());
            }
        });
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}