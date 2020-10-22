package com.dinalie.objectdetection;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.dinalie.objectdetection.service.firebaseobjectdetectorservice.FirebaseObjectDetectorService;
import com.dinalie.objectdetection.service.locationservice.LocationTrack;
import com.dinalie.objectdetection.service.mqttservice.MqttService;
//import com.google.firebase.ml.vision.cloud.label.FirebaseVisionCloudLabel;
//import com.google.firebase.ml.vision.cloud.label.FirebaseVisionCloudLabelDetector;
import com.dinalie.objectdetection.utils.LocalStorageHandler;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.objects.FirebaseVisionObject;
//import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetector;


import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;


public class MainActivity extends AppCompatActivity {


    private ImageView imageView;
    private ImageView receivedImageView;
    private Bitmap bitmaImage;
    private Button snap, detect;
    private TextView textviewLabel;
    private FirebaseVisionImage image;
    private LocationTrack locationTrack;
    private ProgressDialog progressDialog;
    static final int REQUEST_IMAGE_CAPTURE = 1;

    private double longitude;
    private double latitude;

    MqttService mqttService;
    FirebaseObjectDetectorService firebaseObjectDetectorService;
    private static String serverUrl = LocalStorageHandler.ReadData("url");;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationTrack = new LocationTrack(MainActivity.this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading");
        imageView = findViewById(R.id.imageview);
        receivedImageView = findViewById(R.id.imageview3);
        snap = findViewById(R.id.start_stop_button);
        detect = findViewById(R.id.button3);
        textviewLabel = findViewById(R.id.textView);
        firebaseObjectDetectorService = pvtFirebaseObjectDetectorService;

        snap.setOnClickListener(takePictureListner);
        detect.setOnClickListener(detectButtonListner);

        getCurrentLocation();
        startMqttService();

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


    private final View.OnClickListener takePictureListner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dispatchTakePictureIntent();
        }
    };

    private final View.OnClickListener detectButtonListner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            firebaseObjectDetectorService.detectImage(bitmaImage, MainActivity.this);
        }
    };


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            bitmaImage = (Bitmap) extras.get("data");
            imageView.setImageBitmap(bitmaImage);
        }
    }

    void startMqttService(){

        mqttService = new MqttService(MainActivity.this, "AndroidThingSub", this.serverUrl, "second_topic") {
            @Override
            protected void getReceivedMessage(String topic, MqttMessage message) {

                runOnUiThread(() -> {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(message.getPayload(), 0, message.getPayload().length);
                    receivedImageView.setImageBitmap(bitmap);
                });
            }
        };
    }


    private MqttService mqttServiceClient = new MqttService(MainActivity.this, "AndroidThingSub", this.serverUrl, "second_topic") {
        @Override
        protected void getReceivedMessage(String topic, MqttMessage message) {

            runOnUiThread(() -> {
                Bitmap bitmap = BitmapFactory.decodeByteArray(message.getPayload(), 0, message.getPayload().length);
                receivedImageView.setImageBitmap(bitmap);
            });
        }
    };


    private FirebaseObjectDetectorService pvtFirebaseObjectDetectorService = new FirebaseObjectDetectorService() {
        @Override
        public void dataReceived(List<FirebaseVisionImageLabel> firebaseVisionCloudLabels, Bitmap bitmap) throws JSONException {
            progressDialog.dismiss();

            JSONObject jsonObject = new JSONObject();
            byte[] imagebytes = convertBitmapToByteArray(bitmaImage);
            String s = Base64.getEncoder().encodeToString(imagebytes);

            jsonObject.put("img", s);
            jsonObject.put("lat", latitude);
            jsonObject.put("lan", longitude);

            mqttService.publishData("first_topic", jsonObject.toString().getBytes());


            List<JSONObject> labels = firebaseVisionCloudLabels.stream().map(label -> convertToJsonObject(label)).collect(Collectors.toList());
            JSONObject jsonObject2 = new JSONObject();
            jsonObject2.put("labels", labels);
            mqttService.publishData("second_topic", jsonObject2.toString().getBytes());

        }

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
        public void dataReceivedDetectedObjects(List<FirebaseVisionObject> firebaseVisionObjects, Bitmap bitmaImage) {
        }

        @Override
        public void failure(Exception e) {

        }
    };

    private byte[] convertBitmapToByteArray(Bitmap bitmaImage) {

        Bitmap compressedBitmap = Bitmap.createScaledBitmap(bitmaImage, bitmaImage.getWidth(), bitmaImage.getHeight(), true);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }


}


//    private FirebaseObjectDetectorService pvtFirebaseObjectDetectorService = new FirebaseObjectDetectorService() {
//        @Override
//        public void dataReceived(List<FirebaseVisionCloudLabel> firebaseVisionCloudLabels, Bitmap bitmap) throws JSONException {
//            progressDialog.dismiss();
//            for (FirebaseVisionCloudLabel label : firebaseVisionCloudLabels) {
//                String cloudlabel = "Object: " + label.getLabel() + "\n" + "Confidence: " + label.getConfidence();
//                textviewLabel.setText(cloudlabel);
//            }
//
//
//            List<String> labels = firebaseVisionCloudLabels.stream().map(label -> label.getLabel()).collect(Collectors.toList());
//
//            JSONObject jsonObject = new JSONObject();
//            jsonObject.put("labels", labels);
//
//            byte[] imagebytes = convertBitmapToByteArray(bitmaImage);
//
//            String s = Base64.getEncoder().encodeToString(imagebytes);
//            jsonObject.put("img", s);
//
//            mqttService.publishData("first_topic", jsonObject.toString().getBytes());
//            mqttService.publishData("second_topic", imagebytes);
//
//
//        }
//
//        @Override
//        public void failure(Exception e) {
//            Toast.makeText(MainActivity.this, "labelling failed", Toast.LENGTH_SHORT).show();
//            progressDialog.dismiss();
//        }
//    };




