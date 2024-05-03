package bluedot.com.au.bluedotbrazeintegrationapp

import android.content.Context
import au.com.bluedot.point.net.engine.GeoTriggeringEventReceiver
import au.com.bluedot.point.net.engine.event.GeoTriggerEvent
import com.braze.Braze
import com.braze.models.outgoing.BrazeProperties
import java.util.Date

class BluedotGeoTriggerReceiver: GeoTriggeringEventReceiver() {
    private val customEventEntry = "bluedot_entry"
    private val customEventExit = "bluedot_exit"

    override fun onZoneEntryEvent(entryEvent: GeoTriggerEvent, context: Context) {
        println("Zone ${entryEvent.zoneInfo.name}, fence ${entryEvent.entryEvent()?.fenceName} entered at: ${Date()}")

        val eventProperties = BrazeProperties()
        eventProperties.addProperty("zone_id", entryEvent.zoneInfo.id.toString())
        eventProperties.addProperty("zone_name", entryEvent.zoneInfo.name)
        eventProperties.addProperty("latitude",
            entryEvent.entryEvent()?.locations?.get(0)?.latitude
        )
        eventProperties.addProperty("longitude", entryEvent.entryEvent()?.locations?.get(0)?.longitude)
        eventProperties.addProperty("fence_id", entryEvent.entryEvent()?.fenceId.toString())
        eventProperties.addProperty("fence_name", entryEvent.entryEvent()?.fenceName)

        entryEvent.zoneInfo.customData.forEach { data ->
            eventProperties.addProperty(data.key, data.value)
        }

        Braze.getInstance(context).logCustomEvent(customEventEntry, eventProperties)
    }

    override fun onZoneExitEvent(exitEvent: GeoTriggerEvent, context: Context) {
        println("Zone ${exitEvent.zoneInfo.name}, fence ${exitEvent.exitEvent()?.fenceName} exited at: ${Date()}")

        val eventProperties = BrazeProperties()
        eventProperties.addProperty("zone_id", exitEvent.zoneInfo.id.toString())
        eventProperties.addProperty("zone_name", exitEvent.zoneInfo.name)
        eventProperties.addProperty("dwellTime", exitEvent.exitEvent()?.dwellTime)
        eventProperties.addProperty("fence_id", exitEvent.exitEvent()?.fenceId.toString())
        eventProperties.addProperty("fence_name", exitEvent.exitEvent()?.fenceName)

        exitEvent.zoneInfo.customData.forEach { data ->
            eventProperties.addProperty(data.key, data.value)
        }

        Braze.getInstance(context).logCustomEvent(customEventExit, eventProperties)
    }

    override fun onZoneInfoUpdate(context: Context) {
    }
}