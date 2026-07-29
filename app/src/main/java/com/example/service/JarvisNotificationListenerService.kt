package com.example.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class JarvisNotificationListenerService : NotificationListenerService() {

    companion object {
        private var lastNotificationText: String = ""

        fun getLastNotification(): String {
            return if (lastNotificationText.isNotBlank()) lastNotificationText else "No recent unread notifications"
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn?.let {
            val extras = it.notification?.extras
            val title = extras?.getString("android.title") ?: ""
            val text = extras?.getCharSequence("android.text")?.toString() ?: ""
            val pkg = it.packageName ?: ""

            if (text.isNotBlank() && !pkg.contains("com.example") && !pkg.contains("jarvis")) {
                lastNotificationText = "Notification from $title: $text"
                Log.d("JarvisNotification", lastNotificationText)
            }
        }
    }
}
