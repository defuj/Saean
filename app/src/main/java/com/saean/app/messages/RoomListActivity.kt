package com.saean.app.messages

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
            loadRoomStore()
        }else{
            contentType = "account"
            tabChat.visibility = View.GONE
            loadRoomAccount()
        }

    }

    private fun loadRoomStore() {

    }

    private fun loadRoomAccount() {
        val email = MyFunctions.changeToUnderscore(sharedPreferences!!.getString(Cache.email,"")!!)
        database.getReference("user/$email/userMessage").addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                recyclerRoom.visibility = View.GONE
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    if(snapshot.hasChildren()){
                        recyclerRoom.visibility = View.VISIBLE
                        room = ArrayList()
                        room!!.clear()
                        recyclerRoom.layoutManager = LinearLayoutManager(this@RoomListActivity,LinearLayoutManager.VERTICAL,false)

                        for(content in snapshot.children){
                            val model = RoomModel()
                            model.storeID = content.key.toString()
                            model.type = "store"
                            model.user = email
                            room!!.add(model)
                        }

                        room!!.reverse()
                        val adapter = RoomListAdapter(this@RoomListActivity,room!!)
                        recyclerRoom.adapter = adapter
                    }else{
                        recyclerRoom.visibility = View.GONE
                    }
                }else{
                    recyclerRoom.visibility = View.GONE
                }
            }
        })
    }
}