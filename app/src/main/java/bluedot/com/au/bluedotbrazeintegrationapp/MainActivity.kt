package bluedot.com.au.bluedotbrazeintegrationapp

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import au.com.bluedot.point.net.engine.ServiceManager

class MainActivity : AppCompatActivity() {

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
}
