package com.saean.app.store

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cn.pedant.SweetAlert.SweetAlertDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.saean.app.R
import com.saean.app.helper.Cache
import com.saean.app.helper.MyFunctions
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_create_order.*
import kotlinx.android.synthetic.main.activity_create_order.btnDeleteImageProduct1
import kotlinx.android.synthetic.main.activity_create_order.btnDeleteImageProduct2
import kotlinx.android.synthetic.main.activity_create_order.btnDeleteImageProduct3
import kotlinx.android.synthetic.main.activity_create_order.btnDeleteImageProduct4
import kotlinx.android.synthetic.main.activity_create_order.productImage1
import kotlinx.android.synthetic.main.activity_create_order.productImage2
import kotlinx.android.synthetic.main.activity_create_order.productImage3
import kotlinx.android.synthetic.main.activity_create_order.productImage4
import kotlinx.android.synthetic.main.activity_create_order.serviceDescription
import java.util.*

class CreateOrderActivity : AppCompatActivity() {
    private lateinit var database: FirebaseDatabase
    private var sharedPreferences : SharedPreferences? = null
    private lateinit var storage : FirebaseStorage
    private lateinit var storageReference : StorageReference

    private var image1 = ""
    private var image2 = ""
    private var image3 = ""
    private var image4 = ""
    private var imageCount = 0
    private var browse = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_order)
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference
        sharedPreferences = getSharedPreferences(Cache.cacheName,0)

        setupFunctions()
    }

    private fun setupFunctions() {
        toolbarCreateOrder.setNavigationOnClickListener {
            finish()
        }

        openStore.setOnClickListener {
            val storeID = intent.getStringExtra("storeID")
            val go = Intent(this,StoreActivity::class.java)
            go.putExtra("storeID",storeID)
            startActivity(go)
        }

        orderTime.text = MyFunctions.getTanggal("dd/MM/yyyy")

        btnCreateOrder.setOnClickListener {
            if(problemDescription.text.length >= 10){
                if(imageCount == 0){
                    val dialog = SweetAlertDialog(this,SweetAlertDialog.WARNING_TYPE)
                    dialog.titleText = "Perhatian"
                    dialog.contentText = "Anda tidak akan menyertakan gambar?"
                    dialog.confirmText = "Iya"
                    dialog.cancelText = "Batalkan"
                    dialog.setConfirmClickListener {
                        dialog.dismissWithAnimation()
                        createOrder()
                    }
                    dialog.setCancelClickListener {
                        dialog.dismissWithAnimation()
                    }
                    dialog.show()
                }else{
                    createOrder()
                }
            }else{
                val dialog = SweetAlertDialog(this,SweetAlertDialog.WARNING_TYPE)
                dialog.titleText = "Perhatian"
                dialog.contentText = "Harap tuliskan permasalahan pada kolom deskripsi masalah. (Minimal 10 karakter)"
                dialog.show()
            }
        }

        setupInformation()
        setupStoreInformation()
        setupAddImages()
    }

    private fun createOrder() {
        if(MyFunctions.getConnectivityStatus(this)){
            val progress = SweetAlertDialog(this,SweetAlertDialog.PROGRESS_TYPE)
            progress.titleText = "Silahkan tunggu"
            progress.setCancelable(false)
            progress.show()

            val serviceID = intent.getStringExtra("serviceID")
            val storeID = intent.getStringExtra("storeID")
            val email = sharedPreferences!!.getString(Cache.email,"")

            val order = database.getReference("order")
            val idOrder = "${MyFunctions.randomAlphaNumeric(4)}-${MyFunctions.getTime()}"
            //val key = order.push().key.toString()
            order.child(idOrder).child("orderID").setValue(idOrder)
            order.child(idOrder).child("orderTime").setValue(MyFunctions.getTime())
            order.child(idOrder).child("orderStore").setValue(storeID)
            order.child(idOrder).child("orderUser").setValue(email)
            order.child(idOrder).child("orderService").setValue(serviceID)
            order.child(idOrder).child("orderDescription").setValue(problemDescription.text.toString())
            order.child(idOrder).child("orderStatus").setValue(0)
            order.child(idOrder).child("orderProcess").setValue(0)

            if(imageCount > 0){
                val img = database.getReference("order/$idOrder/orderPicture")
                if(image1.isNotEmpty()){
                    val keyImg = img.push().key.toString()
                    img.child(keyImg).setValue(image1)
                }
                if(image2.isNotEmpty()){
                    val keyImg = img.push().key.toString()
                    img.child(keyImg).setValue(image2)
                }
                if(image3.isNotEmpty()){
                    val keyImg = img.push().key.toString()
                    img.child(keyImg).setValue(image3)
                }
                if(image4.isNotEmpty()){
                    val keyImg = img.push().key.toString()
                    img.child(keyImg).setValue(image4)
                }
            }

            progress.dismissWithAnimation()
            val dialog = SweetAlertDialog(this,SweetAlertDialog.SUCCESS_TYPE)
            dialog.titleText = "Berhasil"
            dialog.contentText = "Orderan Anda telah dibuat"
            dialog.setOnDismissListener {
                val intent = Intent(this,UserOrderActivity::class.java)
                intent.putExtra("orderID",idOrder)
                startActivity(intent)
                finish()
            }
            dialog.show()
        }else{
            val dialog = SweetAlertDialog(this,SweetAlertDialog.WARNING_TYPE)
            dialog.titleText = "Failed"
            dialog.contentText = "Tidak ada koneksi internet"
            dialog.show()
        }
    }

    private fun setupAddImages() {
        productImage1.setOnClickListener {
            browse = "image1"
            CropImage.activity()
                .setOutputCompressFormat(Bitmap.CompressFormat.WEBP)
                .setOutputCompressQuality(75)
                .start(this)
        }
        productImage2.setOnClickListener {
            browse = "image2"
            CropImage.activity()
                .setOutputCompressFormat(Bitmap.CompressFormat.WEBP)
                .setOutputCompressQuality(75)
                .start(this)
        }
        productImage3.setOnClickListener {
            browse = "image3"
            CropImage.activity()
                .setOutputCompressFormat(Bitmap.CompressFormat.WEBP)
                .setOutputCompressQuality(75)
                .start(this)
        }
        productImage4.setOnClickListener {
            browse = "image4"
            CropImage.activity()
                .setOutputCompressFormat(Bitmap.CompressFormat.WEBP)
                .setOutputCompressQuality(75)
                .start(this)
        }

        btnDeleteImageProduct1.setOnClickListener {
            image1 = ""
            imageCount -=1
            btnDeleteImageProduct1.visibility = View.GONE
            productImage1.setImageResource(0)
        }

        btnDeleteImageProduct2.setOnClickListener {
            image2 = ""
            imageCount -=1
            btnDeleteImageProduct2.visibility = View.GONE
            productImage2.setImageResource(0)
        }

        btnDeleteImageProduct3.setOnClickListener {
            image3 = ""
            imageCount -=1
            btnDeleteImageProduct3.visibility = View.GONE
            productImage3.setImageResource(0)
        }

        btnDeleteImageProduct4.setOnClickListener {
            image4 = ""
            imageCount -=1
            btnDeleteImageProduct4.visibility = View.GONE
            productImage4.setImageResource(0)
        }
    }

    private fun setupStoreInformation() {
        val storeID = intent.getStringExtra("storeID")
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
                    Glide.with(this@CreateOrderActivity)
                        .load(info.child("storePicture").getValue(String::class.java))
                        .apply(RequestOptions.circleCropTransform())
                        .into(storePicture)
                }else{
                    Glide.with(this@CreateOrderActivity)
                        .load(R.drawable.image_placeholder)
                        .apply(RequestOptions.circleCropTransform())
                        .into(storePicture)
                }


                storeAddress.text = info.child("storeAddress").getValue(String::class.java)
                storeRating.rating = info.child("storeRating").getValue(Float::class.java)!!
                /** if(info.child("storeFront").exists()){
                    if(info.child("storeFront").hasChildren()){
                        var i = 0
                        for (sf in info.child("storeFront").children){
                            if(i == 0){
                                Glide.with(this@CreateOrderActivity)
                                    .load(sf.getValue(String::class.java))
                                    .into(storePicture)
                                i+=1
                            }
                        }
                    }else{
                        Glide.with(this@CreateOrderActivity)
                            .load(R.drawable.image_placeholder)
                            .into(storePicture)
                    }
                }else{
                    Glide.with(this@CreateOrderActivity)
                        .load(R.drawable.image_placeholder)
                        .into(storePicture)
                }

                **/
            }
        })
    }

    private fun setupInformation() {
        val serviceID = intent.getStringExtra("serviceID")
        val storeID = intent.getStringExtra("storeID")

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
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                val resultUri = result.uri
                val serviceID = intent.getStringExtra("serviceID")
                val storeID = intent.getStringExtra("storeID")
                val filePath : Uri = resultUri
                val ref = storageReference.child("store/$storeID/order/$serviceID/ORDER_${UUID.randomUUID()}")

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
                        Toast.makeText(this,"Gambar berhasil diunggah", Toast.LENGTH_SHORT).show()

                        when (browse) {
                            "image1" -> {
                                imageCount +=1
                                image1 = downloadUri.toString()
                                Glide.with(this)
                                    .load(downloadUri.toString())
                                    .into(productImage1)
                                btnDeleteImageProduct1.visibility = View.VISIBLE
                            }
                            "image2" -> {
                                imageCount +=1
                                image2 = downloadUri.toString()
                                Glide.with(this)
                                    .load(downloadUri.toString())
                                    .into(productImage2)
                                btnDeleteImageProduct2.visibility = View.VISIBLE
                            }
                            "image3" -> {
                                imageCount +=1
                                image3 = downloadUri.toString()
                                Glide.with(this)
                                    .load(downloadUri.toString())
                                    .into(productImage3)
                                btnDeleteImageProduct3.visibility = View.VISIBLE
                            }
                            "image4" -> {
                                imageCount +=1
                                image4 = downloadUri.toString()
                                Glide.with(this)
                                    .load(downloadUri.toString())
                                    .into(productImage4)
                                btnDeleteImageProduct4.visibility = View.VISIBLE
                            }
                        }
                    } else {
                        progress.dismissWithAnimation()
                        Toast.makeText(this,"Gagal mengunggah gambar", Toast.LENGTH_SHORT).show()
                    }
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
                Log.d("Result", error.toString())
            }
        }
    }
}