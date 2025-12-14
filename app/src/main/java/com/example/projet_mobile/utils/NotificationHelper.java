package com.example.projet_mobile.utils;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

public class NotificationHelper {

    // Durées
    public static final int SHORT = Toast.LENGTH_SHORT;
    public static final int LONG = Toast.LENGTH_LONG;

    // Types de notifications
    public enum NotificationType {
        SUCCESS,
        ERROR,
        INFO,
        WARNING
    }

    /**
     * Afficher un Toast simple
     */
    public static void showToast(Context context, String message, int duration) {
        Toast.makeText(context, message, duration).show();
    }

    /**
     * Afficher un Toast avec icône selon le type
     */
    public static void showToast(Context context, String message, NotificationType type) {
        String icon = getIconForType(type);
        Toast.makeText(context, icon + " " + message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Afficher un Snackbar simple
     */
    public static void showSnackbar(View view, String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Afficher un Snackbar avec action
     */
    public static void showSnackbarWithAction(View view, String message, String actionText, View.OnClickListener action) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG)
                .setAction(actionText, action)
                .show();
    }

    /**
     * Afficher un Snackbar coloré selon le type
     */
    public static void showSnackbar(View view, String message, NotificationType type) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
        
        // Personnaliser la couleur selon le type
        int backgroundColor = getColorForType(type);
        snackbar.setBackgroundTint(backgroundColor);
        snackbar.setTextColor(0xFFFFFFFF); // Blanc
        
        snackbar.show();
    }

    /**
     * Notifications pour les changements de statut
     */
    public static void notifyStatusChange(Context context, String oldStatus, String newStatus) {
        String message = getStatusChangeMessage(oldStatus, newStatus);
        NotificationType type = getTypeForStatusChange(newStatus);
        showToast(context, message, type);
    }

    /**
     * Notification pour nouvelle demande
     */
    public static void notifyNewRequest(Context context, String documentType) {
        showToast(context, "Nouvelle demande : " + documentType, NotificationType.INFO);
    }

    /**
     * Notification pour upload réussi
     */
    public static void notifyUploadSuccess(Context context) {
        showToast(context, "PDF uploadé avec succès", NotificationType.SUCCESS);
    }

    /**
     * Notification pour téléchargement réussi
     */
    public static void notifyDownloadSuccess(Context context) {
        showToast(context, "PDF téléchargé avec succès", NotificationType.SUCCESS);
    }

    // Méthodes privées

    private static String getIconForType(NotificationType type) {
        switch (type) {
            case SUCCESS:
                return "✓";
            case ERROR:
                return "✗";
            case INFO:
                return "ℹ";
            case WARNING:
                return "⚠";
            default:
                return "";
        }
    }

    private static int getColorForType(NotificationType type) {
        switch (type) {
            case SUCCESS:
                return 0xFF4CAF50; // Vert
            case ERROR:
                return 0xFFF44336; // Rouge
            case INFO:
                return 0xFF2196F3; // Bleu
            case WARNING:
                return 0xFFFF9800; // Orange
            default:
                return 0xFF757575; // Gris
        }
    }

    private static String getStatusChangeMessage(String oldStatus, String newStatus) {
        switch (newStatus) {
            case "approuvee":
                return "✓ Votre demande a été validée";
            case "pret":
                return "📄 Votre document est prêt !";
            case "rejetee":
                return "✗ Votre demande a été rejetée";
            default:
                return "Statut mis à jour";
        }
    }

    private static NotificationType getTypeForStatusChange(String newStatus) {
        switch (newStatus) {
            case "approuvee":
            case "pret":
                return NotificationType.SUCCESS;
            case "rejetee":
                return NotificationType.ERROR;
            default:
                return NotificationType.INFO;
        }
    }
}
