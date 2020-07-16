package com.saean.app.home.nearbyStore

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.saean.app.R
import com.saean.app.helper.Cache
import com.saean.app.helper.MyFunctions
import com.saean.app.store.StoreActivity
import kotlinx.android.synthetic.main.activity_store.*
import kotlinx.android.synthetic.main.item_list_store_horizontal.view.*
import kotlinx.android.synthetic.main.item_list_store_horizontal.view.storeDistance
import kotlinx.android.synthetic.main.item_list_store_horizontal.view.storeImage
import kotlinx.android.synthetic.main.item_list_store_horizontal.view.storeName
import kotlinx.android.synthetic.main.item_list_store_horizontal.view.storeRating
import kotlinx.android.synthetic.main.item_list_store_horizontal.view.storeStatusOpen
import kotlinx.android.synthetic.main.item_list_store_vertical.view.*
import java.time.LocalTime

class StoreAdapter(private val context: Context,private val listType : String,private val store: ArrayList<StoreModel>) :
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
        val database : FirebaseDatabase = FirebaseDatabase.getInstance()
        val storeID = content.storeID
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

                val today = MyFunctions.getTanggal("EEEE")
                val schedule = database.getReference("store/$storeID/storeSchedule/$today")
                schedule.addValueEventListener(object : ValueEventListener{
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context,"Failed to connect",Toast.LENGTH_SHORT).show()
                        storeStatusOpen.text = "Closed"
                        storeStatusOpen.setBackgroundResource(R.drawable.background_status_store_closed)
                    }

                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.exists()){
                            if(snapshot.child("status").getValue(Boolean::class.java)!!){
                                Log.e("JADWAL","${content.storeName} Buka")
                                val target = LocalTime.parse(MyFunctions.getTanggal("HH:mm:ss"))
                                val start = MyFunctions.formatTanggal(snapshot.child("startOpen").getValue(String::class.java)!!,"HH:mm:ss","HH.mm")
                                val end = MyFunctions.formatTanggal(snapshot.child("endOpen").getValue(String::class.java)!!,"HH:mm:ss","HH.mm")
                                val targetInZone = target.isAfter(LocalTime.parse(start)) && target.isBefore(LocalTime.parse(end))
                                if(targetInZone){
                                    storeStatusOpen.visibility = View.GONE
                                    storeStatusOpen.text = "Open"
                                    storeStatusOpen.setBackgroundResource(R.drawable.background_status_store_closed)
                                }else{
                                    storeStatusOpen.text = "Closed"
                                    storeStatusOpen.setBackgroundResource(R.drawable.background_status_store_closed)
                                }
                            }else{
                                storeStatusOpen.text = "Closed"
                                storeStatusOpen.setBackgroundResource(R.drawable.background_status_store_closed)
                            }
                        }else{
                            storeStatusOpen.text = "Closed"
                            storeStatusOpen.setBackgroundResource(R.drawable.background_status_store_closed)
                        }
                    }

                })

                if(sharedPreferences.getString(Cache.latitude,"")!!.isNotEmpty()){
                    val latitude1 = sharedPreferences.getString(Cache.latitude,"")!!.toDouble()
                    val longitude1 = sharedPreferences.getString(Cache.longitude,"")!!.toDouble()
                    val latitude2 = content.storeLatitude
                    val longitude2 = content.storeLongitude

                    storeDistance.text = if(MyFunctions.countDistance(latitude1, latitude2!!,longitude1,longitude2!!) >= 1){
                        "${MyFunctions.formatDistance(MyFunctions.countDistance(latitude1, latitude2,longitude1,longitude2))} km"
                    }else{
                        "${MyFunctions.formatDistance(MyFunctions.countDistance(latitude1, latitude2,longitude1,longitude2)*1000)} m"
                    }
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

                val today = MyFunctions.getTanggal("EEEE")
                val schedule = database.getReference("store/$storeID/storeSchedule/$today")
                schedule.addValueEventListener(object : ValueEventListener{
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context,"Failed to connect",Toast.LENGTH_SHORT).show()
                        storeStatusOpen.text = "Closed"
                        storeStatusOpen.setBackgroundResource(R.drawable.background_status_store_closed)
                    }

                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.exists()){
                            if(snapshot.child("status").getValue(Boolean::class.java)!!){
                                Log.e("JADWAL","${content.storeName} Buka")
                                val target = LocalTime.parse(MyFunctions.getTanggal("HH:mm:ss"))
                                val start = MyFunctions.formatTanggal(snapshot.child("startOpen").getValue(String::class.java)!!,"HH:mm:ss","HH.mm")
                                val end = MyFunctions.formatTanggal(snapshot.child("endOpen").getValue(String::class.java)!!,"HH:mm:ss","HH.mm")
                                val targetInZone = target.isAfter(LocalTime.parse(start)) && target.isBefore(LocalTime.parse(end))
                                if(targetInZone){
                                    storeStatusOpen.visibility = View.GONE
                                    storeStatusOpen.text = "Open"
                                    storeStatusOpen.setBackgroundResource(R.drawable.background_status_store_closed)
                                }else{
                                    storeStatusOpen.text = "Closed"
                                    storeStatusOpen.setBackgroundResource(R.drawable.background_status_store_closed)
                                }
                            }else{
                                storeStatusOpen.text = "Closed"
                                storeStatusOpen.setBackgroundResource(R.drawable.background_status_store_closed)
                            }
                        }else{
                            storeStatusOpen.text = "Closed"
                            storeStatusOpen.setBackgroundResource(R.drawable.background_status_store_closed)
                        }
                    }

                })

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
            val intent = Intent(context,StoreActivity::class.java)
            intent.putExtra("storeID",storeID)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = store.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
