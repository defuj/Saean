package com.saean.app.store.settings

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.saean.app.R
import com.saean.app.helper.Cache
import com.saean.app.store.StoreAddProductActivity
import com.saean.app.store.model.*
import kotlinx.android.synthetic.main.activity_store_setting_services.*
import kotlinx.android.synthetic.main.activity_store_setting_services.btnClose

class StoreSettingServicesActivity : AppCompatActivity() {
    private var service : ArrayList<ServiceModel>? = null
    private lateinit var database: FirebaseDatabase
    private var sharedPreferences : SharedPreferences? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store_setting_services)
        database = FirebaseDatabase.getInstance()
        sharedPreferences =getSharedPreferences(Cache.cacheName,0)

        setupFunctions()
    }

    private fun setupFunctions() {
        toolbarService.setNavigationOnClickListener {
            finish()
        }

        btnClose.setOnClickListener {
            containerNoticeService.visibility = View.GONE
        }

        btnAddService.setOnClickListener {
            startActivity(Intent(this,
                StoreAddProductActivity::class.java))
        }

        setupService()
    }

    private fun setupService() {
        val storeID = sharedPreferences!!.getString(Cache.storeID,"")
        database.getReference("product/service/$storeID").addValueEventListener(object :
            ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                recyclerStoreService.visibility = View.GONE
                containerNoServices.visibility = View.VISIBLE
                containerNoticeService.visibility = View.VISIBLE
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    if(snapshot.hasChildren()){
                        recyclerStoreService.visibility = View.VISIBLE
                        containerNoServices.visibility = View.GONE
                        containerNoticeService.visibility = View.GONE

                        service = ArrayList()
                        service!!.clear()
                        recyclerStoreService.layoutManager = LinearLayoutManager(this@StoreSettingServicesActivity,LinearLayoutManager.VERTICAL,false)

                        for(services in snapshot.children){
                            val model = ServiceModel()
                            model.serviceID = services.key.toString()
                            model.serviceTitle = services.child("serviceName").getValue(String::class.java)
                            model.serviceDescription = services.child("serviceDescription").getValue(String::class.java)
                            service!!.add(model)
                        }

                        val adapter = ServiceAdapter(this@StoreSettingServicesActivity,service!!)
                        recyclerStoreService.adapter = adapter

                    }else{
                        recyclerStoreService.visibility = View.GONE
                        containerNoServices.visibility = View.VISIBLE
                        containerNoticeService.visibility = View.VISIBLE
                    }
                }else{
                    recyclerStoreService.visibility = View.GONE
                    containerNoServices.visibility = View.VISIBLE
                    containerNoticeService.visibility = View.VISIBLE
                }
            }
        })
    }
}