package com.saean.app.home.nearbyStore

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.saean.app.R
import kotlinx.android.synthetic.main.item_list_store_horizontal.view.*
import kotlinx.android.synthetic.main.item_list_store_horizontal.view.storeDistance
import kotlinx.android.synthetic.main.item_list_store_horizontal.view.storeImage
import kotlinx.android.synthetic.main.item_list_store_horizontal.view.storeName
import kotlinx.android.synthetic.main.item_list_store_horizontal.view.storeRating
import kotlinx.android.synthetic.main.item_list_store_horizontal.view.storeStatusOpen
import kotlinx.android.synthetic.main.item_list_store_vertical.view.*

class StoreAdapter(private val listType : String,private val store: ArrayList<StoreModel>) :
    RecyclerView.Adapter<StoreAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View? = if(listType == "horizontal"){
            LayoutInflater.from(parent.context).inflate(R.layout.item_list_store_horizontal,parent,false)
        }else{
            LayoutInflater.from(parent.context).inflate(R.layout.item_list_store_vertical,parent,false)
        }
        return ViewHolder(v!!)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val content = store[position]

        holder.itemView.run {
            if(listType == "horizontal"){
                if(content.storeImage.isNullOrEmpty()){
                    Glide.with(context)
                        .load(R.drawable.image_placeholder_landscape)
                        .into(storeImage)
                }else{
                    Glide.with(context)
                        .load(content.storeImage)
                        .into(storeImage)
                }

                storeName.text = content.storeName
                storeDescription.text = content.storeDescription
                storeRating.rating = content.storeRating!!

                if(content.storeStatusOpen!!){
                    storeStatusOpen.text = "Open"
                    storeStatusOpen.setBackgroundResource(R.drawable.background_status_store_open)
                }else{
                    storeStatusOpen.text = "Closed"
                    storeStatusOpen.setBackgroundResource(R.drawable.background_status_store_closed)
                }
                storeDistance.text = "1.5 KM"
            }else{
                if(content.storeImage.isNullOrEmpty()){
                    Glide.with(context)
                        .load(R.drawable.image_placeholder_landscape)
                        .into(storeImage)
                }else{
                    Glide.with(context)
                        .load(content.storeImage)
                        .into(storeImage)
                }

                storeName.text = content.storeName
                storeAddress.text = content.storeAddress

                if(content.storeStatusOpen!!){
                    storeStatusOpen.text = "Open"
                    storeStatusOpen.setBackgroundResource(R.drawable.background_status_store_open)
                }else{
                    storeStatusOpen.text = "Closed"
                    storeStatusOpen.setBackgroundResource(R.drawable.background_status_store_closed)
                }
            }
        }

        holder.itemView.setOnClickListener {

        }
    }

    override fun getItemCount() = store.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
