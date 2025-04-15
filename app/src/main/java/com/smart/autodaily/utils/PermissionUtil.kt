package com.smart.autodaily.utils

import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import splitties.init.appCtx


fun hasNotificationPermission() : Boolean{
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        // Android 13及以上版本检查POST_NOTIFICATIONS权限
        ActivityCompat.checkSelfPermission(
            appCtx,
            android.Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        // Android 13以下版本检查通知是否启用
        NotificationManagerCompat.from(appCtx).areNotificationsEnabled()
    }
}