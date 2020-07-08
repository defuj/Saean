package com.saean.app.menus.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.saean.app.createStore.CreateStoreActivity
import com.saean.app.R
import com.saean.app.helper.Cache
import com.saean.app.helper.MyFunctions
import kotlinx.android.synthetic.main.fragment_account.*
import ru.nikartm.support.ImageBadgeView

class AccountFragment : Fragment() {
    private lateinit var database: FirebaseDatabase
    private var sharedPreferences : SharedPreferences? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        database = FirebaseDatabase.getInstance()
        sharedPreferences = activity!!.getSharedPreferences(Cache.cacheName,0)

        setupBadgesToolbar()
        setupFunctions()
    }

    private fun setupBadgesToolbar() {
        val menu = toolbarAccount.menu
        menu.getItem(0).isVisible = true
        val menuMessage = menu.getItem(1).setActionView(R.layout.item_badges_toolbar)
        val menuNotification = menu.getItem(2).setActionView(R.layout.item_badges_toolbar)

        val actionViewMessage = menuMessage.actionView
        val actionViewNotification = menuNotification.actionView

        val badgesMessage = actionViewMessage.findViewById<ImageBadgeView>(R.id.badges)
        badgesMessage.setImageResource(R.drawable.ic_menu_toolbar_home_messages)

        val badgesNotification = actionViewNotification.findViewById<ImageBadgeView>(R.id.badges)
        badgesNotification.setImageResource(R.drawable.ic_menu_toolbar_home_notification)

        badgesMessage.badgeValue = 1
        badgesNotification.badgeValue = 3
    }

    private fun setupFunctions() {
        setupUserInfo()
        setupRefresh()
    }

    private fun setupUserInfo() {
        val email = MyFunctions.changeToUnderscore(sharedPreferences!!.getString(Cache.email,"")!!)
        database.getReference("user/$email").addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                Glide.with(activity!!)
                    .load(activity!!.getString(R.string.default_picture))
                    .apply(RequestOptions.circleCropTransform())
                    .into(userPicture)
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    //set user picture
                    if(snapshot.child("userPicture").exists()){
                        if(snapshot.child("userPicture").getValue(String::class.java)!!.isNotEmpty()){
                            Glide.with(activity!!)
                                .load(snapshot.child("userPicture").getValue(String::class.java)!!)
                                .apply(RequestOptions.circleCropTransform())
                                .into(userPicture)
                        }else{
                            Glide.with(activity!!)
                                .load(activity!!.getString(R.string.default_picture))
                                .apply(RequestOptions.circleCropTransform())
                                .into(userPicture)
                        }
                    }else{
                        Glide.with(activity!!)
                            .load(activity!!.getString(R.string.default_picture))
                            .apply(RequestOptions.circleCropTransform())
                            .into(userPicture)
                    }

                    //set user name
                    userName.text = snapshot.child("userName").getValue(String::class.java)
                    userEmail.text = sharedPreferences!!.getString(Cache.email,"")

                    //check user has have a store or not
                    if(snapshot.child("userStore").exists()){
                        containerOpenStore.visibility = View.GONE
                        containerOpenStoreAction.visibility = View.GONE
                    }else{
                        containerOpenStore.visibility = View.VISIBLE
                    }
                }
            }
        })

        btnClose.setOnClickListener {
            containerOpenStore.visibility = View.GONE
        }

        btnOpenStore.setOnClickListener {
            startActivity(Intent(activity!!,
                CreateStoreActivity::class.java))
        }
    }

    private fun setupRefresh() {
        refreshAccount.setOnRefreshListener {
            object : CountDownTimer(3000,1000){
                override fun onFinish() {
                    refreshAccount.isRefreshing = false
                }

                override fun onTick(millisUntilFinished: Long) {

                }
            }.start()
        }
    }
}