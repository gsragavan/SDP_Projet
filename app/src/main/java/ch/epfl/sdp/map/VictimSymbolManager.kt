package ch.epfl.sdp.map

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.Observer
import ch.epfl.sdp.MainApplication
import ch.epfl.sdp.R
import ch.epfl.sdp.database.data.MarkerData
import com.google.gson.JsonObject
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.Symbol
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import com.mapbox.mapboxsdk.style.layers.Property

class VictimSymbolManager(mapView: MapView, mapboxMap: MapboxMap, style: Style, onMarkerRemove: (String) -> Unit, onLongClickConsumed: () -> Unit) : Observer<Set<MarkerData>> {

    private var symbolManager: SymbolManager = SymbolManager(mapView, mapboxMap, style)

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val markers = mutableMapOf<String, Symbol>()

    companion object {
        private const val ID_ICON_VICTIM: String = "airport"
        private const val VICTIM_MARKER_ID_PROPERTY_NAME = "id"
    }

    init {
        symbolManager.iconAllowOverlap = true
        symbolManager.symbolSpacing = 0F
        symbolManager.iconIgnorePlacement = true
        symbolManager.iconRotationAlignment = Property.ICON_ROTATION_ALIGNMENT_VIEWPORT

        symbolManager.addLongClickListener {
            onLongClickConsumed()
            val markerId = it.data!!.asJsonObject.get(VICTIM_MARKER_ID_PROPERTY_NAME).asString
            onMarkerRemove(markerId)
        }

        style.addImage(ID_ICON_VICTIM, MainApplication.applicationContext().getDrawable(R.drawable.ic_victim)!!)
    }

    override fun onChanged(markers: Set<MarkerData>) {
        val removedMarkers = this.markers.keys - markers.map { it.uuid }
        removedMarkers.forEach {
            symbolManager.delete(this.markers.remove(it))
        }
        markers.filter { !this.markers.containsKey(it.uuid) }.forEach {
            addVictimMarker(it.location!!, it.uuid!!)
        }
    }

    fun onDestroy() {
        symbolManager.onDestroy()
    }

    fun layerId(): String {
        return symbolManager.layerId
    }

    private fun addVictimMarker(latLng: LatLng, markerId: String) {
        val markerProperties = JsonObject()
        markerProperties.addProperty(VICTIM_MARKER_ID_PROPERTY_NAME, markerId)
        val symbolOptions = SymbolOptions()
                .withLatLng(LatLng(latLng))
                .withIconImage(ID_ICON_VICTIM)
                .withData(markerProperties)
        markers[markerId] = symbolManager.create(symbolOptions)
    }
}