package com.saean.app.menus.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.pedant.SweetAlert.SweetAlertDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.saean.app.LoginActivity
import com.saean.app.createStore.CreateStoreActivity
import com.saean.app.R
import com.saean.app.VerificationActivity
import com.saean.app.helper.Cache
import com.saean.app.helper.MyFunctions
import com.saean.app.store.*
import com.saean.app.store.settings.*
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
        //menu.getItem(0).isVisible = true
        val menuMessage = menu.getItem(1).setActionView(R.layout.item_badges_toolbar)
        val menuNotification = menu.getItem(2).setActionView(R.layout.item_badges_toolbar)

        val actionViewMessage = menuMessage.actionView
        val actionViewNotification = menuNotification.actionView

        val badgesMessage = actionViewMessage.findViewById<ImageBadgeView>(R.id.badges)
        badgesMessage.setImageResource(R.drawable.ic_menu_toolbar_home_messages)

        val badgesNotification = actionViewNotification.findViewById<ImageBadgeView>(R.id.badges)
        badgesNotification.setImageResource(R.drawable.ic_menu_toolbar_home_notification)

        val email = MyFunctions.changeToUnderscore(sharedPreferences!!.getString(Cache.email,"")!!)
        database.getReference("notification/$email").addValueEventListener(object : ValueEventListener{
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

        badgesMessage.badgeValue = 1
    }

    private fun setupFunctions() {
        setupUserInfo()
        setupRefresh()
        setupMenus()
    }

    private fun setupMenus() {
        btnAddProduct.setOnClickListener {
            startActivity(Intent(activity!!,StoreAddProductActivity::class.java))
        }

        menuSettingLogout.setOnClickListener {
            val dialog = SweetAlertDialog(activity!!,SweetAlertDialog.WARNING_TYPE)
            dialog.titleText = "Keluar dari akun"
            dialog.contentText = "Apakah Anda ingin keluar dari akun saat ini?"
            dialog.confirmText = "Keluar"
            dialog.cancelText = "Batal"
            dialog.setConfirmClickListener {
                dialog.dismissWithAnimation()

                val edit = sharedPreferences!!.edit()
                edit.clear()
                edit.apply()

                startActivity(Intent(activity!!,LoginActivity::class.java))
                activity!!.finish()
            }
            dialog.setCancelClickListener {
                dialog.dismissWithAnimation()
            }
            dialog.show()
        }

        menuSettingMyInfo.setOnClickListener {

        }

        menuSettingVerify.setOnClickListener {
            startActivity(Intent(activity!!,VerificationActivity::class.java))
        }

        menuSettingChangePassword.setOnClickListener {

        }

        menuSettingAboutApps.setOnClickListener {

        }

        menuSettingTermsConditions.setOnClickListener {

        }

        menuSettingPrivacy.setOnClickListener {

        }
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
                        val edit = sharedPreferences!!.edit()
                        edit.putString(Cache.storeID,snapshot.child("userStore").getValue(String::class.java))
                        edit.apply()

                        menuStoreInfo.setOnClickListener {
                            startActivity(Intent(activity!!,
                                StoreSettingInfoActivity::class.java))
                        }
                        menuStoreProduct.setOnClickListener {
                            startActivity(Intent(activity!!,
                                StoreSettingProductActivity::class.java))
                        }
                        menuStoreService.setOnClickListener {
                            startActivity(Intent(activity!!,
                                StoreSettingServicesActivity::class.java))
                        }
                        menuStoreEtalase.setOnClickListener {
                            startActivity(Intent(activity!!,
                                StoreSettingEtalaseActivity::class.java))
                        }
                        menuStoreNotes.setOnClickListener {
                            startActivity(Intent(activity!!,
                                StoreSettingNotesActivity::class.java))
                        }
                        menuStoreAddress.setOnClickListener {
                            startActivity(Intent(activity!!,
                                StoreSettingAddressActivity::class.java))
                        }
                        menuStoreSchedule.setOnClickListener {
                            startActivity(Intent(activity!!,
                                StoreSettingScheduleActivity::class.java))
                        }

                        database.getReference("store/${snapshot.child("userStore").getValue(String::class.java)}/storeInfo").addValueEventListener(object : ValueEventListener{
                            override fun onCancelled(error: DatabaseError) {

                            }

                            override fun onDataChange(snapshot: DataSnapshot) {
                                storeRating.rating = snapshot.child("storeRating").getValue(Float::class.java)!!
                                storeName.text = snapshot.child("storeName").getValue(String::class.java)
                                storeStatusVerify.text = if(snapshot.child("storeStatus").getValue(Boolean::class.java)!!){"Terverifikasi"}else{"Belum Terverifikasi"}
                                if(snapshot.child("storeStatus").getValue(Boolean::class.java)!!){
                                    storeVerify.visibility = View.GONE
                                    storeVerify2.visibility = View.GONE
                                }else{
                                    storeVerify.visibility = View.VISIBLE
                                    storeVerify2.visibility = View.VISIBLE

                                    storeVerify.setOnClickListener {
                                        startActivity(Intent(activity!!,VerificationActivity::class.java))
                                    }
                                }

                                btnCloseNoticeVerify.setOnClickListener {
                                    storeVerify2.visibility = View.GONE
                                }

                                if(snapshot.child("storePicture").getValue(String::class.java)!!.isNotEmpty()){
                                    Glide.with(activity!!)
                                        .load(snapshot.child("storePicture").getValue(String::class.java)!!)
                                        .apply(RequestOptions.circleCropTransform())
                                        .into(storePicture)
                                }

                                //set store rating

                                // hide container create store
                                containerOpenStore.visibility = View.GONE
                                containerOpenStoreAction.visibility = View.GONE
                                containerMyStore.visibility = View.VISIBLE
                            }
                        })
                    }else{
                        containerOpenStore.visibility = View.VISIBLE
                        containerOpenStoreAction.visibility = View.VISIBLE
                        containerMyStore.visibility = View.GONE
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