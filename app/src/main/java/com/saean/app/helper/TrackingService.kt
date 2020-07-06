package com.saean.app.helper

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.firebase.database.FirebaseDatabase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TrackingService : Service() {

    override fun onCreate() {
        super.onCreate()
        requestLocationUpdates()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun requestLocationUpdates() {
        val sharedPreferences : SharedPreferences = getSharedPreferences(Cache.cacheName,0)
        val request = LocationRequest()
        request.interval = 6000
        request.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val client : FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        if(permission == PackageManager.PERMISSION_GRANTED) {
            client.requestLocationUpdates(request,object : LocationCallback() {
                override fun onLocationResult(p0: LocationResult?) {
                    if(p0!!.lastLocation != null){
                        val edit = sharedPreferences.edit()
                        edit.putString(Cache.latitude,p0.lastLocation.latitude.toString())
                        edit.putString(Cache.longitude,p0.lastLocation.longitude.toString())
                        edit.apply()
                    }
                }
            },null)
        }
    }
}
