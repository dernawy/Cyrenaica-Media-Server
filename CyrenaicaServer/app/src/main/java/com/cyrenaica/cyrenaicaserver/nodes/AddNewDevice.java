package com.cyrenaica.cyrenaicaserver.nodes;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cyrenaica.cyrenaicaserver.R;
import com.cyrenaica.cyrenaicaserver.language.LocalHelper;

public class AddNewDevice extends AppCompatActivity {

    TextView page_label;
    ImageView add_device_icon;
    TextView add_device_icon_label;
    FrameLayout device_type_frame;
    RadioButton add_camera_choice;
    RadioButton add_detector_choice;
    RadioButton add_controller_choice;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_new_device);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        page_label            = findViewById(R.id.id_add_devices_label);
        add_device_icon       = findViewById(R.id.id_add_device_icon);
        add_device_icon_label = findViewById(R.id.id_add_device_icon_label);
        device_type_frame     = findViewById(R.id.id_add_new_device_type_frame);
        add_camera_choice     = findViewById(R.id.id_add_new_camera_choice);
        add_detector_choice   = findViewById(R.id.id_add_new_detector_choice);
        add_controller_choice = findViewById(R.id.id_add_new_controller_choice);

        if (LocalHelper.getLanguage(AddNewDevice.this).equalsIgnoreCase("ar")) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                page_label.setTypeface(getResources().getFont(R.font.changa));
                add_device_icon_label.setTypeface(getResources().getFont(R.font.cairo));
                add_camera_choice.setTypeface(getResources().getFont(R.font.cairo));
                add_detector_choice.setTypeface(getResources().getFont(R.font.cairo));
                add_controller_choice.setTypeface(getResources().getFont(R.font.cairo));
            }

        }
        else
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                page_label.setTypeface(getResources().getFont(R.font.alfa_slab_one_regular));
                add_device_icon_label.setTypeface(getResources().getFont(R.font.robotoslab));
                add_camera_choice.setTypeface(getResources().getFont(R.font.robotoslab));
                add_detector_choice.setTypeface(getResources().getFont(R.font.robotoslab));
                add_controller_choice.setTypeface(getResources().getFont(R.font.robotoslab));
            }
        }

        add_device_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                device_type_frame.setVisibility(View.VISIBLE);

            }
        });

        add_camera_choice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(AddNewDevice.this, NewDeviceSetup.class);
                intent.putExtra("device_type","CAMERA");
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

            }
        });

        add_detector_choice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(AddNewDevice.this, NewDeviceSetup.class);
                intent.putExtra("device_type","detector");
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

            }
        });

        add_controller_choice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(AddNewDevice.this, NewDeviceSetup.class);
                intent.putExtra("device_type","controller");
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

            }
        });
    }
}