package com.saean.app.messages

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.saean.app.R
import com.saean.app.helper.Cache
import com.saean.app.helper.MyFunctions
import kotlinx.android.synthetic.main.item_list_room_chat.view.*

class RoomListAdapter(private val context: Context, private val room: ArrayList<RoomModel>) :
    RecyclerView.Adapter<RoomListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View? = LayoutInflater.from(parent.context).inflate(R.layout.item_list_room_chat,parent,false)
        return ViewHolder(v!!)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val content = room[position]
        val sharedPreferences : SharedPreferences = context.getSharedPreferences(Cache.cacheName,0)
        val email = MyFunctions.changeToUnderscore(sharedPreferences.getString(Cache.email,"")!!)
        val database : FirebaseDatabase = FirebaseDatabase.getInstance()

        holder.itemView.run {
            if(content.type == "store"){
                //get store info
                database.getReference("store/${content.storeID}/storeInfo").addValueEventListener(object : ValueEventListener{
                    override fun onCancelled(error: DatabaseError) {

                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.child("storePicture").getValue(String::class.java)!!.isNotEmpty()){
                            Glide.with(context)
                                .load(snapshot.child("storePicture").getValue(String::class.java)!!)
                                .apply(RequestOptions.circleCropTransform())
                                .into(roomPicture)
                        }else{
                            Glide.with(context)
                                .load(R.drawable.image_placeholder_circle)
                                .apply(RequestOptions.circleCropTransform())
                                .into(roomPicture)
                        }
                        roomName.text = snapshot.child("storeName").getValue(String::class.java)!!
                        if(snapshot.child("storeStatus").getValue(Boolean::class.java)!!){
                            roomName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_store_verified_1,0,0,0)
                        }else{
                            roomName.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0)
                        }
                    }
                })

                //get last message
                database.getReference("message").orderByChild("messageStore").equalTo(content.storeID).limitToLast(1).addValueEventListener(object : ValueEventListener{
                    override fun onCancelled(error: DatabaseError) {

                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.hasChildren()){
                            for (message in snapshot.children){
                                roomLastMessage.text = if(message.child("messageType").getValue(String::class.java)!! == "text"){
                                    message.child("messageContent").getValue(String::class.java)
                                }else{
                                    "Gambar"
                                }
                                roomTime.text = MyFunctions.formatMillie(message.child("messageTime").getValue(Long::class.java)!!,"dd MMM")
                            }
                        }
                    }
                })

                //count unread message
                database.getReference("message").orderByChild("messageStore").equalTo(content.storeID).limitToLast(1).addValueEventListener(object : ValueEventListener{
                    override fun onCancelled(error: DatabaseError) {

                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.hasChildren()){
                            var counter = 0
                            for (message in snapshot.children){
                                if(message.child("messageStatus").getValue(String::class.java)!! == "unread"){
                                    if(message.child("messageReceiver").getValue(String::class.java)!! == email){
                                        counter +=1
                                        if(counter > 0){
                                            roomUnreadCount.text = if(counter > 99){"99+"}else{"$counter"}
                                            roomUnreadCount.visibility = View.VISIBLE
                                        }else{
                                            roomUnreadCount.visibility = View.INVISIBLE
                                        }
                                    }
                                }
                            }

                            if(counter > 0){
                                roomUnreadCount.text = if(counter > 99){"99+"}else{"$counter"}
                                roomUnreadCount.visibility = View.VISIBLE
                            }else{
                                roomUnreadCount.visibility = View.INVISIBLE
                            }
                        }
                    }
                })
            }else{

            }
        }

        holder.itemView.setOnClickListener {

        }
    }

    override fun getItemCount() = room.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
