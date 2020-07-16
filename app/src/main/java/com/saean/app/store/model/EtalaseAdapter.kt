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
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase
import com.saean.app.R
import com.saean.app.helper.Cache
import com.saean.app.helper.MyFunctions
import com.saean.app.store.ProductDetailActivity
import com.saean.app.store.StoreAddProductActivity
import kotlinx.android.synthetic.main.item_list_store_etalase.view.*
import kotlinx.android.synthetic.main.item_list_store_service.view.*

class EtalaseAdapter(private val context: Context, private val banner: ArrayList<BannerModel>) :
    RecyclerView.Adapter<EtalaseAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View? = LayoutInflater.from(parent.context).inflate(R.layout.item_list_store_etalase,parent,false)
        return ViewHolder(v!!)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val content = banner[position]

        holder.itemView.run {
            val sharedPreferences : SharedPreferences = context.getSharedPreferences(Cache.cacheName,0)
            val storeID = sharedPreferences.getString(Cache.storeID,"")
            val database : FirebaseDatabase = FirebaseDatabase.getInstance()

            Glide.with(context)
                .load(content.bannerImage)
                .into(storeEtalase)

            btnDeleteEtalase.setOnClickListener {
                val dialog = SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                dialog.titleText = "Hapus Gambar"
                dialog.contentText = "Apakah Anda ingin menghapus gambar ini?"
                dialog.confirmText = "Iya, hapus"
                dialog.cancelText = "Tidak"
                dialog.setConfirmClickListener {
                    database.getReference("store/$storeID/storeInfo/storeFront").child("${content.bannerID}").removeValue()
                    dialog.dismissWithAnimation()
                    Toast.makeText(context,"Gambar telah dihapus", Toast.LENGTH_SHORT).show()
                }
                dialog.setCancelClickListener {
                    dialog.dismissWithAnimation()
                }
                dialog.show()
            }
        }
    }

    override fun getItemCount() = banner.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
