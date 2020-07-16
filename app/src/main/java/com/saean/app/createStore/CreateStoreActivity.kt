package com.saean.app.createStore

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.firebase.database.FirebaseDatabase
import com.saean.app.R
import com.saean.app.createStore.fragments.CreateStore1Fragment
import com.saean.app.createStore.fragments.CreateStore2Fragment
import com.saean.app.helper.Cache
import com.saean.app.helper.MyFunctions
import kotlinx.android.synthetic.main.activity_create_store.*

class CreateStoreActivity : AppCompatActivity() {
    private lateinit var database: FirebaseDatabase
    private var sharedPreferences : SharedPreferences? = null

    var storeName = ""
    var storeDescription = ""
    var storeAddress = ""
    var storeLongitude = 0.0
    var storeLatitude = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_store)
        database = FirebaseDatabase.getInstance()
        sharedPreferences = getSharedPreferences(Cache.cacheName,0)

        setupFunctions()
    }

    private fun setupFunctions() {
        setupFragments()
    }

    private fun setupFragments() {
        //load fragments
        val adapter = CreateStoreAdapter(supportFragmentManager, listOf(CreateStore1Fragment(),CreateStore2Fragment()))
        createStorePager.adapter = adapter
    }

    fun setCurrentItem(i : Int){
        createStorePager.currentItem = i
    }

    fun createStore(){
        if(storeName.isNotEmpty() && storeDescription.isNotEmpty() && storeAddress.isNotEmpty() && storeLatitude != 0.0 && storeLongitude != 0.0){
            if(!MyFunctions.getConnectivityStatus(this)){
                return
            }

            val progress = SweetAlertDialog(this,SweetAlertDialog.PROGRESS_TYPE)
            progress.titleText = "Sedang membuat toko"
            progress.contentText = "Toko kamu sedang dipersiapkan, sebentar lagi jadi pengusaha nih .."
            progress.setCancelable(false)
            progress.show()

            object : CountDownTimer(3000,1000){
                override fun onFinish() {
                    val email = MyFunctions.changeToUnderscore(sharedPreferences!!.getString(Cache.email,"")!!)
                    val user = database.getReference("user/$email")
                    val store = database.getReference("store")
                    val storeID = store.push().key.toString()
                    store.child(storeID).child("storeInfo").child("storeAdmin").setValue(sharedPreferences!!.getString(Cache.email,""))
                    store.child(storeID).child("storeInfo").child("storePicture").setValue("")
                    store.child(storeID).child("storeInfo").child("storeRating").setValue(0.0F)
                    //store.child(storeID).child("storeInfo").child("storeFront").setValue("")
                    store.child(storeID).child("storeInfo").child("storeName").setValue(storeName)
                    store.child(storeID).child("storeInfo").child("storeAddress").setValue(storeAddress)
                    store.child(storeID).child("storeInfo").child("storeDescription").setValue(storeDescription)
                    store.child(storeID).child("storeInfo").child("storeStatus").setValue(false)
                    store.child(storeID).child("storeInfo").child("storeCreated").setValue(MyFunctions.getTime())
                    store.child(storeID).child("storeInfo").child("storeLocation").child("latitude").setValue(storeLatitude)
                    store.child(storeID).child("storeInfo").child("storeLocation").child("longitude").setValue(storeLongitude)
                    store.child(storeID).child("storeInfo").child("storeMedia").child("website").setValue("")
                    store.child(storeID).child("storeInfo").child("storeMedia").child("instagram").setValue("")
                    store.child(storeID).child("storeInfo").child("storeMedia").child("facebook").setValue("")
                    store.child(storeID).child("storeInfo").child("storeMedia").child("twitter").setValue("")

                    store.child(storeID).child("storeSchedule").child("Minggu").child("status").setValue(false)
                    store.child(storeID).child("storeSchedule").child("Minggu").child("startOpen").setValue("00.00")
                    store.child(storeID).child("storeSchedule").child("Minggu").child("endOpen").setValue("00.00")

                    store.child(storeID).child("storeSchedule").child("Senin").child("status").setValue(false)
                    store.child(storeID).child("storeSchedule").child("Senin").child("startOpen").setValue("00.00")
                    store.child(storeID).child("storeSchedule").child("Senin").child("endOpen").setValue("00.00")

                    store.child(storeID).child("storeSchedule").child("Selasa").child("status").setValue(false)
                    store.child(storeID).child("storeSchedule").child("Selasa").child("startOpen").setValue("00.00")
                    store.child(storeID).child("storeSchedule").child("Selasa").child("endOpen").setValue("00.00")

                    store.child(storeID).child("storeSchedule").child("Rabu").child("status").setValue(false)
                    store.child(storeID).child("storeSchedule").child("Rabu").child("startOpen").setValue("00.00")
                    store.child(storeID).child("storeSchedule").child("Rabu").child("endOpen").setValue("00.00")

                    store.child(storeID).child("storeSchedule").child("Kamis").child("status").setValue(false)
                    store.child(storeID).child("storeSchedule").child("Kamis").child("startOpen").setValue("00.00")
                    store.child(storeID).child("storeSchedule").child("Kamis").child("endOpen").setValue("00.00")

                    store.child(storeID).child("storeSchedule").child("Jumat").child("status").setValue(false)
                    store.child(storeID).child("storeSchedule").child("Jumat").child("startOpen").setValue("00.00")
                    store.child(storeID).child("storeSchedule").child("Jumat").child("endOpen").setValue("00.00")

                    store.child(storeID).child("storeSchedule").child("Sabtu").child("status").setValue(false)
                    store.child(storeID).child("storeSchedule").child("Sabtu").child("startOpen").setValue("00.00")
                    store.child(storeID).child("storeSchedule").child("Sabtu").child("endOpen").setValue("00.00")

                    user.child("userStore").setValue(storeID)

                    progress.dismissWithAnimation()

                    val dialog = SweetAlertDialog(this@CreateStoreActivity,SweetAlertDialog.SUCCESS_TYPE)
                    dialog.titleText = "Selamat tokomu sudah jadi"
                    dialog.contentText = "Sekarang waktunya untuk mengelola usahamu"
                    dialog.setCancelable(false)
                    dialog.setConfirmClickListener {
                        dialog.dismissWithAnimation()
                        finish()
                    }
                    dialog.show()
                }

                override fun onTick(millisUntilFinished: Long) {

                }
            }.start()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if(createStorePager.currentItem == 0){
            val dialog = SweetAlertDialog(this,SweetAlertDialog.WARNING_TYPE)
            dialog.titleText = "Oops"
            dialog.contentText = "Anda ingin membatalkan pembuatan toko?"
            dialog.confirmText = "Iya"
            dialog.cancelText = "Tidak"
            dialog.setConfirmClickListener {
                dialog.dismissWithAnimation()
                finish()
            }
            dialog.setOnCancelListener {
                dialog.dismissWithAnimation()
            }
            dialog.show()
        }else{
            createStorePager.setCurrentItem(createStorePager.currentItem-1,false)
        }
    }
}