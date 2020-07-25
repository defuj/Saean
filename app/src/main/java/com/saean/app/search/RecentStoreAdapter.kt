package com.saean.app.search

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.saean.app.R
import com.saean.app.store.StoreActivity
import kotlinx.android.synthetic.main.item_list_recent_store.view.*

class RecentStoreAdapter(private val context: Context, private val store: ArrayList<RecentStoreModel>) :
RecyclerView.Adapter<RecentStoreAdapter.ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentStoreAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_list_recent_store,parent,false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int = store.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecentStoreAdapter.ViewHolder, position: Int) {
        val content = store[position]

        holder.itemView.run {
            if(!content.storePicture.isNullOrEmpty()){
                Glide.with(context)
                    .load(content.storePicture)
                    .into(storePicture)
            }

            holder.itemView.setOnClickListener {
                val intent = Intent(context, StoreActivity::class.java)
                intent.putExtra("storeID",content.storeID)
                context.startActivity(intent)
            }
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}