package com.saean.app.notification

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.firebase.database.FirebaseDatabase
import com.saean.app.R
import com.saean.app.helper.Cache
import com.saean.app.helper.MyFunctions
import kotlinx.android.synthetic.main.item_list_notification.view.*

class NotificationAdapter(private val context: Context, private val notification: ArrayList<NotificationModel>) :
    RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View? = LayoutInflater.from(parent.context).inflate(R.layout.item_list_notification,parent,false)
        return ViewHolder(v!!)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val content = notification[position]
        val sharedPreferences : SharedPreferences = context.getSharedPreferences(Cache.cacheName,0)
        val email = MyFunctions.changeToUnderscore(sharedPreferences.getString(Cache.email,"")!!)
        val database : FirebaseDatabase = FirebaseDatabase.getInstance()

        holder.itemView.run {
            notificationTitle.text = content.notificationTitle
            notificationBody.text = content.notificationBody
            notificationTime.text = MyFunctions.formatMillie(content.notificationTime!!,"dd MMM")
            if(content.notificationStatus == "unread"){
                containerNotification.setBackgroundColor(Color.parseColor("#31FFEB3B"))
            }else{
                containerNotification.setBackgroundColor(Color.parseColor("#FFFFFF"))
            }
        }

        holder.itemView.setOnClickListener {
            database.getReference("notification/$email/${content.notificationID}").child("notificationStatus").setValue("read")
            val dialog = SweetAlertDialog(context,SweetAlertDialog.NORMAL_TYPE)
            dialog.titleText = content.notificationTitle
            dialog.contentText = content.notificationBody
            dialog.show()
        }
    }

    override fun getItemCount() = notification.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
