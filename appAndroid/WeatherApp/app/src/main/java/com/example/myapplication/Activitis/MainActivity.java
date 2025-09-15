/*** --- Function of module --- 
 * Manage list place which locate hardware to draw data.
 * Add/Delete place
 * Can forward to MainActivity1 
 */

package com.example.myapplication.Activitis;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;


public class MainActivity extends AppCompatActivity {

    Button add;                         // Add new card button
    AlertDialog dialog;                 // Station name entry dialog box
    LinearLayout layout;                // Container contains cards
    SharedPreferences sharedPreferences;// Save status of cards

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // mapping to xml
        add = findViewById(R.id.add);
        layout = findViewById(R.id.container);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (Build.VERSION.SDK_INT >= 33){
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1001);
            }
        }
//        Intent serviceIntent = new Intent(this, FirebaseMonitoringService.class);
//        startService(serviceIntent);

        buildDialog(); // Create dialog to enter name's station

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (layout.getChildCount() >= 1) {
                    // Only add 1 station
                    Toast.makeText(MainActivity.this, "Hiện chỉ có 1 trạm quan trắc", Toast.LENGTH_SHORT).show();
                } else {
                    dialog.show();
                }
            }
        });

        // Restore origin status of programd
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
                saveState(); // Save status after delete cardView
            }
        });

        layout.addView(cardView);
        saveState(); // Save status after add new cardView
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