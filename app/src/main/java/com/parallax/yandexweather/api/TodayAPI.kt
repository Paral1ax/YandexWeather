package com.parallax.yandexweather.api

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.android.volley.Request
import com.android.volley.TimeoutError
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.parallax.yandexweather.MainActivity
import org.json.JSONArray
import org.json.JSONObject
import java.net.UnknownHostException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.collections.ArrayList

class TodayAPI(city: String, context: Context): AbstractAPI(city, context) {

    private val temperatureAd: ArrayList<String> = ArrayList()  //список температур для ресайклера
    private val weatherAd: ArrayList<String> = ArrayList()  //список с названием погод словами для ресайклера
    private val icons: ArrayList<String> = ArrayList()  //ссылки на иконки для ресайклера
    private val time: ArrayList<String> = ArrayList()   //список со временем для ресайклера

    constructor(city: String, context: Context, lat: Double, lon:Double) : this(city, context) {
        this.lat = lat
        this.lon = lon
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getDataByAPI(byCity: Boolean) {
        var url: String = if (byCity)
            "https://api.weatherapi.com/v1/forecast.json?key=${todayAPIKey}&q=${city}&days=2&aqi=no&alerts=no"  //ссылка апи
        else "https://api.weatherapi.com/v1/forecast.json?key=${todayAPIKey}&q=${lat},${lon}&days=2&aqi=no&alerts=no"
        try {
            val queue = Volley.newRequestQueue(context)  //очередь
            val request = StringRequest(
                Request.Method.GET, url,  //реквест

                { response ->  //респонс
                    getTodayForecastTemperature(response)  //обработка джейсона и получение необходимых полей из него
                },
                {
                    Log.d("CurLog", "$it error in TodayAPI class")
                }
            )
            queue.add(request)
        } catch (v: VolleyError) {
            Log.d("CurLog", "Volley exception on getApiDataForOneWeek method: $v")
        } catch (e: UnknownHostException) {
            Log.d("CurLog", "Host exception on getApiDataForOneWeek method: $e")
        } catch (e: TimeoutError) {
            Log.d("CurLog", "Timeout exception on getApiDataForOneWeek method: $e")
        } catch (e: Exception) {
            Log.d("CurLog", "Unknown exception on getApiDataForOneWeek method: $e")
        }
    }

    /**
     * Первичная обработка Json
     * И получение данных по часам о двух днях
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getTodayForecastTemperature(response: String) {
        val json = JSONObject(response)
        Log.d("CurLog", "Today weather response $response")  //много раз логируем для дебага
        val forecast = json.getJSONObject("forecast")
        val forecastDay = forecast.getJSONArray("forecastday")
        Log.d("CurLog", "Массив погоды на сегодня по часам = $forecastDay")  //много раз логируем для дебага
        getHourlyForecast(0, forecastDay)
        getHourlyForecast(1, forecastDay)
    }

    /**
     * Получаем почасовую информацию
     * температуру, название, иконки, время
     * обрабатываем время
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getHourlyForecast(index: Int, day: JSONArray) {
        var start = 0
        val daily = day.getJSONObject(index)
        val hourly = daily.getJSONArray("hour")
        while (start < 24) {
            var now = hourly.getJSONObject(start)
            temperatureAd.add(now.getString("temp_c"))
            weatherAd.add(now.getJSONObject("condition").getString("text"))
            icons.add("https:" + now.getJSONObject("condition").getString("icon"))
            val date = now.getString("time")
            val temp = getTodayTime(date)
            val split = temp.split('T')
            val splitDate = split[0].split('-')
            time.add("${splitDate[2]}.${splitDate[1]} ${split[1]}")
            start++
        }
        Log.d("CurLog", "Нулевое время = ${time[0]}")
    }

    /**
     * форматирование времени
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getTodayTime(date: String): String {
        val pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        return LocalDateTime.parse(date, pattern).toString()
    }

    public fun getIcon(): ArrayList<String> {
        return icons
    }

    public fun getTemp(): ArrayList<String> {
        return temperatureAd
    }

    public fun getTime(): ArrayList<String> {
        return time
    }

    public fun deleteParams() {
        icons.clear()
        temperatureAd.clear()
        time.clear()
    }

    companion object {
        const val todayAPIKey = "21ebf137f4b848b5816132133222605"
    }
}