package com.example.m_matule.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.m_matule.R;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttp;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AuthActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private ImageButton btnPassword;
    private TextView tvRestore, tvRegister;

    private final String SUPABASE_URL = "https://jlfhfhacsvbwqvrwsbwl.supabase.co";
    private final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImpsZmhmaGFjc3Zid3F2cndzYndsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzkyNDUwMTIsImV4cCI6MjA1NDgyMTAxMn0.k2rhSed1cyn9PMR7dK1lZHOiWIBegw7OF8uySkK1BMM";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        String email;
        String password;

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        btnPassword = findViewById(R.id.btn_password);
        tvRegister = findViewById(R.id.tv_register);
        tvRestore = findViewById(R.id.tv_restore);

        btnPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePasswordVisibility();
            }
        });

        // Обработчик для входа
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(AuthActivity.this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
                } else {
                    performLogin(email, password); // Выполняем авторизацию
                }
            }
        });
    }

    // Метод для переключения видимости пароля
    private void togglePasswordVisibility() {
        if (etPassword.getTransformationMethod() instanceof PasswordTransformationMethod) {
            etPassword.setTransformationMethod(null); // Показать пароль
        } else {
            etPassword.setTransformationMethod(new PasswordTransformationMethod()); // Скрыть пароль
        }
    }

    // Метод для выполнения входа
    private void performLogin(String email, String password) {
        signIn(email, password); // Вызываем метод signIn для отправки запроса
    }

    // Метод для выполнения входа через Supabase
    private void signIn(String email, String password) {
        OkHttpClient client = new OkHttpClient();

        // JSON тело запроса
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("email", email);
            jsonBody.put("password", password);
        } catch (Exception e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);

        // URL для аутентификации
        String url = SUPABASE_URL + "/auth/v1/token?grant_type=password";

        // Запрос
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        // Отправка запроса
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(AuthActivity.this, "Ошибка при отправке запроса: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
                Log.e("AuthActivity", "Ошибка при отправке запроса: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Log.d("AuthActivity", "Успешная авторизация: " + responseBody);

                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        String accessToken = jsonResponse.getString("access_token");
                        String refreshToken = jsonResponse.getString("refresh_token");

                        Log.d("AuthActivity", "Access Token: " + accessToken);
                        Log.d("AuthActivity", "Refresh Token: " + refreshToken);

                        // Сохранение токенов (опционально)
                        // Например, можно сохранить их в SharedPreferences

                        // Переход на HomeActivity при успешной авторизации
                        runOnUiThread(() -> {
                            Intent intent = new Intent(AuthActivity.this, HomeActivity.class);
                            startActivity(intent);
                        });

                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            Toast.makeText(AuthActivity.this, "Ошибка при обработке ответа", Toast.LENGTH_SHORT).show();
                        });
                        Log.e("AuthActivity", "Ошибка при парсинге ответа: " + e.getMessage());
                    }
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(AuthActivity.this, "Неверные данные для входа", Toast.LENGTH_SHORT).show();
                    });
                    Log.e("AuthActivity", "Ошибка сервера: " + response.code());
                }
            }
        });
    }
    }