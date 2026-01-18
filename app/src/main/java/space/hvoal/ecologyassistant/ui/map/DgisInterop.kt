package space.hvoal.ecologyassistant.ui.map

import android.util.Size
import ru.dgis.sdk.Context
import ru.dgis.sdk.coordinates.GeoPoint
import ru.dgis.sdk.geometry.GeoPointWithElevation
import ru.dgis.sdk.map.CameraPosition
import ru.dgis.sdk.map.Map
import ru.dgis.sdk.map.MapObjectManager
import ru.dgis.sdk.map.MarkerOptions
import ru.dgis.sdk.map.Zoom
import ru.dgis.sdk.map.imageFromResource
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

    // --- NEW: отдельная иконка для проектов ---
    @JvmStatic
    fun markerOptionsProject(sdkContext: Context, point: GeoPointWithElevation, userData: Any?): MarkerOptions {
        val icon = imageFromResource(
            sdkContext,
            R.drawable.ic_pin, // текущая "старая" иконка -> оставим для проектов
            Size(72, 72)
        )

        return MarkerOptions(
            position = point,
            icon = icon,
            text = null,
            userData = userData
        )
    }

    // --- NEW: отдельная иконка для парков ---
    @JvmStatic
    fun markerOptionsPark(sdkContext: Context, point: GeoPointWithElevation, userData: Any?): MarkerOptions {
        val icon = imageFromResource(
            sdkContext,
            R.drawable.smart_ecology_eco_nature_world_icon_,
            Size(72, 72)
        )

        return MarkerOptions(
            position = point,
            icon = icon,
            text = null,
            userData = userData
        )
    }

    @JvmStatic
    fun markerOptions(sdkContext: Context, point: GeoPointWithElevation, userData: Any?): MarkerOptions {
        return markerOptionsProject(sdkContext, point, userData)
    }

    @JvmStatic
    fun markerOptions(point: GeoPointWithElevation, userData: Any?): MarkerOptions {
        return MarkerOptions(
            position = point,
            icon = null,
            text = null,
            userData = userData
        )
    }

    @JvmStatic
    fun moveCamera(map: Map, lat: Double, lng: Double, zoom: Float) {
        val pos = CameraPosition(
            GeoPoint(lat, lng),
            Zoom(zoom)
        )
        map.camera.position = pos
    }

    @JvmStatic
    fun cameraCenter(map: Map): DoubleArray {
        val p = map.camera.position.point
        val lat = p.latitude.value
        val lng = p.longitude.value
        return doubleArrayOf(lat, lng)
    }
}
