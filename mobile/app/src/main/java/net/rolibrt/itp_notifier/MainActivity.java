package net.rolibrt.itp_notifier;

import android.Manifest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private static final int SMS_PERMISSION_CODE = 1001;

    private final OkHttpClient client = new OkHttpClient();
    private DataEntryAdapter adapter;
    private Button selectAllButton;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ActivityResultLauncher<Intent> settingsLauncher;
    private boolean allSelected = false;

    private final List<DataEntry> dataEntryList = new ArrayList<>();
    private final Queue<DataEntry> entryQueue = new LinkedList<>();
    private JSONArray jsonArray = new JSONArray();
    private boolean sendingSms = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RecyclerView recyclerView = findViewById(R.id.dataEntryList);
        selectAllButton = findViewById(R.id.selectAllButton);
        Button sendAllButton = findViewById(R.id.sendAllButton);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DataEntryAdapter(dataEntryList);
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            fetchData();
            swipeRefreshLayout.setRefreshing(false);
        });
        findViewById(R.id.button_settings).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
        sendAllButton.setOnClickListener(v -> {
            int selectedCount = (int) dataEntryList.stream().filter(DataEntry::isSelected).count();
            if (selectedCount == 0) {
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this, "Selectați cel puțin un item!", Toast.LENGTH_LONG).show()
                );
                return;
            }
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Send SMS")
                    .setMessage("Sigur doriți să trimiteți SMS la toți cei " +
                            selectedCount + "?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                                != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(this,
                                    new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
                        } else {
                            sendSelectedSms();
                        }
                    })
                    .setNegativeButton("Anulare", null)
                    .show();
        });
        settingsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        fetchData();
                    }
                }
        );
        findViewById(R.id.button_settings).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            settingsLauncher.launch(intent);
        });
        selectAllButton.setOnClickListener(v -> {
            if (dataEntryList.isEmpty()) return;
            setSelected(!allSelected, true);
        });
        fetchData();
    }

    private void setSelected(boolean value, boolean notify) {
        allSelected = value;
        for (DataEntry entry : dataEntryList) {
            entry.setSelected(allSelected);
        }
        selectAllButton.setText(allSelected ? "Deselect All" : "Select All");
        if (notify) adapter.notifyDataSetChanged();
    }

    private void fetchData() {
        SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
        String apiUrl = prefs.getString(SettingsActivity.KEY_API_URL, SettingsActivity.VALUE_API_URL);
        String apiKey = prefs.getString(SettingsActivity.KEY_API_KEY, "");
        Request request = new Request.Builder()
                .url(apiUrl + "/entries")  // Replace with your actual endpoint
                .addHeader("X-API-KEY", apiKey)            // Optional: API key if using
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Runs on a background thread
                Log.e("API_ERROR", "Exception fetching data", e);
                runOnUiThread(() -> {
                    setSelected(false, true);
                    Toast.makeText(MainActivity.this, "Failed to fetch data because: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> {
                        setSelected(false, true);
                        Toast.makeText(MainActivity.this, "Server error", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }
                String jsonString = response.body().string();
                try {
                    JSONArray jsonArray = new JSONArray(jsonString);
                    dataEntryList.clear();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        DataEntry subscription = new DataEntry(
                                obj.getLong("id"),
                                obj.getString("phone"),
                                obj.getString("tag"),
                                LocalDate.parse(obj.getString("date")),
                                LocalDate.parse(obj.getString("expiry")),
                                obj.getString("creator"),
                                obj.getBoolean("reminded"),
                                obj.getBoolean("expired"), false
                        );
                        dataEntryList.add(subscription);
                    }
                    runOnUiThread(() -> setSelected(false, true));
                } catch (JSONException e) {
                    Log.e("TEST", "Parse error", e);
                    runOnUiThread(() ->
                            Toast.makeText(MainActivity.this, "Parse error", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendSelectedSms();
            } else {
                Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void sendSelectedSms() {
        entryQueue.clear();
        dataEntryList.stream()
                .filter(DataEntry::isSelected)
                .forEach(entryQueue::add);
        if (entryQueue.isEmpty()) return;
        jsonArray = new JSONArray();
        sendingSms = true;
        sendNextSms();
    }

    private void sendNextSms() {
        if (entryQueue.isEmpty()) {
            sendingSms = false;
            Toast.makeText(this, "All SMS messages handled.", Toast.LENGTH_SHORT).show();
            SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
            String apiUrl = prefs.getString(SettingsActivity.KEY_API_URL, SettingsActivity.VALUE_API_URL);
            String apiKey = prefs.getString(SettingsActivity.KEY_API_KEY, "");
            JSONObject jsonBody = new JSONObject();
            try {
                jsonBody.put("ids", jsonArray);
            } catch (JSONException e) {
                Log.e("TEST", "Failed to convert to json", e);
                Toast.makeText(this, "Failed to convert to json!", Toast.LENGTH_SHORT).show();
                return;
            }

            RequestBody requestBody = RequestBody.create(jsonBody.toString(),
                    MediaType.parse("application/json"));

            Request request = new Request.Builder()
                    .url(apiUrl + "/update")
                    .addHeader("X-API-KEY", apiKey)
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("SMSUpdate", "Failed to send update", e);
                    fetchData();
                }

                @Override
                public void onResponse(Call call, Response response) {
                    if (response.isSuccessful()) {
                        Log.d("SMSUpdate", "Server updated successfully");
                    } else {
                        Log.e("SMSUpdate", "Server error: " + response.code());
                    }
                    fetchData();
                }
            });
            return;
        }
        DataEntry entry = entryQueue.poll();
        if (entry == null || !entry.isSelected() || entry.isReminded()) return;
        SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
        boolean smsService = prefs.getBoolean(SettingsActivity.KEY_SMS_SERVICE, false);
        String smsText = prefs.getString(SettingsActivity.KEY_SMS_TEXT, SettingsActivity.VALUE_SMS_TEXT);
        String message = smsText
                .replace("{TAG}", entry.getTag())
                .replace("{DATE}", Utils.formatter.format(entry.getExpiry()));

        if (smsService) {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("smsto:" + entry.getPhone()));
            intent.putExtra("sms_body", message);

            startActivity(intent);
        } else {
            Log.d("MockSMS", "Would send SMS to: " + entry.getPhone());
            Toast.makeText(this, "Mock SMS to " + entry.getPhone(), Toast.LENGTH_SHORT).show();

            new Handler(Looper.getMainLooper()).postDelayed(this::sendNextSms, 1000);
        }
        entry.setReminded(true);
        jsonArray.put(entry.getId());
        dataEntryList.remove(entry);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check if we were in SMS flow
        if (sendingSms) {
            // Add a slight delay to avoid racing with resume
            new Handler(Looper.getMainLooper()).postDelayed(this::sendNextSms, 500);
        }
    }
}