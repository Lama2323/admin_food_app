package com.example.adminfoodapp.utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import java.io.ByteArrayOutputStream;

public class ImageCompressor {
    public static Bitmap compressBitmap(Bitmap bitmap, int maxSizeBytes) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scale;

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        int quality = 100;
        while (outputStream.toByteArray().length > maxSizeBytes && quality > 0) {
            outputStream.reset();
            quality -= 10;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
        }

        if (outputStream.toByteArray().length > maxSizeBytes) {
            scale = (float) Math.sqrt((double) maxSizeBytes / outputStream.toByteArray().length);
        } else {
            return bitmap;
        }

        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
    }
}