package com.saean.app.messages

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.saean.app.R
import com.saean.app.helper.Cache
import com.saean.app.helper.MyFunctions
import kotlinx.android.synthetic.main.activity_room_list.*

class RoomListActivity : AppCompatActivity() {
    private var room : ArrayList<RoomModel>? = null

    private var sharedPreferences : SharedPreferences? = null
    private lateinit var database: FirebaseDatabase
    private var contentType = "account"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_list)
        sharedPreferences = getSharedPreferences(Cache.cacheName,0)
        database = FirebaseDatabase.getInstance()

        setupFunction()
    }

    private fun setupFunction() {
        toolbarRoomList.setNavigationOnClickListener {
            finish()
        }
        if(sharedPreferences!!.getString(Cache.storeID,"")!!.isNotEmpty()){
            contentType = "store"
            tabChat.visibility = View.VISIBLE
            tabChat.getTabAt(0)!!.select()
            loadRoomStore()
        }else{
            contentType = "account"
            tabChat.visibility = View.GONE
            tabChat.getTabAt(1)!!.select()
            loadRoomAccount()
        }

        tabChat.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                if(tab!!.position == 0){
                    contentType = "store"
                    loadRoomStore()
                }else{
                    contentType = "account"
                    loadRoomAccount()
                }
            }
        })

    }

    private fun loadRoomStore() {
        val storeID = sharedPreferences!!.getString(Cache.storeID,"")!!
        database.getReference("store/$storeID/storeMessage").addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                recyclerRoom.visibility = View.GONE
                containerNoRooms.visibility = View.VISIBLE
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    if(snapshot.hasChildren()){
                        containerNoRooms.visibility = View.GONE
                        recyclerRoom.visibility = View.VISIBLE
                        room = ArrayList()
                        room!!.clear()
                        recyclerRoom.layoutManager = LinearLayoutManager(this@RoomListActivity,LinearLayoutManager.VERTICAL,false)

                        for(content in snapshot.children){
                            database.getReference("message").orderByChild("messageRoom").equalTo(content.key.toString()).limitToLast(1).addValueEventListener(object : ValueEventListener{
                                override fun onCancelled(error: DatabaseError) {

                                }

                                override fun onDataChange(snapshot: DataSnapshot) {
                                    for(message in snapshot.children){
                                        val model = RoomModel()
                                        model.roomID = content.key.toString()
                                        model.storeID = message.child("messageStore").getValue(String::class.java)
                                        model.user = message.child("messageUser").getValue(String::class.java)
                                        model.type = "user"
                                        room!!.add(model)

                                        if(recyclerRoom.adapter != null){
                                            recyclerRoom.adapter!!.notifyDataSetChanged()
                                        }
                                    }
                                }
                            })
                        }

                        room!!.reverse()
                        val adapter = RoomListAdapter(this@RoomListActivity,room!!)
                        recyclerRoom.adapter = adapter
                    }else{
                        recyclerRoom.visibility = View.GONE
                        containerNoRooms.visibility = View.VISIBLE
                    }
                }else{
                    recyclerRoom.visibility = View.GONE
                    containerNoRooms.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun loadRoomAccount() {
        val email = MyFunctions.changeToUnderscore(sharedPreferences!!.getString(Cache.email,"")!!)
        database.getReference("user/$email/userMessage").addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                recyclerRoom.visibility = View.GONE
                containerNoRooms.visibility = View.VISIBLE
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    if(snapshot.hasChildren()){
                        containerNoRooms.visibility = View.GONE
                        recyclerRoom.visibility = View.VISIBLE
                        room = ArrayList()
                        room!!.clear()
                        recyclerRoom.layoutManager = LinearLayoutManager(this@RoomListActivity,LinearLayoutManager.VERTICAL,false)

                        for(content in snapshot.children){
                            database.getReference("message").orderByChild("messageRoom").equalTo(content.key.toString()).limitToLast(1).addValueEventListener(object : ValueEventListener{
                                override fun onCancelled(error: DatabaseError) {

                                }

                                override fun onDataChange(snapshot: DataSnapshot) {
                                    for(message in snapshot.children){
                                        val model = RoomModel()
                                        model.roomID = content.key.toString()
                                        model.storeID = message.child("messageStore").getValue(String::class.java)
                                        model.user = message.child("messageUser").getValue(String::class.java)
                                        model.type = "store"
                                        room!!.add(model)

                                        if(recyclerRoom.adapter != null){
                                            recyclerRoom.adapter!!.notifyDataSetChanged()
                                        }
                                    }

                                }
                            })
                        }

                        room!!.reverse()
                        val adapter = RoomListAdapter(this@RoomListActivity,room!!)
                        recyclerRoom.adapter = adapter
                    }else{
                        recyclerRoom.visibility = View.GONE
                        containerNoRooms.visibility = View.VISIBLE
                    }
                }else{
                    recyclerRoom.visibility = View.GONE
                    containerNoRooms.visibility = View.VISIBLE
                }
            }
        })
    }
}