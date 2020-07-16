package com.saean.app.store

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.saean.app.R
import com.saean.app.store.model.ScheduleAdapter
import com.saean.app.store.model.ScheduleModel
import kotlinx.android.synthetic.main.activity_store_info.*
import kotlinx.android.synthetic.main.activity_store_info.containerOpeningHours
import kotlinx.android.synthetic.main.activity_store_info.storeName

class StoreInfoActivity : AppCompatActivity() {
    private lateinit var database: FirebaseDatabase
    private var schedule : ArrayList<ScheduleModel>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store_info)
        database = FirebaseDatabase.getInstance()

        toolbarStoreInfo.setNavigationOnClickListener{
            finish()
        }

        setupStoreInfo()
        setupSchedule()
    }

    private fun setupSchedule() {
        val storeID = intent.getStringExtra("storeID")!!
        database.getReference("store/$storeID/storeSchedule").addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                containerOpeningHours.visibility = View.GONE
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    if(snapshot.hasChildren()){
                        containerOpeningHours.visibility = View.VISIBLE
                        schedule = ArrayList()
                        schedule!!.clear()

                        recyclerOpeningHours!!.layoutManager = LinearLayoutManager(this@StoreInfoActivity,
                            LinearLayoutManager.VERTICAL,false)
                        for(content in snapshot.children){
                            val model = ScheduleModel()
                            model.scheduleDay = content.key.toString()
                            model.scheduleStatus = content.child("status").getValue(Boolean::class.java)!!
                            model.scheduleStartOpen = content.child("startOpen").getValue(String::class.java)
                            model.scheduleEndOpen = content.child("endOpen").getValue(String::class.java)
                            schedule!!.add(model)
                        }

                        val days = arrayOf("Minggu","Senin","Selasa","Rabu","Kamis","Jumat","Sabtu")
                        for(i in 0 until schedule!!.size){
                            if(schedule!![i].scheduleDay != days[i]){
                                for(m in 0 until schedule!!.size){
                                    if(schedule!![m].scheduleDay == days[i]){
                                        schedule!!.add(i,schedule!![m])
                                        schedule!!.removeAt(m+1)
                                    }
                                }
                            }
                        }

                        val adapter = ScheduleAdapter(this@StoreInfoActivity,schedule!!)
                        recyclerOpeningHours.adapter = adapter
                    }else{
                        containerOpeningHours.visibility = View.GONE
                    }
                }else{
                    containerOpeningHours.visibility = View.GONE
                }
            }
        })
    }

    private fun setupStoreInfo() {
        val progress = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        progress.titleText = "Silahkan tunggu"
        progress.setCancelable(false)
        progress.show()

        val storeID = intent.getStringExtra("storeID")!!
        database.getReference("store/$storeID/storeInfo").addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                progress.dismissWithAnimation()

                val dialog = SweetAlertDialog(this@StoreInfoActivity,SweetAlertDialog.ERROR_TYPE)
                dialog.titleText = "Failed"
                dialog.contentText = "Gagal memuat informasi"
                dialog.setCancelable(false)
                dialog.confirmText = "Coba lagi"
                dialog.cancelText = "Tutup"
                dialog.setConfirmClickListener {
                    dialog.dismissWithAnimation()
                    setupStoreInfo()
                }
                dialog.setOnCancelListener {
                    dialog.dismissWithAnimation()
                    finish()
                }
                dialog.show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                storeName.text = snapshot.child("storeName").getValue(String::class.java)
                storeDescription.text = snapshot.child("storeDescription").getValue(String::class.java)
                progress.dismissWithAnimation()
            }
        })
    }
}