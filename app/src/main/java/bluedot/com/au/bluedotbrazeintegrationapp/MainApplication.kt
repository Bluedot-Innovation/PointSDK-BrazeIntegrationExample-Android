package bluedot.com.au.bluedotbrazeintegrationapp

import android.Manifest
import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import au.com.bluedot.point.net.engine.*
import com.appboy.Appboy
import com.appboy.AppboyLifecycleCallbackListener

/*
 * @author Bluedot Innovation
 * Copyright (c) 2019 Bluedot Innovation. All rights reserved.
 * MainApplication demonstrates the implementation Bluedot Point SDK and related callbacks.
 */
class MainApplication : Application(), InitializationResultListener, GeoTriggeringStatusListener {

    private lateinit var mServiceManager: ServiceManager

    // BrazeApp
    private val projectId = "e3eb0689-5a35-4114-8273-85ba12084564" //project Id for the App
    // set this to true if you want to start the SDK with service sticky and auto-start mode on boot complete.
    // Please refer to Bluedot Developer documentation for further information.

    override fun onCreate() {
        super.onCreate()

        registerActivityLifecycleCallbacks(AppboyLifecycleCallbackListener(false, false))

        // initialize Bluedot point sdk
        initPointSDK()
    }

    fun initPointSDK() {

        val checkPermissionFine =
            ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            )

        if (checkPermissionFine == PackageManager.PERMISSION_GRANTED) {
            mServiceManager = ServiceManager.getInstance(this)

            if (!mServiceManager.isBluedotServiceInitialized) {
                mServiceManager.initialize(projectId, this)
            }
        } else {
            requestPermissions()
        }

    }

    private fun requestPermissions() {
        val intent = Intent(applicationContext, RequestPermissionActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    /**
     * Creates notification channel and notification, required for foreground service notification.
     * @return notification
     */

    private fun createNotification(): Notification {
        val channelId = "Bluedot" + getString(R.string.app_name)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Bluedot Service" + getString(R.string.app_name)
            val notificationChannel =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            notificationChannel.enableLights(false)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(false)
            val notificationManager =
                this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)

            val notification = Notification.Builder(applicationContext, channelId)
                .setContentTitle(getString(R.string.foreground_notification_title))
                .setContentText(getString(R.string.foreground_notification_text))
                .setStyle(
                    Notification.BigTextStyle()
                        .bigText(getString(R.string.foreground_notification_text))
                )
                .setOngoing(true)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setSmallIcon(R.mipmap.ic_launcher)

            return notification.build()
        } else {

            val notification = NotificationCompat.Builder(applicationContext, channelId)
                .setContentTitle(getString(R.string.foreground_notification_title))
                .setContentText(getString(R.string.foreground_notification_text))
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(getString(R.string.foreground_notification_text))
                )
                .setOngoing(true)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setSmallIcon(R.mipmap.ic_launcher)

            return notification.build()
        }
    }

    override fun onInitializationFinished(error: BDError?) {
        if (error != null) {
            Toast.makeText(
                applicationContext,
                "Bluedot Point SDK initialization error: ${error.reason}",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        Appboy.getInstance(this).changeUser("bluedot_sdk_and_brazer_sdk_integration_android")
        println("Bluedot Point SDK authenticated")
        val notification = createNotification()

        GeoTriggeringService.builder()
            .notification(notification)
            .start(applicationContext, this)
    }

    override fun onGeoTriggeringResult(error: BDError?) {
        if (error != null) {
            Toast.makeText(
                applicationContext,
                "Bluedot Point SDK start GeoTrigerring error: ${error.reason}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
