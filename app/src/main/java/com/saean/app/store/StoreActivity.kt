package com.saean.app.store

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Rect
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.saean.app.R
import com.saean.app.helper.Cache
import com.saean.app.helper.MyFunctions
import com.saean.app.store.model.*
import kotlinx.android.synthetic.main.activity_store.*
import kotlinx.android.synthetic.main.item_list_store_horizontal.view.*

class StoreActivity : AppCompatActivity() {
    private lateinit var database: FirebaseDatabase
    private var sharedPreferences : SharedPreferences? = null
    private var service : ArrayList<ServiceModel>? = null
    private var product : ArrayList<ProductModel>? = null
    private var hasRated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store)
        database = FirebaseDatabase.getInstance()
        sharedPreferences = getSharedPreferences(Cache.cacheName,0)

        setupFunctions()
    }

    private fun setupFunctions() {
        toolbarStore.setNavigationOnClickListener {
            finish()
        }

        btnOpenMaps.setOnClickListener {
            val storeID = intent.getStringExtra("storeID")!!
            database.getReference("store/$storeID/storeInfo/storeLocation").addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@StoreActivity,"Koneksi internet bermasalah", Toast.LENGTH_SHORT).show()
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        val storeLatitude = snapshot.child("latitude").getValue(Double::class.java)!!
                        val storeLongitude = snapshot.child("longitude").getValue(Double::class.java)!!
                        val gmmIntentUri = Uri.parse("google.navigation:q=$storeLatitude,$storeLongitude")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        mapIntent.setPackage("com.google.android.apps.maps")
                        try {
                            startActivity(mapIntent)
                        }catch (e:Exception){
                            Toast.makeText(this@StoreActivity,"Google Maps not installed", Toast.LENGTH_SHORT).show()
                        }
                    }else{
                        Toast.makeText(this@StoreActivity,"Lokasi tidak ditemukan", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }

        recyclerListProduct!!.addItemDecoration(object : RecyclerView.ItemDecoration(){
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                val position = parent.getChildAdapterPosition(view)
                val spanCount = 2
                val spacing = 10
                if(position>=0){
                    val column = position % spanCount
                    outRect.left = spacing - column * spacing / spanCount
                    outRect.right = (column + 1) * spacing / spanCount

                    if (position < spanCount) {
                        outRect.top = spacing
                    }

                    outRect.bottom = spacing
                }else {
                    outRect.left = 0
                    outRect.right = 0
                    outRect.top = 0
                    outRect.bottom = 0
                }
            }
        })

        setupRatingBar()
        setupStoreInformation()
        setupServiceList()
        setupProductList()
    }

    private fun setupRatingBar() {
        val email = MyFunctions.changeToUnderscore(sharedPreferences!!.getString(Cache.email,"")!!)
        val storeID = intent.getStringExtra("storeID")!!
        database.getReference("store/$storeID/storeRate").child(email).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                hasRated = true
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    ratingBar.rating = snapshot.getValue(Float::class.java)!!
                }
                hasRated = true
            }
        })

        ratingBar.setOnRatingChangeListener { _, rating ->
            if(hasRated){
                database.getReference("store/$storeID/storeRate").child(email).setValue(rating)
                database.getReference("store/$storeID/storeRate").addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onCancelled(error: DatabaseError) {
                        val dialog = SweetAlertDialog(this@StoreActivity,SweetAlertDialog.ERROR_TYPE)
                        dialog.titleText = "Failed"
                        dialog.contentText = "Gagal memberikan rating, silahkan coba lagi!"
                        dialog.show()
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        val total = snapshot.childrenCount.toInt()
                        var rateTotal = 0
                        for (rate in snapshot.children){
                            rateTotal += rate.getValue(Int::class.java)!!
                        }
                        val result = rateTotal/total
                        database.getReference("store/$storeID/storeInfo").child("storeRating").setValue(result)

                        val dialog = SweetAlertDialog(this@StoreActivity,SweetAlertDialog.SUCCESS_TYPE)
                        dialog.titleText = "Berhasil"
                        dialog.contentText = "Terimakasih telah memberi penilaian Anda pada toko kami."
                        dialog.show()
                    }
                })
            }
        }
    }

    private fun setupStoreInformation() {
        val progress = SweetAlertDialog(this,SweetAlertDialog.PROGRESS_TYPE)
        progress.titleText = "Silahkan tunggu"
        progress.setCancelable(false)
        progress.show()

        if(intent.getStringExtra("storeID")!!.isNotEmpty()){
            val storeID = intent.getStringExtra("storeID")!!
            database.getReference("store/$storeID/storeInfo").addValueEventListener(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                    progress.dismissWithAnimation()

                    val dialog = SweetAlertDialog(this@StoreActivity,SweetAlertDialog.ERROR_TYPE)
                    dialog.titleText = "Failed"
                    dialog.contentText = "Gagal memuat informasi"
                    dialog.setCancelable(false)
                    dialog.confirmText = "Coba lagi"
                    dialog.cancelText = "Tutup"
                    dialog.setConfirmClickListener {
                        dialog.dismissWithAnimation()
                        setupStoreInformation()
                    }
                    dialog.setOnCancelListener {
                        dialog.dismissWithAnimation()
                        finish()
                    }
                    dialog.show()
                }

                @SuppressLint("SetTextI18n")
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        storeName.text = snapshot.child("storeName").getValue(String::class.java)
                        storeAddress.text = snapshot.child("storeAddress").getValue(String::class.java)
                        if(snapshot.child("storeFront").getValue(String::class.java)!!.isNotEmpty()){
                            Glide.with(this@StoreActivity)
                                .load(snapshot.child("storeFront").getValue(String::class.java))
                                .into(storeFront)
                        }else{
                            Glide.with(this@StoreActivity)
                                .load(R.drawable.image_placeholder_landscape)
                                .into(storeFront)
                        }
                        storeRating.text = "${snapshot.child("storeRating").getValue(Float::class.java)!!}"
                        if(sharedPreferences!!.getString(Cache.latitude,"")!!.isNotEmpty()){
                            val latitude1 = sharedPreferences!!.getString(Cache.latitude,"")!!.toDouble()
                            val longitude1 = sharedPreferences!!.getString(Cache.longitude,"")!!.toDouble()
                            val latitude2 = snapshot.child("storeLocation").child("latitude").getValue(Double::class.java)!!
                            val longitude2 = snapshot.child("storeLocation").child("longitude").getValue(Double::class.java)!!

                            storeDistance.text = if(MyFunctions.countDistance(latitude1, latitude2,longitude1,longitude2) >= 1){
                                "${MyFunctions.formatDistance(MyFunctions.countDistance(latitude1, latitude2,longitude1,longitude2))} km"
                            }else{
                                "${MyFunctions.formatDistance(MyFunctions.countDistance(latitude1, latitude2,longitude1,longitude2)*1000)} m"
                            }
                        }

                        if(snapshot.child("storeDescription").exists()){
                            if(snapshot.child("storeDescription").getValue(String::class.java)!!.isNotEmpty()){
                                btnInfoStore.visibility = View.VISIBLE
                                btnInfoStore.setOnClickListener {
                                    val dialog = SweetAlertDialog(this@StoreActivity,SweetAlertDialog.NORMAL_TYPE)
                                    dialog.titleText = snapshot.child("storeName").getValue(String::class.java)
                                    dialog.contentText = snapshot.child("storeDescription").getValue(String::class.java)
                                    dialog.show()
                                }
                            }else{
                                btnInfoStore.visibility = View.GONE
                            }
                        }else{
                            btnInfoStore.visibility = View.GONE
                        }
                        progress.dismissWithAnimation()
                    }else{
                        progress.dismissWithAnimation()

                        val dialog = SweetAlertDialog(this@StoreActivity,SweetAlertDialog.ERROR_TYPE)
                        dialog.titleText = "Failed"
                        dialog.contentText = "Informasi tidak ditemukan"
                        dialog.setCancelable(false)
                        dialog.confirmText = "Tutup"
                        dialog.setConfirmClickListener {
                            dialog.dismissWithAnimation()
                            finish()
                        }
                        dialog.show()
                    }
                }
            })
        }else{
            finish()
        }
    }

    private fun setupServiceList() {
        val storeID = intent.getStringExtra("storeID")!!
        database.getReference("product/service/$storeID").addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                containerListService.visibility = View.GONE
                containerNoService.visibility = View.VISIBLE
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    if(snapshot.hasChildren()){
                        containerListService.visibility = View.VISIBLE
                        containerNoService.visibility = View.GONE

                        service = ArrayList()
                        service!!.clear()
                        recyclerListService!!.layoutManager = LinearLayoutManager(this@StoreActivity,LinearLayoutManager.VERTICAL,false)
                        for(services in snapshot.children){
                            val model = ServiceModel()
                            model.serviceID = services.key.toString()
                            model.serviceTitle = services.child("serviceName").getValue(String::class.java)
                            model.serviceDescription = services.child("serviceDescription").getValue(String::class.java)
                            service!!.add(model)
                        }

                        val adapter = UserServiceAdapter(this@StoreActivity,service!!)
                        recyclerListService.adapter = adapter
                    }else{
                        containerListService.visibility = View.GONE
                        containerNoService.visibility = View.VISIBLE
                    }
                }else{
                    containerListService.visibility = View.GONE
                    containerNoService.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun setupProductList() {
        val storeID = intent.getStringExtra("storeID")!!
        database.getReference("product/goods/$storeID").addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                containerListProduct.visibility = View.GONE
                containerNoProduct.visibility = View.VISIBLE
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    if(snapshot.hasChildren()){
                        containerListProduct.visibility = View.VISIBLE
                        containerNoProduct.visibility = View.GONE

                        product = ArrayList()
                        product!!.clear()
                        recyclerListProduct!!.layoutManager = GridLayoutManager(this@StoreActivity,2,GridLayoutManager.VERTICAL,false)
                        for(products in snapshot.children){
                            val model = ProductModel()
                            model.productID = products.key.toString()
                            model.productName = products.child("goodsName").getValue(String::class.java)
                            model.productDescription = products.child("goodsDescription").getValue(String::class.java)
                            model.productStock = products.child("goodsStock").getValue(Int::class.java)
                            try {
                                model.productPrice = products.child("goodsPrice").getValue(Double::class.java)!!
                            }catch (e:Exception){
                                model.productPrice = 0.0
                            }

                            var bannerProduct: ArrayList<BannerModel>
                            if(products.child("goodsPicture").exists()){
                                bannerProduct = ArrayList()
                                bannerProduct.clear()
                                for(images in products.child("goodsPicture").children){
                                    val banner = BannerModel()
                                    banner.bannerID = images.key.toString()
                                    banner.bannerImage = images.getValue(String::class.java)
                                    bannerProduct.add(banner)
                                }
                                model.productImage = bannerProduct
                            }
                            model.storeID = storeID
                            product!!.add(model)
                        }

                        val adapter = UserProductAdapter(this@StoreActivity,product!!)
                        recyclerListProduct.adapter = adapter
                    }else{
                        containerListProduct.visibility = View.GONE
                        containerNoProduct.visibility = View.VISIBLE
                    }
                }else{
                    containerListProduct.visibility = View.GONE
                    containerNoProduct.visibility = View.VISIBLE
                }
            }
        })
    }
}