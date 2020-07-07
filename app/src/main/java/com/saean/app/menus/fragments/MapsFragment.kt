package com.saean.app.menus.fragments

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import cn.pedant.SweetAlert.SweetAlertDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.compat.GeoDataClient
import com.google.android.libraries.places.compat.PlaceDetectionClient
import com.google.android.libraries.places.compat.Places
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.saean.app.R
import com.saean.app.helper.Cache
import com.saean.app.helper.MyFunctions

class MapsFragment : Fragment() {
    private lateinit var database: FirebaseDatabase
    private var sharedPreferences : SharedPreferences? = null
    private lateinit var mMap: GoogleMap
    private val defaultZoom = 16.0F //18.6F
    private var zoomToUser = 18.6F
    private var mGeoDataClient : GeoDataClient? = null
    private var mPlaceDetectionClient : PlaceDetectionClient? = null
    private var mFusedLocationProviderClient : FusedLocationProviderClient? = null
    private val markerWidth = 80
    private val markerHeight = 80
    private var myLocation : LatLng? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mGeoDataClient = Places.getGeoDataClient(activity!!)
        mPlaceDetectionClient = Places.getPlaceDetectionClient(activity!!)
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity!!)
        sharedPreferences = activity!!.getSharedPreferences(Cache.cacheName,0)
        database = FirebaseDatabase.getInstance()

        setupFunctions()
    }

    private fun setupFunctions() {
        setupMaps()
    }

    private fun setupMaps() {
        val mapFragment : SupportMapFragment = activity!!.supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        //mapFragment.getMapAsync(activity!!)
    }

    /**override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        mMap.uiSettings.isMyLocationButtonEnabled = false

        getMyLocation()
    }
    */

    private fun getMyLocation() {
        if(MyFunctions.getConnectivityStatus(activity!!)){
            if(MyFunctions.gpsCheck(activity!!)){
                val request = LocationRequest()
                //request.interval = 120000
                request.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                val client : FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity!!)
                if (ActivityCompat.checkSelfPermission(activity!!, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity!!, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return
                }
                client.requestLocationUpdates(request,object : LocationCallback() {
                    override fun onLocationResult(p0: LocationResult?) {
                        if(p0!!.lastLocation != null){
                            p0.lastLocation.latitude
                            p0.lastLocation.longitude

                            val position = LatLng(p0.lastLocation.latitude,p0.lastLocation.longitude)
                            myLocation = position

                            val email = MyFunctions.changeToUnderscore(sharedPreferences!!.getString(Cache.email,"")!!)
                            database.getReference("user/$email").addListenerForSingleValueEvent(object : ValueEventListener{
                                override fun onCancelled(error: DatabaseError) {

                                }

                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if(snapshot.child("userLocation").exists()){
                                        mMap.clear()
                                        if(snapshot.child("userPicture").getValue(String::class.java)!!.isNotEmpty()){
                                            Glide.with(activity!!)
                                                .asBitmap()
                                                .load(snapshot.child("userPicture").getValue(String::class.java)!!)
                                                .apply(RequestOptions.circleCropTransform())
                                                .into(object : CustomTarget<Bitmap>() {
                                                    override fun onLoadCleared(placeholder: Drawable?) {

                                                    }

                                                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                                        var bitmap = MyFunctions.getResizedBitmap(resource,markerHeight,markerWidth)
                                                        bitmap = MyFunctions.addBorderToCircularBitmap(bitmap!!,2,
                                                            Color.WHITE)
                                                        bitmap = MyFunctions.addShadowToCircularBitmap(bitmap!!,4, Color.LTGRAY)
                                                        mMap.addMarker(
                                                            MarkerOptions()
                                                                .position(position)
                                                                .title("Lokasi Saya")
                                                                .icon(BitmapDescriptorFactory.fromBitmap(bitmap)))
                                                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position,defaultZoom))
                                                        mMap.setOnInfoWindowClickListener {

                                                        }
                                                        mMap.setOnMarkerClickListener {
                                                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(it.position,zoomToUser))
                                                            it.showInfoWindow()
                                                            return@setOnMarkerClickListener true
                                                        }
                                                        showNearbyStore()
                                                    }
                                                })
                                        }else{
                                            Glide.with(activity!!)
                                                .asBitmap()
                                                .load("https://www.cobdoglaps.sa.edu.au/wp-content/uploads/2017/11/placeholder-profile-sq.jpg")
                                                .apply(RequestOptions.circleCropTransform())
                                                .into(object : CustomTarget<Bitmap>() {
                                                    override fun onLoadCleared(placeholder: Drawable?) {

                                                    }

                                                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                                        var bitmap = MyFunctions.getResizedBitmap(resource,markerHeight,markerWidth)
                                                        bitmap = MyFunctions.addBorderToCircularBitmap(bitmap!!,2,
                                                            Color.WHITE)
                                                        bitmap = MyFunctions.addShadowToCircularBitmap(bitmap!!,4, Color.LTGRAY)
                                                        mMap.addMarker(
                                                            MarkerOptions()
                                                                .position(position)
                                                                .title("Lokasi Saya")
                                                                .icon(BitmapDescriptorFactory.fromBitmap(bitmap)))
                                                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position,defaultZoom))
                                                        mMap.setOnInfoWindowClickListener {

                                                        }
                                                        mMap.setOnMarkerClickListener {
                                                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(it.position,zoomToUser))
                                                            it.showInfoWindow()
                                                            return@setOnMarkerClickListener true
                                                        }
                                                        showNearbyStore()
                                                    }
                                                })
                                        }
                                    }
                                }
                            })
                        }
                    }
                },null)
            }else{
                val dialog = SweetAlertDialog(activity,SweetAlertDialog.WARNING_TYPE)
                dialog.titleText = "Warning"
                dialog.contentText = "Please enable location services!"
                dialog.setCancelable(false)
                dialog.setConfirmClickListener {
                    dialog.dismissWithAnimation()
                    getMyLocation()
                }
                dialog.show()
            }
        }
    }

    private fun showNearbyStore(){

    }
}
