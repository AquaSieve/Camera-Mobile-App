package com.dinalie.objectdetection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class OptionsActivity extends AppCompatActivity {

    private Button singleImage;

    private Button imageStreamer;

    private Button Ml;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        singleImage = findViewById(R.id.button1);
        imageStreamer = findViewById(R.id.button3);
        Ml = findViewById(R.id.button4);


        singleImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OptionsActivity.this,MainActivity.class);
                startActivity(intent);
                OptionsActivity.this.finish();
            }
        });

        imageStreamer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OptionsActivity.this,BasicImageStreamer.class);
                startActivity(intent);
                OptionsActivity.this.finish();
            }
        });

        Ml.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OptionsActivity.this,FirebaseDetectedImageStreamer.class);
                startActivity(intent);
                OptionsActivity.this.finish();
            }
        });
    }

}