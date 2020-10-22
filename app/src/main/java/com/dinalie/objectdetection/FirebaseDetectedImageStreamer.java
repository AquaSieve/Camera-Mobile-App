package com.dinalie.objectdetection;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;

import com.camerakit.CameraKitView;
import com.dinalie.objectdetection.service.cameraservice.CameraService;
import com.dinalie.objectdetection.service.firebaseobjectdetectorservice.FirebaseObjectDetectorService;
import com.dinalie.objectdetection.service.locationservice.LocationTrack;
import com.dinalie.objectdetection.service.mqttservice.MqttService;
import com.dinalie.objectdetection.utils.LocalStorageHandler;
import com.dinalie.objectdetection.utils.UtilMethods;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.objects.FirebaseVisionObject;
//import com.google.firebase.ml.vision.cloud.label.FirebaseVisionCloudLabel;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

public class FirebaseDetectedImageStreamer extends AppCompatActivity {

    private static Bitmap currentBitmap;
    private Button startStopButton;
    private CameraKitView cameraKitView;

    private CameraService cameraService;
    private MqttService mqttService;

    private Handler handler;
    private Handler firebaseDataHandler;
    private boolean start = false;

    private FirebaseObjectDetectorService firebaseObjectDetectorService;

    private boolean isPreviousImageResultReceived = true;


    private LocationTrack locationTrack;
    private double longitude;
    private double latitude;
    private static String serverUrl = LocalStorageHandler.ReadData("url");;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_image_streamer);

        locationTrack = new LocationTrack(FirebaseDetectedImageStreamer.this);

        this.cameraKitView = findViewById(R.id.camera);
        startStopButton = findViewById(R.id.start_stop_button);

        mqttService = mqttServiceClient;
        firebaseObjectDetectorService = pvtFirebaseObjectDetectorService;

        handler = new Handler();
        firebaseDataHandler = new Handler();

        startStopButton.setOnClickListener(startStopButtonListner);

        getCurrentLocation();
    }


    private void getCurrentLocation() {

        if (locationTrack.canGetLocation()) {
            longitude = locationTrack.getLongitude();
            latitude = locationTrack.getLatitude();
            System.out.println("lan  :::" + latitude + "long" + longitude);
        } else {
            locationTrack.showSettingsAlert();
        }
    }

    private View.OnClickListener startStopButtonListner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            String text = !start ? "stop" : "start";
            if (start) {
                handler.removeCallbacks(runnable);
                firebaseDataHandler.removeCallbacks(firebaseDataHandlerRunnable);
            } else {
                handler.postDelayed(runnable, 50);
//                firebaseDataHandler.postDelayed(firebaseDataHandlerRunnable, 100);
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

    private Runnable firebaseDataHandlerRunnable = new Runnable() {
        @Override
        public void run() {
            firebaseObjectDetectorService.detectImage(FirebaseDetectedImageStreamer.currentBitmap, FirebaseDetectedImageStreamer.this);
        }
    };

    private CameraService cameraServiceClient = new CameraService() {
        @Override
        public void onCapture(Bitmap bitmap) {

            FirebaseDetectedImageStreamer.currentBitmap = bitmap;
            byte[] imagebytes = UtilMethods.convertBitmapToByteArray(bitmap);

            JSONObject jsonObject = new JSONObject();
            String s = Base64.getEncoder().encodeToString(imagebytes);
            try {

                jsonObject.put("lat", latitude);
                jsonObject.put("lan", longitude);

                jsonObject.put("img", s);
                mqttService.publishData("first_topic", jsonObject.toString().getBytes());

                if (isPreviousImageResultReceived) {
                    isPreviousImageResultReceived = false;
                    firebaseDataHandler.post(firebaseDataHandlerRunnable);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private MqttService mqttServiceClient = new MqttService(FirebaseDetectedImageStreamer.this, "AndroidThingSub", this.serverUrl, "second_topic") {
        @Override
        protected void getReceivedMessage(String topic, MqttMessage message) {
        }
    };


    private FirebaseObjectDetectorService pvtFirebaseObjectDetectorService = new FirebaseObjectDetectorService() {
        @Override
        public void dataReceived(List<FirebaseVisionImageLabel> firebaseVisionCloudLabels, Bitmap bitmap) throws JSONException {
            List<JSONObject> labels = firebaseVisionCloudLabels.stream().map(label -> convertToJsonObject(label)).collect(Collectors.toList());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("labels", labels);
            mqttService.publishData("second_topic", jsonObject.toString().getBytes());
            isPreviousImageResultReceived = true;
        }

        @Override
        public void dataReceivedDetectedObjects(List<FirebaseVisionObject> firebaseVisionObjects, Bitmap bitmaImage) {

        }

        @Override
        public void failure(Exception e) {
            isPreviousImageResultReceived = true;
        }
    };

    private JSONObject convertToJsonObject(FirebaseVisionImageLabel label) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", label.getText());
            jsonObject.put("confident", label.getConfidence());
            return jsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

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