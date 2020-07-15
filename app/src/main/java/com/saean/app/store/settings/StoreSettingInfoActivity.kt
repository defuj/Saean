package com.saean.app.store.settings

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import cn.pedant.SweetAlert.SweetAlertDialog
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.saean.app.R
import com.saean.app.helper.Cache
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_store_setting_info.*
import java.util.*

class StoreSettingInfoActivity : AppCompatActivity() {
    private lateinit var database: FirebaseDatabase
    private var sharedPreferences : SharedPreferences? = null

    private lateinit var storage : FirebaseStorage
    private lateinit var storageReference : StorageReference

    private var newIconURL = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store_setting_info)
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference
        sharedPreferences =getSharedPreferences(Cache.cacheName,0)

        setupFunctions()
    }

    private fun setupFunctions() {
        setupInformation()

        toolbarStoreSetting.setNavigationOnClickListener {
            finish()
        }

        btnBrowseFile.setOnClickListener {
            CropImage.activity()
                .setOutputCompressFormat(Bitmap.CompressFormat.WEBP)
                .setOutputCompressQuality(75)
                .setAspectRatio(1,1)
                .start(this)
        }

        btnSave.setOnClickListener {
            val storeID = sharedPreferences!!.getString(Cache.storeID,"")

            if(storeName.text.isNotEmpty() && storeDescription.text.isNotEmpty()){
                database.getReference("store/$storeID/storeInfo").child("storeName").setValue(storeName.text.toString())
                database.getReference("store/$storeID/storeInfo").child("storeDescription").setValue(storeDescription.text.toString())

                if(newIconURL.isNotEmpty()){
                    database.getReference("store/$storeID/storeInfo").child("storePicture").setValue(newIconURL)
                }

                val dialog = SweetAlertDialog(this@StoreSettingInfoActivity,SweetAlertDialog.SUCCESS_TYPE)
                dialog.titleText = "Berhasil"
                dialog.contentText = "Perubahan pada toko telah disimpan"
                dialog.setConfirmClickListener {
                    dialog.dismissWithAnimation()
                }
                dialog.show()
            }else{
                val dialog = SweetAlertDialog(this@StoreSettingInfoActivity,SweetAlertDialog.ERROR_TYPE)
                dialog.titleText = "Oops"
                dialog.contentText = "Harap lengkapi form."
                dialog.setConfirmClickListener {
                    dialog.dismissWithAnimation()
                }
                dialog.show()
            }
        }
    }

    private fun setupInformation() {
        val progress = SweetAlertDialog(this,SweetAlertDialog.PROGRESS_TYPE)
        progress.titleText = "Silahkan tunggu"
        progress.contentText = "Mendapatkan informasi toko"
        progress.setCancelable(false)
        progress.show()

        val storeID = sharedPreferences!!.getString(Cache.storeID,"")
        database.getReference("store/$storeID/storeInfo").addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                progress.dismissWithAnimation()
                val dialog = SweetAlertDialog(this@StoreSettingInfoActivity,SweetAlertDialog.ERROR_TYPE)
                dialog.titleText = "Oops"
                dialog.contentText = "Gagal mendapatkan informasi toko. Silahkan mencoba lagi."
                dialog.setCancelable(false)
                dialog.setConfirmClickListener {
                    dialog.dismissWithAnimation()
                    finish()
                }
                dialog.show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    if(snapshot.child("storePicture").getValue(String::class.java)!!.isNotEmpty()){
                        Glide.with(this@StoreSettingInfoActivity)
                            .load(snapshot.child("storePicture").getValue(String::class.java)!!)
                            .into(storeIcon)
                    }else{
                        Glide.with(this@StoreSettingInfoActivity)
                            .load(R.drawable.ic_my_business)
                            .into(storeIcon)
                    }

                    storeName.setText(snapshot.child("storeName").getValue(String::class.java)!!)
                    storeDescription.setText(snapshot.child("storeDescription").getValue(String::class.java)!!)

                    progress.dismissWithAnimation()
                }else{
                    finish()
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                val resultUri = result.uri
                val storeID = sharedPreferences!!.getString(Cache.storeID,"")
                val filePath : Uri = resultUri
                val ref = storageReference.child("store/$storeID/storeIcon_${UUID.randomUUID()}")

                val progress = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
                progress.titleText = "Sedang mengunggah"
                progress.setCancelable(false)
                progress.show()

                ref.putFile(filePath).continueWithTask {task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    ref.downloadUrl
                }.addOnCompleteListener {task ->
                    if (task.isSuccessful) {
                        progress.dismissWithAnimation()
                        val downloadUri = task.result
                        Log.e("IMAGE_URL","$downloadUri")
                        showDialog("Berhasil","Gambar berhasil diunggah", SweetAlertDialog.SUCCESS_TYPE)
                        newIconURL = downloadUri.toString()

                        Glide.with(this)
                            .load(newIconURL)
                            .into(storeIcon)
                    } else {
                        progress.dismissWithAnimation()
                        showDialog("Failed","Gagal mengunggah gambar", SweetAlertDialog.ERROR_TYPE)
                    }
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
                Log.d("Result", error.toString())
            }
        }
    }

    private fun showDialog(title: String, content : String, type : Int){
        val dialog = SweetAlertDialog(this,type)
        dialog.titleText = title
        dialog.contentText = content
        //dialog.show()

        Toast.makeText(this,content,Toast.LENGTH_LONG).show()
    }
}