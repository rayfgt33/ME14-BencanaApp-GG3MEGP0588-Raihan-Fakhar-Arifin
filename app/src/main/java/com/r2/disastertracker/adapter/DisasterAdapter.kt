package com.r2.disastertracker.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.r2.disastertracker.R
import com.r2.disastertracker.data.DisasterResponses
import com.r2.disastertracker.data.Geometry
import com.squareup.picasso.Picasso
import retrofit2.Callback

class DisasterAdapter (val context: Context, private var disasterList: ArrayList<Geometry>):
    RecyclerView.Adapter<DisasterAdapter.DisasterAdapterHolder>(){
    var image : Int = 0


    class DisasterAdapterHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val ivImage : ImageView = itemView.findViewById(R.id.IvImage)
        val tvDisaster : TextView = itemView.findViewById(R.id.tvDisaster)
        val tvTitle : TextView = itemView.findViewById(R.id.tvTitle)
        val tvRegion : TextView = itemView.findViewById(R.id.tvRegion)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DisasterAdapterHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list, parent, false)
        return DisasterAdapterHolder(itemView)
    }

    override fun getItemCount(): Int {
        return disasterList.size
    }

    override fun onBindViewHolder(holder: DisasterAdapterHolder, position: Int) {
        val currentDisaster = disasterList[position].properties
        val stringDateTime = dateToStringList(currentDisaster.created)
        val date = stringDateTime[0]
        val time = stringDateTime[1]


        holder.tvDisaster.text = currentDisaster.disasterType
        when(currentDisaster.disasterType){
            "flood" -> holder.tvTitle.text = "Bencana: Banjir"
            "volcano" -> holder.tvTitle.text = "Bencana: Gunung Merapi"
            "wind" -> holder.tvTitle.text = "Bencana: Angin"
            "haze" -> holder.tvTitle.text = "Bencana: Kabut"
            "fire" -> holder.tvTitle.text = "Bencana: Kebakaran"
            "earthquake" -> holder.tvTitle.text = "Bencana: Gempa Bumi"
            else -> holder.tvTitle.text = "Bencana: Unknown"
        }
        when(currentDisaster.disasterType){
            "flood" -> image = R.drawable.banjir_1
            "volcano" -> image = R.drawable.volcano
            "wind" -> image = R.drawable.wind
            "haze" -> image = R.drawable.haze
            "fire" -> image = R.drawable.fire
            "earthquake" -> image = R.drawable.earthquake
            else -> image = R.drawable.rounded
        }
        Picasso.get()
            .load(currentDisaster.imgUrl)
            .resize(400,300)
            .placeholder(image)
            .into(holder.ivImage)
        holder.tvRegion.text = currentDisaster.tags.region
        holder.tvDate.text = date
        holder.tvTime.text = time

    }

    fun dateToStringList(string : String): List<String>{
        val delim = "T"
        val delim1 = "."
        val createdArr = string.split(delim, delim1)
        return createdArr
    }

    fun setData(data: ArrayList<Geometry>){
        disasterList.clear()
        disasterList.addAll(data)
        notifyDataSetChanged()
    }

    fun setFilteredList(disasterList : ArrayList<Geometry>){
        this.disasterList.clear()
        this.disasterList = disasterList
        notifyDataSetChanged()
    }

    fun getData (): ArrayList<Geometry>{
        return this.disasterList
    }


}