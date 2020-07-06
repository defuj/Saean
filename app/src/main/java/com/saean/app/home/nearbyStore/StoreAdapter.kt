package com.saean.app.home.nearbyStore

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.islamkhsh.CardSliderAdapter
import com.saean.app.R
import kotlinx.android.synthetic.main.item_list_slider_promo.view.*
import kotlinx.android.synthetic.main.item_list_store.view.*

class StoreAdapter(private val store: ArrayList<StoreModel>) :
    RecyclerView.Adapter<StoreAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_list_store,parent,false)
        return ViewHolder(v)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val content = store[position]

        holder.itemView.run {
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
        }

        holder.itemView.setOnClickListener {

        }
    }

    override fun getItemCount() = store.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}