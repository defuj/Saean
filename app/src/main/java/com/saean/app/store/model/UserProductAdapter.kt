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
import kotlinx.android.synthetic.main.item_list_store_product.view.*

class UserProductAdapter(private val context: Context, private val product: ArrayList<ProductModel>) :
    RecyclerView.Adapter<UserProductAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View? = LayoutInflater.from(parent.context).inflate(R.layout.item_list_store_product_user,parent,false)
        return ViewHolder(v!!)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val content = product[position]

        holder.itemView.run {
            val sharedPreferences : SharedPreferences = context.getSharedPreferences(Cache.cacheName,0)
            val storeID = sharedPreferences.getString(Cache.storeID,"")
            val database : FirebaseDatabase = FirebaseDatabase.getInstance()
            productName.text = MyFunctions.capitalize(content.productName!!)
            productPrice.text = MyFunctions.formatUang(content.productPrice!!)

            if(content.productStock!! > 0){
                containerProductOut.visibility = View.GONE
                productReady.visibility = View.GONE
                productReady.text = "Tersedia"
                productReady.setBackgroundResource(R.drawable.background_status_store_open)
            }else{
                containerProductOut.visibility = View.GONE
                productReady.visibility = View.VISIBLE
                productReady.text = "Tidak Tersedia"
                productReady.setBackgroundResource(R.drawable.background_status_store_closed)
            }

            if(content.productImage != null){
                Glide.with(context)
                    .load(content.productImage!![0].bannerImage)
                    .into(productImage)
            }else{
                Glide.with(context)
                    .load(R.drawable.image_placeholder)
                    .into(productImage)
            }

            productStock.text = if(content.productStock!! > 10){
                "sisa > ${content.productStock}"
            }else{
                if(content.productStock!! > 0){
                    "tersisa ${content.productStock}"
                }else{
                    "habis"
                }
            }
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context,ProductDetailActivity::class.java)
            intent.putExtra("productType","barang")
            intent.putExtra("productID",content.productID)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = product.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
