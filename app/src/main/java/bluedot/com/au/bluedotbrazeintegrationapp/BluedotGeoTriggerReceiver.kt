package bluedot.com.au.bluedotbrazeintegrationapp

import android.content.Context
import au.com.bluedot.point.net.engine.GeoTriggeringEventReceiver
import au.com.bluedot.point.net.engine.ZoneInfo
import au.com.bluedot.point.net.engine.event.GeoTriggerEvent
import com.braze.Braze
import com.braze.models.outgoing.BrazeProperties
import java.util.*

class BluedotGeoTriggerReceiver: GeoTriggeringEventReceiver() {
    private val customEventEntry = "bluedot_entry"
    private val customEventExit = "bluedot_exit"

    override fun onZoneEntryEvent(geoTriggerEvent: GeoTriggerEvent, context: Context) {
        println("Zone ${geoTriggerEvent.zoneInfo.name}, fence ${geoTriggerEvent.entryEvent()?.fenceName} entered at: ${Date()}")

        val eventProperties = BrazeProperties()
        eventProperties.addProperty("zone_id", geoTriggerEvent.zoneInfo.id)
        eventProperties.addProperty("zone_name", geoTriggerEvent.zoneInfo.name)
        eventProperties.addProperty("latitude",
            geoTriggerEvent.entryEvent()?.locations?.get(0)?.latitude
        )
        eventProperties.addProperty("longitude", geoTriggerEvent.entryEvent()?.locations?.get(0)?.longitude)
        eventProperties.addProperty("fence_id", geoTriggerEvent.entryEvent()?.fenceId)
        eventProperties.addProperty("fence_name", geoTriggerEvent.entryEvent()?.fenceName)

        geoTriggerEvent.zoneInfo.customData?.forEach { data ->
            eventProperties.addProperty(data.key, data.value)
        }

        Braze.getInstance(context).logCustomEvent(customEventEntry, eventProperties)
    }

    override fun onZoneExitEvent(geoTriggerEvent: GeoTriggerEvent, context: Context) {
        println("Zone ${geoTriggerEvent.zoneInfo.name}, fence ${geoTriggerEvent.exitEvent()?.fenceName} exited at: ${Date()}")

        val eventProperties = BrazeProperties()
        eventProperties.addProperty("zone_id", geoTriggerEvent.zoneInfo.id)
        eventProperties.addProperty("zone_name", geoTriggerEvent.zoneInfo.name)
        eventProperties.addProperty("dwellTime", geoTriggerEvent.exitEvent()?.dwellTime)
        eventProperties.addProperty("fence_id", geoTriggerEvent.exitEvent()?.fenceId)
        eventProperties.addProperty("fence_name", geoTriggerEvent.exitEvent()?.fenceName)

        geoTriggerEvent.zoneInfo.customData.forEach { data ->
            eventProperties.addProperty(data.key, data.value)
        }

        Braze.getInstance(context).logCustomEvent(customEventExit, eventProperties)
    }

    override fun onZoneInfoUpdate(context: Context) {
    }
}