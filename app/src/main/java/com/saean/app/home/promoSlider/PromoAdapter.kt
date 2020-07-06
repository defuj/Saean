package com.saean.app.home.promoSlider

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.islamkhsh.CardSliderAdapter
import com.saean.app.R
import kotlinx.android.synthetic.main.item_list_slider_promo.view.*

class PromoAdapter(private val promo: ArrayList<PromoModel>) :
    CardSliderAdapter<PromoAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_list_slider_promo,parent,false)
        return ViewHolder(v)
    }

    override fun bindVH(holder: ViewHolder, position: Int) {
        val content = promo[position]

        holder.itemView.run {
            if(content.image == ""){
                Glide.with(context)
                    .load(R.drawable.image_placeholder_landscape)
                    .into(imgSlider)
            }else{
                Glide.with(context)
                    .load(content.image)
                    .into(imgSlider)
            }
        }
    }

    override fun getItemCount() = promo.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}