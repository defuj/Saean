package com.saean.app.createStore.fragments

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.saean.app.R
import com.saean.app.createStore.CreateStoreActivity
import com.saean.app.helper.Cache
import com.saean.app.helper.MyFunctions
import kotlinx.android.synthetic.main.fragment_create_store2.*


class CreateStore2Fragment : Fragment() {
    private var sharedPreferences : SharedPreferences? = null
    private lateinit var mMap: GoogleMap
    private lateinit var googleMap: GoogleMap
    private val defaultZoom = 18.6F //18.6F

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_store2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferences = activity!!.getSharedPreferences(Cache.cacheName,0)
        MapsInitializer.initialize(activity!!)
        setupMaps()
        setupFunctions()
    }

    private fun setupMaps() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapView2) as SupportMapFragment
        mapFragment.getMapAsync{
            googleMap = it
            googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            googleMap.uiSettings.isMyLocationButtonEnabled = true

            if(sharedPreferences!!.getString(Cache.latitude,"")!!.isNotEmpty()){
                val latitude = sharedPreferences!!.getString(Cache.latitude,"")!!.toDouble()
                val longitude = sharedPreferences!!.getString(Cache.longitude,"")!!.toDouble()

                (activity as CreateStoreActivity).storeLatitude = latitude
                (activity as CreateStoreActivity).storeLongitude = longitude

                var position = LatLng(latitude,longitude)
                googleMap.clear()
                val marker = googleMap.addMarker(
                    MarkerOptions()
                        .position(position)
                        .draggable(true)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
                marker.isVisible = false
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position,defaultZoom))
                googleMap.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener{
                    override fun onMarkerDragEnd(p0: Marker?) {
                        (activity as CreateStoreActivity).storeLatitude = p0!!.position.latitude
                        (activity as CreateStoreActivity).storeLongitude =  p0.position.longitude
                    }

                    override fun onMarkerDragStart(p0: Marker?) {

                    }

                    override fun onMarkerDrag(p0: Marker?) {

                    }
                })

                googleMap.setOnCameraMoveListener {
                    position = googleMap.cameraPosition.target
                    marker.position = position
                    (activity as CreateStoreActivity).storeLatitude = position.latitude
                    (activity as CreateStoreActivity).storeLongitude =  position.longitude
                }

                getMyLocation.setOnClickListener {
                    val request = LocationRequest()
                    request.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                    val client : FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity!!)
                    if (ActivityCompat.checkSelfPermission(activity!!, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity!!, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return@setOnClickListener
                    }
                    client.requestLocationUpdates(request,object : LocationCallback() {
                        override fun onLocationResult(p0: LocationResult?) {
                            if(p0!!.lastLocation != null){
                                position = LatLng(p0.lastLocation.latitude,p0.lastLocation.longitude)
                                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position,defaultZoom))
                                marker.position = position
                                (activity as CreateStoreActivity).storeLatitude = position.latitude
                                (activity as CreateStoreActivity).storeLongitude =  position.longitude
                            }
                        }
                    },null)
                }
            }
        }
    }

    private fun setupFunctions() {
        toolbarCreateStore2.setNavigationOnClickListener {
            (activity as CreateStoreActivity).setCurrentItem(0)
        }

        setupForm()
    }

    private fun setupForm() {
        storeAddress.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                if(s!!.isNotEmpty()){
                    (activity as CreateStoreActivity).storeAddress = s.toString()
                    btnNext2.setBackgroundResource(R.drawable.background_button_create_store_active)
                    btnNext2.isEnabled = true
                }else{
                    btnNext2.setBackgroundResource(R.drawable.background_button_create_store_disabled)
                    btnNext2.isEnabled = false
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })

        btnNext2.setOnClickListener {
            if(btnNext2.isEnabled){
                (activity as CreateStoreActivity).createStore()
            }
        }
    }
}