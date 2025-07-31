package net.rolibrt.itp_notifier;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private EditText editApiUrl, editApiKey, editSmsText;
    private Switch toggleSms;

    private SharedPreferences prefs;

    public static final String PREFS_NAME = "app_settings";
    public static final String KEY_API_URL = "api_url";
    public static final String KEY_API_KEY = "api_key";
    public static final String KEY_SMS_TEXT = "sms_text";
    public static final String KEY_SMS_SERVICE = "sms_service";
    public static final String VALUE_API_URL = "http://localhost/api";
    public static final String VALUE_SMS_TEXT = "Buna ziua, vă aducem la cunoștință că Inspecția Tehnică Periodică (ITP) " +
            "a vehiculului dumneavoastră cu numărul de înmatriculare {TAG} expiră în data de {DATE}. \n\n" +
            "Toate cele bune, \nNumeleFirmei";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        editApiUrl = findViewById(R.id.editApiUrl);
        editApiKey = findViewById(R.id.editApiKey);
        editSmsText = findViewById(R.id.editSmsText);
        toggleSms = findViewById(R.id.toggle_sms_service);

        Button btnSaveSettings = findViewById(R.id.btnSaveSettings);
        findViewById(R.id.button_back).setOnClickListener(v -> finish());

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Load saved settings if any
        editApiUrl.setText(prefs.getString(KEY_API_URL, VALUE_API_URL));
        editApiKey.setText(prefs.getString(KEY_API_KEY, ""));
        editSmsText.setText(prefs.getString(KEY_SMS_TEXT, VALUE_SMS_TEXT));
        toggleSms.setChecked(prefs.getBoolean(KEY_SMS_SERVICE, false));

        btnSaveSettings.setOnClickListener(v -> {
            String apiUrl = editApiUrl.getText().toString().trim();
            String apiKey = editApiKey.getText().toString().trim();
            String smsText = editSmsText.getText().toString().trim();
            boolean smsService = toggleSms.isChecked();

            // Basic validation
            if (apiUrl.isEmpty() || apiKey.isEmpty() || smsText.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            prefs.edit()
                    .putString(KEY_API_URL, apiUrl)
                    .putString(KEY_API_KEY, apiKey)
                    .putString(KEY_SMS_TEXT, smsText)
                    .putBoolean(KEY_SMS_SERVICE, smsService)
                    .apply();
            Toast.makeText(this, "Settings saved!", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        });
    }
}
