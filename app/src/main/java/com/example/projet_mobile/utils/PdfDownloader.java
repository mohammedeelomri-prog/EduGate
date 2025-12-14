package com.example.projet_mobile.utils;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Base64;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;

public class PdfDownloader {

    public static void downloadAndOpenPdf(Context context, String pdfData, String fileName) {
        if (pdfData == null || pdfData.isEmpty()) {
            Toast.makeText(context, "PDF non disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Vérifier si c'est un Base64 ou une URL
            if (pdfData.startsWith("data:application/pdf;base64,")) {
                // C'est un PDF en Base64, le sauvegarder localement
                savePdfFromBase64(context, pdfData, fileName);
            } else if (pdfData.startsWith("http")) {
                // C'est une URL Firebase Storage (si jamais activé plus tard)
                downloadFromUrl(context, pdfData, fileName);
            } else {
                Toast.makeText(context, "Format de PDF non reconnu", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(context, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private static void savePdfFromBase64(Context context, String base64Data, String fileName) {
        try {
            // Extraire le Base64 (enlever le préfixe "data:application/pdf;base64,")
            String base64Pdf = base64Data.substring(base64Data.indexOf(",") + 1);
            
            // Décoder le Base64
            byte[] pdfBytes = Base64.decode(base64Pdf, Base64.DEFAULT);
            
            // Créer le fichier dans Downloads
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File pdfFile = new File(downloadsDir, fileName);
            
            // Écrire les bytes dans le fichier
            FileOutputStream fos = new FileOutputStream(pdfFile);
            fos.write(pdfBytes);
            fos.close();
            
            Toast.makeText(context, "PDF téléchargé avec succès", Toast.LENGTH_SHORT).show();
            
            // Ouvrir le PDF
            openPdf(context, fileName);
            
        } catch (Exception e) {
            Toast.makeText(context, "Erreur de téléchargement: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private static void downloadFromUrl(Context context, String pdfUrl, String fileName) {
        try {
            // Créer la requête de téléchargement
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(pdfUrl));
            request.setTitle("Téléchargement du document");
            request.setDescription("Téléchargement de " + fileName);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
            request.setAllowedOverMetered(true);
            request.setAllowedOverRoaming(true);

            // Obtenir le DownloadManager et lancer le téléchargement
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            if (downloadManager != null) {
                downloadManager.enqueue(request);
                Toast.makeText(context, "Téléchargement démarré...", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(context, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public static void openPdf(Context context, String fileName) {
        try {
            File file = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS), fileName);

            if (file.exists()) {
                Uri uri = FileProvider.getUriForFile(context,
                        context.getPackageName() + ".provider", file);

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, "application/pdf");
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_GRANT_READ_URI_PERMISSION);

                context.startActivity(Intent.createChooser(intent, "Ouvrir le PDF avec"));
            } else {
                Toast.makeText(context, "Fichier non trouvé", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(context, "Aucune application pour ouvrir les PDF", Toast.LENGTH_SHORT).show();
        }
    }
}
