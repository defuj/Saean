package com.saean.app.home.nearbyStore

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.saean.app.R
import com.saean.app.helper.Cache
import com.saean.app.helper.MyFunctions
import kotlinx.android.synthetic.main.item_list_store_horizontal.view.*
import kotlinx.android.synthetic.main.item_list_store_horizontal.view.storeDistance
import kotlinx.android.synthetic.main.item_list_store_horizontal.view.storeImage
import kotlinx.android.synthetic.main.item_list_store_horizontal.view.storeName
import kotlinx.android.synthetic.main.item_list_store_horizontal.view.storeRating
import kotlinx.android.synthetic.main.item_list_store_horizontal.view.storeStatusOpen
import kotlinx.android.synthetic.main.item_list_store_vertical.view.*

class StoreAdapter(context: Context,private val listType : String,private val store: ArrayList<StoreModel>) :
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
            val sharedPreferences : SharedPreferences = context.getSharedPreferences(Cache.cacheName,0)
            if(listType == "horizontal"){
                if(content.storeFront.isNullOrEmpty()){
                    Glide.with(context)
                        .load(R.drawable.image_placeholder_landscape)
                        .into(storeImage)
                }else{
                    Glide.with(context)
                        .load(content.storeFront)
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
                if(sharedPreferences.getString(Cache.latitude,"")!!.isNotEmpty()){
                    val latitude1 = sharedPreferences.getString(Cache.latitude,"")!!.toDouble()
                    val longitude1 = sharedPreferences.getString(Cache.longitude,"")!!.toDouble()
                    val latitude2 = content.storeLatitude
                    val longitude2 = content.storeLongitude

                    storeDistance.text = "${MyFunctions.formatDistance(MyFunctions.countDistance(latitude1,latitude2!!,longitude1,longitude2!!))} KM"
                }

                btnOpenMaps.setOnClickListener {
                    val gmmIntentUri = Uri.parse("google.navigation:q=${content.storeLatitude},${content.storeLongitude}")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")
                    try {
                        context.startActivity(mapIntent)
                    }catch (e:Exception){
                        Toast.makeText(context,"Google Maps not installed",Toast.LENGTH_SHORT).show()
                    }
                }
            }else{
                if(content.storeFront.isNullOrEmpty()){
                    Glide.with(context)
                        .load(R.drawable.image_placeholder_landscape)
                        .into(storeImage)
                }else{
                    Glide.with(context)
                        .load(content.storeFront)
                        .into(storeImage)
                }

                if(content.storeImage.isNullOrEmpty()){
                    Glide.with(context)
                        .load(R.drawable.ic_my_business)
                        .apply(RequestOptions.circleCropTransform())
                        .into(storeIcon)
                }else{
                    Glide.with(context)
                        .load(content.storeImage)
                        .apply(RequestOptions.circleCropTransform())
                        .into(storeIcon)
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

                btnOpenInMaps.setOnClickListener {
                    val gmmIntentUri = Uri.parse("google.navigation:q=${content.storeLatitude},${content.storeLongitude}")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")
                    try {
                        context.startActivity(mapIntent)
                    }catch (e:Exception){
                        Toast.makeText(context,"Google Maps not installed",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        holder.itemView.setOnClickListener {

        }
    }

    override fun getItemCount() = store.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
