package com.dinalie.objectdetection;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;

import com.camerakit.CameraKitView;
import com.dinalie.objectdetection.service.cameraservice.CameraService;
import com.dinalie.objectdetection.service.mqttservice.MqttService;
import com.dinalie.objectdetection.utils.LocalStorageHandler;
import com.dinalie.objectdetection.utils.UtilMethods;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Base64;

public class BasicImageStreamer extends AppCompatActivity {

    private Button startStopButton;
    private CameraKitView cameraKitView;

    private CameraService cameraService;
    private MqttService mqttService;

    private Handler handler;
    private boolean start = false;
    private static String serverUrl = LocalStorageHandler.ReadData("url");;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_image_streamer);

        this.cameraKitView = findViewById(R.id.camera);
        startStopButton = findViewById(R.id.start_stop_button);

        mqttService = mqttServiceClient;

        handler = new Handler();

        startStopButton.setOnClickListener(startStopButtonListner);

    }

    private View.OnClickListener startStopButtonListner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            String text = start? "stop" : "start";
            if(start){
                handler.removeCallbacks(runnable);
            }else{
                handler.postDelayed(runnable, 50);
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                   startStopButton.setText(text);
                }
            });
            start = !start;
        }
    };


    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            cameraService.capture(cameraKitView);
            handler.postDelayed(runnable, 50);

        }

    };


    private CameraService cameraServiceClient = new CameraService() {
        @Override
        public void onCapture(Bitmap bitmap) {

            byte[] imagebytes = UtilMethods.convertBitmapToByteArray(bitmap);

            JSONObject jsonObject = new JSONObject();
            String s = Base64.getEncoder().encodeToString(imagebytes);
            try {
                jsonObject.put("img", s);
                mqttService.publishData("first_topic", jsonObject.toString().getBytes());

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
//"tcp://192.168.8.138:1883"
    private MqttService mqttServiceClient = new MqttService(BasicImageStreamer.this,"AndroidThingSub", serverUrl, "second_topic" ) {
        @Override
        protected void getReceivedMessage(String topic, MqttMessage message) {

        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        cameraKitView.onStart();
        cameraService = cameraServiceClient;
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraKitView.onResume();
    }

    @Override
    protected void onPause() {
        cameraKitView.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        cameraKitView.onStop();
        super.onStop();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        cameraKitView.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }



}