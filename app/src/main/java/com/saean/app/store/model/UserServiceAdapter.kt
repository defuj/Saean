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
import com.google.firebase.database.FirebaseDatabase
import com.saean.app.R
import com.saean.app.helper.Cache
import com.saean.app.helper.MyFunctions
import com.saean.app.store.CreateOrderActivity
import com.saean.app.store.ProductDetailActivity
import kotlinx.android.synthetic.main.item_list_store_service_user.view.*

class UserServiceAdapter(private val context: Context, private val service: ArrayList<ServiceModel>) :
    RecyclerView.Adapter<UserServiceAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View? = LayoutInflater.from(parent.context).inflate(R.layout.item_list_store_service_user,parent,false)
        return ViewHolder(v!!)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val content = service[position]
        val sharedPreferences : SharedPreferences = context.getSharedPreferences(Cache.cacheName,0)
        val storeID = sharedPreferences.getString(Cache.storeID,"")
        val database : FirebaseDatabase = FirebaseDatabase.getInstance()

        holder.itemView.run {
            serviceTitle.text = MyFunctions.capitalize(content.serviceTitle!!)
            serviceDescription.text = content.serviceDescription!!
        }

        holder.itemView.setOnClickListener {
            if(storeID == content.storeID){
                val dialog = SweetAlertDialog(context,SweetAlertDialog.WARNING_TYPE)
                dialog.titleText = "Perhatian"
                dialog.contentText = "Anda tidak bisa membuat order untuk toko sendiri."
                dialog.show()
            }else{
                val intent = Intent(context, CreateOrderActivity::class.java)
                intent.putExtra("serviceID",content.serviceID)
                intent.putExtra("storeID",content.storeID)
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount() = service.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
