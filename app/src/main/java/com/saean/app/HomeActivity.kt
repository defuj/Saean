package com.saean.app

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.saean.app.helper.Cache
import com.saean.app.helper.MyFunctions
import com.saean.app.home.adapter.HomeAdapter
import com.saean.app.network.ApiServices
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {
    private var sharedPreferences: SharedPreferences ?= null
    private lateinit var database: FirebaseDatabase

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

        val checkBlocked = database.getReference("${ApiServices.clientID}/config/blocked")
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

                        setupFunctions()
                    }
                }
            }
        })
    }

    private fun setupFunctions() {
        val dialog = SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE)
        dialog.titleText = "Selamat Datang"
        dialog.contentText = "Terimakasih atas kunjungan Anda, silahkan kembali beberapa saat lagi."
        dialog.showCancelButton(true)
        dialog.setConfirmClickListener {
            dialog.dismissWithAnimation()
        }
        dialog.setOnDismissListener {
            dialog.dismissWithAnimation()
        }
        dialog.setCancelable(true)
        dialog.show()

        loadFragment()
    }

    private fun loadFragment() {
        menuBottomHome!!.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.menu_home -> pagerHome!!.setCurrentItem(0,false)
                R.id.menu_maps -> pagerHome!!.setCurrentItem(1,false)
                R.id.menu_cart -> {
                    if(MyFunctions.checkBoolean(this, Cache.logged)){
                        pagerHome!!.setCurrentItem(2,false)
                    }else{
                        startActivity(Intent(this,LoginActivity::class.java))
                        pagerHome!!.setCurrentItem(0,false)
                        menuBottomHome!!.selectedItemId = R.id.menu_home
                    }
                }
                R.id.menu_account -> {
                    if(MyFunctions.checkBoolean(this, Cache.logged)){
                        pagerHome!!.setCurrentItem(3,false)
                    }else{
                        startActivity(Intent(this,LoginActivity::class.java))
                        pagerHome!!.setCurrentItem(0,false)
                        menuBottomHome!!.selectedItemId = R.id.menu_home
                    }
                }
            }
            true
        }

        //load fragments
        val adapterHome = HomeAdapter(supportFragmentManager)
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