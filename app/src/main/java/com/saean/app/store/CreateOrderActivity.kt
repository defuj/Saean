package com.saean.app.store

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.saean.app.R
import com.saean.app.helper.Cache
import com.saean.app.helper.MyFunctions
import kotlinx.android.synthetic.main.activity_create_order.*

class CreateOrderActivity : AppCompatActivity() {
    private lateinit var database: FirebaseDatabase
    private var sharedPreferences : SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_order)
        database = FirebaseDatabase.getInstance()
        sharedPreferences = getSharedPreferences(Cache.cacheName,0)

        val serviceID = intent.getStringExtra("serviceID")
        val storeID = intent.getStringExtra("storeID")

        setupFunctions()
    }

    private fun setupFunctions() {
        toolbarCreateOrder.setNavigationOnClickListener {
            finish()
        }

        orderTime.text = MyFunctions.getTanggal("dd/MM/yyyy")

        setupInformation()
    }

    private fun setupInformation() {
        val serviceID = intent.getStringExtra("serviceID")
        val storeID = intent.getStringExtra("storeID")

        database.getReference("product/service/$storeID/$serviceID").addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                finish()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    serviceTitle.text = snapshot.child("serviceName").getValue(String::class.java)
                    serviceDescription.text = snapshot.child("serviceDescription").getValue(String::class.java)
                }else{
                    finish()
                }
            }
        })
    }
}