/*** --- Function of module --- 
 * Display weather currently which recieiced from sensor
 * Take data sensor from firebase
 * Calculate and figure out health advise based on data
 * Show predict follow hourly by RecyclerView "HourlyAdapters"
 * Send notification for user when the index reaches the threshold.
 */

package com.example.myapplication.Activitis;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.text.SimpleDateFormat;

import com.example.myapplication.Adapters.HourlyAdapters;
import com.example.myapplication.Domains.Hourly;
import com.example.myapplication.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


import org.json.JSONObject;
import org.json.JSONArray;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.lang.String;
import java.util.TimeZone;


public class MainActivity1 extends AppCompatActivity {
    private RecyclerView.Adapter adapterHourly;
    private RecyclerView recyclerView;
    private Handler handler;
    private boolean isServiceRunning = false;
    private String PBui, PTemp, PCo, PHumid, PRain;
    private String Text;
    TextView tvTemp, tvTime, tvBui, tvCo, tvHumid, tvChatluong, tvtb, tvtb2;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);


        tvTime = findViewById(R.id.textView2);
        tvTemp = findViewById(R.id.textView3);
        tvBui = findViewById(R.id.textView5);
        tvCo = findViewById(R.id.textView7);
        tvHumid = findViewById(R.id.textView9);
        tvChatluong = findViewById(R.id.textView4);
        tvtb = findViewById(R.id.textViewtb);
        tvtb2 = findViewById(R.id.textViewtb2);
        imageView = findViewById(R.id.img);
        tvBui.addTextChangedListener(new MyTextWatcher());
        tvCo.addTextChangedListener(new MyTextWatcher());
        tvHumid.addTextChangedListener(new MyTextWatcher());
        tvTemp.addTextChangedListener(new MyTextWatcher());

        handler = new Handler(Looper.getMainLooper());
        startUpdatingTime();

        initRecyclerview();
        setVariable();
        setVariable1();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRefHumid = database.getReference("Humidity");
        DatabaseReference myRefNhietDo = database.getReference("Temperature");
        DatabaseReference myRefBui = database.getReference("Dust Density");
        DatabaseReference myRefCo = database.getReference("Co Value");
        DatabaseReference myRefRain = database.getReference("Rain");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("Rain").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String rain = dataSnapshot.getValue(String.class);
                PRain = rain;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (rain.equals("0")) {
                            imageView.setImageResource(R.drawable.rainy);
                            tvtb2.setText("Bạn hãy nhớ cầm theo ô khi đi ra ngoài nhé!");

                        } else {
                            imageView.setImageResource(R.drawable.cloudy);
                            tvtb2.setText("  ");
                        }
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Xử lý lỗi nếu cần
            }
        });
        databaseReference.child("Humidity").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String humid = dataSnapshot.getValue(String.class);
                PHumid = humid;
                if (Double.parseDouble(humid) <70) {
                    tvtb.setText("Lời khuyên : Hãy uống nhiều nước ");
                    return;
                }
                updateTextView();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý lỗi nếu có
            }
        });
        databaseReference.child("Temperature").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String temp = dataSnapshot.getValue(String.class);
                PTemp = temp;
                if (Double.parseDouble(temp) <15) {
                    tvtb.setText("Lời khuyên : Nhớ mặc áo ấm ");
                    return;
                }
                updateTextView();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý lỗi nếu có
            }
        });
        databaseReference.child("Dust Density").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String bui = dataSnapshot.getValue(String.class);
                PBui = bui;
                if (Double.parseDouble(bui) > 65 ) {
                    tvtb.setText("Lời khuyên :Không nên ra ngoài");
                    return;
                }



                // Cập nhật các biến khác nếu cần
                updateTextView();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý lỗi nếu có
            }
        });
        databaseReference.child("Co Value").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String co = dataSnapshot.getValue(String.class);
                PCo = co;
                if (Double.parseDouble(co) > 650) {
                    tvtb.setText("Lời khuyên :Không nên ra ngoài");
                    return;
                }
                // Cập nhật các biến khác nếu cần
                updateTextView();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý lỗi nếu có
            }
        });

        myRefNhietDo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String nhietDo = dataSnapshot.getValue(String.class);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvTemp.setText(nhietDo + "°C"); // Hiển thị nhiệt độ trên tv2
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Xử lý lỗi nếu có
            }
        });
        myRefBui.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String Bui = dataSnapshot.getValue(String.class);
                double buiValue = Double.parseDouble(Bui);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvBui.setText(Bui + "µg/m³"); // Hiển thị nồng độ bụi
                        double coValue = Double.parseDouble(tvCo.getText().toString().replace("ppm", "")); // Lấy giá trị CO từ TextView tvCo
                        checkDangerousLevel(coValue, buiValue); // Truyền cả hai giá trị vào phương thức checkDangerousLevel
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Xử lý lỗi nếu có
            }
        });
        myRefCo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String Co = dataSnapshot.getValue(String.class);
                double coValue = Double.parseDouble(Co);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvCo.setText(Co + "ppm"); // Hiển thị nồng độ CO
                        double buiValue = Double.parseDouble(tvBui.getText().toString().replace("µg/m³", "")); // Lấy giá trị bụi từ TextView tvBui
                        checkDangerousLevel(coValue, buiValue); // Truyền cả hai giá trị vào phương thức checkDangerousLevel
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Xử lý lỗi nếu có
            }
        });


        myRefHumid.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String Humid = dataSnapshot.getValue(String.class);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvHumid.setText(Humid + "%"); // Hiển thị nhiệt độ trên tv2
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Xử lý lỗi nếu có
            }
        });
    }

    private void checkDangerousLevel(double coValue, double buiValue) {
        if (coValue > 2000 || buiValue > 65) {
            tvChatluong.setText("Chất lượng không khí: Nguy hiểm");
            sendNotification("Cảnh báo", "Chất lượng không khí: Nguy hiểm!");
        } else if ((2000 < coValue && coValue > 650) || (36 < buiValue && buiValue < 55)) {
            tvChatluong.setText("Chất lượng không khí: Có hại cho sức khỏe");
            sendNotification("Cảnh báo", "Chất lượng không khí: Có hại cho sức khỏe");
        } else if ((650< coValue && coValue > 400) || (13 < buiValue && buiValue < 15)) {
            tvChatluong.setText("Chất lượng không khí: Vừa phải ");
        } else {
            tvChatluong.setText("Chất lượng không khí: An toàn");

        }
    }

    private void startUpdatingTime() {
        handler.post(updateTimeRunnable);
    }

    private final Runnable updateTimeRunnable = new Runnable() {
        @Override
        public void run() {
            updateTime();
            handler.postDelayed(this, 1000); // Cập nhật mỗi giây
        }
    };

//    private void updateTime() {
//        SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd/MM/yyyy, HH:mm:ss", Locale.getDefault());
//        String currentDateTime = sdf.format(new Date());
//        tvTime.setText(currentDateTime);
//    }
    private void updateTime() {
        Locale viVN = new Locale("vi", "VN");
        SimpleDateFormat sdf =
                new SimpleDateFormat("EEEE dd/MM/yyyy, HH:mm:ss", viVN);
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh")); // hoặc theo máy

        String current = sdf.format(new Date());
        tvTime.setText(current); // muốn viết hoa chữ cái đầu
    }

    private class MyTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // Gọi phương thức update khi bất kỳ TextView nào thay đổi
            updateTextView();
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }

    private void updateTextView() {
        // Lấy dữ liệu từ các TextView
        // Khai báo các biến để tính toán
        double PYes = 0, PNo = 0, P1 = 0, P2 = 0, P3 = 0;
        double PBuiYes = 0, PBuibyNo = 0, PBui1 = 0, PBui2 = 0, PBui3 = 0;
        double PTempbyYes = 0, PTempbyNo = 0, PTemp1 = 0, PTemp2 = 0, PTemp3 = 0;
        double PCoYes = 0, PCoNo = 0, PCo1 = 0, PCo2 = 0, PCo3 = 0;
        double PHumidYes = 0, PHumidNo = 0, PHumid1 = 0, PHumid2 = 0, PHumid3 = 0;


        //Dust: >56 Kem, 36-55 co hai cho sk, 13-15, Trung binh, <12 tot
        //CO: 0-150 TB, 150-200 xau, 200-250,250-300 rat xau, >300 Nguy hiem
        //Nhiet do: 0-15 Lanh, 15-30 mat me, 30-40 nong
        //Do am: <50% Kho, 50-70 ly tuong, >70 am uot
        // Tính tỉ lệ Yes và No từ dữ liệu của A
        //Yes: Dust<15,  CO<150, Nhiet do 15-30,doam 50-70
        String[][] A = {{"2.7", "320", "27", "77", "Yes"},
                {"3.0", "330", "27", "77", "Yes"},
                {"7.0", "340", "27", "76", "Yes"},
                {"7.3", "332", "30", "77", "Yes"},
                {"8.8", "332", "30", "76", "Yes"},
                {"1.8", "332 ", "28", "74", "Yes"},
                {"2.1", "334", "30", "73", "Yes"},
                {"1.8", "333", "29", "72", "Yes"},
                {"2.9", "334", "31", "76", "Yes"},
                {"7.2", "333", "27", "75", "Yes"},
                {"9.2", "340", "27", "75", "Yes"},
                {"4.1", "336", "27", "77", "Yes"},
                {"1.4", "332", "28", "74", "Yes"},
                {"6.7", "333", "28", "73", "Yes"},
                {"4.6", "334", "28", "72", "Yes"},
                {"9.5", "332", "28", "71", "Yes"},
                {"8.5", "340", "29", "77", "Yes"},
                {"10.2", "365", "28", "76", "Yes"},
                {"8.3", "356", "27", "77", "Yes"},
                {"9.2", "344", "28", "77", "Yes"},
                {"6.2", "324", "28", "78", "Yes"},
                {"5.3", "335", "28", "75", "Yes"},
                {"5.4", "336", "27", "77", "Yes"},
                {"3.1", "342", "27", "71", "Yes"},
                {"4.4", "343", "27", "76", "Yes"},//
                {"5.3", "343", "27", "75", "Yes"},
                {"4.7", "347", "27", "77", "Yes"},
                {"1.3", "343", "30", "74", "Yes"},
                {"6.7", "349", "29", "73", "Yes"},
                {"4.6", "351", "26", "72", "Yes"},
                {"9.5", "381", "33", "71", "Yes"},
                {"8.5", "373", "34", "77", "Yes"},
                {"10.7", "365", "28", "76", "Yes"},
                {"8.9", "377", "27", "77", "Yes"},
                {"9.3", "347", "28", "77", "Yes"},
                {"7.3", "321", "28", "78", "Yes"},
                {"6.1", "335", "28", "75", "Yes"},
                {"5.4", "336", "27", "77", "Yes"},
                {"3.7", "389", "27", "71", "Yes"},
                {"4.5", "353", "27", "76", "Yes"},
                {"1.8", "332 ", "28", "74", "Yes"},
                {"2.1", "334", "30", "73", "Yes"},
                {"1.8", "333", "29", "72", "Yes"},
                {"2.9", "334", "31", "76", "Yes"},
                {"7.2", "333", "27", "75", "Yes"},
                {"9.2", "340", "27", "75", "Yes"},
                {"4.1", "336", "27", "77", "Yes"},
                {"1.4", "332", "28", "74", "Yes"},
                {"6.7", "333", "28", "73", "Yes"},
                {"4.6", "334", "28", "72", "Yes"},
                {"9.5", "332", "28", "71", "Yes"},
                {"8.5", "340", "29", "77", "Yes"},
                {"10.2", "365", "28", "76", "Yes"},
                {"8.3", "356", "27", "77", "Yes"},
                {"9.2", "344", "28", "77", "Yes"},
                {"6.2", "324", "28", "78", "Yes"},
                {"5.3", "335", "28", "75", "Yes"},
                {"5.4", "336", "27", "77", "Yes"},
                {"3.1", "342", "27", "71", "Yes"},
                {"4.4", "343", "27", "76", "Yes"},//
                {"5.3", "343", "27", "75", "Yes"},
                {"4.7", "347", "27", "77", "Yes"},
                {"1.3", "343", "30", "74", "Yes"},
                {"6.7", "349", "29", "73", "Yes"},
                {"4.6", "351", "26", "72", "Yes"},
                {"9.5", "381", "33", "71", "Yes"},
                {"8.5", "373", "34", "77", "Yes"},
                {"10.7", "365", "28", "76", "Yes"},
                {"3.5", "659", "28", "77", "No"},
                {"5.7", "659", "30", "78", "No"},
                {"2.9", "659", "30", "76", "No"},
                {"3.1", "659", "34", "78", "No"},
                {"2.2", "659", "30", "77", "No"},
                {"2.2", "659", "27", "78", "No"},
                {"9.1", "659", "28", "78", "No"},
                {"4.2", "651", "29", "75", "No"},
                {"1.1", "640", "30", "77", "No"},
                {"5.3", "658", "31", "76", "No"},
                {"7.5", "659", "30", "78", "No"},
                {"7.8", "653", "27", "76", "No"},
                {"7.6", "523", "31", "77", "No"},
                {"2.7", "525", "27", "77", "No"},
                {"2.9", "519", "27", "76", "No"},
                {"5.3", "505", "30", "77", "No"},
                {"0.6", "500", "31", "76", "No"},
                {"36.3", "519", "28", "75", "No"},
                {"27.3", "659", "27", "78", "No"},
                {"52.1", "523", "27", "76", "No"},
                {"51.2", "621", "27", "80", "No"},
                {"66.3", "333", "29", "77", "No"},
                {"43.2", "651", "29", "78", "No"},
                {"52.7", "660", "30", "79", "No"},
                {"32.9", "660", "30", "76", "No"},
                {"33.1", "659", "33", "79", "No"},
                {"28.1", "659", "35", "76", "No"},
                {"29.3", "667", "31", "79", "No"},
                {"30.4", "653", "29", "76", "No"},
                {"27.4", "651", "34", "74", "No"},
                {"24.3", "598", "31", "76", "No"},
                {"22.2", "576", "36", "76", "No"},
                {"27.5", "659", "36", "71", "No"},
                {"47.2", "651", "26", "73", "No"},
                {"57.6", "527", "33", "71", "No"},
                {"22.7", "527", "26", "70", "No"},
                {"29.9", "529", "26", "77", "No"},
                {"52.3", "525", "33", "79", "No"},
                {"44.6", "500", "33", "76", "No"},
                {"38.3", "517", "28", "75", "No"},
                {"27.7", "659", "26", "78", "No"},
                {"52.3", "528", "29", "76", "No"},
                {"53.6", "621", "27", "80", "No"},
                {"66.8", "337", "27", "76", "No"},
                {"36.3", "519", "28", "75", "No"},
                {"27.3", "659", "27", "78", "No"},
                {"52.1", "523", "27", "76", "No"},
                {"51.2", "621", "27", "80", "No"},
                {"66.3", "333", "29", "77", "No"},
                {"43.2", "651", "29", "78", "No"},
                {"52.7", "660", "30", "79", "No"},
                {"32.9", "660", "30", "76", "No"},
                {"33.1", "659", "33", "79", "No"},
                {"28.1", "659", "35", "76", "No"},
                {"29.3", "667", "31", "79", "No"},
                {"30.4", "653", "29", "76", "No"},
                {"27.4", "651", "34", "74", "No"},
                {"24.3", "598", "31", "76", "No"},
                {"22.2", "576", "36", "76", "No"},
                {"27.5", "659", "36", "71", "No"},
                {"47.2", "651", "26", "73", "No"},
                {"57.6", "527", "33", "71", "No"},
                {"22.7", "527", "26", "70", "No"},
                {"29.9", "529", "26", "77", "No"},
                {"52.3", "525", "33", "79", "No"},
                {"44.6", "500", "33", "76", "No"},
                {"38.3", "517", "28", "75", "No"},
                {"27.7", "659", "26", "78", "No"},
                {"5.5", "402", "32", "73", "1"},
                {"5.9", "399", "33", "77", "1"},
                {"13.2", "391", "32", "72", "1"},
                {"15.9", "388", "27", "72", "1"},
                {"17.3", "401", "28", "80", "1"},
                {"16.5", "536", "27", "71", "1"},
                {"25.7", "601", "28", "70", "1"},
                {"55.2", "433", "28", "72", "1"},
                {"9.4", "422", "28", "72", "1"},
                {"11.3", "387", "30", "77", "1"},
                {"10.1", "389", "27", "76", "1"},
                {"17.2", "356", "30", "78", "1"},
                {"29.3", "403", "27", "79", "1"},
                {"26.1", "423", "31", "80", "1"},
                {"13.5", "513", "27", "81", "1"},
                {"24.3", "397", "31", "79", "1"},
                {"43.2", "377", "28", "72", "1"},
                {"11.2", "387", "29", "74", "1"},
                {"5.5", "402", "32", "73", "1"},
                {"5.9", "397", "31", "77", "1"},
                {"13.3", "397", "33", "74", "1"},
                {"15.1", "383", "28", "72", "1"},
                {"17.7", "425", "29", "78", "1"},
                {"16.7", "537", "26", "77", "1"},
                {"25.1", "549", "27", "70", "1"},
                {"18.2", "422", "29", "72", "1"},
                {"9.4", "429", "27", "74", "1"},
                {"11.1", "357", "31", "72", "1"},
                {"10.9", "383", "25", "79", "1"},
                {"17.6", "356", "25", "74", "1"},
                {"29.1", "487", "29", "72", "1"},
                {"26.7", "427", "33", "80", "1"},
                {"13.8", "514", "24", "80", "1"},
                {"24.65", "391", "35", "76", "1"},
                {"18.5", "373", "27", "74", "1"},
                {"11.5", "383", "27", "73", "1"},
                {"26.9", "364", "30", "76", "1"},
                {"15.2", "399", "27", "69", "1"},
                {"17.5", "366", "28", "70", "1"},
                {"29.3", "403", "27", "79", "1"},
                {"26.1", "423", "31", "80", "1"},
                {"13.5", "513", "27", "81", "1"},
                {"24.3", "397", "31", "79", "1"},
                {"43.2", "377", "28", "72", "1"},
                {"11.2", "387", "29", "74", "1"},
                {"5.5", "402", "32", "73", "1"},
                {"5.9", "397", "31", "77", "1"},
                {"13.3", "397", "33", "74", "1"},
                {"15.1", "383", "28", "72", "1"},
                {"17.7", "425", "29", "78", "1"},
                {"16.7", "537", "26", "77", "1"},
                {"25.1", "549", "27", "70", "1"},
                {"18.2", "422", "29", "72", "1"},
                {"9.4", "429", "27", "74", "1"},
                {"11.1", "357", "31", "72", "1"},
                {"10.9", "383", "25", "79", "1"},
                {"17.6", "356", "25", "74", "1"},
                {"29.1", "487", "29", "72", "1"},
                {"26.7", "427", "33", "80", "1"},
                {"13.8", "514", "24", "80", "1"},
                {"24.65", "391", "35", "76", "1"},
                {"18.5", "373", "27", "74", "1"},
                {"11.5", "383", "27", "73", "1"},
                {"26.9", "364", "30", "76", "1"},
                {"15.2", "399", "27", "69", "1"},
                {"17.5", "366", "28", "70", "1"},
                {"2.6", "365", "32", "69", "2"},
                {"3.6", "375", "33", "68", "2"},
                {"3.7", "373", "28", "67", "2"},
                {"5.3", "379", "28", "66", "2"},
                {"6.7", "403", "28", "65", "2"},
                {"6.3", "383", "29", "64", "2"},
                {"7.6", "395", "33", "63", "2"},
                {"6.2", "379", "32", "62", "2"},
                {"7.3", "355", "30", "61", "2"},
                {"8.3", "367", "30", "68", "2"},
                {"9.3", "356", "29", "70", "2"},
                {"12.3", "367", "30", "62", "2"},
                {"7.9", "356", "29", "58", "2"},
                {"8.3", "367", "29", "62", "2"},
                {"7.6", "378", "28", "67", "2"},
                {"8.5", "387", "29", "61", "2"},
                {"8.3", "365", "29", "62", "2"},
                {"9.1", "355", "29", "67", "2"},
                {"8.2", "391", "28", "72", "2"},
                {"9.3", "343", "30", "73", "2"},
                {"6.8", "353", "29", "65", "2"},//
                {"2.7", "365", "35", "66", "2"},
                {"3.6", "375", "36", "67", "2"},
                {"3.7", "372", "37", "67", "2"},
                {"5.3", "379", "31", "66", "2"},
                {"6.7", "403", "33", "65", "2"},
                {"6.3", "383", "39", "64", "2"},
                {"7.6", "395", "40", "67", "2"},
                {"6.2", "379", "33", "68", "2"},
                {"7.3", "355", "31", "69", "2"},
                {"8.3", "367", "30", "65", "2"},
                {"9.3", "356", "29", "66", "2"},
                {"12.3", "367", "30", "67", "2"},
                {"7.9", "351", "29", "69", "2"},
                {"8.3", "367", "29", "66", "2"},
                {"7.6", "378", "31", "69", "2"},
                {"8.5", "387", "29", "65", "2"},
                {"8.3", "365", "39", "67", "2"},
                {"9.1", "355", "37", "66", "2"},
                {"8.2", "391", "39", "69", "2"},
                {"9.3", "343", "30", "71", "2"},
                {"6.8", "353", "36", "65", "2"},
                {"6.7", "403", "28", "65", "2"},
                {"6.3", "383", "29", "64", "2"},
                {"7.6", "395", "33", "63", "2"},
                {"6.2", "379", "32", "62", "2"},
                {"7.3", "355", "30", "61", "2"},
                {"8.3", "367", "30", "68", "2"},
                {"9.3", "356", "29", "70", "2"},
                {"12.3", "367", "30", "62", "2"},
                {"7.9", "356", "29", "58", "2"},
                {"8.3", "367", "29", "62", "2"},
                {"7.6", "378", "28", "67", "2"},
                {"8.5", "387", "29", "61", "2"},
                {"8.3", "365", "29", "62", "2"},
                {"9.1", "355", "29", "67", "2"},
                {"8.2", "391", "28", "72", "2"},
                {"9.3", "343", "30", "73", "2"},
                {"6.8", "353", "29", "65", "2"},//
                {"2.7", "365", "35", "66", "2"},
                {"3.6", "375", "36", "67", "2"},
                {"3.7", "372", "37", "67", "2"},
                {"5.3", "379", "31", "66", "2"},
                {"6.7", "403", "33", "65", "2"},
                {"6.3", "383", "39", "64", "2"},
                {"7.6", "395", "40", "67", "2"},
                {"6.2", "379", "33", "68", "2"},
                {"7.3", "355", "31", "69", "2"},
                {"8.3", "367", "30", "65", "2"},
                {"9.3", "356", "29", "66", "2"},
                {"12.3", "367", "30", "67", "2"},
                {"7.9", "351", "29", "69", "2"},
                {"8.3", "367", "29", "66", "2"},
                {"7.6", "378", "31", "69", "2"},
                {"8.5", "387", "29", "65", "2"},
                {"8.3", "365", "39", "67", "2"},
                {"9.1", "355", "37", "66", "2"},
                {"5.2", "361", "18", "68", "3"},
                {"7.7", "360", "17", "70", "3"},
                {"2.6", "365", "16", "69", "3"},
                {"3.6", "375", "15", "68", "3"},
                {"3.7", "373", "17", "67", "3"},
                {"5.7", "379", "20", "66", "3"},
                {"6.9", "403", "21", "65", "3"},
                {"6.3", "383", "16", "64", "3"},
                {"7.6", "395", "13", "63", "3"},
                {"6.2", "379", "12", "62", "3"},
                {"7.3", "355", "17", "61", "3"},
                {"8.3", "367", "16", "68", "3"},
                {"9.3", "356", "14", "70", "3"},
                {"12.3", "367", "13", "62", "3"},
                {"7.9", "356", "12", "58", "3"},
                {"8.3", "367", "18", "62", "3"},
                {"7.6", "378", "16", "67", "3"},
                {"8.5", "387", "11", "61", "3"},
                {"8.3", "365", "9", "62", "3"},
                {"9.7", "355", "10", "67", "3"},
                {"8.3", "391", "14", "72", "3"},
                {"9.2", "343", "13", "73", "3"},//
                {"7.7", "360", "18", "68", "3"},
                {"2.6", "365", "19", "69", "3"},
                {"3.6", "375", "14", "62", "3"},
                {"3.7", "373", "13", "67", "3"},
                {"5.7", "379", "11", "66", "3"},
                {"6.9", "403", "9", "65", "3"},
                {"6.3", "383", "18", "64", "3"},
                {"7.6", "395", "19", "63", "3"},
                {"6.2", "379", "21", "62", "3"},
                {"7.3", "355", "20", "61", "3"},
                {"8.3", "367", "17", "68", "3"},
                {"9.3", "356", "19", "70", "3"},
                {"12.3", "367", "18", "62", "3"},
                {"7.9", "356", "15", "58", "3"},
                {"8.3", "367", "14", "62", "3"},
                {"7.6", "378", "13", "67", "3"},
                {"8.5", "387", "15", "61", "3"},
                {"8.3", "365", "19", "62", "3"},
                {"9.7", "355", "15", "67", "3"},
                {"8.3", "391", "16", "72", "3"},
                {"9.2", "343", "18", "73", "3"},
                {"6.5", "353", "19", "65", "3"},
                {"6.2", "379", "12", "62", "3"},
                {"7.3", "355", "17", "61", "3"},
                {"8.3", "367", "16", "68", "3"},
                {"9.3", "356", "14", "70", "3"},
                {"12.3", "367", "13", "62", "3"},
                {"7.9", "356", "12", "58", "3"},
                {"8.3", "367", "18", "62", "3"},
                {"7.6", "378", "16", "67", "3"},
                {"8.5", "387", "11", "61", "3"},
                {"8.3", "365", "9", "62", "3"},
                {"9.7", "355", "10", "67", "3"},
                {"8.3", "391", "14", "72", "3"},
                {"9.2", "343", "13", "73", "3"},//
                {"7.7", "360", "18", "68", "3"},
                {"2.6", "365", "19", "69", "3"},
                {"3.6", "375", "14", "62", "3"},
                {"3.7", "373", "13", "67", "3"},
                {"5.7", "379", "11", "66", "3"},
                {"6.9", "403", "9", "65", "3"},
                {"6.3", "383", "18", "64", "3"},
                {"7.6", "395", "19", "63", "3"},
                {"6.2", "379", "21", "62", "3"},
                {"7.3", "355", "20", "61", "3"},
                {"8.3", "367", "17", "68", "3"},
                {"9.3", "356", "19", "70", "3"},
                {"12.3", "367", "18", "62", "3"},
                {"7.9", "356", "15", "58", "3"},
                {"8.3", "367", "14", "62", "3"},
                {"7.6", "378", "13", "67", "3"},
                {"8.5", "387", "15", "61", "3"},};


        for (String[] anA : A) {
            if (anA[4].equals("Yes")) {
                PYes++;
            } else if (anA[4].equals("1")) {
                P1++;

            } else if (anA[4].equals("2")) {
                P2++;

            } else if (anA[4].equals("3")) {
                P3++;

            } else {
                PNo++;
            }
        }

        // Tính tỉ lệ input1
        for (String[] anA : A) {
            if (anA[4].equals("Yes") && anA[0].equals(PBui)) {
                PBuiYes++;
            }
            if (anA[4].equals("No") && anA[0].equals(PBui)) {
                PBuibyNo++;
            }
            if (anA[4].equals("1") && anA[0].equals(PBui)) {
                PBui1++;
            }
            if (anA[4].equals("2") && anA[0].equals(PBui)) {
                PBui2++;
            }
            if (anA[4].equals("3") && anA[0].equals(PBui)) {
                PBui3++;
            }

        }

        // Tính tỉ lệ input2
        for (String[] anA : A) {
            if (anA[4].equals("Yes") && anA[1].equals(PCo)) {
                PCoYes++;
            }
            if (anA[4].equals("No") && anA[1].equals(PCo)) {
                PCoNo++;
            }
            if (anA[4].equals("1") && anA[1].equals(PCo)) {
                PCo1++;
            }
            if (anA[4].equals("2") && anA[1].equals(PCo)) {
                PCo2++;
            }
            if (anA[4].equals("3") && anA[1].equals(PCo)) {
                PCo3++;
            }
        }

        // Tính tỉ lệ input3
        for (String[] anA : A) {
            if (anA[4].equals("Yes") && anA[2].equals(PTemp)) {
                PTempbyYes++;
            }
            if (anA[4].equals("No") && anA[2].equals(PTemp)) {
                PTempbyNo++;
            }
            if (anA[4].equals("1") && anA[2].equals(PTemp)) {
                PTemp1++;
                ;
            }
            if (anA[4].equals("2") && anA[2].equals(PTemp)) {
                PTemp2++;
                ;
            }
            if (anA[4].equals("3") && anA[2].equals(PTemp)) {
                PTemp3++;
                ;
            }
        }

        // Tính tỉ lệ input4
        for (String[] anA : A) {
            if (anA[4].equals("Yes") && anA[3].equals(PHumid)) {
                PHumidYes++;
            }
            if (anA[4].equals("No") && anA[3].equals(PHumid)) {
                PHumidNo++;
            }
            if (anA[4].equals("1") && anA[3].equals(PHumid)) {
                PHumid1++;
            }
            if (anA[4].equals("2") && anA[3].equals(PHumid)) {
                PHumid2++;
            }
            if (anA[4].equals("3") && anA[3].equals(PHumid)) {
                PHumid3++;
            }
        }

        // Tính toán kết quả
        double Z1 = (PBuiYes / PYes) * (PTempbyYes / PYes) * (PCoYes / PYes) * (PHumidYes / PYes);
        double Z2 = (PBuibyNo / PNo) * (PTempbyNo / PNo) * (PCoNo / PNo) * (PHumidNo / PNo);
        double Z3 = (PBui1 / P1) * (PTemp1 / P1) * (PCo1 / P1) * (PHumid1 / P1);
        double Z4 = (PBui2 / P2) * (PTemp2 / P2) * (PCo2 / P2) * (PHumid2 / P2);
        double Z5 = (PBui3 / P3) * (PTemp3 / P3) * (PCo3 / P3) * (PHumid3 / P3);

        double ZYes = Z1 / (Z1 + Z2 + Z3 + Z4 + Z5);
        double ZNo = Z2 / (Z1 + Z2 + Z3 + Z4 + Z5);
        double Zkq1 = Z3 / (Z1 + Z2 + Z3 + Z4 + Z5);
        double Zkq2 = Z4 / (Z1 + Z2 + Z3 + Z4 + Z5);
        double Zkq3 = Z5 / (Z1 + Z2 + Z3 + Z4 + Z5);


        // Sau khi tính toán kết quả
        if (Zkq1 > ZYes && Zkq1 > ZNo && Zkq1 > Zkq2 && Zkq1 > Zkq3) {
            tvtb.setText("Lời khuyên : Hãy đeo khẩu trang");
        } else if (Zkq2 > ZYes && Zkq2 > ZNo && Zkq2 > Zkq1 && Zkq2 > Zkq3) {
            tvtb.setText("Lời khuyên : Hãy uống nhiều nước ");
        } else if (Zkq3 > ZYes && Zkq3 > ZNo && Zkq3 > Zkq1 && Zkq3 > Zkq2) {
            tvtb.setText("Lời khuyên : Nhớ mặc áo ấm ");

        } else if (ZNo > Zkq1 && ZNo > ZYes && ZNo > Zkq2 && ZNo > Zkq3) {
            tvtb.setText("Lời khuyên: Không nên ra ngoài");
        } else if (ZYes > Zkq1 && ZYes > ZNo && ZYes > Zkq2 && ZYes > Zkq3) {
            tvtb.setText("Lời khuyên : Trời rất đẹp, Có thể ra ngoài");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateTimeRunnable);
    }


    private void setVariable() {
        TextView next7dayBtn = findViewById(R.id.nextBtn);
        next7dayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity1.this, FutureActivity.class));
            }
        });
    }

    private void setVariable1() {
        ConstraintLayout backBtn = findViewById(R.id.backBtn1);
        backBtn.setOnClickListener(v -> startActivity(new Intent(MainActivity1.this, MainActivity.class)));
    }



    private void sendNotification(String title, String message) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "notification_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName = "Notification Channel";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, importance);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        Intent intent = new Intent(this, MainActivity1.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.cloudy)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);

        notificationManager.notify(0, notificationBuilder.build());
    }

    private void initRecyclerview() {
        ArrayList<Hourly> items = new ArrayList<>();
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat outputFormat = new SimpleDateFormat("HH");
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://api.openweathermap.org/data/2.5/forecast?lat=21.053731&lon=105.7325319&appid=f1bb8c8754ef4707f529c00449b1c751")
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
                        JSONObject jsonObject = new JSONObject(apiResponse);
                        JSONArray list = jsonObject.getJSONArray("list");

                        for (int i = 0; i < list.length(); i++) {
                            JSONObject item = list.getJSONObject(i);
                            String dtTxt = item.getString("dt_txt");

                            // Phân tích chuỗi ngày giờ
                            Date date = inputFormat.parse(dtTxt);

                            // Chỉ lấy phần giờ
                            String hour = outputFormat.format(date);

                            JSONObject main = item.getJSONObject("main");
                            double temp = main.getDouble("temp");

                            JSONArray weather = item.getJSONArray("weather");
                            JSONObject weatherItem = weather.getJSONObject(0);
                            String description = weatherItem.getString("description");
                            String icon;
                            boolean isNightTime = Integer.parseInt(hour) > 18 || Integer.parseInt(hour) < 6;

                            switch (description) {
                                case "clear sky":
                                    icon = isNightTime ? "moon" : "sunny";
                                    break;
                                case "scattered clouds":
                                case "few clouds":
                                    icon = isNightTime ? "night" : "cloudy_sunny";
                                    break;
                                case "overcast clouds":
                                case "broken clouds":
                                    icon = "cloudy";
                                    break;
                                case "light rain":
                                case "moderate rain":
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
                            if (hour.equals("00")) {
                                hour = "00";
                            }

                            String amPm;
                            int hourInt = Integer.parseInt(hour);
                            if (hourInt == 0) {
                                hourInt = 0;
                                amPm = "AM";
                            } else if (hourInt < 12) {
                                amPm = "AM";
                            } else if (hourInt == 12) {
                                amPm = "PM";
                            } else {
                                hourInt -= 12;
                                amPm = "PM";
                            }
                            items.add(new Hourly(hourInt+" " + amPm, (int) (temp - 273.15), icon));
                            // Kiểm tra nếu dtTxt kết thúc là 21:00:00 thì dừng lại
                            if (dtTxt.endsWith("21:00:00")) {
                                break;
                            }

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(() -> {

                        recyclerView = findViewById(R.id.view1);
                        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));

                        adapterHourly = new HourlyAdapters(items, MainActivity1.this);
                        recyclerView.setAdapter(adapterHourly);
                        adapterHourly.notifyDataSetChanged(); // Cập nhật dữ liệu mới cho adapter
                    });
                }
            }
        });
    }
}