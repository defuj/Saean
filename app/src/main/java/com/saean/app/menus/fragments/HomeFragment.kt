package com.saean.app.menus.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.islamkhsh.CardSliderIndicator
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.saean.app.R
import com.saean.app.helper.Cache
import com.saean.app.home.nearbyStore.StoreAdapter
import com.saean.app.home.nearbyStore.StoreModel
import com.saean.app.home.promoSlider.PromoAdapter
import com.saean.app.home.promoSlider.PromoModel
import kotlinx.android.synthetic.main.fragment_home.*
import ru.nikartm.support.ImageBadgeView

class HomeFragment : Fragment() {
    private lateinit var database: FirebaseDatabase
    private var sharedPreferences : SharedPreferences? = null
    private var promo : ArrayList<PromoModel>? = null
    private var store : ArrayList<StoreModel>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        database = FirebaseDatabase.getInstance()
        sharedPreferences = activity!!.getSharedPreferences(Cache.cacheName,0)

        setupBadgesToolbar()
        setupFunctions()
    }

    private fun setupBadgesToolbar() {
        val menu = toolbarHome.menu

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
        setupRefresh()
        setupSliderPromo()
        setupNearbyStore()
    }

    private fun setupNearbyStore() {
        store = ArrayList()
        store!!.clear()
        storeNearbySlider.layoutManager = LinearLayoutManager(activity,LinearLayoutManager.HORIZONTAL,false)

        for(i in 0 until 5){
            val model = StoreModel()
            model.storeID = "id$i"
            model.storeName = "This is name of the Store $i"
            model.storeImage = "https://pemmzchannel.com/wp-content/uploads/2020/02/IT-Galeri-Store-1000x570.jpg"
            model.storeDescription = activity!!.getString(R.string.lorem_ipsum)
            model.storeStatusOpen = true
            model.storeRating = i.toFloat()
            store!!.add(model)
        }
        val adapter = StoreAdapter(store!!)
        storeNearbySlider.adapter = adapter
    }

    private fun setupSliderPromo() {
        promo = ArrayList()
        promo!!.clear()

        database.getReference("config/promo").addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists() && snapshot.hasChildren()){
                    for(content in snapshot.children){
                        val model = PromoModel()
                        model.image = content.child("image").getValue(String::class.java)
                        promo!!.add(model)
                    }

                    indicator.indicatorsToShow = if(snapshot.childrenCount >= 5){
                        5
                    }else{
                        snapshot.childrenCount.toInt()
                    }
                    val adapter = PromoAdapter(promo!!)
                    promoSlider.adapter = adapter
                }
            }
        })
    }

    private fun setupRefresh() {
        refreshHome!!.setOnRefreshListener {
            object : CountDownTimer(2000,1000){
                override fun onFinish() {
                    setupSliderPromo()
                    setupNearbyStore()
                    refreshHome!!.isRefreshing = false
                }

                override fun onTick(millisUntilFinished: Long) {

                }
            }.start()
        }
    }
}