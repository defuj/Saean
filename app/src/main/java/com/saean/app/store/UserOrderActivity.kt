package com.saean.app.store

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import cn.pedant.SweetAlert.SweetAlertDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.saean.app.R
import com.saean.app.helper.Cache
import com.saean.app.helper.MyFunctions
import kotlinx.android.synthetic.main.activity_user_order.*
import kotlinx.android.synthetic.main.activity_user_order.orderTime
import kotlinx.android.synthetic.main.activity_user_order.serviceDescription
import kotlinx.android.synthetic.main.activity_user_order.serviceTitle
import kotlinx.android.synthetic.main.activity_user_order.storeAddress
import kotlinx.android.synthetic.main.activity_user_order.storeName
import kotlinx.android.synthetic.main.activity_user_order.storePicture
import kotlinx.android.synthetic.main.activity_user_order.storeRating

class UserOrderActivity : AppCompatActivity() {
    private lateinit var database: FirebaseDatabase
    private var sharedPreferences : SharedPreferences?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_order)
        database = FirebaseDatabase.getInstance()
        sharedPreferences = getSharedPreferences(Cache.cacheName,0)

        setupFunctions()
    }

    private fun setupFunctions() {
        toolbarOrder.setNavigationOnClickListener {
            finish()
        }

        setupInformationOrder()
    }

    private fun setupInformationOrder() {
        val progress = SweetAlertDialog(this,SweetAlertDialog.PROGRESS_TYPE)
        progress.titleText = "Silahkan tunggu"
        progress.setCancelable(false)
        progress.show()

        val idOrder = intent.getStringExtra("orderID")
        database.getReference("order/$idOrder").addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                progress.dismissWithAnimation()
                val dialog = SweetAlertDialog(this@UserOrderActivity,SweetAlertDialog.ERROR_TYPE)
                dialog.titleText = "Failed"
                dialog.contentText = "Gagal mendapatkan informasi order"
                dialog.confirmText = "Coba lagi"
                dialog.cancelText = "Tutup"
                dialog.setConfirmClickListener {
                    dialog.dismissWithAnimation()
                    setupInformationOrder()
                }
                dialog.setOnCancelListener {
                    dialog.dismissWithAnimation()
                    finish()
                }
                dialog.show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    orderID.text = snapshot.child("orderID").getValue(String::class.java)
                    orderTime.text = MyFunctions.formatMillie(snapshot.child("orderTime").getValue(Long::class.java)!!,"dd/MM/yyyy H:m:s")
                    orderDescription.text = snapshot.child("orderDescription").getValue(String::class.java)

                    snapshot.child("orderStatus").getValue(Int::class.java)
                    snapshot.child("orderProcess").getValue(Int::class.java)
                    //get store information
                    val serviceID = snapshot.child("orderService").getValue(String::class.java)
                    val storeID = snapshot.child("orderStore").getValue(String::class.java)
                    database.getReference("store/$storeID/storeInfo").addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onCancelled(error: DatabaseError) {

                        }

                        override fun onDataChange(info: DataSnapshot) {
                            storeName.text = info.child("storeName").getValue(String::class.java)
                            if(info.child("storeStatus").getValue(Boolean::class.java)!!){
                                storeName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_store_verified_1,0,0,0)
                            }else{
                                storeName.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0)
                            }

                            if(info.child("storePicture").getValue(String::class.java)!!.isNotEmpty()){
                                Glide.with(this@UserOrderActivity)
                                    .load(info.child("storePicture").getValue(String::class.java))
                                    .apply(RequestOptions.circleCropTransform())
                                    .into(storePicture)
                            }else{
                                Glide.with(this@UserOrderActivity)
                                    .load(R.drawable.image_placeholder)
                                    .apply(RequestOptions.circleCropTransform())
                                    .into(storePicture)
                            }
                            storeAddress.text = info.child("storeAddress").getValue(String::class.java)
                            storeRating.rating = info.child("storeRating").getValue(Float::class.java)!!
                        }
                    })

                    //get service information
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
                    progress.dismissWithAnimation()
                }else{
                    progress.dismissWithAnimation()
                    finish()
                }
            }
        })
    }
}