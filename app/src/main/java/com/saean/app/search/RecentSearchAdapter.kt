package com.saean.app.search

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.saean.app.R
import com.saean.app.helper.Cache
import com.saean.app.helper.MyFunctions
import kotlinx.android.synthetic.main.item_list_recent_search.view.*

class RecentSearchAdapter(private val context: Context, private val product: ArrayList<RecentSearchModel>) :
RecyclerView.Adapter<RecentSearchAdapter.ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentSearchAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_list_recent_search,parent,false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int = product.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecentSearchAdapter.ViewHolder, position: Int) {
        val content = product[position]
        val sharedPreferences : SharedPreferences = context.getSharedPreferences(Cache.cacheName,0)
        val database : FirebaseDatabase = FirebaseDatabase.getInstance()

        holder.itemView.run {
            txtSearchRecent.text = content.search

            btnDeleteRecentSearch.setOnClickListener {
                val email = MyFunctions.changeToUnderscore(sharedPreferences.getString(Cache.email,"")!!)
                database.getReference("user/$email/recentSearch").child(MyFunctions.changeToUnderscore(content.search!!)).removeValue()
            }

            holder.itemView.setOnClickListener {
                val intent = Intent(context, SearchActivity::class.java)
                intent.putExtra("search",content.search)
                context.startActivity(intent)
                (context as RecentSearchActivity).finish()
            }
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}