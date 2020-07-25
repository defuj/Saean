package com.saean.app.menus.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.saean.app.notification.NotificationActivity
import com.saean.app.R
import com.saean.app.helper.Cache
import com.saean.app.helper.MyFunctions
import com.saean.app.home.nearbyStore.StoreAdapter
import com.saean.app.home.nearbyStore.StoreModel
import com.saean.app.home.promoSlider.PromoAdapter
import com.saean.app.home.promoSlider.PromoModel
import com.saean.app.messages.RoomListActivity
import com.saean.app.search.RecentSearchActivity
import kotlinx.android.synthetic.main.fragment_home.*
import ru.nikartm.support.ImageBadgeView

class HomeFragment : Fragment() {
    private lateinit var database: FirebaseDatabase
    private var sharedPreferences : SharedPreferences? = null
    private var promo : ArrayList<PromoModel>? = null
    private var storeNearby : ArrayList<StoreModel>? = null
    private var storeOthers : ArrayList<StoreModel>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        database = FirebaseDatabase.getInstance()
        sharedPreferences = activity!!.getSharedPreferences(Cache.cacheName,0)

        setupBadgesToolbar()
        setupFunctions()
    }

    private fun setupBadgesToolbar() {
        val menu = toolbarHome.menu

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

        val myStore = sharedPreferences!!.getString(Cache.storeID,"_")
        var unread = 0
        database.getReference("message").orderByChild("messageReceiver").equalTo(email).addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                badgesMessage.badgeValue = 0
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
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

        database.getReference("message").orderByChild("messageReceiver").equalTo(myStore).addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                badgesMessage.badgeValue = 0
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
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
            startActivity(Intent(activity!!, RoomListActivity::class.java))
        }

        badgesNotification.setOnClickListener {
            startActivity(Intent(activity!!, NotificationActivity::class.java))
        }
    }

    private fun setupFunctions() {
        goToSearch.setOnClickListener {
            startActivity(Intent(activity!!,RecentSearchActivity::class.java))
        }

        setupRefresh()
        setupSliderPromo()
        setupNearbyStore()
        setupOtherStore()
    }

    private fun setupOtherStore() {
        storeOthers = ArrayList()
        storeOthers!!.clear()
        recyclerOtherStore.layoutManager = LinearLayoutManager(activity,LinearLayoutManager.VERTICAL,false)

        /* for(i in 0 until 19){
            val model = StoreModel()
            model.storeID = "id${i+1}"
            model.storeName = "This is name of the Store ${i+1}"
            model.storeImage = "https://pemmzchannel.com/wp-content/uploads/2020/02/IT-Galeri-Store-1000x570.jpg"
            model.storeAddress = "Sumedang Utara"
            model.storeStatusOpen = true
            storeOthers!!.add(model)
        } */

        database.getReference("store").addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.hasChildren()){
                    for(content in snapshot.children){
                        val info = content.child("storeInfo")
                        val model = StoreModel()
                        model.storeID = content.key
                        model.storeName = info.child("storeName").getValue(String::class.java)
                        model.storeImage = info.child("storePicture").getValue(String::class.java)
                        model.storeAddress = info.child("storeAddress").getValue(String::class.java)
                        model.storeDescription = info.child("storeDescription").getValue(String::class.java)
                        model.storeStatusOpen = info.child("storeOpen").getValue(Boolean::class.java)
                        model.storeRating = info.child("storeRating").getValue(Float::class.java)
                        if(info.child("storeFront").exists()){
                            if(info.child("storeFront").hasChildren()){
                                var i = 0
                                for (sf in info.child("storeFront").children){
                                    if(i == 0){
                                        model.storeFront = sf.getValue(String::class.java)
                                        i+=1
                                    }
                                }
                            }else{
                                model.storeFront = ""
                            }
                        }else{
                            model.storeFront = ""
                        }
                        model.storeLatitude = info.child("storeLocation").child("latitude").getValue(Double::class.java)!!
                        model.storeLongitude = info.child("storeLocation").child("longitude").getValue(Double::class.java)!!
                        storeOthers!!.add(model)

                        if(recyclerOtherStore.adapter != null){
                            recyclerOtherStore.adapter!!.notifyDataSetChanged()
                        }
                    }

                    val adapter = StoreAdapter(activity!!,"vertical",storeOthers!!)
                    recyclerOtherStore.adapter = adapter
                }
            }
        })
    }

    private fun setupNearbyStore() {
        storeNearby = ArrayList()
        storeNearby!!.clear()
        storeNearbySlider.layoutManager = LinearLayoutManager(activity,LinearLayoutManager.HORIZONTAL,false)

        database.getReference("store").addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.hasChildren()){
                    for(content in snapshot.children){
                        val info = content.child("storeInfo")
                        val model = StoreModel()
                        model.storeID = content.key.toString()
                        model.storeName = info.child("storeName").getValue(String::class.java)
                        model.storeImage = info.child("storePicture").getValue(String::class.java)
                        model.storeAddress = info.child("storeAddress").getValue(String::class.java)
                        model.storeDescription = info.child("storeDescription").getValue(String::class.java)
                        model.storeStatusOpen = info.child("storeOpen").getValue(Boolean::class.java)
                        model.storeRating = info.child("storeRating").getValue(Float::class.java)
                        if(info.child("storeFront").exists()){
                            if(info.child("storeFront").hasChildren()){
                                var i = 0
                                for (sf in info.child("storeFront").children){
                                    if(i == 0){
                                        model.storeFront = sf.getValue(String::class.java)
                                        i+=1
                                    }
                                }
                            }else{
                                model.storeFront = ""
                            }
                        }else{
                            model.storeFront = ""
                        }
                        model.storeLatitude = info.child("storeLocation").child("latitude").getValue(Double::class.java)!!
                        model.storeLongitude = info.child("storeLocation").child("longitude").getValue(Double::class.java)!!
                        storeNearby!!.add(model)

                        if(storeNearbySlider.adapter != null){
                            storeNearbySlider.adapter!!.notifyDataSetChanged()
                        }
                    }

                    val adapter = StoreAdapter(activity!!,"horizontal",storeNearby!!)
                    storeNearbySlider.adapter = adapter
                }
            }
        })

    }

    private fun setupSliderPromo() {
        promo = ArrayList()
        promo!!.clear()

        database.getReference("config/promo").addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists() && snapshot.hasChildren()){
                    for(content in snapshot.children){
                        val model = PromoModel()
                        model.image = content.child("image").getValue(String::class.java)
                        promo!!.add(model)
                    }

                    indicator.indicatorsToShow = if(snapshot.childrenCount >= 5){
                        5
                    }else{
                        snapshot.childrenCount.toInt()
                    }
                    val adapter = PromoAdapter(promo!!)
                    promoSlider.adapter = adapter
                }
            }
        })
    }

    private fun setupRefresh() {
        refreshHome!!.setOnRefreshListener {
            object : CountDownTimer(2000,1000){
                override fun onFinish() {
                    setupSliderPromo()
                    setupNearbyStore()
                    setupOtherStore()
                    refreshHome!!.isRefreshing = false
                }

                override fun onTick(millisUntilFinished: Long) {

                }
            }.start()
        }
    }
}