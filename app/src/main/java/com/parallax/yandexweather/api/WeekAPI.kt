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
import org.json.JSONException
import org.json.JSONObject
import java.net.UnknownHostException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class WeekAPI(city: String, context: Context): AbstractAPI(city, context) {

    private val temperatureAd: ArrayList<String> = ArrayList()  //список температур для ресайклера
    private val iconAd: ArrayList<String> = ArrayList()  //наименование типа погоды для ресайклера
    private val dateAd: ArrayList<String> = ArrayList()
     constructor(city: String, context: Context, lat: Double, lon:Double) : this(city, context) {
        this.lat = lat
        this.lon = lon
    }
    /**
     * метод для работы с api на следующие дни, я решил взять всего 4 отметки, 3 часа вперед, 24, 48 и 72, апи предоставляет до 5 дней вперед по 3 часа каждый, я взял 25 полей, то есть 3*25 = 75 часов вперед
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun getDataByAPI(byCity: Boolean) {
        val url: String = if (byCity)
            "https://api.weatherapi.com/v1/forecast.json?key=21ebf137f4b848b5816132133222605&q=${city}&days=7&aqi=no&alerts=no"  //ссылка апи
        else "https://api.weatherapi.com/v1/forecast.json?key=21ebf137f4b848b5816132133222605&q=${lat},${lon}&days=7&aqi=no&alerts=no"
        try {
            val queue = Volley.newRequestQueue(context)  //очередь
            val request = StringRequest(
                Request.Method.GET, url,  //реквест

                { response ->  //респонс
                    getWeeklyForecast(response)  //обработка джейсона и получение необходимых полей из него
                },
                {
                    Log.d("CurLog", "$it Something Happened here")
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
     * обработка джейсона с информацией о погоде на ближайшие дни
     * В бесплатном API доступна информация только на 3 дня вперед
     * поэтому пришлось взять только 3 дня
     * а именно start < 3
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getWeeklyForecast(response: String) {
        val json = JSONObject(response)
        Log.d("CurLog", "Response WeekAPI $response")  //много раз логируем для дебага
        val forecast = json.getJSONObject("forecast")
        val forecastDay = forecast.getJSONArray("forecastday")
        Log.d("CurLog", "Массив погоды на сегодня по часам = $forecastDay")  //много раз логируем для дебага
        var start = 0
        while (start < 3) {
            getWeeklyForecastPerDay(start, forecastDay)
            start++
        }
    }

    /**
     * Получение прогноза погоды
     * температура, иконка, время
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getWeeklyForecastPerDay(index: Int, day: JSONArray) {
        print("nen")
        Log.d("CurLog", "Дебажим day = $day")
        val daily = day.getJSONObject(index)
        val date = daily.getString("date")
        val main = daily.getJSONObject("day")
        val averageTemp = main.getString("avgtemp_c")
        val icon = main.getJSONObject("condition").getString("icon")
        val temp = getTodayTime(date)
        val splitDate = temp.split('-')


        temperatureAd.add(averageTemp)
        iconAd.add("https:$icon")
        dateAd.add("${splitDate[2]}.${splitDate[1]}")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getTodayTime(date: String): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return LocalDate.parse(date, formatter).toString()
    }

    fun getWeekTemperature(): ArrayList<String> {
        return temperatureAd
    }

    fun getWeekWeather(): ArrayList<String> {
        return iconAd
    }

    fun getWeekTime(): ArrayList<String> {
        return dateAd
    }

    public fun deleteParams() {
        iconAd.clear()
        temperatureAd.clear()
        dateAd.clear()
    }
}