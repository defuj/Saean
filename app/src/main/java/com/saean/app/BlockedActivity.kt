package com.saean.app

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.saean.app.helper.Cache
import com.saean.app.network.ApiServices
import kotlinx.android.synthetic.main.activity_blocked.*

class BlockedActivity : AppCompatActivity() {
    private lateinit var database : FirebaseDatabase
    private var sharedPreferences : SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blocked)
        database = FirebaseDatabase.getInstance()
        sharedPreferences = getSharedPreferences(Cache.cacheName,0)

        database.getReference("${ApiServices.clientID}/config/blocked").addValueEventListener(object :
            ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()){
                    if(p0.child("status").getValue(Boolean::class.java)!!){
                        try {
                            Glide.with(this@BlockedActivity).load(p0.child("image").getValue(String::class.java)).into(blockedImage)
                        }catch (e : Exception){

                        }

                        try {
                            blockedMessageTitle!!.text = p0.child("messageTitle").getValue(String::class.java)
                        }catch (e : Exception){

                        }

                        try {
                            blockedMessageBody!!.text = p0.child("messageBody").getValue(String::class.java)
                        }catch (e : Exception){

                        }

                        try {
                            btnBlockedAction!!.text = p0.child("buttonText").getValue(String::class.java)
                        }catch (e : Exception){

                        }
                    }else{
                        val edit = sharedPreferences!!.edit()
                        edit.putBoolean(Cache.blocked, false)
                        edit.apply()

                        startActivity(Intent(this@BlockedActivity,
                            HomeActivity::class.java))
                        finish()
                    }
                }else{
                    val edit = sharedPreferences!!.edit()
                    edit.putBoolean(Cache.blocked, false)
                    edit.apply()

                    startActivity(Intent(this@BlockedActivity,
                        HomeActivity::class.java))
                    finish()
                }
            }
        })

        btnBlockedAction!!.setOnClickListener {
            close()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        close()
    }

    private fun close(){
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }
}