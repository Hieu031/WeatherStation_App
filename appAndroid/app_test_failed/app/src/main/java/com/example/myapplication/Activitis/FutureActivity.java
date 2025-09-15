/*** --- Function of module --- 
 * Display weather forecard follow day (5 days)
 * Get data to predict from OpenWeatherMap API
 * Display index detail: Tem, Hum, status weather, wind speed, probability of rain.
 * Show list forecard by RecyclerView "FutureAdapters"
 */

package com.example.myapplication.Activitis;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.example.myapplication.Adapters.FutureAdapters;
import com.example.myapplication.Domains.FutureDomain;
import com.example.myapplication.R;

import java.util.ArrayList;

import android.widget.ImageView;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import java.util.Calendar;

public class FutureActivity extends AppCompatActivity {
    private RecyclerView.Adapter adapterTomorrow;
    public RecyclerView recyclerView;
    TextView textViewTemp, textViewWind, textViewRain;
    TextView textViewHumidity;
    TextView textViewDescription;
    ImageView Icon;
    private static final String API_URL = "https://api.openweathermap.org/data/2.5/forecast?lat=21.053731&lon=105.7325319&appid=f1bb8c8754ef4707f529c00449b1c751";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_future);

        initRecyclerView();
        setVariable();
        Icon=findViewById(R.id.imageView7);
        textViewTemp = findViewById(R.id.textView14);
        textViewHumidity = findViewById(R.id.textView9);
        textViewDescription = findViewById(R.id.textView15);
        textViewWind = findViewById(R.id.textView7);
        textViewRain = findViewById(R.id.textView5);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(API_URL)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String apiResponse = response.body().string();

                    try {
                        JSONArray listArray = new JSONObject(apiResponse).getJSONArray("list");
                        for (int i = 0; i < listArray.length(); i++) {
                            JSONObject listItem = listArray.getJSONObject(i);
                            String dtTxt = listItem.getString("dt_txt");

                            if (dtTxt.endsWith("12:00:00")) {
                                JSONObject main = listItem.getJSONObject("main");
                                final double temp = main.getDouble("temp");
                                final int humidity = main.getInt("humidity");

                                JSONArray weatherArray = listItem.getJSONArray("weather");
                                JSONObject weather = weatherArray.getJSONObject(0);
                                final String description = weather.getString("description");
                                JSONObject wind = listItem.getJSONObject("wind");
                                final double speed = wind.getDouble("speed");
                                double pop = listItem.getDouble("pop");
                                String capitalizedDescription;
                                String icon="";
                                switch (description) {
                                    case "clear sky":
                                        capitalizedDescription = "Trời quang";
                                        icon = "sunny";
                                        break;
                                    case "few clouds":
                                        capitalizedDescription = "Ít mây";
                                        icon = "cloudy_sunny";
                                        break;
                                    case "overcast clouds":
                                        capitalizedDescription = "Trời âm u";
                                        icon = "cloudy";
                                        break;
                                    case "scattered clouds":
                                        capitalizedDescription = "Mây rải rác";
                                        icon = "cloudy_sunny";
                                        break;
                                    case "broken clouds":
                                        capitalizedDescription = "Nhiều mây";
                                        icon = "cloudy";
                                        break;
                                    case "light rain":
                                        capitalizedDescription = "Mưa nhỏ";
                                        icon = "rainy";
                                        break;
                                    case "heavy intensity rain":
                                        capitalizedDescription = "Mưa lớn";
                                        icon = "rainy";
                                        break;
                                    case "moderate rain":
                                        capitalizedDescription = "Có mưa";
                                        icon = "rainy";
                                        break;
                                    case "thunderstorm":
                                        capitalizedDescription = "Có bão";
                                        icon = "storm";
                                        break;
                                    case "snow":
                                        capitalizedDescription = "Có tuyết";
                                        icon = "snowy";
                                        break;
                                    case "light snow":
                                        capitalizedDescription = "Tuyết rơi ít";
                                        icon = "snowy";
                                        break;
                                    case "mist":
                                        capitalizedDescription = "Sương mù";
                                        icon = "misty";
                                        break;
                                    default:
                                        capitalizedDescription = description;
                                        break;
                                }
                                final String finalIcon = icon;
                                runOnUiThread(() -> {
                                    int iconResourceId = getResources().getIdentifier(finalIcon, "drawable", getPackageName());
                                    Icon.setImageResource(iconResourceId);


                                    textViewTemp.setText(String.format("%.1f", (temp - 273.15)) + "°");
                                    textViewHumidity.setText(humidity + "%");
                                    textViewDescription.setText(capitalizedDescription);
                                    textViewWind.setText(speed + "m/s");
                                    textViewRain.setText((int)(pop * 100) + "%");
                                });

                                break;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void setVariable() {
        ConstraintLayout backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> startActivity(new Intent(FutureActivity.this, MainActivity1.class)));
    }

    private void initRecyclerView() {
        ArrayList<FutureDomain> items = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_WEEK, 2);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(API_URL)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String apiResponse = response.body().string();

                    try {
                        JSONArray listArray = new JSONObject(apiResponse).getJSONArray("list");
                        int count = 0;

                        for (int i = 0; i < listArray.length(); i++) {
                            JSONObject listItem = listArray.getJSONObject(i);
                            String dtTxt = listItem.getString("dt_txt");

                            if (dtTxt.endsWith("00:00:00")) {
                                count++;
                                if (count >= 2 && count <= 5) {
                                    JSONObject main = listItem.getJSONObject("main");
                                    final double temp = main.getDouble("temp");
                                    final int humidity = main.getInt("humidity");

                                    JSONArray weatherArray = listItem.getJSONArray("weather");
                                    JSONObject weather = weatherArray.getJSONObject(0);
                                    final String description = weather.getString("description");
                                    String capitalizedDescription;
                                    switch (description) {
                                        case "clear sky":
                                            capitalizedDescription = "Trời quang";
                                            break;
                                        case "overcast clouds":
                                            capitalizedDescription = "Trời âm u";
                                            break;
                                        case "scattered clouds":
                                            capitalizedDescription = "Mây rải rác";
                                            break;
                                        case "broken clouds":
                                            capitalizedDescription = "Nhiều mây";
                                            break;
                                        case "light rain":
                                            capitalizedDescription = "Mưa nhỏ";
                                            break;
                                        case "moderate rain":
                                            capitalizedDescription = "Mưa vừa";
                                            break;
                                        case "heavy intensity rain":
                                            capitalizedDescription = "Mưa lớn";
                                            break;
                                        case "thunderstorm":
                                            capitalizedDescription = "Mưa bão";
                                            break;
                                        case "snow":
                                            capitalizedDescription = "Có tuyết";
                                            break;
                                        case "light snow":
                                            capitalizedDescription = "Tuyết rơi ít";
                                            break;

                                        default:
                                            capitalizedDescription = description;
                                            break;
                                    }

                                    for (int j = 0; j < 1; j++) {
                                        int nextDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                                        calendar.add(Calendar.DAY_OF_WEEK, 1);

                                        String dayOfWeek;
                                        switch (nextDayOfWeek) {
                                            case Calendar.SUNDAY:
                                                dayOfWeek = "CN";
                                                break;
                                            case Calendar.MONDAY:
                                                dayOfWeek = "T2";
                                                break;
                                            case Calendar.TUESDAY:
                                                dayOfWeek = "T3";
                                                break;
                                            case Calendar.WEDNESDAY:
                                                dayOfWeek = "T4";
                                                break;
                                            case Calendar.THURSDAY:
                                                dayOfWeek = "T5";
                                                break;
                                            case Calendar.FRIDAY:
                                                dayOfWeek = "T6";
                                                break;
                                            case Calendar.SATURDAY:
                                                dayOfWeek = "T7";
                                                break;
                                            default:
                                                dayOfWeek = "";
                                                break;
                                        }
                                        String icon;
                                        switch (description) {
                                            case "clear sky":
                                                icon = "sunny";
                                                break;
                                            case "overcast clouds":
                                            case "broken clouds":
                                                icon = "cloudy";
                                                break;
                                            case "scattered clouds":
                                                icon = "cloudy_sunny";
                                                break;
                                            case "light rain":
                                            case "moderate rain":
                                            case "heavy intensity rain":
                                                icon = "rainy";
                                                break;
                                            case "thunderstorm":
                                                icon = "storm";
                                                break;
                                            case "snow":
                                            case "light snow":
                                                icon = "snowy";
                                                break;
                                            case "mist":
                                                icon = "misty";
                                                break;
                                            default:
                                                icon = "cloudy";
                                                break;
                                        }

                                        items.add(new FutureDomain(dayOfWeek, icon, capitalizedDescription, (int) (temp - 273.15), humidity));

                                        }
                                    }
                                }
                            }


                        runOnUiThread(() -> {
                            recyclerView = findViewById(R.id.view2);
                            recyclerView.setLayoutManager(new LinearLayoutManager(FutureActivity.this, LinearLayoutManager.VERTICAL, false));
                            adapterTomorrow = new FutureAdapters(items, FutureActivity.this);
                            recyclerView.setAdapter(adapterTomorrow);
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}