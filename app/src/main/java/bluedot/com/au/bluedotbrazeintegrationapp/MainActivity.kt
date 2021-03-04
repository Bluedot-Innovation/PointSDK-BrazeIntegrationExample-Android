package bluedot.com.au.bluedotbrazeintegrationapp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import au.com.bluedot.point.net.engine.GeoTriggeringService
import bluedot.com.au.bluedotbrazeintegrationapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root

        setContentView(view)

        binding.bStartStopSDK.setOnClickListener {
            val app: MainApplication = this.application as MainApplication

            if (GeoTriggeringService.isRunning()) {
                binding.bStartStopSDK.text = getString(R.string.stop_sdk)
                GeoTriggeringService.stop(this, app)
            } else {
                binding.bStartStopSDK.text = getString(R.string.start_sdk)

                val notification: Notification = createNotification()

                GeoTriggeringService.builder()
                    .notification(notification)
                    .start(applicationContext, app)
            }
            finish()
        }
    }

    override fun onResume() {
        super.onResume()

        if (GeoTriggeringService.isRunning()) {
            binding.bStartStopSDK.text = getString(R.string.stop_sdk)
        } else {
            binding.bStartStopSDK.text = getString(R.string.start_sdk)
        }
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
}
