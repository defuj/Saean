package com.saean.app

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.saean.app.helper.Cache
import com.saean.app.helper.MyFunctions
import com.saean.app.helper.TrackingService
import com.saean.app.menus.MenusAdapter
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {
    private var sharedPreferences: SharedPreferences ?= null
    private lateinit var database: FirebaseDatabase
    private val permissionRequest = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContentView(R.layout.activity_home)
        database = FirebaseDatabase.getInstance()

        sharedPreferences = getSharedPreferences(Cache.cacheName,0)
        if(sharedPreferences!!.getBoolean(Cache.blocked,false)){
            startActivity(Intent(this, BlockedActivity::class.java))
            finish()
        }

        val checkBlocked = database.getReference("config/blocked")
        checkBlocked.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()){
                    if(p0.child("status").getValue(Boolean::class.java)!!){
                        val edit = sharedPreferences!!.edit()
                        edit.putBoolean(Cache.blocked, true)
                        edit.apply()

                        startActivity(Intent(this@HomeActivity,
                            BlockedActivity::class.java))
                        finish()
                    }else{
                        val edit = sharedPreferences!!.edit()
                        edit.putBoolean(Cache.blocked, false)
                        edit.apply()
                    }
                }
            }
        })

        setupGPS()
        setupOnlineStatus()
    }

    private fun setupOnlineStatus() {
        val email = MyFunctions.changeToUnderscore(sharedPreferences!!.getString(Cache.email,"")!!)
        val userOnlineStatus = database.getReference("user/$email").child("userOnlineStatus")
        userOnlineStatus.onDisconnect().setValue(false)

        val userOnlineTime = database.getReference("user/$email").child("userOnlineTime")
        userOnlineTime.onDisconnect().setValue(MyFunctions.getTime())

        userOnlineStatus.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val connected = snapshot.getValue(Boolean::class.java) ?: false
                    if(!connected){
                        database.getReference("user/$email").child("userOnlineStatus").setValue(true)
                    }
                }else{
                    database.getReference("user/$email").child("userOnlineStatus").setValue(true)
                    database.getReference("user/$email").child("userOnlineTime").setValue(MyFunctions.getTime())
                }
            }
        })
    }

    private fun setupGPS() {
        if(MyFunctions.gpsCheck(this)){
            if(MyFunctions.gpsPermissionCheck(this)){
                startTrackerService()
                setupFunctions()
            }else{
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION), permissionRequest)
            }
        }else{
            val dialog = SweetAlertDialog(this,SweetAlertDialog.WARNING_TYPE)
            dialog.titleText = "Warning"
            dialog.contentText = "Please enable location services!"
            dialog.setCancelable(false)
            dialog.setConfirmClickListener {
                dialog.dismissWithAnimation()
                setupGPS()
            }
            dialog.show()
        }
    }

    private fun startTrackerService() {
        startService(Intent(this, TrackingService::class.java))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray) {
        setupGPS()
    }

    private fun setupFunctions() {
        setupFragment()
    }

    private fun setupFragment() {
        menuBottomHome!!.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.menu_home -> pagerHome!!.setCurrentItem(0,false)
                R.id.menu_feed -> pagerHome!!.setCurrentItem(1,false)
                R.id.menu_cart -> pagerHome!!.setCurrentItem(2,false)
                R.id.menu_account -> pagerHome!!.setCurrentItem(3,false)
            }
            true
        }

        //load fragments
        val adapterHome = MenusAdapter(supportFragmentManager)
        pagerHome.adapter = adapterHome
        pagerHome.offscreenPageLimit = 4
    }

    override fun onBackPressed() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }
}