package com.saean.app.menus.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.saean.app.NotificationActivity
import com.saean.app.R
import com.saean.app.helper.Cache
import com.saean.app.helper.MyFunctions
import com.saean.app.home.nearbyStore.StoreAdapter
import com.saean.app.home.nearbyStore.StoreModel
import com.saean.app.messages.RoomListActivity
import kotlinx.android.synthetic.main.fragment_favorite.*
import kotlinx.android.synthetic.main.fragment_home.*
import ru.nikartm.support.ImageBadgeView


class FavoriteFragment : Fragment() {
    private lateinit var database: FirebaseDatabase
    private var sharedPreferences : SharedPreferences? = null
    private var store : ArrayList<StoreModel>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favorite, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        database = FirebaseDatabase.getInstance()
        sharedPreferences = activity!!.getSharedPreferences(Cache.cacheName,0)

        setupBadgesToolbar()
        setupFunctions()
    }

    private fun setupBadgesToolbar() {
        val menu = toolbarFavorite.menu

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
            startActivity(Intent(activity!!, RoomListActivity::class.java))
        }

        badgesNotification.setOnClickListener {
            startActivity(Intent(activity!!, NotificationActivity::class.java))
        }
    }

    private fun setupFunctions() {

        setupRefresh()
        setupList()
    }

    private fun setupList() {
        val email = MyFunctions.changeToUnderscore(sharedPreferences!!.getString(Cache.email,"")!!)
        database.getReference("favorite/store/$email").addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                recyclerFavorite.visibility = View.GONE
                containerNoFavorite.visibility = View.VISIBLE
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    if(snapshot.hasChildren()){
                        recyclerFavorite.visibility = View.VISIBLE
                        containerNoFavorite.visibility = View.GONE

                        store = ArrayList()
                        store!!.clear()
                        recyclerFavorite.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL,false)

                        for(content in snapshot.children){
                            val storeID = content.key.toString()
                            database.getReference("store/$storeID").addValueEventListener(object : ValueEventListener{
                                override fun onCancelled(error: DatabaseError) {

                                }

                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val info = snapshot.child("storeInfo")
                                    val model = StoreModel()
                                    model.storeID = snapshot.key
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
                                    store!!.add(model)

                                    if(recyclerFavorite.adapter != null){
                                        recyclerFavorite.adapter!!.notifyDataSetChanged()
                                    }
                                }
                            })
                        }
                        val adapter = StoreAdapter(activity!!,"vertical",store!!)
                        recyclerFavorite.adapter = adapter
                    }else{
                        recyclerFavorite.visibility = View.GONE
                        containerNoFavorite.visibility = View.VISIBLE
                    }
                }else{
                    recyclerFavorite.visibility = View.GONE
                    containerNoFavorite.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun setupRefresh() {
        refreshFavorite.setOnRefreshListener {
            object : CountDownTimer(3000,1000){
                override fun onFinish() {
                    setupList()
                    refreshFavorite.isRefreshing = false
                }

                override fun onTick(millisUntilFinished: Long) {

                }
            }.start()
        }
    }
}