package com.saean.app.notification

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.saean.app.R
import com.saean.app.helper.Cache
import com.saean.app.helper.MyFunctions
import kotlinx.android.synthetic.main.activity_notification.*

class NotificationActivity : AppCompatActivity() {
    private var notification : ArrayList<NotificationModel>? = null
    private var sharedPreferences : SharedPreferences? = null
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)
        sharedPreferences = getSharedPreferences(Cache.cacheName,0)
        database = FirebaseDatabase.getInstance()

        startSetup()
    }

    private fun startSetup() {
        if(MyFunctions.getConnectivityStatus(this)){
            setupFunctions()
            setupList()
        }else{
            val dialog = SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
            dialog.titleText = "Failed"
            dialog.contentText = "Koneksi internet bermasalah, silahkan coba lagi!"
            dialog.confirmText = "Coba lagi"
            dialog.cancelText = "Tutup"
            dialog.setCancelable(false)
            dialog.setConfirmClickListener {
                dialog.dismissWithAnimation()
                startSetup()
            }
            dialog.setCancelClickListener {
                dialog.dismissWithAnimation()
                finish()
            }
            dialog.show()
        }
    }

    private fun setupList() {
        val email = MyFunctions.changeToUnderscore(sharedPreferences!!.getString(Cache.email,"")!!)
        database.getReference("notification/$email").addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    if(snapshot.hasChildren()){
                        notification = ArrayList()
                        notification!!.clear()
                        recyclerNotification.layoutManager = LinearLayoutManager(this@NotificationActivity,LinearLayoutManager.VERTICAL,false)
                        for(content in snapshot.children){
                            val model = NotificationModel()
                            model.notificationID = content.key.toString()
                            model.notificationBody = content.child("notificationBody").getValue(String::class.java)
                            model.notificationTitle = content.child("notificationTitle").getValue(String::class.java)
                            model.notificationStatus = content.child("notificationStatus").getValue(String::class.java)
                            model.notificationTime = content.child("notificationTime").getValue(Long::class.java)
                            notification!!.add(model)
                        }

                        notification!!.reverse()
                        val adapter = NotificationAdapter(this@NotificationActivity,notification!!)
                        recyclerNotification.adapter = adapter
                    }
                }
            }
        })
    }

    private fun setupFunctions() {
        toolbarNotification.setNavigationOnClickListener {
            finish()
        }
    }
}