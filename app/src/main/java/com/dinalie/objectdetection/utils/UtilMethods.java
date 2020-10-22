package com.dinalie.objectdetection.utils;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;

public class UtilMethods {

    public static byte[] convertBitmapToByteArray(Bitmap bitmaImage) {
        Bitmap compressedBitmap = Bitmap.createScaledBitmap(bitmaImage, 480, 550, true);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 20, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

}
