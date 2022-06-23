package com.parallax.yandexweather

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SelectCityRecyclerview(
    private var geoAd: ArrayList<String>,
    private var weatherAd: ArrayList<String>,
    private var temperatureAd: ArrayList<String>,
    private var context: Context
): RecyclerView.Adapter<SelectCityRecyclerview.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var geoPlace: TextView
        var weatherKind: TextView
        var temperature: TextView

        init {
            geoPlace = itemView.findViewById(R.id.geoPlace)
            weatherKind = itemView.findViewById(R.id.weatherKindWords)
            temperature = itemView.findViewById(R.id.cityTemperature)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.select_city_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.geoPlace.text = geoAd[position]

        holder.weatherKind.text = weatherAd[position]

        holder.temperature.text = temperatureAd[position]
    }

    override fun getItemCount(): Int {
        return geoAd.size
    }
}