package com.saean.app

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
import cn.pedant.SweetAlert.SweetAlertDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.saean.app.helper.Cache
import com.saean.app.helper.MyFunctions
import com.saean.app.store.*
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_store_setting_info.*
import kotlinx.android.synthetic.main.activity_verification.*
import kotlinx.android.synthetic.main.fragment_account.*
import java.util.*

class VerificationActivity : AppCompatActivity() {
    private lateinit var database: FirebaseDatabase
    private var sharedPreferences : SharedPreferences? = null
    private lateinit var storage : FirebaseStorage
    private lateinit var storageReference : StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification)
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference
        sharedPreferences =getSharedPreferences(Cache.cacheName,0)

        setupFunctions()
        setupUserInformation()
    }

    private fun setupFunctions() {
        toolbarVerify.setNavigationOnClickListener {
            finish()
        }

        btnUploadKTP.setOnClickListener {
            CropImage.activity()
                .setOutputCompressFormat(Bitmap.CompressFormat.WEBP)
                .setOutputCompressQuality(75)
                .setAspectRatio(3,2)
                .start(this)
        }
    }

    private fun setupUserInformation() {
        val email = MyFunctions.changeToUnderscore(sharedPreferences!!.getString(Cache.email,"")!!)
        database.getReference("user/$email").addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                when {
                    snapshot.child("userVerify").getValue(Int::class.java)!! == 0 -> {
                        //not yet verify
                        containerNotVerified.visibility = View.VISIBLE
                        containerProcessVerified.visibility = View.GONE
                        containerVerified.visibility = View.GONE

                        val dialog = SweetAlertDialog(this@VerificationActivity,SweetAlertDialog.NORMAL_TYPE)
                        dialog.titleText = "Mari verifikasi data dirimu"
                        dialog.contentText = "Kamu hanya perlu upload foto KTP dan verifikasi wajah."
                        dialog.confirmText = "Mulai"
                        dialog.setConfirmClickListener {
                            dialog.dismissWithAnimation()
                        }
                        dialog.setCancelable(false)
                        dialog.show()
                    }
                    snapshot.child("userVerify").getValue(Int::class.java)!! == 1 -> {
                        //process verify
                        containerNotVerified.visibility = View.GONE
                        containerProcessVerified.visibility = View.VISIBLE
                        containerVerified.visibility = View.GONE
                    }
                    else -> {
                        //has been verify
                        containerNotVerified.visibility = View.GONE
                        containerProcessVerified.visibility = View.GONE
                        containerVerified.visibility = View.VISIBLE
                    }
                }
            }
        })

        database.getReference("user/$email").addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    when {
                        snapshot.child("userVerify").getValue(Int::class.java)!! == 0 -> {
                            //not yet verify
                            containerNotVerified.visibility = View.VISIBLE
                            containerProcessVerified.visibility = View.GONE
                            containerVerified.visibility = View.GONE
                        }
                        snapshot.child("userVerify").getValue(Int::class.java)!! == 1 -> {
                            //process verify
                            containerNotVerified.visibility = View.GONE
                            containerProcessVerified.visibility = View.VISIBLE
                            containerVerified.visibility = View.GONE
                        }
                        else -> {
                            //has been verify
                            containerNotVerified.visibility = View.GONE
                            containerProcessVerified.visibility = View.GONE
                            containerVerified.visibility = View.VISIBLE
                        }
                    }
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val email = MyFunctions.changeToUnderscore(sharedPreferences!!.getString(Cache.email,"")!!)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                val resultUri = result.uri
                val filePath : Uri = resultUri
                val ref = storageReference.child("user/$email/KTP_${UUID.randomUUID()}")

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
                        database.getReference("user/$email").child("userVerify").setValue(1)
                        database.getReference("user/$email").child("userKTP").setValue(downloadUri.toString())
                        Toast.makeText(this,"Gambar berhasil diunggah",Toast.LENGTH_SHORT).show()
                        val dialog = SweetAlertDialog(this,SweetAlertDialog.SUCCESS_TYPE)
                        dialog.titleText = "Berhasil"
                        dialog.contentText = "Kami akan segera meninjau data diri Anda"
                        dialog.setOnDismissListener {

                        }
                        dialog.show()
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