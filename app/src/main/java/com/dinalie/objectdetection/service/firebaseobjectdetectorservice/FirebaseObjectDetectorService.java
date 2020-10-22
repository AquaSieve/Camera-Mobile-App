package com.dinalie.objectdetection.service.firebaseobjectdetectorservice;


import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
//import com.google.firebase.ml.vision.cloud.label.FirebaseVisionCloudLabel;
//import com.google.firebase.ml.vision.cloud.label.FirebaseVisionCloudLabelDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionCloudImageLabelerOptions;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.firebase.ml.vision.objects.FirebaseVisionObject;
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetector;
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetectorOptions;
//import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetectorOptions;

import org.json.JSONException;

import java.util.List;

//public abstract class FirebaseObjectDetectorService {
//
//    private FirebaseVisionCloudLabelDetector cloudlabeldetector;
//
//    public FirebaseObjectDetectorService(){
//
//        FirebaseVisionLabelDetectorOptions options = new FirebaseVisionLabelDetectorOptions
//                .Builder()
//                .setConfidenceThreshold(0.7f)
//                .build();
//
//        cloudlabeldetector=
//                FirebaseVision.getInstance().getVisionCloudLabelDetector();
//
//    }
//
//
//    public void detectImage(Bitmap bitmaImage, final Context context) {
//
//
//            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmaImage);
//            cloudlabeldetector.detectInImage(image)
//                    .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionCloudLabel>>() {
//                        @Override
//                        public void onSuccess(List<FirebaseVisionCloudLabel> firebaseVisionCloudLabels) {
//                            try {
//                                dataReceived(firebaseVisionCloudLabels, bitmaImage);
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    })
//                    .addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            failure(e);
//                        }
//                    });
//
//
//
//    }
//
//    public abstract void dataReceived(List<FirebaseVisionCloudLabel> firebaseVisionCloudLabels,Bitmap bitmap) throws JSONException;
//
//    public abstract void failure(Exception e);
//
//
//}

public abstract class FirebaseObjectDetectorService {


    private FirebaseVisionImageLabeler cloudlabeldetector;
    private FirebaseVisionObjectDetector objectDetector;

    public FirebaseObjectDetectorService() {

        FirebaseVisionObjectDetectorOptions options2 =
                new FirebaseVisionObjectDetectorOptions.Builder()
                        .setDetectorMode(FirebaseVisionObjectDetectorOptions.SINGLE_IMAGE_MODE)
                        .enableClassification()  // Optional
                        .build();

        FirebaseVisionCloudImageLabelerOptions options =
                new FirebaseVisionCloudImageLabelerOptions.Builder()
                        .setConfidenceThreshold(0.5f)
                        .build();

        cloudlabeldetector = FirebaseVision.getInstance()
                .getCloudImageLabeler(options);

        objectDetector =
                FirebaseVision.getInstance().getOnDeviceObjectDetector(options2);
    }


    public void detectImage(Bitmap bitmaImage, final Context context) {

        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmaImage);

//        objectDetector.processImage(image).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionObject>>() {
//            @Override
//            public void onSuccess(List<FirebaseVisionObject> firebaseVisionObjects) {
//                dataReceivedDetectedObjects(firebaseVisionObjects, bitmaImage);
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//
//            }
//        });


        cloudlabeldetector.processImage(image).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
            @Override
            public void onSuccess(List<FirebaseVisionImageLabel> firebaseVisionImageLabels) {
                try {
                    dataReceived(firebaseVisionImageLabels, bitmaImage);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                failure(e);
            }
        });

//
//        cloudlabeldetector.processImage(image)
//                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionCloudLabel>>() {
//                    @Override
//                    public void onSuccess(List<FirebaseVisionCloudLabel> firebaseVisionCloudLabels) {
//                        try {
//                            dataReceived(firebaseVisionCloudLabels, bitmaImage);
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        failure(e);
//                    }
//                });
//
//
    }

//    public abstract void dataReceived(List<FirebaseVisionCloudLabel> firebaseVisionCloudLabels, Bitmap bitmap) throws JSONException;
//
//    public abstract void failure(Exception e);

    public abstract void dataReceived(List<FirebaseVisionImageLabel> firebaseVisionCloudLabels, Bitmap bitmap) throws JSONException;

    public abstract void dataReceivedDetectedObjects(List<FirebaseVisionObject> firebaseVisionObjects, Bitmap bitmaImage);

    public abstract void failure(Exception e);

}