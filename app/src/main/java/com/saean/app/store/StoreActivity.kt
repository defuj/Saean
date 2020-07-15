package com.saean.app.store

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.saean.app.R
import com.saean.app.helper.Cache
import com.saean.app.helper.MyFunctions
import com.saean.app.store.model.ServiceModel
import com.saean.app.store.model.UserServiceAdapter
import kotlinx.android.synthetic.main.activity_store.*
import kotlinx.android.synthetic.main.item_list_store_horizontal.view.*

class StoreActivity : AppCompatActivity() {
    private lateinit var database: FirebaseDatabase
    private var sharedPreferences : SharedPreferences? = null
    private var service : ArrayList<ServiceModel>? = null
    private var hasRated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store)
        database = FirebaseDatabase.getInstance()
        sharedPreferences = getSharedPreferences(Cache.cacheName,0)

        setupFunctions()
    }

    private fun setupFunctions() {
        toolbarStore.setNavigationOnClickListener {
            finish()
        }

        setupRatingBar()
        setupStoreInformation()
        setupServiceList()
    }

    private fun setupRatingBar() {
        val email = MyFunctions.changeToUnderscore(sharedPreferences!!.getString(Cache.email,"")!!)
        val storeID = intent.getStringExtra("storeID")!!
        database.getReference("store/$storeID/storeRate").child(email).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                hasRated = true
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    ratingBar.rating = snapshot.getValue(Float::class.java)!!
                }
                hasRated = true
            }
        })

        ratingBar.setOnRatingChangeListener { _, rating ->
            if(hasRated){
                database.getReference("store/$storeID/storeRate").child(email).setValue(rating)
                database.getReference("store/$storeID/storeRate").addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onCancelled(error: DatabaseError) {

                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        val total = snapshot.childrenCount.toInt()
                        var rateTotal = 0
                        for (rate in snapshot.children){
                            rateTotal += rate.getValue(Int::class.java)!!
                        }
                        val result = rateTotal/total
                        database.getReference("store/$storeID/storeInfo").child("storeRating").setValue(result)
                    }
                })
            }
        }
    }

    private fun setupStoreInformation() {
        val progress = SweetAlertDialog(this,SweetAlertDialog.PROGRESS_TYPE)
        progress.titleText = "Silahkan tunggu"
        progress.setCancelable(false)
        progress.show()

        if(intent.getStringExtra("storeID")!!.isNotEmpty()){
            val storeID = intent.getStringExtra("storeID")!!
            database.getReference("store/$storeID/storeInfo").addValueEventListener(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                    progress.dismissWithAnimation()

                    val dialog = SweetAlertDialog(this@StoreActivity,SweetAlertDialog.ERROR_TYPE)
                    dialog.titleText = "Failed"
                    dialog.contentText = "Gagal memuat informasi"
                    dialog.setCancelable(false)
                    dialog.confirmText = "Coba lagi"
                    dialog.cancelText = "Tutup"
                    dialog.setConfirmClickListener {
                        dialog.dismissWithAnimation()
                        setupStoreInformation()
                    }
                    dialog.setOnCancelListener {
                        dialog.dismissWithAnimation()
                        finish()
                    }
                    dialog.show()
                }

                @SuppressLint("SetTextI18n")
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        storeName.text = snapshot.child("storeName").getValue(String::class.java)
                        storeAddress.text = snapshot.child("storeAddress").getValue(String::class.java)
                        if(snapshot.child("storeFront").getValue(String::class.java)!!.isNotEmpty()){
                            Glide.with(this@StoreActivity)
                                .load(snapshot.child("storeFront").getValue(String::class.java))
                                .into(storeFront)
                        }else{
                            Glide.with(this@StoreActivity)
                                .load(R.drawable.image_placeholder_landscape)
                                .into(storeFront)
                        }
                        storeRating.text = "${snapshot.child("storeRating").getValue(Float::class.java)!!}"
                        if(sharedPreferences!!.getString(Cache.latitude,"")!!.isNotEmpty()){
                            val latitude1 = sharedPreferences!!.getString(Cache.latitude,"")!!.toDouble()
                            val longitude1 = sharedPreferences!!.getString(Cache.longitude,"")!!.toDouble()
                            val latitude2 = snapshot.child("storeLocation").child("latitude").getValue(Double::class.java)!!
                            val longitude2 = snapshot.child("storeLocation").child("longitude").getValue(Double::class.java)!!

                            storeDistance.text = if(MyFunctions.countDistance(latitude1, latitude2,longitude1,longitude2) >= 1){
                                "${MyFunctions.formatDistance(MyFunctions.countDistance(latitude1, latitude2,longitude1,longitude2))} km"
                            }else{
                                "${MyFunctions.formatDistance(MyFunctions.countDistance(latitude1, latitude2,longitude1,longitude2)*1000)} m"
                            }
                        }
                        progress.dismissWithAnimation()
                    }else{
                        progress.dismissWithAnimation()

                        val dialog = SweetAlertDialog(this@StoreActivity,SweetAlertDialog.ERROR_TYPE)
                        dialog.titleText = "Failed"
                        dialog.contentText = "Informasi tidak ditemukan"
                        dialog.setCancelable(false)
                        dialog.confirmText = "Tutup"
                        dialog.setConfirmClickListener {
                            dialog.dismissWithAnimation()
                            finish()
                        }
                        dialog.show()
                    }
                }
            })
        }else{
            finish()
        }
    }

    private fun setupServiceList() {
        val storeID = intent.getStringExtra("storeID")!!
        database.getReference("product/service/$storeID").addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                containerListService.visibility = View.GONE
                containerNoService.visibility = View.VISIBLE
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    if(snapshot.hasChildren()){
                        containerListService.visibility = View.VISIBLE
                        containerNoService.visibility = View.GONE

                        service = ArrayList()
                        service!!.clear()
                        recyclerListService!!.layoutManager = LinearLayoutManager(this@StoreActivity,LinearLayoutManager.VERTICAL,false)
                        for(services in snapshot.children){
                            val model = ServiceModel()
                            model.serviceID = services.key.toString()
                            model.serviceTitle = services.child("serviceName").getValue(String::class.java)
                            model.serviceDescription = services.child("serviceDescription").getValue(String::class.java)
                            service!!.add(model)
                        }

                        val adapter = UserServiceAdapter(this@StoreActivity,service!!)
                        recyclerListService.adapter = adapter
                    }else{
                        containerListService.visibility = View.GONE
                        containerNoService.visibility = View.VISIBLE
                    }
                }else{
                    containerListService.visibility = View.GONE
                    containerNoService.visibility = View.VISIBLE
                }
            }
        })
    }
}