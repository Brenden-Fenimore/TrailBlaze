package com.example.trailblaze

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Context

class NotificationHelper(private val context: Context) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val channelId = "park_notifications"

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Park Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for friend requests and updates"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun setupFirestoreListeners(userId: String) {
        val db = FirebaseFirestore.getInstance()

        // Listen for pending requests
        db.collection("users").document(userId)
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let {
                    val pendingRequests = it.get("pendingRequests") as? List<String> ?: emptyList()
                    if (pendingRequests.isNotEmpty()) {
                        showNotification(
                            "New Friend Request",
                            "You have ${pendingRequests.size} pending friend requests"
                        )
                    }
                }
            }

        // Listen for pending notifications
        db.collection("users").document(userId)
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let {
                    val pendingNotifications = it.get("pendingNotifications") as? List<String> ?: emptyList()
                    if (pendingNotifications.isNotEmpty()) {
                        showNotification(
                            "New Notification",
                            "You have ${pendingNotifications.size} new notifications"
                        )
                    }
                }
            }
    }

    private fun showNotification(title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}