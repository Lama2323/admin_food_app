package com.example.adminfoodapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.example.adminfoodapp.classes.Order;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportActivity extends AppCompatActivity {

    private EditText etFromDate;
    private EditText etToDate;
    private Button btnFilter;
    private TextView tvRevenue;
    private TextView tvOrderCount;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        etFromDate = findViewById(R.id.etFromDate);
        etToDate = findViewById(R.id.etToDate);
        btnFilter = findViewById(R.id.btnFilter);
        tvRevenue = findViewById(R.id.tvRevenue);
        tvOrderCount = findViewById(R.id.tvOrderCount);

        etFromDate.setOnClickListener(v -> showDatePickerDialog(etFromDate));
        etToDate.setOnClickListener(v -> showDatePickerDialog(etToDate));

        btnFilter.setOnClickListener(v -> {
            String fromDateStr = etFromDate.getText().toString();
            String toDateStr = etToDate.getText().toString();
            loadReport(fromDateStr, toDateStr);
        });
    }

    private void loadReport(String fromDateStr, String toDateStr) {
        if (fromDateStr.isEmpty() || toDateStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ ngày bắt đầu và ngày kết thúc", Toast.LENGTH_SHORT).show();
            return;
        }

        Date fromDate;
        Date toDate;

        try {
            fromDate = dateFormat.parse(fromDateStr);
            toDate = dateFormat.parse(toDateStr);
        } catch (ParseException e) {
            Toast.makeText(this, "Định dạng ngày tháng không đúng", Toast.LENGTH_SHORT).show();
            return;
        }

        if (fromDate.after(toDate)) {
            Toast.makeText(this, "Ngày bắt đầu phải trước ngày kết thúc", Toast.LENGTH_SHORT).show();
            return;
        }

        Date startDate = getStartOfDay(fromDate);
        Date endDate = getEndOfDay(toDate);

        String whereClause = "is_done = true" +
                " and (updated >= '" + startDate + "' or (updated is null and created >= '" + startDate + "'))" +
                " and (updated <= '" + endDate + "' or (updated is null and created <= '" + endDate + "'))";

        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setWhereClause(whereClause);

        Backendless.Data.of(Order.class).find(queryBuilder, new AsyncCallback<List<Order>>() {
            @Override
            public void handleResponse(List<Order> response) {
                long totalRevenue = 0;
                int orderCount = response.size();

                for (Order order : response) {
                    totalRevenue += order.getTotal();
                }

                tvRevenue.setText("Doanh thu: " + totalRevenue);
                tvOrderCount.setText("Số đơn hàng: " + orderCount);
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(ReportActivity.this, "Error: " + fault.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("ReportActivity", "Error loading report: " + fault.getMessage());
            }
        });
    }

    private void showDatePickerDialog(final EditText editText) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year1);
                    calendar.set(Calendar.MONTH, monthOfYear);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    Date selectedDate = calendar.getTime();
                    editText.setText(dateFormat.format(selectedDate));
                }, year, month, day);

        datePickerDialog.show();
    }

    private Date getStartOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private Date getEndOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
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
        } else if (id == R.id.menu_report) {
            return true;
        } else if (id == R.id.menu_order) {
            Intent intent = new Intent(this, OrderActivity.class);
            startActivity(intent);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}