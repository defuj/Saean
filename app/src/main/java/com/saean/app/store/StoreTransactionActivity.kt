package com.saean.app.store

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.saean.app.R
import com.saean.app.helper.Cache
import com.saean.app.helper.MyFunctions
import com.saean.app.store.model.OrderModel
import com.saean.app.store.model.StoreOrderAdapter
import kotlinx.android.synthetic.main.activity_store_transaction.*

class StoreTransactionActivity : AppCompatActivity() {
    private lateinit var database: FirebaseDatabase
    private var sharedPreferences : SharedPreferences? = null
    private var order : ArrayList<OrderModel>? = null
    private var filter = "Semua"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store_transaction)
        database = FirebaseDatabase.getInstance()
        sharedPreferences = getSharedPreferences(Cache.cacheName,0)

        setupFunctions()
    }

    private fun setupFunctions() {
        toolbarTransaction.setNavigationOnClickListener {
            finish()
        }

        setupFilter()
        setupSearch()
        setupList()
    }

    private fun setupSearch() {
        searchTransaction!!.setOnEditorActionListener { v, actionId, _ ->
            if(actionId == EditorInfo.IME_ACTION_SEARCH) {
                MyFunctions.closeKeyboard(this)
                v.clearFocus()
                setupList()
            }
            return@setOnEditorActionListener true
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupFilter() {
        filter = intent.getStringExtra("filter")!!
        btnFilter.text = filter

        btnFilter.setOnClickListener {
            val alertDialog: AlertDialog.Builder = AlertDialog.Builder(this)
            alertDialog.setTitle("Filter")
            val items = arrayOf("Order Baru","Order Diterima","Order Ditolak","Diproses","Menunggu Konfirmasi","Dibatalkan","Sedang Perbaikan","Selesai")
            val checkedItem =
                when (filter) {
                    "Order Baru" -> {
                        0
                    }
                    "Order Diterima" -> {
                        1
                    }
                    "Order Ditolak" -> {
                        2
                    }
                    "Diproses" -> {
                        3
                    }
                    "Menunggu Konfirmasi" -> {
                        4
                    }
                    "Dibatalkan" -> {
                        5
                    }
                    "Sedang Perbaikan" -> {
                        6
                    }
                    else -> {
                        7
                    }
                }
            alertDialog.setSingleChoiceItems(items, checkedItem) { dialog, which ->
                when (which) {
                    0 -> {
                        filter = "Order Baru"
                        btnFilter.text = "Order Baru"
                        dialog.dismiss()
                        setupList()
                    }

                    1 -> {
                        filter = "Order Diterima"
                        btnFilter.text = "Order Diterima"
                        dialog.dismiss()
                        setupList()
                    }
                    2 -> {
                        filter = "Order Ditolak"
                        btnFilter.text = "Order Ditolak"
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
                        filter = "Menunggu Konfirmasi"
                        btnFilter.text = "Menunggu Konfirmasi"
                        dialog.dismiss()
                        setupList()
                    }
                    5 -> {
                        filter = "Dibatalkan"
                        btnFilter.text = "Dibatalkan"
                        dialog.dismiss()
                        setupList()
                    }
                    6 -> {
                        filter = "Sedang Perbaikan"
                        btnFilter.text = "Sedang Perbaikan"
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

    private fun setupList() {
        val storeID = sharedPreferences!!.getString(Cache.storeID,"")
        database.getReference("order").orderByChild("orderStore").equalTo("$storeID").addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                containerNoTransaction.visibility = View.VISIBLE
                recyclerTransaction.visibility = View.GONE
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    if(snapshot.hasChildren()){
                        order = ArrayList()
                        order!!.clear()
                        recyclerTransaction.layoutManager = LinearLayoutManager(this@StoreTransactionActivity, LinearLayoutManager.VERTICAL,false)

                        for(content in snapshot.children){
                            if(content.child("orderStore").getValue(String::class.java)!! == storeID){
                                when (filter) {
                                    "Order Baru" -> {
                                        if(content.child("orderStatus").getValue(Int::class.java)!! == 0){
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
                        val adapter = StoreOrderAdapter(this@StoreTransactionActivity,order!!)
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
}