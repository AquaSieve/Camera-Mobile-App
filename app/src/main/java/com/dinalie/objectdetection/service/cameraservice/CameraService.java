package com.dinalie.objectdetection.service.cameraservice;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.camerakit.CameraKitView;

public abstract class CameraService {


    public void capture(CameraKitView cameraKitView){
        cameraKitView.captureImage(new CameraKitView.ImageCallback() {
            @Override
            public void onImage(CameraKitView cameraKitView, byte[] bytes) {
                Bitmap capturedBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                Bitmap compressedBitmap = Bitmap.createScaledBitmap(capturedBitmap, capturedBitmap.getWidth() , capturedBitmap.getHeight() , true);
                onCapture(capturedBitmap);
            }
        });
    }

    public abstract void onCapture(Bitmap bitmap);

}
