package bluedot.com.au.bluedotbrazeintegrationapp

import android.content.Context
import android.widget.Toast
import au.com.bluedot.point.net.engine.BDError
import au.com.bluedot.point.net.engine.BluedotServiceReceiver

class BluedotErrorReceiver: BluedotServiceReceiver() {
    override fun onBluedotServiceError(error: BDError, context: Context) {
        Toast.makeText(
            context, "Bluedot Service Error " + error.getReason(),
            Toast.LENGTH_LONG
        ).show()
    }

}