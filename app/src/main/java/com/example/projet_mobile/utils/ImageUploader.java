package com.example.projet_mobile.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class ImageUploader {

    private static final String TAG = "ImageUploader";
    private static final int MAX_IMAGE_SIZE = 500 * 1024;

    public interface UploadCallback {
        void onProgress(int progress);
        void onSuccess(String base64Image);
        void onFailure(String error);
    }

    public static void uploadImage(Context context, Uri imageUri, UploadCallback callback) {
        if (imageUri == null) {
            callback.onFailure("Aucune image sélectionnée");
            return;
        }

        try {
            callback.onProgress(10);

            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                callback.onFailure("Impossible de lire l'image");
                return;
            }

            callback.onProgress(30);

            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            if (originalBitmap == null) {
                callback.onFailure("Format d'image non valide");
                return;
            }

            callback.onProgress(50);

            Bitmap compressedBitmap = compressImage(originalBitmap);

            callback.onProgress(70);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            if (byteArray.length > MAX_IMAGE_SIZE) {
                callback.onFailure("Image trop volumineuse (max 500 KB). Veuillez choisir une image plus petite.");
                return;
            }

            callback.onProgress(90);

            String base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT);
            String dataUrl = "data:image/jpeg;base64," + base64Image;

            Log.d(TAG, "Image converted to Base64, size: " + base64Image.length() + " chars");

            callback.onProgress(100);
            callback.onSuccess(dataUrl);

            originalBitmap.recycle();
            compressedBitmap.recycle();

        } catch (Exception e) {
            Log.e(TAG, "Error uploading image", e);
            callback.onFailure("Erreur: " + e.getMessage());
        }
    }

    private static Bitmap compressImage(Bitmap original) {
        int maxWidth = 800;
        int maxHeight = 800;

        int width = original.getWidth();
        int height = original.getHeight();

        float ratio = Math.min(
                (float) maxWidth / width,
                (float) maxHeight / height
        );

        if (ratio >= 1) {
            return original;
        }

        int newWidth = Math.round(width * ratio);
        int newHeight = Math.round(height * ratio);

        return Bitmap.createScaledBitmap(original, newWidth, newHeight, true);
    }

    public static Bitmap decodeBase64(String base64Image) {
        try {
            if (base64Image.startsWith("data:image")) {
                base64Image = base64Image.substring(base64Image.indexOf(",") + 1);
            }

            byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            Log.e(TAG, "Error decoding Base64 image", e);
            return null;
        }
    }
}
