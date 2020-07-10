 package com.saean.app.store

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.saean.app.R
import com.saean.app.helper.Cache
import kotlinx.android.synthetic.main.activity_store_setting_schedule.*

 class StoreSettingScheduleActivity : AppCompatActivity() {
     private lateinit var database: FirebaseDatabase
     private var sharedPreferences : SharedPreferences? = null
     private lateinit var storage : FirebaseStorage
     private lateinit var storageReference : StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store_setting_schedule)
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference
        sharedPreferences =getSharedPreferences(Cache.cacheName,0)

        setupFunctions()
    }

     private fun setupFunctions() {
         toolbarSchedule.setNavigationOnClickListener { finish() }
     }
 }