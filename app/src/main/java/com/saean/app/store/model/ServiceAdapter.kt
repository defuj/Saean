package com.saean.app.store.model

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.firebase.database.FirebaseDatabase
import com.saean.app.R
import com.saean.app.helper.Cache
import com.saean.app.helper.MyFunctions
import com.saean.app.store.StoreAddProductActivity
import kotlinx.android.synthetic.main.item_list_store_service.view.*

class ServiceAdapter(context: Context, private val service: ArrayList<ServiceModel>) :
    RecyclerView.Adapter<ServiceAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View? = LayoutInflater.from(parent.context).inflate(R.layout.item_list_store_service,parent,false)
        return ViewHolder(v!!)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val content = service[position]

        holder.itemView.run {
            val sharedPreferences : SharedPreferences = context.getSharedPreferences(Cache.cacheName,0)
            val storeID = sharedPreferences.getString(Cache.storeID,"")
            val database : FirebaseDatabase = FirebaseDatabase.getInstance()

            serviceTitle.text = MyFunctions.capitalize(content.serviceTitle!!)
            serviceDescription.text = content.serviceDescription!!

            btnDelete.setOnClickListener {
                val dialog = SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                dialog.titleText = "Hapus Layanan"
                dialog.contentText = "Apakah Anda ingin menghapus layanan ini?"
                dialog.confirmText = "Iya, hapus"
                dialog.cancelText = "Tidak"
                dialog.setConfirmClickListener {
                    database.getReference("product/service/$storeID").child("${content.serviceID}").removeValue()
                    dialog.dismissWithAnimation()
                    Toast.makeText(context,"Layanan telah dihapus", Toast.LENGTH_SHORT).show()
                }
                dialog.setCancelClickListener {
                    dialog.dismissWithAnimation()
                }
                dialog.show()
            }

            btnEdit.setOnClickListener {
                val intent = Intent(context, StoreAddProductActivity::class.java)
                intent.putExtra("productID",content.serviceID)
                intent.putExtra("productName",content.serviceTitle)
                intent.putExtra("productDescription",content.serviceDescription)
                intent.putExtra("productType","jasa")

                context.startActivity(intent)
            }
        }

        holder.itemView.setOnClickListener {

        }
    }

    override fun getItemCount() = service.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
