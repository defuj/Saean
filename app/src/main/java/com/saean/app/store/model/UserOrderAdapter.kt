package com.saean.app.store.model

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.saean.app.R
import com.saean.app.helper.Cache
import com.saean.app.helper.MyFunctions
import com.saean.app.store.CreateOrderActivity
import com.saean.app.store.ProductDetailActivity
import com.saean.app.store.UserOrderActivity
import kotlinx.android.synthetic.main.item_list_store_order_user.view.*
import kotlinx.android.synthetic.main.item_list_store_service_user.view.*

class UserOrderAdapter(private val context: Context, private val order: ArrayList<OrderModel>) :
    RecyclerView.Adapter<UserOrderAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View? = LayoutInflater.from(parent.context).inflate(R.layout.item_list_store_order_user,parent,false)
        return ViewHolder(v!!)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val content = order[position]
        val sharedPreferences : SharedPreferences = context.getSharedPreferences(Cache.cacheName,0)
        val database : FirebaseDatabase = FirebaseDatabase.getInstance()

        holder.itemView.run {
            database.getReference("store/${content.orderStore}/storeInfo").addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    orderTo.text = "Order ke ${snapshot.child("storeName").getValue(String::class.java)}"
                }
            })
            orderTime.text = MyFunctions.formatMillie(content.orderTime!!,"dd/MM/yyyy h:m:s")
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context,UserOrderActivity::class.java)
            intent.putExtra("orderID",content.orderID)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = order.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
