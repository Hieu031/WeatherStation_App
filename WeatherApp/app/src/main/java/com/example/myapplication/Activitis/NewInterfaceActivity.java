/*** --- Function of module --- 
 * Display data from Firebase for a specific location
 * Can turn back "MainActivity"
 */

package com.example.myapplication.Activitis;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NewInterfaceActivity extends AppCompatActivity {

    TextView tv1, tv2;
    Button back;
    DatabaseReference mData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_interface);

        tv1 = findViewById(R.id.textView);
        tv2 = findViewById(R.id.textView2);
        back = findViewById(R.id.back);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Return Screen MainActivity
                onBackPressed();
            }
        });

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("doam");
        DatabaseReference myRefNhietDo = database.getReference("nhietdo");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String message = dataSnapshot.getValue(String.class);
                // Update interface at main flow
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Display data in tv1
                        tv1.setText(message);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Handler error (if have)
            }
        });
        myRefNhietDo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String nhietDo = dataSnapshot.getValue(String.class);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv2.setText("Nhiệt độ: " + nhietDo); // Display temperature in tv2
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Handler error (if have)
            }
        });
    }

    // Function return MainActivity
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(NewInterfaceActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); 
    }
}