package bluedot.com.au.bluedotbrazeintegrationapp

import android.content.Context
import au.com.bluedot.point.net.engine.GeoTriggeringEventReceiver
import au.com.bluedot.point.net.engine.ZoneEntryEvent
import au.com.bluedot.point.net.engine.ZoneExitEvent
import au.com.bluedot.point.net.engine.ZoneInfo
import com.braze.Braze
import com.braze.models.outgoing.BrazeProperties
import java.util.*

class BluedotGeoTriggerReceiver: GeoTriggeringEventReceiver() {
    private val customEventEntry = "bluedot_entry"
    private val customEventExit = "bluedot_exit"

    override fun onZoneEntryEvent(entryEvent: ZoneEntryEvent, context: Context) {
        println("Zone ${entryEvent.zoneInfo.zoneName}, fence ${entryEvent.fenceInfo.name} entered at: ${Date()}")

        val eventProperties = BrazeProperties()
        eventProperties.addProperty("zone_id", entryEvent.zoneInfo.zoneId)
        eventProperties.addProperty("zone_name", entryEvent.zoneInfo.zoneName)
        eventProperties.addProperty("latitude", entryEvent.locationInfo.latitude)
        eventProperties.addProperty("longitude", entryEvent.locationInfo.longitude)
        eventProperties.addProperty("fence_id", entryEvent.fenceInfo.id)
        eventProperties.addProperty("fence_name", entryEvent.fenceInfo.name)

        entryEvent.zoneInfo.getCustomData()?.forEach { data ->
            eventProperties.addProperty(data.key, data.value)
        }

        Braze.getInstance(context).logCustomEvent(customEventEntry, eventProperties)
    }

    override fun onZoneExitEvent(exitEvent: ZoneExitEvent, context: Context) {
        println("Zone ${exitEvent.zoneInfo.zoneName}, fence ${exitEvent.fenceInfo.name} exited at: ${Date()}")

        val eventProperties = BrazeProperties()
        eventProperties.addProperty("zone_id", exitEvent.zoneInfo.zoneId)
        eventProperties.addProperty("zone_name", exitEvent.zoneInfo.zoneName)
        eventProperties.addProperty("dwellTime", exitEvent.dwellTime)
        eventProperties.addProperty("fence_id", exitEvent.fenceInfo.id)
        eventProperties.addProperty("fence_name", exitEvent.fenceInfo.name)

        exitEvent.zoneInfo.getCustomData()?.forEach { data ->
            eventProperties.addProperty(data.key, data.value)
        }

        Braze.getInstance(context).logCustomEvent(customEventExit, eventProperties)
    }

    override fun onZoneInfoUpdate(zones: List<ZoneInfo>, context: Context) {
        println("Zones updated at: ${Date()} \nZoneInfos count: ${zones.count()}")
    }
}