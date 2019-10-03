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
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import au.com.bluedot.application.model.Proximity
import au.com.bluedot.point.ApplicationNotificationListener
import au.com.bluedot.point.ServiceStatusListener
import au.com.bluedot.point.net.engine.BDError
import au.com.bluedot.point.net.engine.BeaconInfo
import au.com.bluedot.point.net.engine.FenceInfo
import au.com.bluedot.point.net.engine.LocationInfo
import au.com.bluedot.point.net.engine.ServiceManager
import au.com.bluedot.point.net.engine.ZoneInfo
import com.appboy.Appboy
import com.appboy.AppboyLifecycleCallbackListener
import com.appboy.models.outgoing.AppboyProperties

/*
 * @author Bluedot Innovation
 * Copyright (c) 2019 Bluedot Innovation. All rights reserved.
 * MainApplication demonstrates the implementation Bluedot Point SDK and related callbacks.
 */
class MainApplication : Application(), ServiceStatusListener, ApplicationNotificationListener {

    private lateinit var mServiceManager: ServiceManager

    // BrazeApp
    private val apiKey = "" //API key for the App 
    // set this to true if you want to start the SDK with service sticky and auto-start mode on boot complete.
    // Please refer to Bluedot Developer documentation for further information.
    private var restartMode = true
    private val customEventEntry = "bluedot_entry"
    private val customEventExit = "bluedot_exit"

    override fun onCreate() {
        super.onCreate()

        registerActivityLifecycleCallbacks(AppboyLifecycleCallbackListener())

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

            if (!mServiceManager.isBlueDotPointServiceRunning) {
                // Setting Notification for foreground service, required for Android Oreo and above.
                // Setting targetAllAPIs to TRUE will display foreground notification for Android versions lower than Oreo
                mServiceManager.setForegroundServiceNotification(createNotification(), false)
                mServiceManager.sendAuthenticationRequest(apiKey, this, restartMode)
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
     *
     * It is called when BlueDotPointService started successful, your app logic code using the Bluedot service could start from here.
     *
     * This method is off the UI thread.
     */
    override fun onBlueDotPointServiceStartedSuccess() {
        mServiceManager.subscribeForApplicationNotification(this)
        Appboy.getInstance(this).changeUser("bluedot_sdk_and_brazer_sdk_integration_android")
    }

    /**
     *
     * This method notifies the client application that BlueDotPointService is stopped. Your app could release the resources related to Bluedot service from here.
     *
     * It is called off the UI thread.
     */
    override fun onBlueDotPointServiceStop() {
        mServiceManager.unsubscribeForApplicationNotification(this)
    }

    /**
     *
     * The method delivers the error from BlueDotPointService by a generic BDError. There are several types of error such as
     * - BDAuthenticationError (fatal)
     * - BDNetworkError (fatal / non fatal)
     * - LocationServiceNotEnabledError (fatal / non fatal)
     * - RuleDownloadError (non fatal)
     * - BLENotAvailableError (non fatal)
     * - BluetoothNotEnabledError (non fatal)
     *
     *  The BDError.isFatal() indicates if error is fatal and service is not operable.
     * Followed by onBlueDotPointServiceStop() indicating service is stopped.
     *
     *  The BDError.getReason() is useful to analyse error cause.
     * @param bdError
     */
    override fun onBlueDotPointServiceError(bdError: BDError) {
        println("onBlueDotPointServiceError = $bdError")
    }

    /**
     *
     * The method deliveries the ZoneInfo list when the rules are updated. Your app is able to get the latest ZoneInfo when the rules are updated.
     * @param list
     */
    override fun onRuleUpdate(list: List<ZoneInfo>) {

    }

    /**
     * This callback happens when user is subscribed to Application Notification
     * and check into any fence under that Zone
     * @param fenceInfo      - Fence triggered
     * @param zoneInfo   - Zone information Fence belongs to
     * @param location   - geographical coordinate where trigger happened
     * @param customData - custom data associated with this Custom Action
     * @param isCheckOut - CheckOut will be tracked and delivered once device left the Fence
     */
    override fun onCheckIntoFence(
        fenceInfo: FenceInfo,
        zoneInfo: ZoneInfo,
        location: LocationInfo,
        customData: Map<String, String>?,
        isCheckOut: Boolean
    ) {
        val eventProperties = AppboyProperties()
        eventProperties.addProperty("zone_id", zoneInfo.zoneId)
        eventProperties.addProperty("zone_name", zoneInfo.zoneName)
        eventProperties.addProperty("latitude", location.latitude)
        eventProperties.addProperty("longitude", location.longitude)
        eventProperties.addProperty("fence_id", fenceInfo.id)
        eventProperties.addProperty("fence_name", fenceInfo.name)

        customData?.forEach { data ->
            eventProperties.addProperty(data.key, data.value)
        }

        Appboy.getInstance(this).logCustomEvent(customEventEntry, eventProperties)
    }

    /**
     * This callback happens when user is subscribed to Application Notification
     * and checked out from fence under that Zone
     * @param fenceInfo     - Fence user is checked out from
     * @param zoneInfo  - Zone information Fence belongs to
     * @param dwellTime - time spent inside the Fence; in minutes
     * @param customData - custom data associated with this Custom Action
     */
    override fun onCheckedOutFromFence(
        fenceInfo: FenceInfo,
        zoneInfo: ZoneInfo,
        dwellTime: Int,
        customData: Map<String, String>?
    ) {
        val eventProperties = AppboyProperties()
        eventProperties.addProperty("zone_id", zoneInfo.zoneId)
        eventProperties.addProperty("zone_name", zoneInfo.zoneName)
        eventProperties.addProperty("dwellTime", dwellTime)
        eventProperties.addProperty("fence_id", fenceInfo.id)
        eventProperties.addProperty("fence_name", fenceInfo.name)

        customData?.forEach { data ->
            eventProperties.addProperty(data.key, data.value)
        }

        Appboy.getInstance(this).logCustomEvent(customEventExit, eventProperties)
    }

    /**
     * This callback happens when user is subscribed to Application Notification
     * and check into any beacon under that Zone
     * @param beaconInfo - Beacon triggered
     * @param zoneInfo   - Zone information Beacon belongs to
     * @param location   - geographical coordinate where trigger happened
     * @param proximity  - the proximity at which the trigger occurred
     * @param customData - custom data associated with this Custom Action
     * @param isCheckOut - CheckOut will be tracked and delivered once device left the Beacon advertisement range
     */
    override fun onCheckIntoBeacon(
        beaconInfo: BeaconInfo,
        zoneInfo: ZoneInfo,
        location: LocationInfo,
        proximity: Proximity,
        customData: Map<String, String>?,
        isCheckOut: Boolean
    ) {
        val eventProperties = AppboyProperties()
        eventProperties.addProperty("zone_id", zoneInfo.zoneId)
        eventProperties.addProperty("zone_name", zoneInfo.zoneName)
        eventProperties.addProperty("beacon_id", beaconInfo.id)
        eventProperties.addProperty("latitude", location.latitude)
        eventProperties.addProperty("longitude", location.longitude)

        customData?.forEach { data ->
            eventProperties.addProperty(data.key, data.value)
        }

        Appboy.getInstance(this).logCustomEvent(customEventEntry, eventProperties)
    }

    /**
     * This callback happens when user is subscribed to Application Notification
     * and checked out from beacon under that Zone
     * @param beaconInfo - Beacon is checked out from
     * @param zoneInfo   - Zone information Beacon belongs to
     * @param dwellTime  - time spent inside the Beacon area; in minutes
     * @param customData - custom data associated with this Custom Action
     */
    override fun onCheckedOutFromBeacon(
        beaconInfo: BeaconInfo,
        zoneInfo: ZoneInfo,
        dwellTime: Int,
        customData: Map<String, String>?
    ) {
        val eventProperties = AppboyProperties()
        eventProperties.addProperty("zone_id", zoneInfo.zoneId)
        eventProperties.addProperty("zone_name", zoneInfo.zoneName)
        eventProperties.addProperty("beacon_id", beaconInfo.id)
        eventProperties.addProperty("dwellTime", dwellTime)

        customData?.forEach { data ->
            eventProperties.addProperty(data.key, data.value)
        }

        Appboy.getInstance(this).logCustomEvent(customEventExit, eventProperties)
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
                .setStyle(Notification.BigTextStyle().bigText(getString(R.string.foreground_notification_text)))
                .setOngoing(true)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setSmallIcon(R.mipmap.ic_launcher)

            return notification.build()
        } else {

            val notification = NotificationCompat.Builder(applicationContext, channelId)
                .setContentTitle(getString(R.string.foreground_notification_title))
                .setContentText(getString(R.string.foreground_notification_text))
                .setStyle(NotificationCompat.BigTextStyle().bigText(getString(R.string.foreground_notification_text)))
                .setOngoing(true)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setSmallIcon(R.mipmap.ic_launcher)

            return notification.build()
        }
    }
}
