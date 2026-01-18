package space.hvoal.ecologyassistant.ui.map

import ru.dgis.sdk.coordinates.GeoPoint
import ru.dgis.sdk.geometry.GeoPointWithElevation
import ru.dgis.sdk.map.CameraPosition
import ru.dgis.sdk.map.Image
import ru.dgis.sdk.map.Map
import ru.dgis.sdk.map.MapObjectManager
import ru.dgis.sdk.map.MapView
import ru.dgis.sdk.map.MarkerOptions
import ru.dgis.sdk.map.Zoom
import space.hvoal.ecologyassistant.R

object DgisInterop {

    @JvmStatic
    fun createObjectManager(map: Map): MapObjectManager {
        return MapObjectManager(map)
    }

    @JvmStatic
    fun point(lat: Double, lng: Double): GeoPointWithElevation {
        return GeoPointWithElevation(
            latitude = lat,
            longitude = lng
        )
    }

    @JvmStatic
    fun markerOptions(point: GeoPointWithElevation, userData: Any?): MarkerOptions {
        return MarkerOptions(
            position = point,
            icon = null,
            text = "Хуй",
            userData = userData
        )
    }

    @JvmStatic
    fun moveCamera(map: Map, lat: Double, lng: Double, zoom: Float) {
        val pos = CameraPosition(
            GeoPoint(lat, lng),
            Zoom(zoom)
        )
        // так надёжнее, чем move(), и не требует анимационных параметров
        map.camera.position = pos

    }

    @JvmStatic
    fun cameraCenter(map: ru.dgis.sdk.map.Map): DoubleArray {
        val p = map.camera.position.point
        // Latitude / Longitude -> Double
        val lat = p.latitude.value
        val lng = p.longitude.value
        return doubleArrayOf(lat, lng)
    }
}
