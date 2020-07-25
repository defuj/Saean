package com.saean.app.menus.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.saean.app.notification.NotificationActivity
import com.saean.app.R
import com.saean.app.helper.Cache
import com.saean.app.helper.MyFunctions
import com.saean.app.messages.RoomListActivity
import com.saean.app.store.model.OrderModel
import com.saean.app.store.model.UserOrderAdapter
import kotlinx.android.synthetic.main.fragment_transactions.*
import ru.nikartm.support.ImageBadgeView

class TransactionsFragment : Fragment() {
    private lateinit var database: FirebaseDatabase
    private var sharedPreferences : SharedPreferences? = null
    private var order : ArrayList<OrderModel>? = null

    private var filter = "Semua"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_transactions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        database = FirebaseDatabase.getInstance()
        sharedPreferences = activity!!.getSharedPreferences(Cache.cacheName,0)

        setupBadgesToolbar()
        setupFunctions()
    }

    private fun setupBadgesToolbar() {
        val menu = toolbarTransaction.menu

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

        val myStore = sharedPreferences!!.getString(Cache.storeID,"_")
        var unread = 0
        database.getReference("message").orderByChild("messageReceiver").equalTo(email).addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                badgesMessage.badgeValue = 0
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
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

        database.getReference("message").orderByChild("messageReceiver").equalTo(myStore).addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                badgesMessage.badgeValue = 0
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
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

        setupFilter()
        setupSearch()
        setupList()
    }

    private fun setupList() {
        val email = sharedPreferences!!.getString(Cache.email,"")
        database.getReference("order").orderByChild("orderUser").equalTo("$email").addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                containerNoTransaction.visibility = View.VISIBLE
                recyclerTransaction.visibility = View.GONE
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    if(snapshot.hasChildren()){
                        order = ArrayList()
                        order!!.clear()
                        recyclerTransaction.layoutManager = LinearLayoutManager(activity!!,LinearLayoutManager.VERTICAL,false)

                        for(content in snapshot.children){
                            if(content.child("orderUser").getValue(String::class.java)!! == email){
                                when (filter) {
                                    "Semua" -> {
                                        if(searchTransaction.text.isNotEmpty()){
                                            if(content.child("orderID").getValue(String::class.java)!!.contains(searchTransaction.text.toString()) ||
                                                content.child("orderDescription").getValue(String::class.java)!!.contains(searchTransaction.text.toString()) ||
                                                MyFunctions.formatMillie(content.child("orderTime").getValue(Long::class.java)!!,"dd MMMM yyyy").contains(searchTransaction.text.toString())){
                                                val model = OrderModel()
                                                model.orderID = content.child("orderID").getValue(String::class.java)!!
                                                model.orderDescription = content.child("orderDescription").getValue(String::class.java)!!
                                                model.orderProcess = content.child("orderProcess").getValue(Int::class.java)!!
                                                model.orderService = content.child("orderService").getValue(String::class.java)!!
                                                model.orderStore = content.child("orderStore").getValue(String::class.java)!!
                                                model.orderTime = content.child("orderTime").getValue(Long::class.java)!!
                                                model.orderUser = content.child("orderUser").getValue(String::class.java)!!
                                                model.orderStatus = content.child("orderStatus").getValue(Int::class.java)!!
                                                order!!.add(model)
                                                if(recyclerTransaction.adapter != null){
                                                    recyclerTransaction.adapter!!.notifyDataSetChanged()
                                                }

                                                if(order!!.size > 0){
                                                    containerNoTransaction.visibility = View.GONE
                                                    recyclerTransaction.visibility = View.VISIBLE
                                                }else{
                                                    containerNoTransaction.visibility = View.VISIBLE
                                                    recyclerTransaction.visibility = View.GONE
                                                }
                                            }else{
                                                if(order!!.size > 0){
                                                    containerNoTransaction.visibility = View.GONE
                                                    recyclerTransaction.visibility = View.VISIBLE
                                                }else{
                                                    containerNoTransaction.visibility = View.VISIBLE
                                                    recyclerTransaction.visibility = View.GONE
                                                }
                                            }
                                        }else{
                                            val model = OrderModel()
                                            model.orderID = content.child("orderID").getValue(String::class.java)!!
                                            model.orderDescription = content.child("orderDescription").getValue(String::class.java)!!
                                            model.orderProcess = content.child("orderProcess").getValue(Int::class.java)!!
                                            model.orderService = content.child("orderService").getValue(String::class.java)!!
                                            model.orderStore = content.child("orderStore").getValue(String::class.java)!!
                                            model.orderTime = content.child("orderTime").getValue(Long::class.java)!!
                                            model.orderUser = content.child("orderUser").getValue(String::class.java)!!
                                            model.orderStatus = content.child("orderStatus").getValue(Int::class.java)!!
                                            order!!.add(model)
                                            if(recyclerTransaction.adapter != null){
                                                recyclerTransaction.adapter!!.notifyDataSetChanged()
                                            }

                                            if(order!!.size > 0){
                                                containerNoTransaction.visibility = View.GONE
                                                recyclerTransaction.visibility = View.VISIBLE
                                            }else{
                                                containerNoTransaction.visibility = View.VISIBLE
                                                recyclerTransaction.visibility = View.GONE
                                            }
                                        }

                                    }
                                    "Diterima" -> {
                                        if(content.child("orderStatus").getValue(Int::class.java)!! == 1){
                                            if(searchTransaction.text.isNotEmpty()){
                                                if(content.child("orderID").getValue(String::class.java)!!.contains(searchTransaction.text.toString()) ||
                                                    content.child("orderDescription").getValue(String::class.java)!!.contains(searchTransaction.text.toString()) ||
                                                    MyFunctions.formatMillie(content.child("orderTime").getValue(Long::class.java)!!,"dd MMMM yyyy").contains(searchTransaction.text.toString())){
                                                    val model = OrderModel()
                                                    model.orderID = content.child("orderID").getValue(String::class.java)!!
                                                    model.orderDescription = content.child("orderDescription").getValue(String::class.java)!!
                                                    model.orderProcess = content.child("orderProcess").getValue(Int::class.java)!!
                                                    model.orderService = content.child("orderService").getValue(String::class.java)!!
                                                    model.orderStore = content.child("orderStore").getValue(String::class.java)!!
                                                    model.orderTime = content.child("orderTime").getValue(Long::class.java)!!
                                                    model.orderUser = content.child("orderUser").getValue(String::class.java)!!
                                                    model.orderStatus = content.child("orderStatus").getValue(Int::class.java)!!
                                                    order!!.add(model)
                                                    if(recyclerTransaction.adapter != null){
                                                        recyclerTransaction.adapter!!.notifyDataSetChanged()
                                                    }

                                                    if(order!!.size > 0){
                                                        containerNoTransaction.visibility = View.GONE
                                                        recyclerTransaction.visibility = View.VISIBLE
                                                    }else{
                                                        containerNoTransaction.visibility = View.VISIBLE
                                                        recyclerTransaction.visibility = View.GONE
                                                    }
                                                }else{
                                                    if(order!!.size > 0){
                                                        containerNoTransaction.visibility = View.GONE
                                                        recyclerTransaction.visibility = View.VISIBLE
                                                    }else{
                                                        containerNoTransaction.visibility = View.VISIBLE
                                                        recyclerTransaction.visibility = View.GONE
                                                    }
                                                }
                                            }else{
                                                val model = OrderModel()
                                                model.orderID = content.child("orderID").getValue(String::class.java)!!
                                                model.orderDescription = content.child("orderDescription").getValue(String::class.java)!!
                                                model.orderProcess = content.child("orderProcess").getValue(Int::class.java)!!
                                                model.orderService = content.child("orderService").getValue(String::class.java)!!
                                                model.orderStore = content.child("orderStore").getValue(String::class.java)!!
                                                model.orderTime = content.child("orderTime").getValue(Long::class.java)!!
                                                model.orderUser = content.child("orderUser").getValue(String::class.java)!!
                                                model.orderStatus = content.child("orderStatus").getValue(Int::class.java)!!
                                                order!!.add(model)
                                                if(recyclerTransaction.adapter != null){
                                                    recyclerTransaction.adapter!!.notifyDataSetChanged()
                                                }

                                                if(order!!.size > 0){
                                                    containerNoTransaction.visibility = View.GONE
                                                    recyclerTransaction.visibility = View.VISIBLE
                                                }else{
                                                    containerNoTransaction.visibility = View.VISIBLE
                                                    recyclerTransaction.visibility = View.GONE
                                                }
                                            }
                                        }else{
                                            if(order!!.size > 0){
                                                containerNoTransaction.visibility = View.GONE
                                                recyclerTransaction.visibility = View.VISIBLE
                                            }else{
                                                containerNoTransaction.visibility = View.VISIBLE
                                                recyclerTransaction.visibility = View.GONE
                                            }
                                        }
                                    }
                                    "Ditolak" -> {
                                        if(content.child("orderStatus").getValue(Int::class.java)!! == 2){
                                            if(searchTransaction.text.isNotEmpty()){
                                                if(content.child("orderID").getValue(String::class.java)!!.contains(searchTransaction.text.toString()) ||
                                                    content.child("orderDescription").getValue(String::class.java)!!.contains(searchTransaction.text.toString()) ||
                                                    MyFunctions.formatMillie(content.child("orderTime").getValue(Long::class.java)!!,"dd MMMM yyyy").contains(searchTransaction.text.toString())){
                                                    val model = OrderModel()
                                                    model.orderID = content.child("orderID").getValue(String::class.java)!!
                                                    model.orderDescription = content.child("orderDescription").getValue(String::class.java)!!
                                                    model.orderProcess = content.child("orderProcess").getValue(Int::class.java)!!
                                                    model.orderService = content.child("orderService").getValue(String::class.java)!!
                                                    model.orderStore = content.child("orderStore").getValue(String::class.java)!!
                                                    model.orderTime = content.child("orderTime").getValue(Long::class.java)!!
                                                    model.orderUser = content.child("orderUser").getValue(String::class.java)!!
                                                    model.orderStatus = content.child("orderStatus").getValue(Int::class.java)!!
                                                    order!!.add(model)
                                                    if(recyclerTransaction.adapter != null){
                                                        recyclerTransaction.adapter!!.notifyDataSetChanged()
                                                    }

                                                    if(order!!.size > 0){
                                                        containerNoTransaction.visibility = View.GONE
                                                        recyclerTransaction.visibility = View.VISIBLE
                                                    }else{
                                                        containerNoTransaction.visibility = View.VISIBLE
                                                        recyclerTransaction.visibility = View.GONE
                                                    }
                                                }else{
                                                    if(order!!.size > 0){
                                                        containerNoTransaction.visibility = View.GONE
                                                        recyclerTransaction.visibility = View.VISIBLE
                                                    }else{
                                                        containerNoTransaction.visibility = View.VISIBLE
                                                        recyclerTransaction.visibility = View.GONE
                                                    }
                                                }
                                            }else{
                                                val model = OrderModel()
                                                model.orderID = content.child("orderID").getValue(String::class.java)!!
                                                model.orderDescription = content.child("orderDescription").getValue(String::class.java)!!
                                                model.orderProcess = content.child("orderProcess").getValue(Int::class.java)!!
                                                model.orderService = content.child("orderService").getValue(String::class.java)!!
                                                model.orderStore = content.child("orderStore").getValue(String::class.java)!!
                                                model.orderTime = content.child("orderTime").getValue(Long::class.java)!!
                                                model.orderUser = content.child("orderUser").getValue(String::class.java)!!
                                                model.orderStatus = content.child("orderStatus").getValue(Int::class.java)!!
                                                order!!.add(model)
                                                if(recyclerTransaction.adapter != null){
                                                    recyclerTransaction.adapter!!.notifyDataSetChanged()
                                                }

                                                if(order!!.size > 0){
                                                    containerNoTransaction.visibility = View.GONE
                                                    recyclerTransaction.visibility = View.VISIBLE
                                                }else{
                                                    containerNoTransaction.visibility = View.VISIBLE
                                                    recyclerTransaction.visibility = View.GONE
                                                }
                                            }
                                        }else{
                                            if(order!!.size > 0){
                                                containerNoTransaction.visibility = View.GONE
                                                recyclerTransaction.visibility = View.VISIBLE
                                            }else{
                                                containerNoTransaction.visibility = View.VISIBLE
                                                recyclerTransaction.visibility = View.GONE
                                            }
                                        }
                                    }
                                    "Diproses" -> {
                                        if(content.child("orderProcess").getValue(Int::class.java)!! == 1){
                                            if(searchTransaction.text.isNotEmpty()){
                                                if(content.child("orderID").getValue(String::class.java)!!.contains(searchTransaction.text.toString()) ||
                                                    content.child("orderDescription").getValue(String::class.java)!!.contains(searchTransaction.text.toString()) ||
                                                    MyFunctions.formatMillie(content.child("orderTime").getValue(Long::class.java)!!,"dd MMMM yyyy").contains(searchTransaction.text.toString())){
                                                    val model = OrderModel()
                                                    model.orderID = content.child("orderID").getValue(String::class.java)!!
                                                    model.orderDescription = content.child("orderDescription").getValue(String::class.java)!!
                                                    model.orderProcess = content.child("orderProcess").getValue(Int::class.java)!!
                                                    model.orderService = content.child("orderService").getValue(String::class.java)!!
                                                    model.orderStore = content.child("orderStore").getValue(String::class.java)!!
                                                    model.orderTime = content.child("orderTime").getValue(Long::class.java)!!
                                                    model.orderUser = content.child("orderUser").getValue(String::class.java)!!
                                                    model.orderStatus = content.child("orderStatus").getValue(Int::class.java)!!
                                                    order!!.add(model)
                                                    if(recyclerTransaction.adapter != null){
                                                        recyclerTransaction.adapter!!.notifyDataSetChanged()
                                                    }

                                                    if(order!!.size > 0){
                                                        containerNoTransaction.visibility = View.GONE
                                                        recyclerTransaction.visibility = View.VISIBLE
                                                    }else{
                                                        containerNoTransaction.visibility = View.VISIBLE
                                                        recyclerTransaction.visibility = View.GONE
                                                    }
                                                }else{
                                                    if(order!!.size > 0){
                                                        containerNoTransaction.visibility = View.GONE
                                                        recyclerTransaction.visibility = View.VISIBLE
                                                    }else{
                                                        containerNoTransaction.visibility = View.VISIBLE
                                                        recyclerTransaction.visibility = View.GONE
                                                    }
                                                }
                                            }else{
                                                val model = OrderModel()
                                                model.orderID = content.child("orderID").getValue(String::class.java)!!
                                                model.orderDescription = content.child("orderDescription").getValue(String::class.java)!!
                                                model.orderProcess = content.child("orderProcess").getValue(Int::class.java)!!
                                                model.orderService = content.child("orderService").getValue(String::class.java)!!
                                                model.orderStore = content.child("orderStore").getValue(String::class.java)!!
                                                model.orderTime = content.child("orderTime").getValue(Long::class.java)!!
                                                model.orderUser = content.child("orderUser").getValue(String::class.java)!!
                                                model.orderStatus = content.child("orderStatus").getValue(Int::class.java)!!
                                                order!!.add(model)
                                                if(recyclerTransaction.adapter != null){
                                                    recyclerTransaction.adapter!!.notifyDataSetChanged()
                                                }

                                                if(order!!.size > 0){
                                                    containerNoTransaction.visibility = View.GONE
                                                    recyclerTransaction.visibility = View.VISIBLE
                                                }else{
                                                    containerNoTransaction.visibility = View.VISIBLE
                                                    recyclerTransaction.visibility = View.GONE
                                                }
                                            }
                                        }else{
                                            if(order!!.size > 0){
                                                containerNoTransaction.visibility = View.GONE
                                                recyclerTransaction.visibility = View.VISIBLE
                                            }else{
                                                containerNoTransaction.visibility = View.VISIBLE
                                                recyclerTransaction.visibility = View.GONE
                                            }
                                        }
                                    }
                                    "Butuh Konfirmasi" -> {
                                        if(content.child("orderProcess").getValue(Int::class.java)!! == 2){
                                            if(searchTransaction.text.isNotEmpty()){
                                                if(content.child("orderID").getValue(String::class.java)!!.contains(searchTransaction.text.toString()) ||
                                                    content.child("orderDescription").getValue(String::class.java)!!.contains(searchTransaction.text.toString()) ||
                                                    MyFunctions.formatMillie(content.child("orderTime").getValue(Long::class.java)!!,"dd MMMM yyyy").contains(searchTransaction.text.toString())){
                                                    val model = OrderModel()
                                                    model.orderID = content.child("orderID").getValue(String::class.java)!!
                                                    model.orderDescription = content.child("orderDescription").getValue(String::class.java)!!
                                                    model.orderProcess = content.child("orderProcess").getValue(Int::class.java)!!
                                                    model.orderService = content.child("orderService").getValue(String::class.java)!!
                                                    model.orderStore = content.child("orderStore").getValue(String::class.java)!!
                                                    model.orderTime = content.child("orderTime").getValue(Long::class.java)!!
                                                    model.orderUser = content.child("orderUser").getValue(String::class.java)!!
                                                    model.orderStatus = content.child("orderStatus").getValue(Int::class.java)!!
                                                    order!!.add(model)
                                                    if(recyclerTransaction.adapter != null){
                                                        recyclerTransaction.adapter!!.notifyDataSetChanged()
                                                    }

                                                    if(order!!.size > 0){
                                                        containerNoTransaction.visibility = View.GONE
                                                        recyclerTransaction.visibility = View.VISIBLE
                                                    }else{
                                                        containerNoTransaction.visibility = View.VISIBLE
                                                        recyclerTransaction.visibility = View.GONE
                                                    }
                                                }else{
                                                    if(order!!.size > 0){
                                                        containerNoTransaction.visibility = View.GONE
                                                        recyclerTransaction.visibility = View.VISIBLE
                                                    }else{
                                                        containerNoTransaction.visibility = View.VISIBLE
                                                        recyclerTransaction.visibility = View.GONE
                                                    }
                                                }
                                            }else{
                                                val model = OrderModel()
                                                model.orderID = content.child("orderID").getValue(String::class.java)!!
                                                model.orderDescription = content.child("orderDescription").getValue(String::class.java)!!
                                                model.orderProcess = content.child("orderProcess").getValue(Int::class.java)!!
                                                model.orderService = content.child("orderService").getValue(String::class.java)!!
                                                model.orderStore = content.child("orderStore").getValue(String::class.java)!!
                                                model.orderTime = content.child("orderTime").getValue(Long::class.java)!!
                                                model.orderUser = content.child("orderUser").getValue(String::class.java)!!
                                                model.orderStatus = content.child("orderStatus").getValue(Int::class.java)!!
                                                order!!.add(model)
                                                if(recyclerTransaction.adapter != null){
                                                    recyclerTransaction.adapter!!.notifyDataSetChanged()
                                                }

                                                if(order!!.size > 0){
                                                    containerNoTransaction.visibility = View.GONE
                                                    recyclerTransaction.visibility = View.VISIBLE
                                                }
                                            }

                                        }else{
                                            if(order!!.size > 0){
                                                containerNoTransaction.visibility = View.GONE
                                                recyclerTransaction.visibility = View.VISIBLE
                                            }else{
                                                containerNoTransaction.visibility = View.VISIBLE
                                                recyclerTransaction.visibility = View.GONE
                                            }
                                        }
                                    }
                                    "Batal" -> {
                                        if(content.child("orderProcess").getValue(Int::class.java)!! == 3){
                                            if(searchTransaction.text.isNotEmpty()){
                                                if(content.child("orderID").getValue(String::class.java)!!.contains(searchTransaction.text.toString()) ||
                                                    content.child("orderDescription").getValue(String::class.java)!!.contains(searchTransaction.text.toString()) ||
                                                    MyFunctions.formatMillie(content.child("orderTime").getValue(Long::class.java)!!,"dd MMMM yyyy").contains(searchTransaction.text.toString())){
                                                    val model = OrderModel()
                                                    model.orderID = content.child("orderID").getValue(String::class.java)!!
                                                    model.orderDescription = content.child("orderDescription").getValue(String::class.java)!!
                                                    model.orderProcess = content.child("orderProcess").getValue(Int::class.java)!!
                                                    model.orderService = content.child("orderService").getValue(String::class.java)!!
                                                    model.orderStore = content.child("orderStore").getValue(String::class.java)!!
                                                    model.orderTime = content.child("orderTime").getValue(Long::class.java)!!
                                                    model.orderUser = content.child("orderUser").getValue(String::class.java)!!
                                                    model.orderStatus = content.child("orderStatus").getValue(Int::class.java)!!
                                                    order!!.add(model)
                                                    if(recyclerTransaction.adapter != null){
                                                        recyclerTransaction.adapter!!.notifyDataSetChanged()
                                                    }

                                                    if(order!!.size > 0){
                                                        containerNoTransaction.visibility = View.GONE
                                                        recyclerTransaction.visibility = View.VISIBLE
                                                    }else{
                                                        containerNoTransaction.visibility = View.VISIBLE
                                                        recyclerTransaction.visibility = View.GONE
                                                    }
                                                }else{
                                                    if(order!!.size > 0){
                                                        containerNoTransaction.visibility = View.GONE
                                                        recyclerTransaction.visibility = View.VISIBLE
                                                    }else{
                                                        containerNoTransaction.visibility = View.VISIBLE
                                                        recyclerTransaction.visibility = View.GONE
                                                    }
                                                }
                                            }else{
                                                val model = OrderModel()
                                                model.orderID = content.child("orderID").getValue(String::class.java)!!
                                                model.orderDescription = content.child("orderDescription").getValue(String::class.java)!!
                                                model.orderProcess = content.child("orderProcess").getValue(Int::class.java)!!
                                                model.orderService = content.child("orderService").getValue(String::class.java)!!
                                                model.orderStore = content.child("orderStore").getValue(String::class.java)!!
                                                model.orderTime = content.child("orderTime").getValue(Long::class.java)!!
                                                model.orderUser = content.child("orderUser").getValue(String::class.java)!!
                                                model.orderStatus = content.child("orderStatus").getValue(Int::class.java)!!
                                                order!!.add(model)
                                                if(recyclerTransaction.adapter != null){
                                                    recyclerTransaction.adapter!!.notifyDataSetChanged()
                                                }

                                                if(order!!.size > 0){
                                                    containerNoTransaction.visibility = View.GONE
                                                    recyclerTransaction.visibility = View.VISIBLE
                                                }
                                            }

                                        }else{
                                            if(order!!.size > 0){
                                                containerNoTransaction.visibility = View.GONE
                                                recyclerTransaction.visibility = View.VISIBLE
                                            }else{
                                                containerNoTransaction.visibility = View.VISIBLE
                                                recyclerTransaction.visibility = View.GONE
                                            }
                                        }
                                    }
                                    "Perbaikan" -> {
                                        if(content.child("orderProcess").getValue(Int::class.java)!! == 4){
                                            if(searchTransaction.text.isNotEmpty()){
                                                if(content.child("orderID").getValue(String::class.java)!!.contains(searchTransaction.text.toString()) ||
                                                    content.child("orderDescription").getValue(String::class.java)!!.contains(searchTransaction.text.toString()) ||
                                                    MyFunctions.formatMillie(content.child("orderTime").getValue(Long::class.java)!!,"dd MMMM yyyy").contains(searchTransaction.text.toString())){
                                                    val model = OrderModel()
                                                    model.orderID = content.child("orderID").getValue(String::class.java)!!
                                                    model.orderDescription = content.child("orderDescription").getValue(String::class.java)!!
                                                    model.orderProcess = content.child("orderProcess").getValue(Int::class.java)!!
                                                    model.orderService = content.child("orderService").getValue(String::class.java)!!
                                                    model.orderStore = content.child("orderStore").getValue(String::class.java)!!
                                                    model.orderTime = content.child("orderTime").getValue(Long::class.java)!!
                                                    model.orderUser = content.child("orderUser").getValue(String::class.java)!!
                                                    model.orderStatus = content.child("orderStatus").getValue(Int::class.java)!!
                                                    order!!.add(model)
                                                    if(recyclerTransaction.adapter != null){
                                                        recyclerTransaction.adapter!!.notifyDataSetChanged()
                                                    }

                                                    if(order!!.size > 0){
                                                        containerNoTransaction.visibility = View.GONE
                                                        recyclerTransaction.visibility = View.VISIBLE
                                                    }else{
                                                        containerNoTransaction.visibility = View.VISIBLE
                                                        recyclerTransaction.visibility = View.GONE
                                                    }
                                                }else{
                                                    if(order!!.size > 0){
                                                        containerNoTransaction.visibility = View.GONE
                                                        recyclerTransaction.visibility = View.VISIBLE
                                                    }else{
                                                        containerNoTransaction.visibility = View.VISIBLE
                                                        recyclerTransaction.visibility = View.GONE
                                                    }
                                                }
                                            }else{
                                                val model = OrderModel()
                                                model.orderID = content.child("orderID").getValue(String::class.java)!!
                                                model.orderDescription = content.child("orderDescription").getValue(String::class.java)!!
                                                model.orderProcess = content.child("orderProcess").getValue(Int::class.java)!!
                                                model.orderService = content.child("orderService").getValue(String::class.java)!!
                                                model.orderStore = content.child("orderStore").getValue(String::class.java)!!
                                                model.orderTime = content.child("orderTime").getValue(Long::class.java)!!
                                                model.orderUser = content.child("orderUser").getValue(String::class.java)!!
                                                model.orderStatus = content.child("orderStatus").getValue(Int::class.java)!!
                                                order!!.add(model)
                                                if(recyclerTransaction.adapter != null){
                                                    recyclerTransaction.adapter!!.notifyDataSetChanged()
                                                }

                                                if(order!!.size > 0){
                                                    containerNoTransaction.visibility = View.GONE
                                                    recyclerTransaction.visibility = View.VISIBLE
                                                }
                                            }

                                        }else{
                                            if(order!!.size > 0){
                                                containerNoTransaction.visibility = View.GONE
                                                recyclerTransaction.visibility = View.VISIBLE
                                            }else{
                                                containerNoTransaction.visibility = View.VISIBLE
                                                recyclerTransaction.visibility = View.GONE
                                            }
                                        }
                                    }
                                    "Selesai" -> {
                                        if(content.child("orderProcess").getValue(Int::class.java)!! == 5){
                                            if(searchTransaction.text.isNotEmpty()){
                                                if(content.child("orderID").getValue(String::class.java)!!.contains(searchTransaction.text.toString()) ||
                                                    content.child("orderDescription").getValue(String::class.java)!!.contains(searchTransaction.text.toString()) ||
                                                    MyFunctions.formatMillie(content.child("orderTime").getValue(Long::class.java)!!,"dd MMMM yyyy").contains(searchTransaction.text.toString())){

                                                    val model = OrderModel()
                                                    model.orderID = content.child("orderID").getValue(String::class.java)!!
                                                    model.orderDescription = content.child("orderDescription").getValue(String::class.java)!!
                                                    model.orderProcess = content.child("orderProcess").getValue(Int::class.java)!!
                                                    model.orderService = content.child("orderService").getValue(String::class.java)!!
                                                    model.orderStore = content.child("orderStore").getValue(String::class.java)!!
                                                    model.orderTime = content.child("orderTime").getValue(Long::class.java)!!
                                                    model.orderUser = content.child("orderUser").getValue(String::class.java)!!
                                                    model.orderStatus = content.child("orderStatus").getValue(Int::class.java)!!
                                                    order!!.add(model)
                                                    if(recyclerTransaction.adapter != null){
                                                        recyclerTransaction.adapter!!.notifyDataSetChanged()
                                                    }

                                                    if(order!!.size > 0){
                                                        containerNoTransaction.visibility = View.GONE
                                                        recyclerTransaction.visibility = View.VISIBLE
                                                    }else{
                                                        containerNoTransaction.visibility = View.VISIBLE
                                                        recyclerTransaction.visibility = View.GONE
                                                    }
                                                }else{
                                                    if(order!!.size > 0){
                                                        containerNoTransaction.visibility = View.GONE
                                                        recyclerTransaction.visibility = View.VISIBLE
                                                    }else{
                                                        containerNoTransaction.visibility = View.VISIBLE
                                                        recyclerTransaction.visibility = View.GONE
                                                    }
                                                }
                                            }else{
                                                val model = OrderModel()
                                                model.orderID = content.child("orderID").getValue(String::class.java)!!
                                                model.orderDescription = content.child("orderDescription").getValue(String::class.java)!!
                                                model.orderProcess = content.child("orderProcess").getValue(Int::class.java)!!
                                                model.orderService = content.child("orderService").getValue(String::class.java)!!
                                                model.orderStore = content.child("orderStore").getValue(String::class.java)!!
                                                model.orderTime = content.child("orderTime").getValue(Long::class.java)!!
                                                model.orderUser = content.child("orderUser").getValue(String::class.java)!!
                                                model.orderStatus = content.child("orderStatus").getValue(Int::class.java)!!
                                                order!!.add(model)
                                                if(recyclerTransaction.adapter != null){
                                                    recyclerTransaction.adapter!!.notifyDataSetChanged()
                                                }

                                                if(order!!.size > 0){
                                                    containerNoTransaction.visibility = View.GONE
                                                    recyclerTransaction.visibility = View.VISIBLE
                                                }else{
                                                    containerNoTransaction.visibility = View.VISIBLE
                                                    recyclerTransaction.visibility = View.GONE
                                                }
                                            }
                                        }else{
                                            if(order!!.size > 0){
                                                containerNoTransaction.visibility = View.GONE
                                                recyclerTransaction.visibility = View.VISIBLE
                                            }else{
                                                containerNoTransaction.visibility = View.VISIBLE
                                                recyclerTransaction.visibility = View.GONE
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        order!!.reverse()
                        val adapter = UserOrderAdapter(activity!!,order!!)
                        recyclerTransaction.adapter = adapter
                    }else{
                        containerNoTransaction.visibility = View.VISIBLE
                        recyclerTransaction.visibility = View.GONE
                    }
                }else{
                    containerNoTransaction.visibility = View.VISIBLE
                    recyclerTransaction.visibility = View.GONE
                }
            }
        })
    }

    private fun setupFilter() {
        btnFilter.setOnClickListener {
            val alertDialog: AlertDialog.Builder = AlertDialog.Builder(activity!!)
            alertDialog.setTitle("Filter")
            val items = arrayOf("Semua","Diterima","Ditolak","Diproses","Butuh Konfirmasi","Batal","Perbaikan","Selesai")
            val checkedItem =
                when (filter) {
                    "Semua" -> {
                        0
                    }
                    "Diterima" -> {
                        1
                    }
                    "Ditolak" -> {
                        2
                    }
                    "Diproses" -> {
                        3
                    }
                    "Butuh Konfirmasi" -> {
                        4
                    }
                    "Batal" -> {
                        5
                    }
                    "Perbaikan" -> {
                        6
                    }
                    else -> {
                        7
                    }
                }
            alertDialog.setSingleChoiceItems(items, checkedItem) { dialog, which ->
                when (which) {
                    0 -> {
                        filter = "Semua"
                        btnFilter.text = "Semua"
                        dialog.dismiss()
                        setupList()
                    }

                    1 -> {
                        filter = "Diterima"
                        btnFilter.text = "Diterima"
                        dialog.dismiss()
                        setupList()
                    }
                    2 -> {
                        filter = "Ditolak"
                        btnFilter.text = "Ditolak"
                        dialog.dismiss()
                        setupList()
                    }

                    3 -> {
                        filter = "Diproses"
                        btnFilter.text = "Diproses"
                        dialog.dismiss()
                        setupList()
                    }
                    4 -> {
                        filter = "Butuh Konfirmasi"
                        btnFilter.text = "Butuh Konfirmasi"
                        dialog.dismiss()
                        setupList()
                    }
                    5 -> {
                        filter = "Batal"
                        btnFilter.text = "Batal"
                        dialog.dismiss()
                        setupList()
                    }
                    6 -> {
                        filter = "Perbaikan"
                        btnFilter.text = "Perbaikan"
                        dialog.dismiss()
                        setupList()
                    }
                    7 -> {
                        filter = "Selesai"
                        btnFilter.text = "Selesai"
                        dialog.dismiss()
                        setupList()
                    }
                }
            }
            val alert: AlertDialog = alertDialog.create()
            //alert.setCanceledOnTouchOutside(false)
            alert.show()
        }
    }

    private fun setupSearch() {
        searchTransaction!!.setOnEditorActionListener { v, actionId, _ ->
            if(actionId == EditorInfo.IME_ACTION_SEARCH) {
                MyFunctions.closeKeyboard(activity!!)
                v.clearFocus()
                setupList()
            }
            return@setOnEditorActionListener true
        }
    }

}