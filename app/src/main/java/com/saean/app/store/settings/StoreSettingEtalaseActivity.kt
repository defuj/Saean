package com.saean.app.store.settings

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.saean.app.R
import com.saean.app.helper.Cache
import com.saean.app.store.model.BannerModel
import com.saean.app.store.model.EtalaseAdapter
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_store_setting_etalase.*
import kotlinx.android.synthetic.main.activity_store_setting_etalase.toolbarStoreSetting
import java.util.*
import kotlin.collections.ArrayList

class StoreSettingEtalaseActivity : AppCompatActivity() {
    private lateinit var database: FirebaseDatabase
    private var sharedPreferences : SharedPreferences? = null

    private lateinit var storage : FirebaseStorage
    private lateinit var storageReference : StorageReference

    private var picture : ArrayList<BannerModel>? = null

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
        setupStoreFrontList()

        toolbarStoreSetting.setNavigationOnClickListener {
            finish()
        }

        btnAddEtalase.setOnClickListener {
            CropImage.activity()
                .setOutputCompressFormat(Bitmap.CompressFormat.WEBP)
                .setOutputCompressQuality(75)
                .setAspectRatio(2,1)
                .start(this)
        }
    }

    private fun setupStoreFrontList() {
        val storeID = sharedPreferences!!.getString(Cache.storeID,"")
        database.getReference("store/$storeID/storeInfo/storeFront").addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                recyclerEtalase.visibility = View.GONE
                containerNoEtalase.visibility = View.VISIBLE
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    if(snapshot.hasChildren()){
                        recyclerEtalase.visibility = View.VISIBLE
                        containerNoEtalase.visibility = View.GONE

                        picture = ArrayList()
                        picture!!.clear()

                        recyclerEtalase.layoutManager = LinearLayoutManager(this@StoreSettingEtalaseActivity,LinearLayoutManager.VERTICAL,false)
                        for(content in snapshot.children){
                            val model = BannerModel()
                            model.bannerID = content.key.toString()
                            model.bannerImage = content.getValue(String::class.java)
                            picture!!.add(model)
                        }

                        val adapter = EtalaseAdapter(this@StoreSettingEtalaseActivity,picture!!)
                        recyclerEtalase.adapter = adapter
                    }else{
                        recyclerEtalase.visibility = View.GONE
                        containerNoEtalase.visibility = View.VISIBLE
                    }
                }else{
                    recyclerEtalase.visibility = View.GONE
                    containerNoEtalase.visibility = View.VISIBLE
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
                val ref = storageReference.child("store/$storeID/storeFront/storeFront_${UUID.randomUUID()}")

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

                        val upload = database.getReference("store/$storeID/storeInfo/storeFront")
                        val key = upload.push().key.toString()
                        upload.child(key).setValue(downloadUri.toString())
                        Toast.makeText(this,"Gambar berhasil diunggah",Toast.LENGTH_SHORT).show()
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