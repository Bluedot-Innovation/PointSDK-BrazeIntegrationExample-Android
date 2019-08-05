# Braze Custom Event Example
This is a sample app which integrates Bluedot SDK and Braze SDK using Kotlin.

## Getting Started

### To add Bluedot SDK
Step 1: In the root gradle add `maven { url 'https://jitpack.io' } maven { url "https://appboy.github.io/appboy-android-sdk/sdk" }` under the repositories section.

Step 2: In the app gradle add
```
implementation "org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version"
implementation 'com.github.Bluedot-Innovation:PointSDK-Android:1.13.2'
implementation 'com.google.firebase:firebase-messaging:19.0.1'
implementation 'com.google.firebase:firebase-core:17.0.0'
```
under the dependencies.

Step 3: In the same file add `apply plugin: 'com.google.gms.google-services'` at the end of the file.

Step 4: In the AndroidManifest.xml add `<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>`
In the same file add `<activity android:name="bluedot.com.au.bluedotbrazeintegrationapp.RequestPermissionActivity"/>`

### To add Braze SDK
Step 1: In the root gradle add `maven { url "https://appboy.github.io/appboy-android-sdk/sdk" }`

Step 2: In the app gradle add
```
implementation "com.appboy:android-sdk-ui:+"
```

Step 3: Create appboy.xml and add the following code:
```
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="com_appboy_api_key">REPLACE_WITH_YOUR_API_KEY</string>
    <string translatable="false" name="com_appboy_custom_endpoint">sdk.iad-03.braze.com</string>
    <bool translatable="false" name="com_appboy_firebase_cloud_messaging_registration_enabled">true</bool>
    <string translatable="false" name="com_appboy_firebase_cloud_messaging_sender_id">your_fcm_sender_id_here</string>
    <drawable name="com_appboy_push_small_notification_icon">@drawable/ic_stat_notify_droidboy</drawable>
    <drawable name="com_appboy_push_large_notification_icon">@drawable/ic_stat_notify_droidboy_large</drawable>
    <integer name="com_appboy_default_notification_accent_color">0xFFf33e3e</integer>
    <bool name="com_appboy_handle_push_deep_links_automatically">true</bool>
</resources>
```

Step 4: In the AndroidManifest.xml add the following:
```
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<service android:name="com.appboy.AppboyFirebaseMessagingService">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT" />
    </intent-filter>
</service>
```

### MainActivity.kt
Add the below code in the MainActivity.kt
```
lateinit var bStopSDK: Button

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    initUI()
}

private fun initUI() {
    bStopSDK = findViewById(R.id.bStopSDK)
    bStopSDK.setOnClickListener {
        stopSDK()
    }
}

private fun stopSDK() {
    ServiceManager.getInstance(this).stopPointService()
    finish()
}
```

### RequestPermissionActivity.kt
Add the below code in the RequestPermissionActivity.kt
```
internal val PERMISSION_REQUEST_CODE = 1
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    //Request permission required for location
    ActivityCompat.requestPermissions(
        this,
        arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
        PERMISSION_REQUEST_CODE
    )
}

override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    when (requestCode) {
        PERMISSION_REQUEST_CODE -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            (application as MainApplication).initPointSDK()

        } else {
            //Permissions denied

        }
    }
    finish()
}
```

### MainApplication.kt
Create an application in Bluedot and then add the below code in MainApplication.kt
```
internal lateinit var mServiceManager: ServiceManager

    private val apiKey = Bluedot API key for the App 
    internal var restartMode = true
    private val customEventEntry = "bluedot_entry"
    private val customEventExit = "bluedot_exit"

    override fun onCreate() {
        super.onCreate()

        registerActivityLifecycleCallbacks(AppboyLifecycleCallbackListener())

        // initialize Bluedot point sdk
        initPointSDK()
    }

    fun initPointSDK() {

        val checkPermissionCoarse =
            ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION)
        val checkPermissionFine =
            ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)

        if (checkPermissionCoarse == PackageManager.PERMISSION_GRANTED && checkPermissionFine == PackageManager.PERMISSION_GRANTED) {
            mServiceManager = ServiceManager.getInstance(this)

            if (!mServiceManager.isBlueDotPointServiceRunning()) {
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
        customData: Map<String, String>,
        isCheckOut: Boolean
    ) {
        val eventProperties = AppboyProperties()
        eventProperties.addProperty("zone_id", zoneInfo.zoneId)
        eventProperties.addProperty("zone_name", zoneInfo.zoneName)
        eventProperties.addProperty("latitude", location.latitude)
        eventProperties.addProperty("longitude", location.longitude)
        eventProperties.addProperty("fence_id", fenceInfo.id)
        eventProperties.addProperty("fence_name", fenceInfo.name)

        for (data in customData) {
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
        customData: Map<String, String>
    ) {
        val eventProperties = AppboyProperties()
        eventProperties.addProperty("zone_id", zoneInfo.zoneId)
        eventProperties.addProperty("zone_name", zoneInfo.zoneName)
        eventProperties.addProperty("dwellTime", dwellTime)
        eventProperties.addProperty("fence_id", fenceInfo.id)
        eventProperties.addProperty("fence_name", fenceInfo.name)

        for (data in customData) {
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
        customData: Map<String, String>,
        isCheckOut: Boolean
    ) {
        val eventProperties = AppboyProperties()
        eventProperties.addProperty("zone_id", zoneInfo.zoneId)
        eventProperties.addProperty("zone_name", zoneInfo.zoneName)
        eventProperties.addProperty("beacon_id", beaconInfo.id)
        eventProperties.addProperty("latitude", location.latitude)
        eventProperties.addProperty("longitude", location.longitude)

        for (data in customData) {
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
        customData: Map<String, String>
    ) {
        val eventProperties = AppboyProperties()
        eventProperties.addProperty("zone_id", zoneInfo.zoneId)
        eventProperties.addProperty("zone_name", zoneInfo.zoneName)
        eventProperties.addProperty("beacon_id", beaconInfo.id)
        eventProperties.addProperty("dwellTime", dwellTime)

        for (data in customData) {
            eventProperties.addProperty(data.key, data.value)
        }

        Appboy.getInstance(this).logCustomEvent(customEventExit, eventProperties)
    }

    /**
     * Creates notification channel and notification, required for foreground service notification.
     * @return notification
     */

    private fun createNotification(): Notification {
        val channelId: String
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = "Bluedot" + getString(R.string.app_name)
            val channelName = "Bluedot Service" + getString(R.string.app_name)
            val notificationChannel =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            notificationChannel.enableLights(false)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(false)
            val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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

            val notification = NotificationCompat.Builder(applicationContext)
                .setContentTitle(getString(R.string.foreground_notification_title))
                .setContentText(getString(R.string.foreground_notification_text))
                .setStyle(NotificationCompat.BigTextStyle().bigText(getString(R.string.foreground_notification_text)))
                .setOngoing(true)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setPriority(PRIORITY_MAX)
                .setSmallIcon(R.mipmap.ic_launcher)

            return notification.build()
        }
    }

    companion object {
        private val TAG = "BDApp"
    }
```