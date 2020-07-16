package com.saean.app.store.model

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
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
import kotlinx.android.synthetic.main.item_list_store_opening_hours.view.*
import kotlinx.android.synthetic.main.item_list_store_service.view.*

class ScheduleAdapter(private val context: Context, private val schedule: ArrayList<ScheduleModel>) :
    RecyclerView.Adapter<ScheduleAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View? = LayoutInflater.from(parent.context).inflate(R.layout.item_list_store_opening_hours,parent,false)
        return ViewHolder(v!!)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val content = schedule[position]

        holder.itemView.run {
            val today = MyFunctions.getTanggal("EEEE")
            if(MyFunctions.checkStringSimilarity(today,content.scheduleDay!!) > 0.8){
                //today :v
            }else{
                //not today :v
                scheduleIcon.setColorFilter(ContextCompat.getColor(context, R.color.BLACK110))
                scheduleDay.setTextColor(context.resources.getColor(R.color.BLACK40,context.theme))
                scheduleTime.setTextColor(context.resources.getColor(R.color.BLACK40,context.theme))
            }

            if(content.scheduleStatus!!){
                scheduleDay.text = content.scheduleDay
                scheduleTime.text = "${content.scheduleStartOpen.toString().replace(".",":")} - ${content.scheduleEndOpen.toString().replace(".",":")}"
            }else{
                scheduleDay.text = content.scheduleDay
                scheduleTime.text = "TUTUP"
            }
        }
    }

    override fun getItemCount() = schedule.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
