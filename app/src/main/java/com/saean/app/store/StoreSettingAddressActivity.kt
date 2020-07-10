package com.saean.app.store

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.core.app.ActivityCompat
import cn.pedant.SweetAlert.SweetAlertDialog
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
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.saean.app.R
import com.saean.app.helper.Cache
import kotlinx.android.synthetic.main.activity_store_setting_address.*
import kotlinx.android.synthetic.main.activity_store_setting_address.storeAddress

class StoreSettingAddressActivity : AppCompatActivity() {
    private var sharedPreferences : SharedPreferences? = null
    private lateinit var googleMap: GoogleMap
    private val defaultZoom = 18.6F

    private lateinit var database: FirebaseDatabase
    private lateinit var storage : FirebaseStorage
    private lateinit var storageReference : StorageReference

    private var storeLatitude = 0.0
    private var storeLongitude = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store_setting_address)
        sharedPreferences = getSharedPreferences(Cache.cacheName,0)
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference
        MapsInitializer.initialize(this)
        setupMaps()
        setupFunctions()
    }

    private fun setupMaps() {
        val storeID = sharedPreferences!!.getString(Cache.storeID,"")
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapView2) as SupportMapFragment
        mapFragment.getMapAsync{
            googleMap = it
            googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            googleMap.uiSettings.isMyLocationButtonEnabled = true

            database.getReference("store/$storeID/storeInfo").addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        val latitude = snapshot.child("storeLocation").child("latitude").getValue(Double::class.java)
                        val longitude = snapshot.child("storeLocation").child("longitude").getValue(Double::class.java)
                        storeAddress.setText(snapshot.child("storeAddress").getValue(String::class.java))

                        storeLatitude = latitude!!
                        storeLongitude = longitude!!

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
                                storeLatitude = p0!!.position.latitude
                                storeLongitude =  p0.position.longitude
                            }

                            override fun onMarkerDragStart(p0: Marker?) {

                            }

                            override fun onMarkerDrag(p0: Marker?) {

                            }
                        })

                        googleMap.setOnCameraMoveListener {
                            position = googleMap.cameraPosition.target
                            marker.position = position
                            storeLatitude = position.latitude
                            storeLongitude =  position.longitude
                        }

                        getStoreLocation.setOnClickListener {
                            val request = LocationRequest()
                            request.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                            val client : FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this@StoreSettingAddressActivity)
                            if (ActivityCompat.checkSelfPermission(this@StoreSettingAddressActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this@StoreSettingAddressActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                return@setOnClickListener
                            }
                            client.requestLocationUpdates(request,object : LocationCallback() {
                                override fun onLocationResult(p0: LocationResult?) {
                                    if(p0!!.lastLocation != null){
                                        position = LatLng(p0.lastLocation.latitude,p0.lastLocation.longitude)
                                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position,defaultZoom))
                                        marker.position = position
                                        storeLatitude = position.latitude
                                        storeLongitude =  position.longitude
                                    }
                                }
                            },null)
                        }
                    }
                }
            })
        }
    }

    private fun setupFunctions() {
        toolbarUpdateAddress.setNavigationOnClickListener {
            finish()
        }

        setupForm()
    }

    private fun setupForm() {
        val storeID = sharedPreferences!!.getString(Cache.storeID,"")

        storeAddress.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if(s!!.isNotEmpty()){
                    if(storeLatitude != 0.0 && storeLongitude != 0.0){
                        btnSetLocation.setBackgroundResource(R.drawable.background_button_create_store_active)
                        btnSetLocation.isEnabled = true
                    }else{
                        btnSetLocation.setBackgroundResource(R.drawable.background_button_create_store_disabled)
                        btnSetLocation.isEnabled = false
                    }
                }else{
                    btnSetLocation.setBackgroundResource(R.drawable.background_button_create_store_disabled)
                    btnSetLocation.isEnabled = false
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })

        btnSetLocation.setOnClickListener {
            if(btnSetLocation.isEnabled){
                val progress = SweetAlertDialog(this,SweetAlertDialog.PROGRESS_TYPE)
                progress.titleText = "Menyimpan Perubahan"
                progress.setCancelable(false)
                progress.show()

                database.getReference("store/$storeID/storeInfo").child("storeAddress").setValue(storeAddress.text.toString())
                database.getReference("store/$storeID/storeInfo").child("storeLocation").child("latitude").setValue(storeLatitude)
                database.getReference("store/$storeID/storeInfo").child("storeLocation").child("longitude").setValue(storeLongitude)

                progress.dismissWithAnimation()

                val dialog = SweetAlertDialog(this,SweetAlertDialog.SUCCESS_TYPE)
                dialog.titleText = "Berhasil"
                dialog.contentText = "Perubahan telah disimpan"
                dialog.show()
            }
        }
    }

}