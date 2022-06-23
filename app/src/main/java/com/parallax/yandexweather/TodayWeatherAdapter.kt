package com.parallax.yandexweather

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class TodayWeatherAdapter(
    private var todayTimeAd: ArrayList<String>,
    private var todayImageAd: ArrayList<String>,
    private var todayTemperatureAd: ArrayList<String>,
    private var context: Context
): RecyclerView.Adapter<TodayWeatherAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var image: ImageView
        var time: TextView
        var temperature: TextView

        init {
            image = itemView.findViewById(R.id.todayWeather)
            time = itemView.findViewById(R.id.todayTime)
            temperature = itemView.findViewById(R.id.todayTemp)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TodayWeatherAdapter.ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.activity_today_weather_listitem, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: TodayWeatherAdapter.ViewHolder,
        position: Int
    ) {
        Glide.with(context)
            .load(todayImageAd[position])
            .into(holder.image)

        holder.time.text = todayTimeAd[position]

        holder.temperature.text = todayTemperatureAd[position]
    }

    override fun getItemCount(): Int {
        return todayImageAd.size
    }

}