package ch.epfl.sdp.searcharea

import com.mapbox.mapboxsdk.geometry.LatLng

class QuadrilateralArea(override val vertices: List<LatLng>) : SearchArea {
    init {
        require(vertices.size == 4)
    }
}