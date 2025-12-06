package com.example.weather.Activitis;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.weather.R;


public class MainActivity extends AppCompatActivity {

    Button add;
    AlertDialog dialog;
    LinearLayout layout;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        add = findViewById(R.id.add);
        layout = findViewById(R.id.container);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

//        Intent serviceIntent = new Intent(this, FirebaseMonitoringService.class);
//        startService(serviceIntent);

        buildDialog();

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
            }
        });

        // Khôi phục trạng thái khi ứng dụng bắt đầu
        restoreState();
    }


    private void buildDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog, null);

        EditText name = view.findViewById(R.id.nameEdit);

        builder.setView(view);
        builder.setTitle("Enter name")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addCard(name.getText().toString());

                    }

                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        dialog = builder.create();
    }

    private void addCard(String name) {
        View cardView = getLayoutInflater().inflate(R.layout.card, null);
        TextView nameView = cardView.findViewById(R.id.name);
        Button delete = cardView.findViewById(R.id.delete);
        nameView.setText(name);

        // Set OnClickListener to open NewInterfaceActivity
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create Intent to open NewInterfaceActivity
                Intent intent = new Intent(MainActivity.this, MainActivity1.class);
                startActivity(intent);
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Remove the card view from the layout
                layout.removeView(cardView);
                saveState(); // Lưu trạng thái sau khi xóa cardView
            }
        });

        layout.addView(cardView);
        saveState(); // Lưu trạng thái sau khi thêm cardView mới
    }

    // Lưu trạng thái của các cardView vào SharedPreferences
    private void saveState() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("cardCount", layout.getChildCount());

        for (int i = 0; i < layout.getChildCount(); i++) {
            View cardView = layout.getChildAt(i);
            TextView nameView = cardView.findViewById(R.id.name);
            String name = nameView.getText().toString();
            editor.putString("cardName_" + i, name);
        }

        editor.apply();
    }

    // Khôi phục trạng thái từ SharedPreferences
    private void restoreState() {
        int cardCount = sharedPreferences.getInt("cardCount", 0);

        for (int i = 0; i < cardCount; i++) {
            String name = sharedPreferences.getString("cardName_" + i, "");
            if (!name.isEmpty()) {
                addCard(name);
            }
        }
    }
}