package com.example.myapplication

import android.content.Context
import android.content.res.AssetManager
import android.location.Location
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.google.android.gms.maps.model.LatLng
import java.io.InputStream
import kotlin.math.*

class LocationUtils(private val context: Context) {

    data class RestStop(val name: String, val latitude: Double, val longitude: Double)

    private val latLonList = mutableListOf<RestStop>()

    init {
        fileRead()
    }

    private fun fileRead() {
        val assetManager: AssetManager = context.assets
        val inputStream = assetManager.open("rest_stops1.csv")
        latLonList.clear()
        latLonList.addAll(readLatLonFromCsv(inputStream))
        Log.d("LocationUtils", latLonList.toString())
    }

    fun getLastLocation(): Task<Location> {
        val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
        return fusedLocationClient.lastLocation
    }

    fun findNearestRestStop(userLocation: LatLng): RestStop? {
        if (latLonList.isEmpty()) {
            return null
        }
        var nearestRestStop: RestStop? = null
        var nearestDistance = Double.MAX_VALUE

        for (restStop in latLonList) {
            val distance = calculateDistance(userLocation, LatLng(restStop.latitude, restStop.longitude))
            if (distance < nearestDistance) {
                nearestDistance = distance
                nearestRestStop = restStop
            }
        }

        return nearestRestStop
    }

    private fun readLatLonFromCsv(inputStream: InputStream): List<RestStop> {
        val latLonList = mutableListOf<RestStop>()

        inputStream.bufferedReader().useLines { lines ->
            val iterator = lines.iterator()
            if (iterator.hasNext()) iterator.next() // 헤더 행을 건너뜁니다.
            iterator.forEachRemaining { line ->
                val columns = line.split(",")
                try {
                    val name = columns[0] // 휴게소 이름이 첫번째 열에 있다고 가정합니다.
                    val latitude = columns[4].toDouble() // 위도가 5번째 열에 있다고 가정합니다.
                    val longitude = columns[5].toDouble() // 경도가 6번째 열에 있다고 가정합니다.
                    latLonList.add(RestStop(name, latitude, longitude))
                } catch (e: NumberFormatException) {
                    Log.e("LocationUtils", "Invalid number format in CSV: ${columns[4]}, ${columns[5]}")
                }
            }
        }
        return latLonList
    }

    private fun calculateDistance(location1: LatLng, location2: LatLng): Double {
        val r = 6371 // Earth's radius in kilometers
        val dLat = Math.toRadians(location2.latitude - location1.latitude)
        val dLon = Math.toRadians(location2.longitude - location1.longitude)
        val lat1 = Math.toRadians(location1.latitude)
        val lat2 = Math.toRadians(location2.latitude)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                sin(dLon / 2) * sin(dLon / 2) * cos(lat1) * cos(lat2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
