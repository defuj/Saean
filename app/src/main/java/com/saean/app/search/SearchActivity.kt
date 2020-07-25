package com.saean.app.search

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.saean.app.R
import com.saean.app.helper.Cache
import com.saean.app.helper.MyFunctions
import com.saean.app.home.nearbyStore.StoreAdapter
import com.saean.app.home.nearbyStore.StoreModel
import com.saean.app.messages.RoomListActivity
import com.saean.app.notification.NotificationActivity
import kotlinx.android.synthetic.main.activity_search.*
import ru.nikartm.support.ImageBadgeView

class SearchActivity : AppCompatActivity() {
    private var storeOthers : ArrayList<StoreModel>? = null
    private lateinit var database: FirebaseDatabase
    private var sharedPreferences : SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        database = FirebaseDatabase.getInstance()
        sharedPreferences = getSharedPreferences(Cache.cacheName,0)

        try {
            searchEdit.text = intent.getStringExtra("search")
        }catch (e:Exception){

        }

        setupFunction()
        setupBadgesToolbar()
        setupSearchResult()
    }

    private fun setupSearchResult() {
        if(searchEdit.text.toString().isNotEmpty()){
            storeOthers = ArrayList()
            storeOthers!!.clear()
            recyclerSearch.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false)

            var resultCount = 0
            database.getReference("store").addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.hasChildren()){
                        for(content in snapshot.children){
                            val storeInfo = content.child("storeInfo")

                            //check service
                            database.getReference("product/service/${content.key}").addListenerForSingleValueEvent(object : ValueEventListener{
                                override fun onCancelled(error: DatabaseError) {

                                }

                                @SuppressLint("SetTextI18n")
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if(snapshot.exists()){
                                        if(snapshot.hasChildren()){
                                            var found = 0
                                            for(serviceInfo in snapshot.children){
                                                val search = searchEdit.text.toString()
                                                val resultDesc = serviceInfo.child("serviceDescription").getValue(String::class.java)
                                                val resultName = serviceInfo.child("serviceName").getValue(String::class.java)
                                                if(resultName!!.contains(search) || resultDesc!!.contains(search)){
                                                    if(found == 0){
                                                        val model = StoreModel()
                                                        model.storeID = content.key
                                                        model.storeName = storeInfo.child("storeName").getValue(String::class.java)
                                                        model.storeImage = storeInfo.child("storePicture").getValue(String::class.java)
                                                        model.storeAddress = storeInfo.child("storeAddress").getValue(String::class.java)
                                                        model.storeDescription = storeInfo.child("storeDescription").getValue(String::class.java)
                                                        model.storeStatusOpen = storeInfo.child("storeOpen").getValue(Boolean::class.java)
                                                        model.storeRating = storeInfo.child("storeRating").getValue(Float::class.java)
                                                        if(storeInfo.child("storeFront").exists()){
                                                            if(storeInfo.child("storeFront").hasChildren()){
                                                                var i = 0
                                                                for (sf in storeInfo.child("storeFront").children){
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
                                                        model.storeLatitude = storeInfo.child("storeLocation").child("latitude").getValue(Double::class.java)!!
                                                        model.storeLongitude = storeInfo.child("storeLocation").child("longitude").getValue(Double::class.java)!!
                                                        storeOthers!!.add(model)
                                                        resultCount +=1

                                                        if(recyclerSearch.adapter != null){
                                                            recyclerSearch.adapter!!.notifyDataSetChanged()

                                                            if(storeOthers!!.size > 0){
                                                                txtResult.visibility = View.VISIBLE
                                                                txtResult.text = "$resultCount hasil pencarian ditemukan dengan kata kunci '${searchEdit.text}'."
                                                                recyclerSearch.visibility = View.VISIBLE
                                                                containerNoSearch.visibility = View.GONE
                                                            }else{
                                                                txtResult.visibility = View.GONE
                                                                recyclerSearch.visibility = View.GONE
                                                                containerNoSearch.visibility = View.VISIBLE
                                                                txtNoResult.text = "Tidak ada hasil pencarian dengan kata kunci '${searchEdit.text}'."
                                                            }
                                                        }
                                                        found +=1
                                                    }
                                                }

                                                if(storeOthers!!.size > 0){
                                                    txtResult.visibility = View.VISIBLE
                                                    txtResult.text = "$resultCount hasil pencarian ditemukan dengan kata kunci '${searchEdit.text}'."
                                                    recyclerSearch.visibility = View.VISIBLE
                                                    containerNoSearch.visibility = View.GONE
                                                }else{
                                                    txtResult.visibility = View.GONE
                                                    recyclerSearch.visibility = View.GONE
                                                    containerNoSearch.visibility = View.VISIBLE
                                                    txtNoResult.text = "Tidak ada hasil pencarian dengan kata kunci '${searchEdit.text}'."
                                                }
                                            }
                                        }
                                    }
                                }
                            })
                        }

                        val adapter = StoreAdapter(this@SearchActivity,"vertical",storeOthers!!)
                        recyclerSearch.adapter = adapter
                    }
                }
            })
        }
    }

    private fun setupFunction() {
        toolbarSearch.setNavigationOnClickListener { finish() }

        searchEdit.setOnClickListener {
            val intent = Intent(this,RecentSearchActivity::class.java)
            intent.putExtra("search",searchEdit.text.toString())
            startActivity(intent)
        }
    }

    private fun setupBadgesToolbar() {
        val menu = toolbarSearch.menu

        val menuMessage = menu.getItem(1).setActionView(R.layout.item_badges_toolbar)
        val menuNotification = menu.getItem(2).setActionView(R.layout.item_badges_toolbar)

        val actionViewMessage = menuMessage.actionView
        val actionViewNotification = menuNotification.actionView

        val badgesMessage = actionViewMessage.findViewById<ImageBadgeView>(R.id.badges)
        badgesMessage.setImageResource(R.drawable.ic_menu_toolbar_home_messages)

        val badgesNotification = actionViewNotification.findViewById<ImageBadgeView>(R.id.badges)
        badgesNotification.setImageResource(R.drawable.ic_menu_toolbar_home_notification)

        val email = MyFunctions.changeToUnderscore(sharedPreferences!!.getString(Cache.email,"")!!)
        database.getReference("notification/$email").orderByChild("notificationStatus").equalTo("unread").addValueEventListener(object :
            ValueEventListener {
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
        database.getReference("message").orderByChild("messageReceiver").equalTo(email).addValueEventListener(object :
            ValueEventListener {
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

        database.getReference("message").orderByChild("messageReceiver").equalTo(myStore).addValueEventListener(object :
            ValueEventListener {
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
            startActivity(Intent(this, RoomListActivity::class.java))
        }

        badgesNotification.setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
        }

    }
}