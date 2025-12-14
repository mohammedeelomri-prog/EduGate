package com.example.projet_mobile.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class PdfUploader {

    public interface UploadCallback {
        void onProgress(int progress);
        void onSuccess(String downloadUrl);
        void onFailure(String error);
    }

    public static void uploadPdf(Context context, Uri pdfUri, String requestId, UploadCallback callback) {
        if (pdfUri == null) {
            Log.e("PdfUploader", "PDF URI is null");
            callback.onFailure("Aucun fichier sélectionné");
            return;
        }

        Log.d("PdfUploader", "Starting upload for requestId: " + requestId);
        Log.d("PdfUploader", "PDF URI: " + pdfUri.toString());

        // Vérifier que c'est bien un PDF
        String mimeType = context.getContentResolver().getType(pdfUri);
        Log.d("PdfUploader", "MIME type: " + mimeType);
        
        if (mimeType == null || !mimeType.equals("application/pdf")) {
            Log.w("PdfUploader", "Invalid MIME type: " + mimeType);
            callback.onFailure("Le fichier doit être un PDF");
            return;
        }

        try {
            // Lire le fichier et le convertir en Base64
            java.io.InputStream inputStream = context.getContentResolver().openInputStream(pdfUri);
            if (inputStream == null) {
                callback.onFailure("Impossible de lire le fichier");
                return;
            }

            // Lire le fichier en bytes
            java.io.ByteArrayOutputStream byteBuffer = new java.io.ByteArrayOutputStream();
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int len;
            long totalBytes = 0;

            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
                totalBytes += len;
                
                // Limiter la taille à 1 MB (Firestore limite)
                if (totalBytes > 1024 * 1024) {
                    inputStream.close();
                    callback.onFailure("Le PDF est trop volumineux (max 1 MB). Utilisez un fichier plus petit.");
                    return;
                }
                
                // Simuler la progression
                int progress = (int) ((totalBytes / (1024.0 * 1024.0)) * 100);
                callback.onProgress(Math.min(progress, 90));
            }
            
            inputStream.close();

            // Convertir en Base64
            byte[] pdfBytes = byteBuffer.toByteArray();
            String base64Pdf = android.util.Base64.encodeToString(pdfBytes, android.util.Base64.DEFAULT);
            
            Log.d("PdfUploader", "PDF converted to Base64, size: " + base64Pdf.length() + " chars");
            
            callback.onProgress(95);
            
            // Retourner le Base64 comme "URL" (on le stockera dans Firestore)
            callback.onSuccess("data:application/pdf;base64," + base64Pdf);
            
        } catch (java.io.IOException e) {
            Log.e("PdfUploader", "IOException during file read", e);
            callback.onFailure("Erreur de lecture du fichier: " + e.getMessage());
        } catch (Exception e) {
            Log.e("PdfUploader", "Exception during upload", e);
            callback.onFailure("Erreur: " + e.getMessage());
        }
    }

    public static String getFileExtension(Context context, Uri uri) {
        String extension;
        if (uri.getScheme().equals("content")) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            extension = mime.getExtensionFromMimeType(context.getContentResolver().getType(uri));
        } else {
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new java.io.File(uri.getPath())).toString());
        }
        return extension;
    }
}
