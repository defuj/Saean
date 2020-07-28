package com.saean.app.messages

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.saean.app.R
import com.saean.app.helper.Cache
import com.saean.app.helper.MyFunctions
import com.theartofdev.edmodo.cropper.CropImage
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions
import kotlinx.android.synthetic.main.activity_room_detail.*
import java.util.*
import kotlin.collections.ArrayList

class RoomDetailActivity : AppCompatActivity() {
    private var messageModel : ArrayList<MessageModel>? = null

    private var roomID = ""
    private var user = ""
    private var store = ""
    private var receiver = ""

    private var sharedPreferences : SharedPreferences? = null
    private lateinit var database: FirebaseDatabase
    private lateinit var storage : FirebaseStorage
    private lateinit var storageReference : StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_detail)
        sharedPreferences = getSharedPreferences(Cache.cacheName,0)
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference

        startSetup()
    }

    private fun startSetup() {
        if(MyFunctions.getConnectivityStatus(this)){
            setupRoom()
            setupFunction()
            getMessages()
        }else{
            val dialog = SweetAlertDialog(this,SweetAlertDialog.ERROR_TYPE)
            dialog.titleText = "Failed"
            dialog.contentText = "Koneksi internet bermasalah, silahkan coba lagi!"
            dialog.confirmText = "Coba lagi"
            dialog.cancelText = "Tutup"
            dialog.setCancelable(false)
            dialog.setConfirmClickListener {
                dialog.dismissWithAnimation()
                startSetup()
            }
            dialog.setCancelClickListener {
                dialog.dismissWithAnimation()
                finish()
            }
            dialog.show()
        }
    }

    private fun getMessages() {
        val email = MyFunctions.changeToUnderscore(sharedPreferences!!.getString(Cache.email,"")!!)
        database.getReference("message").orderByChild("messageRoom").equalTo(roomID).addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                recyclerChat.visibility = View.INVISIBLE
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    if(snapshot.hasChildren()){
                        recyclerChat.visibility = View.VISIBLE
                        messageModel = ArrayList()
                        messageModel!!.clear()
                        val lm = LinearLayoutManager(this@RoomDetailActivity, LinearLayoutManager.VERTICAL,true)
                        recyclerChat.layoutManager = lm

                        for(content in snapshot.children){
                            val model = MessageModel()
                            model.messageID = content.key.toString()
                            model.messageContent = content.child("messageContent").getValue(String::class.java)
                            model.messageReceiver = content.child("messageReceiver").getValue(String::class.java)
                            model.messageSender = content.child("messageSender").getValue(String::class.java)
                            model.messageStatus = content.child("messageStatus").getValue(String::class.java)
                            model.messageStore = content.child("messageStore").getValue(String::class.java)
                            model.messageType = content.child("messageType").getValue(String::class.java)
                            model.messageUser = content.child("messageUser").getValue(String::class.java)
                            model.messageRoom = content.child("messageRoom").getValue(String::class.java)
                            model.messageTime = content.child("messageTime").getValue(Long::class.java)
                            messageModel!!.add(model)

                            val myStore = sharedPreferences!!.getString(Cache.storeID,"_")
                            if(sharedPreferences!!.getString(Cache.activeRoom,"")!! == content.child("messageRoom").getValue(String::class.java)){
                                if(content.child("messageReceiver").getValue(String::class.java)!! == email ||
                                    content.child("messageReceiver").getValue(String::class.java)!! == myStore){
                                    database
                                        .getReference("message")
                                        .child(content.key.toString())
                                        .child("messageStatus").setValue("read")
                                }
                            }
                        }

                        messageModel!!.reverse()
                        val adapter = MessageAdapter(this@RoomDetailActivity,messageModel!!)
                        recyclerChat.adapter = adapter
                    }else{
                        recyclerChat.visibility = View.INVISIBLE
                    }
                }else{
                    recyclerChat.visibility = View.INVISIBLE
                }
            }
        })
    }

    private fun setupRoom() {
        roomID = intent.getStringExtra("roomID")!!
        user = intent.getStringExtra("user")!!
        store = intent.getStringExtra("store")!!
        receiver = intent.getStringExtra("receiver")!!

        val edit = sharedPreferences!!.edit()
        edit.putString(Cache.activeRoom,roomID)
        edit.apply()

        if(receiver == "store"){
            database.getReference("store/$store/storeInfo").addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    roomName.text = snapshot.child("storeName").getValue(String::class.java)
                    if(snapshot.child("storeStatus").getValue(Boolean::class.java)!!){
                        roomName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_store_verified_1,0,0,0)
                    }else{
                        roomName.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0)
                    }
                    if(snapshot.child("storePicture").getValue(String::class.java)!!.isNotEmpty()){
                        Glide.with(this@RoomDetailActivity)
                            .load(snapshot.child("storePicture").getValue(String::class.java))
                            .placeholder(R.drawable.image_placeholder_circle)
                            .apply(RequestOptions.circleCropTransform())
                            .into(roomPicture)
                    }else{
                        Glide.with(this@RoomDetailActivity)
                            .load(R.drawable.image_placeholder_circle)
                            .apply(RequestOptions.circleCropTransform())
                            .into(roomPicture)
                    }

                    //check admin is online
                    val user = MyFunctions.changeToUnderscore(snapshot.child("storeAdmin").getValue(String::class.java)!!)
                    database.getReference("user/$user").addValueEventListener(object : ValueEventListener{
                        override fun onCancelled(error: DatabaseError) {

                        }

                        @SuppressLint("SetTextI18n")
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if(snapshot.exists()){
                                if(snapshot.child("userOnlineStatus").exists()){
                                    if(snapshot.child("userOnlineStatus").getValue(Boolean::class.java)!!){
                                        roomOnline.text = "Sedang Online"
                                        roomOnline.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_online_24dp,0,0,0)
                                    }else{
                                        roomOnline.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0)
                                        roomOnline.text = "Terakhir online ${MyFunctions.formatMillie(snapshot.child("userOnlineTime").getValue(Long::class.java)!!,"dd MMMM yyyy hh:mm")}"
                                    }
                                }else{
                                    roomOnline.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0)
                                    roomOnline.text = "Sedang Offline"
                                }
                            }else{
                                finish()
                            }
                        }
                    })
                }
            })
        }else{
            database.getReference("user/$user").addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {

                }

                @SuppressLint("SetTextI18n")
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        roomName.text = snapshot.child("userName").getValue(String::class.java)
                        if(snapshot.child("userPicture").getValue(String::class.java)!!.isNotEmpty()){
                            if(snapshot.child("userPicture").getValue(String::class.java)!!.isNotEmpty()){
                                Glide.with(this@RoomDetailActivity)
                                    .load(snapshot.child("userPicture").getValue(String::class.java))
                                    .placeholder(R.drawable.image_placeholder_circle)
                                    .apply(RequestOptions.circleCropTransform())
                                    .into(roomPicture)
                            }else{
                                Glide.with(this@RoomDetailActivity)
                                    .load(R.drawable.image_placeholder_circle)
                                    .apply(RequestOptions.circleCropTransform())
                                    .into(roomPicture)
                            }

                            if(snapshot.child("userOnlineStatus").exists()){
                                if(snapshot.child("userOnlineStatus").getValue(Boolean::class.java)!!){
                                    roomOnline.text = "Sedang Online"
                                    roomOnline.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_online_24dp,0,0,0)
                                }else{
                                    roomOnline.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0)
                                    roomOnline.text = "Terakhir online ${MyFunctions.formatMillie(snapshot.child("userOnlineTime").getValue(Long::class.java)!!,"dd MMMM yyyy hh:mm")}"
                                }
                            }else{
                                roomOnline.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0)
                                roomOnline.text = "Sedang Offline"
                            }
                        }

                    }else{
                        finish()
                    }
                }
            })
        }
    }

    private fun setupFunction() {
        val rootView : View = findViewById(R.id.layoutMessage)
        val emoticonIcon = EmojIconActions(this,rootView,messageContent,openEmoticon)
        emoticonIcon.setUseSystemEmoji(false)
        openEmoticon.setOnClickListener {
            emoticonIcon.ShowEmojIcon()
        }

        toolbarRoom.setNavigationOnClickListener {
            finish()
        }

        toolbarRoom.setOnMenuItemClickListener {
            if(it.itemId == R.id.menu_end_chat){
                val dialog = SweetAlertDialog(this,SweetAlertDialog.WARNING_TYPE)
                dialog.titleText = "Perhatian"
                dialog.contentText = "Anda ingin mengakhiri percakapan ini dan menghapusnya?"
                dialog.confirmText = "Iya"
                dialog.cancelText = "Tidak"
                dialog.setConfirmClickListener {
                    dialog.dismissWithAnimation()
                    val progress = SweetAlertDialog(this,SweetAlertDialog.PROGRESS_TYPE)
                    progress.titleText = "Silahkan tunggu"
                    progress.setCancelable(false)
                    progress.show()

                    //delete from store
                    database.getReference("store/$store/storeMessage").child(roomID).removeValue()
                    //delete from user
                    database.getReference("user/$user/userMessage").child(roomID).removeValue()
                    //delete all message
                    database.getReference("message").orderByChild("messageRoom").equalTo(roomID).addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onCancelled(error: DatabaseError) {

                        }

                        override fun onDataChange(snapshot: DataSnapshot) {
                            if(snapshot.exists()){
                                var counter = 0
                                for(content in snapshot.children){
                                    database.getReference("message").child(content.key!!).removeValue()
                                    counter +=1

                                    if(counter == snapshot.childrenCount.toInt()){
                                        progress.dismissWithAnimation()
                                        finish()
                                    }
                                }
                            }else{
                                finish()
                            }
                        }
                    })
                }
                dialog.setCancelClickListener {
                    dialog.dismissWithAnimation()
                }
                dialog.show()
            }
            return@setOnMenuItemClickListener true
        }

        btnSend.setOnClickListener {
            if(messageContent.text.isNotEmpty()){
                sendMessage(messageContent.text.toString(),"text")
            }else{
                Toast.makeText(this,"Harap tulis beberapa kata",Toast.LENGTH_SHORT).show()
            }
        }

        btnSelectImage.setOnClickListener {
            CropImage.activity()
                //.setGuidelines(CropImageView.Guidelines.ON)
                .setAllowFlipping(false)
                .setOutputCompressFormat(Bitmap.CompressFormat.WEBP)
                .setOutputCompressQuality(75)
                .start(this)
        }
    }

    private fun sendMessage(message: String, type: String) {
        roomID = intent.getStringExtra("roomID")!!
        user = intent.getStringExtra("user")!!
        store = intent.getStringExtra("store")!!
        receiver = intent.getStringExtra("receiver")!!

        if(MyFunctions.getConnectivityStatus(this)){
            val newMessage = database.getReference("message")
            val key = newMessage.push().key.toString()
            newMessage.child(key).child("messageTime").setValue(MyFunctions.getTime())
            newMessage.child(key).child("messageContent").setValue(message)
            newMessage.child(key).child("messageType").setValue(type)
            newMessage.child(key).child("messageStatus").setValue("unread")
            newMessage.child(key).child("messageReceiver").setValue(if(receiver == "store"){store}else{user})
            newMessage.child(key).child("messageSender").setValue(if(receiver == "store"){user}else{store})
            newMessage.child(key).child("messageRoom").setValue(roomID)
            newMessage.child(key).child("messageStore").setValue(store)
            newMessage.child(key).child("messageUser").setValue(user)

            //add room to user
            database.getReference("user/$user/userMessage").child(roomID).setValue(MyFunctions.getTime())
            //add room to store
            database.getReference("store/$store/storeMessage").child(roomID).setValue(MyFunctions.getTime())
            messageContent.text.clear()
        }else{
            Toast.makeText(this,"Tidak bisa mengirim pesan",Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                if(MyFunctions.getConnectivityStatus(this)){
                    val resultUri = result.uri
                    val filePath : Uri = resultUri
                    val ref = storageReference.child("message/$roomID/${UUID.randomUUID()}")

                    val progress = SweetAlertDialog(this,SweetAlertDialog.PROGRESS_TYPE)
                    progress.titleText = "Mengirim gambar"
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
                            sendMessage("$downloadUri","image")
                        } else {
                            progress.dismissWithAnimation()
                            Toast.makeText(this,"Gagal mengirim gambar",Toast.LENGTH_SHORT).show()
                        }
                    }
                }else{
                    Toast.makeText(this,"Koneksi internet bermasalah",Toast.LENGTH_SHORT).show()
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
                Log.d("Result", error.toString())
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val edit = sharedPreferences!!.edit()
        edit.putString(Cache.activeRoom,"")
        edit.apply()
    }
}