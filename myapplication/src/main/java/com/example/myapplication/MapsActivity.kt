package com.example.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.myapplication.databinding.ActivityMapsBinding
import java.io.InputStream
import kotlin.math.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // List to hold RestStop objects
    private val latLonList = mutableListOf<RestStop>()

    // Class to hold rest stop information
    data class RestStop(val name: String, val latitude: Double, val longitude: Double)

    // 권한 변수
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
                    permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                enableMyLocation()
            }
            else -> {
                Log.e("MapsActivity", "Location permission not granted")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fileRead()

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // 권한 확인 및 요청
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionRequest.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        } else {
            enableMyLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        mMap.isMyLocationEnabled = true
        val locationRequest = LocationRequest.create().apply {
            interval = 100000000
            fastestInterval = 50000000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    mMap.clear() // Clear existing markers
                    mMap.addMarker(MarkerOptions().position(currentLatLng).title("Current Location"))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 10f))
                    findNearestRestStop(currentLatLng)?.let { nearestRestStop ->
                        val restStopLatLng = LatLng(nearestRestStop.latitude, nearestRestStop.longitude)
                        mMap.addMarker(MarkerOptions().position(restStopLatLng).title(nearestRestStop.name))
                    }
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun fileRead() {
        val assetManager: AssetManager = applicationContext.assets
        val inputStream = assetManager.open("rest_stops1.csv")
        latLonList.clear() // Clear the existing list before reading new data
        latLonList.addAll(readLatLonFromCsv(inputStream))
        Log.d("LatLonList", latLonList.toString())
    }

    private fun findNearestRestStop(userLocation: LatLng): RestStop? {
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
                    Log.e("MapsActivity", "Invalid number format in CSV: ${columns[4]}, ${columns[5]}")
                    // continue to next line if there's a number format exception
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
