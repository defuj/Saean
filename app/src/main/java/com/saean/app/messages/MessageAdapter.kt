package com.saean.app.messages

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.Gravity
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
import kotlinx.android.synthetic.main.item_list_chat.view.*
import kotlinx.android.synthetic.main.item_list_room_chat.view.*

class MessageAdapter(private val context: Context, private val message: ArrayList<MessageModel>) :
    RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View? = LayoutInflater.from(parent.context).inflate(R.layout.item_list_chat,parent,false)
        return ViewHolder(v!!)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val content = message[position]
        val sharedPreferences : SharedPreferences = context.getSharedPreferences(Cache.cacheName,0)
        val email = MyFunctions.changeToUnderscore(sharedPreferences.getString(Cache.email,"")!!)
        val myStore = sharedPreferences.getString(Cache.storeID,"_")!!
        val database : FirebaseDatabase = FirebaseDatabase.getInstance()

        holder.itemView.run {
            if(content.messageSender == email || content.messageSender == myStore){
                messageContainer.setBackgroundResource(R.drawable.background_user_1_message)
                messageMainContainer.gravity = Gravity.END
            }else{
                messageContainer.setBackgroundResource(R.drawable.background_user_2_message)
                messageMainContainer.gravity = Gravity.START
            }

            if(content.messageType == "text"){
                //type text
                messageImage.visibility = View.GONE
                messageContent.visibility = View.VISIBLE
                messageContent.text = content.messageContent
            }else{
                //type image
                messageImage.visibility = View.VISIBLE
                messageContent.visibility = View.GONE
                Glide.with(context)
                    .load(content.messageContent)
                    .into(messageImage)
            }

            val today = MyFunctions.getTanggal("dd-MM-yyyy")
            val messageDay = MyFunctions.formatMillie(content.messageTime!!,"dd-MM-yyyy")
            if(today == messageDay){
                messageTime.text = MyFunctions.formatMillie(content.messageTime!!,"hh:mm")
            }else{
                messageTime.text = MyFunctions.formatMillie(content.messageTime!!,"dd MMM hh:mm")
            }

            if(content.messageStatus == "unread"){
                messageTime.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_chat_sent,0)
            }else{
                messageTime.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_chat_read,0)
            }
        }

    }

    override fun getItemCount() = message.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
