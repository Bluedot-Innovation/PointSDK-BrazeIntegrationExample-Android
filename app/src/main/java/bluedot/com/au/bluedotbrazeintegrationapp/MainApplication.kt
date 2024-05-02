package bluedot.com.au.bluedotbrazeintegrationapp

import android.Manifest
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import au.com.bluedot.point.net.engine.BDError
import au.com.bluedot.point.net.engine.GeoTriggeringStatusListener
import au.com.bluedot.point.net.engine.InitializationResultListener
import au.com.bluedot.point.net.engine.ServiceManager
import com.braze.Braze
import com.braze.BrazeActivityLifecycleCallbackListener

/*
 * @author Bluedot Innovation
 * Copyright (c) 2019 Bluedot Innovation. All rights reserved.
 * MainApplication demonstrates the implementation Bluedot Point SDK and related callbacks.
 */
class MainApplication : Application(), InitializationResultListener, GeoTriggeringStatusListener {

    private lateinit var mServiceManager: ServiceManager

    // BrazeApp
    private val projectId = "" //project Id for the App
    // set this to true if you want to start the SDK with service sticky and auto-start mode on boot complete.
    // Please refer to Bluedot Developer documentation for further information.

    override fun onCreate() {
        super.onCreate()

        registerActivityLifecycleCallbacks(BrazeActivityLifecycleCallbackListener())

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

    override fun onInitializationFinished(error: BDError?) {
        if (error != null) {
            Toast.makeText(
                applicationContext,
                "Bluedot Point SDK initialization error: ${error.reason}",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        Braze.getInstance(this).changeUser("bluedot_sdk_and_brazer_sdk_integration_android")
        println("Bluedot Point SDK authenticated")
    }

    override fun onGeoTriggeringResult(error: BDError?) {
        if (error != null) {
            Toast.makeText(
                applicationContext,
                "Bluedot Point SDK start GeoTriggering error: ${error.reason}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
