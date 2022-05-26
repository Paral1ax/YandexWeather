package com.parallax.yandexweather

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

/**
 * Класс адаптер для ресайклер вью
 */
class NextDaysWeatherRecyclerAdapter(
    private var timeAd: ArrayList<String>,
    private var imageAd: List<Int>,
    private var temperatureAd: ArrayList<String>,
    private var context: Context
) : RecyclerView.Adapter<NextDaysWeatherRecyclerAdapter.ViewHolder>() {

    /**
     * Класс вьюхолдер для ресайклервью
     */
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var image: ImageView
        var time: TextView
        var temperature: TextView

        init {
            image = itemView.findViewById(R.id.weatherPick)
            time = itemView.findViewById(R.id.future_time)
            temperature = itemView.findViewById(R.id.temp)
        }
    }

    /**
     * Реализация необходимых методов после наследования
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.today_weather_listitem, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(context).asBitmap()
            .load(imageAd[position])
            .into(holder.image)

        holder.time.text = timeAd[position]

        holder.temperature.text = temperatureAd[position]
    }

    override fun getItemCount(): Int {
        return timeAd.size
    }
}