package com.saean.app.store

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
import kotlinx.android.synthetic.main.activity_store_setting_etalase.*
import kotlinx.android.synthetic.main.activity_store_setting_etalase.toolbarStoreSetting
import kotlinx.android.synthetic.main.activity_store_setting_info.*
import java.util.*

class StoreSettingEtalaseActivity : AppCompatActivity() {
    private lateinit var database: FirebaseDatabase
    private var sharedPreferences : SharedPreferences? = null

    private lateinit var storage : FirebaseStorage
    private lateinit var storageReference : StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store_setting_etalase)
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

        btnUploadGambar.setOnClickListener {
            CropImage.activity()
                .setOutputCompressFormat(Bitmap.CompressFormat.WEBP)
                .setOutputCompressQuality(75)
                .setAspectRatio(2,1)
                .start(this)
        }
    }

    private fun setupInformation() {
        val progress = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        progress.titleText = "Silahkan tunggu"
        progress.contentText = "Mendapatkan informasi toko"
        progress.setCancelable(false)
        progress.show()

        val storeID = sharedPreferences!!.getString(Cache.storeID,"")
        database.getReference("store/$storeID/storeInfo").addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                progress.dismissWithAnimation()
                val dialog = SweetAlertDialog(this@StoreSettingEtalaseActivity,SweetAlertDialog.ERROR_TYPE)
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
                    if(snapshot.child("storeFront").getValue(String::class.java)!!.isNotEmpty()){
                        Glide.with(this@StoreSettingEtalaseActivity)
                            .load(snapshot.child("storeFront").getValue(String::class.java)!!)
                            .into(storeFront)
                    }

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
                val ref = storageReference.child("store/$storeID/storeFront_${UUID.randomUUID()}")

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
                        database.getReference("store/$storeID/storeInfo").child("storeFront").setValue(downloadUri.toString())
                        Toast.makeText(this,"Gambar berhasil diunggah",Toast.LENGTH_SHORT).show()

                        Glide.with(this)
                            .load(downloadUri.toString())
                            .into(storeFront)
                    } else {
                        progress.dismissWithAnimation()
                        Toast.makeText(this,"Gagal mengunggah gambar",Toast.LENGTH_SHORT).show()
                    }
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
                Log.d("Result", error.toString())
            }
        }
    }
}