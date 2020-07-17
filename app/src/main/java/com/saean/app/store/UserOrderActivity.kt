package com.saean.app.store

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.saean.app.R
import com.saean.app.helper.Cache
import kotlinx.android.synthetic.main.activity_user_order.*

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

        val orderID = intent.getStringExtra("orderID")
        database.getReference("order/$orderID").addValueEventListener(object : ValueEventListener{
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

                }else{
                    progress.dismissWithAnimation()
                    finish()
                }
            }
        })
    }
}