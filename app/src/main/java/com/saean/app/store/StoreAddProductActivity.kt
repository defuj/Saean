package com.saean.app.store

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import cn.pedant.SweetAlert.SweetAlertDialog
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.saean.app.R
import com.saean.app.helper.Cache
import com.saean.app.helper.MyFunctions
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_store_add_product.*
import kotlinx.android.synthetic.main.activity_store_setting_etalase.*
import java.util.*

class StoreAddProductActivity : AppCompatActivity() {
    private var categories = "" // barang atau jasa
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
        setContentView(R.layout.activity_store_add_product)
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference
        sharedPreferences =getSharedPreferences(Cache.cacheName,0)

        setupFunctions()
    }

    private fun setupFunctions() {
        setupAddImages()

        toolbarAddProduct.setNavigationOnClickListener {
            finish()
        }

        btnSaveProduct.setOnClickListener {
            val storeID = sharedPreferences!!.getString(Cache.storeID,"")
            if(productName.text.toString().isNotEmpty()){
                if(categories.isNotEmpty()){
                    when (categories) {
                        "jasa" -> {
                            if(serviceDescription.text.isNotEmpty()){
                                if(MyFunctions.getConnectivityStatus(this)){
                                    val jasa = database.getReference("product/service/$storeID")
                                    val key = jasa.push().key.toString()
                                    jasa.child(key).child("serviceName").setValue(productName.text.toString())
                                    jasa.child(key).child("serviceDescription").setValue(serviceDescription.text.toString())

                                    val dialog = SweetAlertDialog(this,SweetAlertDialog.SUCCESS_TYPE)
                                    dialog.titleText = "Berhasil"
                                    dialog.contentText = "Layanan anda telah ditambahkan"
                                    dialog.setOnDismissListener {
                                        categories = ""
                                        radioGoods.isChecked = false
                                        radioServices.isChecked = false
                                        serviceDescription.text.clear()
                                        productName.text.clear()
                                        containerForGoods.visibility = View.GONE
                                        containerForService.visibility = View.GONE
                                    }
                                    dialog.show()
                                }else{
                                    Toast.makeText(this,"Tidak ada koneksi internet",Toast.LENGTH_SHORT).show()
                                }
                            }else{
                                Toast.makeText(this,"Berikan deskripsi untuk jasa kamu",Toast.LENGTH_SHORT).show()
                            }
                        }
                        "barang" -> {
                            if(goodsDescription.text.isNotEmpty()){
                                if(goodsStock.text.isNotEmpty()){
                                    if(goodsStock.text.toString() != "0"){
                                        if(goodsPrice.text.isNotEmpty()){
                                            if(goodsPrice.text.toString().toInt() >= 100){
                                                if(imageCount > 0){
                                                    if(MyFunctions.getConnectivityStatus(this)){
                                                        val barang = database.getReference("product/goods/$storeID")
                                                        val key = barang.push().key.toString()
                                                        barang.child(key).child("goodsName").setValue(productName.text.toString())
                                                        barang.child(key).child("goodsDescription").setValue(goodsDescription.text.toString())
                                                        barang.child(key).child("goodsStock").setValue(goodsStock.text.toString().toInt())
                                                        barang.child(key).child("goodsPrice").setValue(goodsPrice.text.toString().toInt())

                                                        val gambar = database.getReference("product/goods/$storeID/$key/goodsPicture")
                                                        if(image1.isNotEmpty()){
                                                            val imageKey = gambar.push().key.toString()
                                                            gambar.child(imageKey).setValue(image1)
                                                        }

                                                        if(image2.isNotEmpty()){
                                                            val imageKey = gambar.push().key.toString()
                                                            gambar.child(imageKey).setValue(image2)
                                                        }

                                                        if(image3.isNotEmpty()){
                                                            val imageKey = gambar.push().key.toString()
                                                            gambar.child(imageKey).setValue(image3)
                                                        }

                                                        if(image4.isNotEmpty()){
                                                            val imageKey = gambar.push().key.toString()
                                                            gambar.child(imageKey).setValue(image4)
                                                        }

                                                        val dialog = SweetAlertDialog(this,SweetAlertDialog.SUCCESS_TYPE)
                                                        dialog.titleText = "Berhasil"
                                                        dialog.contentText = "Barang anda telah ditambahkan"
                                                        dialog.setOnDismissListener {
                                                            categories = ""
                                                            radioGoods.isChecked = false
                                                            radioServices.isChecked = false
                                                            goodsDescription.text.clear()
                                                            goodsStock.text.clear()
                                                            goodsPrice.text.clear()
                                                            productName.text.clear()
                                                            containerForGoods.visibility = View.GONE
                                                            containerForService.visibility = View.GONE
                                                        }
                                                        dialog.show()
                                                    }else{
                                                        Toast.makeText(this,"Tidak ada koneksi internet",Toast.LENGTH_SHORT).show()
                                                    }
                                                }else{
                                                    Toast.makeText(this,"Tambahkan minimal 1 gambar",Toast.LENGTH_SHORT).show()
                                                }
                                            }else{
                                                Toast.makeText(this,"Harga minimal adalah Rp 100",Toast.LENGTH_SHORT).show()
                                            }
                                        }else{
                                            Toast.makeText(this,"Harga tidak bisa kosong",Toast.LENGTH_SHORT).show()
                                        }
                                    }else{
                                        Toast.makeText(this,"Stok minimal 1",Toast.LENGTH_SHORT).show()
                                    }
                                }else{
                                    Toast.makeText(this,"Stok tidak bisa kosong",Toast.LENGTH_SHORT).show()
                                }
                            }else{
                                Toast.makeText(this,"Berikan deskripsi untuk barang kamu",Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }else{
                    Toast.makeText(this,"Harap pilih kategori",Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this,"Harap isi nama produk",Toast.LENGTH_SHORT).show()
            }
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

    fun radioSelected(view : View){
        if(view is RadioButton){
            val checked = view.isChecked
            when (view.id) {
                R.id.radioGoods -> {
                    if(checked){
                        categories = "barang"
                        containerForGoods.visibility = View.VISIBLE
                        containerForService.visibility = View.GONE
                        radioServices.isChecked = false
                    }
                }
                R.id.radioServices -> {
                    if(checked){
                        categories = "jasa"
                        containerForGoods.visibility = View.GONE
                        containerForService.visibility = View.VISIBLE
                        radioGoods.isChecked = false
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                val resultUri = result.uri
                val storeID = sharedPreferences!!.getString(Cache.storeID,"")
                val filePath : Uri = resultUri
                val ref = storageReference.child("store/$storeID/Product_${UUID.randomUUID()}")

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
                        Toast.makeText(this,"Gambar berhasil diunggah",Toast.LENGTH_SHORT).show()

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