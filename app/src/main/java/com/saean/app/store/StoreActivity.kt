package com.saean.app.store

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.github.florent37.viewtooltip.ViewTooltip
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.saean.app.NotificationActivity
import com.saean.app.R
import com.saean.app.helper.Cache
import com.saean.app.helper.MyComparator
import com.saean.app.helper.MyFunctions
import com.saean.app.home.promoSlider.PromoModel
import com.saean.app.messages.RoomDetailActivity
import com.saean.app.messages.RoomListActivity
import com.saean.app.store.model.*
import kotlinx.android.synthetic.main.activity_store.*
import ru.nikartm.support.ImageBadgeView
import java.time.LocalTime
import java.util.*
import kotlin.collections.ArrayList

class StoreActivity : AppCompatActivity() {
    private lateinit var database: FirebaseDatabase
    private var sharedPreferences : SharedPreferences? = null
    private var service : ArrayList<ServiceModel>? = null
    private var product : ArrayList<ProductModel>? = null
    private var slider : ArrayList<PromoModel>? = null
    private var schedule : ArrayList<ScheduleModel>? = null
    private var hasRated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store)
        database = FirebaseDatabase.getInstance()
        sharedPreferences = getSharedPreferences(Cache.cacheName,0)

        setupBadgesToolbar()
        setupToolTips()
        setupFunctions()
    }

    private fun setupToolTips() {
        ViewTooltip.on(this,btnInfoStore)
            .text("Klik untuk lihat info & Jadwal buka toko")
            .textColor(Color.WHITE)
            .color(Color.parseColor("#FD7946"))
            .shadowColor(Color.parseColor("#FD7946"))
            .corner(16)
            .arrowWidth(15)
            .arrowHeight(15)
            .distanceWithView(0)
            .position(ViewTooltip.Position.TOP)
            .align(ViewTooltip.ALIGN.START)
            .autoHide(true,5000)
            .clickToHide(true)
            .show()
    }

    private fun setupBadgesToolbar() {
        val menu = toolbarStore.menu

        val menuMessage = menu.getItem(1).setActionView(R.layout.item_badges_toolbar)
        val menuNotification = menu.getItem(2).setActionView(R.layout.item_badges_toolbar)

        val actionViewMessage = menuMessage.actionView
        val actionViewNotification = menuNotification.actionView

        val badgesMessage = actionViewMessage.findViewById<ImageBadgeView>(R.id.badges)
        badgesMessage.setImageResource(R.drawable.ic_menu_toolbar_home_messages)

        val badgesNotification = actionViewNotification.findViewById<ImageBadgeView>(R.id.badges)
        badgesNotification.setImageResource(R.drawable.ic_menu_toolbar_home_notification)

        val email = MyFunctions.changeToUnderscore(sharedPreferences!!.getString(Cache.email,"")!!)
        database.getReference("notification/$email").orderByChild("notificationStatus").equalTo("unread").addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                badgesNotification.badgeValue = 0
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    badgesNotification.badgeValue = snapshot.childrenCount.toInt()
                }else{
                    badgesNotification.badgeValue = 0
                }
            }
        })

        database.getReference("message").orderByChild("messageReceiver").equalTo(email).addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                badgesMessage.badgeValue = 0
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    var unread = 0
                    for (status in snapshot.children){
                        if(status.child("messageStatus").getValue(String::class.java)!! == "unread"){
                            unread +=1
                            badgesMessage.badgeValue = unread
                        }
                    }
                    badgesMessage.badgeValue = unread
                }else{
                    badgesMessage.badgeValue = 0
                }
            }
        })

        badgesMessage.setOnClickListener {
            startActivity(Intent(this, RoomListActivity::class.java))
        }

        badgesNotification.setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
        }

    }

    private fun setupFunctions() {
        toolbarStore.setNavigationOnClickListener {
            finish()
        }

        if(intent.getStringExtra("storeID")!! == sharedPreferences!!.getString(Cache.storeID,"")!!){
            sendChatToStore.visibility = View.GONE
            containerGiveRating.visibility = View.GONE
        }

        sendChatToStore.setOnClickListener {
            val intent = Intent(this,RoomDetailActivity::class.java)
            intent.putExtra("storeID",intent.getStringExtra("storeID")!!)
            startActivity(intent)
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
        setupFavorite()
        //setupSchedule()
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

                        recyclerOpeningHours!!.layoutManager = LinearLayoutManager(this@StoreActivity,LinearLayoutManager.VERTICAL,false)
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

                        val adapter = ScheduleAdapter(this@StoreActivity,schedule!!)
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

    private fun setupFavorite() {
        val storeID = intent.getStringExtra("storeID")!!
        val email = MyFunctions.changeToUnderscore(sharedPreferences!!.getString(Cache.email,"")!!)

        database.getReference("favorite/store/$email").child(storeID).addValueEventListener(object :ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    btnAddFavorite.setImageResource(R.drawable.ic_button_favorite_activated)
                    btnAddFavorite.setOnClickListener {
                        database.getReference("favorite/store/$email").child(storeID).removeValue()
                        Toast.makeText(this@StoreActivity,"Dihapus dari daftar toko favorit",Toast.LENGTH_SHORT).show()
                    }
                }else{
                    btnAddFavorite.setImageResource(R.drawable.ic_button_favorite_not_activate)
                    btnAddFavorite.setOnClickListener {
                        database.getReference("favorite/store/$email").child(storeID).setValue(true)
                        Toast.makeText(this@StoreActivity,"Ditambahkan ke favorit",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
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
                        val total = snapshot.childrenCount.toFloat()
                        var rateTotal = 0.0
                        for (rate in snapshot.children){
                            rateTotal += rate.getValue(Float::class.java)!!
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
                        val today = MyFunctions.getTanggal("EEEE")
                        val schedule = database.getReference("store/$storeID/storeSchedule/$today")
                        schedule.addValueEventListener(object : ValueEventListener{
                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(this@StoreActivity,"Failed to connect",Toast.LENGTH_SHORT).show()
                                storeStatusOpen.visibility = View.VISIBLE
                                storeStatusOpen.text = "Closed"
                                storeStatusOpen.setBackgroundResource(R.drawable.background_status_store_closed)
                            }

                            @RequiresApi(Build.VERSION_CODES.O)
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if(snapshot.exists()){
                                    if(snapshot.child("status").getValue(Boolean::class.java)!!){
                                        val target = LocalTime.parse(MyFunctions.getTanggal("HH:mm:ss"))
                                        val start = MyFunctions.formatTanggal(snapshot.child("startOpen").getValue(String::class.java)!!,"HH:mm:ss","HH.mm")
                                        val end = MyFunctions.formatTanggal(snapshot.child("endOpen").getValue(String::class.java)!!,"HH:mm:ss","HH.mm")
                                        val targetInZone = target.isAfter(LocalTime.parse(start)) && target.isBefore(
                                            LocalTime.parse(end))
                                        if(targetInZone){
                                            storeStatusOpen.visibility = View.GONE
                                            storeStatusOpen.text = "Open"
                                            storeStatusOpen.setBackgroundResource(R.drawable.background_status_store_closed)
                                        }else{
                                            storeStatusOpen.visibility = View.VISIBLE
                                            storeStatusOpen.text = "Closed"
                                            storeStatusOpen.setBackgroundResource(R.drawable.background_status_store_closed)
                                        }
                                    }else{
                                        storeStatusOpen.visibility = View.VISIBLE
                                        storeStatusOpen.text = "Closed"
                                        storeStatusOpen.setBackgroundResource(R.drawable.background_status_store_closed)
                                    }
                                }else{
                                    storeStatusOpen.visibility = View.VISIBLE
                                    storeStatusOpen.text = "Closed"
                                    storeStatusOpen.setBackgroundResource(R.drawable.background_status_store_closed)
                                }
                            }

                        })

                        storeName.text = snapshot.child("storeName").getValue(String::class.java)
                        storeAddress.text = snapshot.child("storeAddress").getValue(String::class.java)

                        if(snapshot.child("storeFront").exists()){
                            if(snapshot.child("storeFront").hasChildren()){
                                slider = ArrayList()
                                slider!!.clear()

                                for(img in snapshot.child("storeFront").children){
                                    val model = PromoModel()
                                    model.image = img.getValue(String::class.java)
                                    slider!!.add(model)
                                }

                                indicatorStoreSlider.indicatorsToShow = if(snapshot.child("storeFront").childrenCount >= 5){
                                    5
                                }else{
                                    snapshot.child("storeFront").childrenCount.toInt()
                                }

                                val adapter = StoreFrontAdapter(slider!!)
                                storeFrontSlider.adapter = adapter
                            }
                        }
                        storeRating.text = MyFunctions.formatDistance(snapshot.child("storeRating").getValue(Float::class.java)!!.toDouble())
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
                                    //dialog.show()

                                    val intent = Intent(this@StoreActivity,StoreInfoActivity::class.java)
                                    intent.putExtra("storeID",storeID)
                                    startActivity(intent)
                                }
                            }else{
                                btnInfoStore.visibility = View.GONE
                            }
                        }else{
                            btnInfoStore.visibility = View.GONE
                        }
                        if(snapshot.child("storeStatus").getValue(Boolean::class.java)!!){
                            storeVerified.visibility = View.VISIBLE
                            storeName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_store_verified_1,0,0,0)
                        }else{
                            storeVerified.visibility = View.INVISIBLE
                            storeName.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0)
                        }
                        if(snapshot.child("storePicture").getValue(String::class.java)!!.isNotEmpty()){
                            Glide.with(this@StoreActivity)
                                .load(snapshot.child("storePicture").getValue(String::class.java)!!)
                                .apply(RequestOptions.circleCropTransform())
                                .into(storeIcon)
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
                            model.storeID = storeID
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