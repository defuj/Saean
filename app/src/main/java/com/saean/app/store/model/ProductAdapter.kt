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

class ProductAdapter(private val context: Context, private val product: ArrayList<ProductModel>) :
    RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View? = LayoutInflater.from(parent.context).inflate(R.layout.item_list_store_product,parent,false)
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
                productReady.text = "Tersedia"
                productReady.setBackgroundResource(R.drawable.background_status_store_open)
            }else{
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

            if(content.productStock!! > 0){
                containerProductOut.visibility = View.GONE
            }else{
                containerProductOut.visibility = View.VISIBLE
            }

            btnDelete.setOnClickListener {
                val dialog = SweetAlertDialog(context,SweetAlertDialog.WARNING_TYPE)
                dialog.titleText = "Hapus Barang"
                dialog.contentText = "Apakah Anda ingin menghapus produk ini?"
                dialog.confirmText = "Iya, hapus"
                dialog.cancelText = "Tidak"
                dialog.setConfirmClickListener {
                    database.getReference("product/goods/$storeID").child("${content.productID}").removeValue()
                    dialog.dismissWithAnimation()
                    Toast.makeText(context,"Produk telah dihapus",Toast.LENGTH_SHORT).show()
                }
                dialog.setCancelClickListener {
                    dialog.dismissWithAnimation()
                }
                dialog.show()
            }

            btnEdit.setOnClickListener {
                val intent = Intent(context,StoreAddProductActivity::class.java)
                intent.putExtra("productID",content.productID)
                intent.putExtra("productName",content.productName)
                intent.putExtra("productDescription",content.productDescription)
                intent.putExtra("productPrice",content.productPrice!!.toInt())
                intent.putExtra("productStock",content.productStock!!)
                intent.putExtra("productType","barang")
                if(content.productImage!!.size > 0){
                    intent.putExtra("imageCount",content.productImage!!.size)
                    for(i in 0 until content.productImage!!.size){
                        intent.putExtra("image${i+1}",content.productImage!![i].bannerImage.toString())
                    }
                }else{
                    intent.putExtra("imageCount",0)
                }

                context.startActivity(intent)
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
