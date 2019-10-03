package bluedot.com.au.bluedotbrazeintegrationapp

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import au.com.bluedot.point.net.engine.ServiceManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.bStopSDK).setOnClickListener {
            ServiceManager.getInstance(this).stopPointService()
            finish()
        }
    }
}
