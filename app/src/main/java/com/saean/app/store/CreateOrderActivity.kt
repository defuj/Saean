package com.saean.app.store

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.FirebaseDatabase
import com.saean.app.R
import com.saean.app.helper.Cache
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
        setupFunctions()
    }

    private fun setupFunctions() {
        toolbarCreateOrder.setNavigationOnClickListener {
            finish()
        }


    }
}