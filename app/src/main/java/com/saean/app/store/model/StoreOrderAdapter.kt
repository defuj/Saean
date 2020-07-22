package com.saean.app.store.model

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
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
import com.saean.app.store.StoreOrderActivity
import com.saean.app.store.UserOrderActivity
import kotlinx.android.synthetic.main.item_list_store_order_store.view.*
import kotlinx.android.synthetic.main.item_list_store_order_user.view.*
import kotlinx.android.synthetic.main.item_list_store_order_user.view.orderProgress
import kotlinx.android.synthetic.main.item_list_store_order_user.view.orderTime
import kotlinx.android.synthetic.main.item_list_store_service_user.view.*

class StoreOrderAdapter(private val context: Context, private val order: ArrayList<OrderModel>) :
    RecyclerView.Adapter<StoreOrderAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View? = LayoutInflater.from(parent.context).inflate(R.layout.item_list_store_order_store,parent,false)
        return ViewHolder(v!!)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val content = order[position]
        val sharedPreferences : SharedPreferences = context.getSharedPreferences(Cache.cacheName,0)
        val database : FirebaseDatabase = FirebaseDatabase.getInstance()

        holder.itemView.run {
            database.getReference("user/${MyFunctions.changeToUnderscore(content.orderUser!!)}").addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    orderFrom.text = "Order dari ${snapshot.child("userName").getValue(String::class.java)}"
                }
            })
            orderTime.text = MyFunctions.formatMillie(content.orderTime!!,"dd/MM/yyyy h:m:s")
            if(content.orderStatus == 0){
                //order waiting confirmation store
                orderProgress.text = "Menunggu Konfirmasi"
                orderProgress.setTextColor(Color.parseColor("#1EA360"))
                orderProgress.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_order_waiting,0,0,0)
            }else if(content.orderStatus == 1){
                //order has confirm by store
                if(content.orderProcess == 0){
                    orderProgress.text = "Menunggu Pemeriksaan"
                    orderProgress.setTextColor(Color.parseColor("#1EA360"))
                    orderProgress.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_order_waiting,0,0,0)
                }else if(content.orderProcess == 1){
                    //Sedang Diproses
                    orderProgress.text = "Sedang Diproses"
                    orderProgress.setTextColor(Color.parseColor("#FFBB33"))
                    orderProgress.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_order_process,0,0,0)
                }else if(content.orderProcess == 2){
                    //Menunggu Keputusan
                    orderProgress.text = "Menunggu Keputusan"
                    orderProgress.setTextColor(Color.parseColor("#1EA360"))
                    orderProgress.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_order_waiting,0,0,0)
                }else if(content.orderProcess == 3){
                    //Batal
                    orderProgress.text = "Dibatalkan"
                    orderProgress.setTextColor(Color.parseColor("#FF6745"))
                    orderProgress.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_order_cancel,0,0,0)
                }else if(content.orderProcess == 4){
                    //Perbaikan
                    orderProgress.text = "Dalam Pemeliharaan"
                    orderProgress.setTextColor(Color.parseColor("#FFBB33"))
                    orderProgress.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_order_process,0,0,0)
                }else if(content.orderProcess == 5){
                    //Selesai
                    orderProgress.text = "Selesai"
                    orderProgress.setTextColor(Color.parseColor("#2196F3"))
                    orderProgress.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_order_finish,0,0,0)
                }
            }else if(content.orderStatus == 2){
                //order has abort by store
                orderProgress.text = "Order Ditolak"
                orderProgress.setTextColor(Color.parseColor("#FF6745"))
                orderProgress.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_order_cancel,0,0,0)
            }
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context,StoreOrderActivity::class.java)
            intent.putExtra("orderID",content.orderID)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = order.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
