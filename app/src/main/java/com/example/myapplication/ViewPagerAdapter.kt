package com.example.myapplication

import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ViewPagerAdapter (private var page: List<String>): RecyclerView.Adapter<ViewPagerAdapter.Pager2ViewHolder>(){
    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val itemTitle: TextView = itemView.findViewById(R.id.marketItem_cost)
    }
    var pos = 1
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Pager2ViewHolder {
        val pageLayout = if(pos==0) R.layout.activity_market_sellpokemon else if (pos==1) R.layout.activity_market_marketlist else  R.layout.activity_market_bank
        return Pager2ViewHolder(LayoutInflater.from(parent.context).inflate(pageLayout, parent, false))
    }

    override fun getItemCount(): Int {
        return page.size
    }

    override fun onBindViewHolder(holder: Pager2ViewHolder, position: Int) {
        holder.itemTitle.text = page[position]
        pos = position
    }
}