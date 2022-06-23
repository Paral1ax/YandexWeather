package com.parallax.yandexweather.api

import android.content.Context
import android.opengl.Visibility
import android.util.Log
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.TimeoutError
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.parallax.yandexweather.MainActivity
import org.json.JSONObject
import java.net.UnknownHostException
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.HashMap

/**
 * Класс для работы с Апи, праймари конструктор имеет только город и контекст
 */
class CurrentAPI(city: String, context: Context): AbstractAPI(city, context) {

    private val currentParams: HashMap<String, String> = HashMap()  //хэшмапа для отображения темпы, ощущения, осадков и влажности

    /**
     * в Секондари конструкторе проставляем долготу и широту для поиска по ним
     */
        constructor(city: String, context: Context, lat: Double, lon:Double) : this(city, context) {
            this.lat = lat
            this.lon = lon
        }

    /**
     * Метод для работы с api погоды
     * Получаем данные о текущем состоянии
     */
    override fun getDataByAPI(byCity: Boolean) {
        val url: String = if (byCity)
            "https://api.weatherapi.com/v1/current.json?key=21ebf137f4b848b5816132133222605&q=${city}&aqi=no" //ссылка на апи
        else "https://api.weatherapi.com/v1/current.json?key=21ebf137f4b848b5816132133222605&q=${lat},${lon}&aqi=no"
        try {
            val queue = Volley.newRequestQueue(context)  //добавляем в очередь на запрос
            val request = StringRequest(
                Request.Method.GET, url,  //отправляем запрос

                { response ->  //получаем ответ в виде джейсона
                    getCurrentWeather(response)
                },
                {
                    Log.d("CurLog", it.toString())

                }
            )
            queue.add(request)
        } catch (v: VolleyError) {
            Log.d("CurLog", "Volley exception on getApiDataForOneDay method: $v")
        } catch (e: UnknownHostException) {
            Log.d("CurLog", "Host exception on getApiDataForOneDay method: $e")
        } catch (e: TimeoutError) {
            Log.d("CurLog", "Timeout exception on getApiDataForOneDay method: $e")
        } catch (e: Exception) {
            Log.d("CurLog", "Unknown exception on getApiDataForOneDay method: $e")
        }
    }

    /**
     * Обработка Json ответа
     * и сохранение данных в хэшмап
     */
    private fun getCurrentWeather(response: String) {
        val json = JSONObject(response)

        val location = json.getJSONObject("location").getString("name")
        val charset: Charset = Charsets.US_ASCII
        val coder = String(location.toByteArray(charset))
        Log.d("CurLog", "Coder meaning: $coder")
        currentParams["location"] = coder

        val current = json.getJSONObject("current")

        val temp = current.getString("temp_c")
        currentParams["temp"] = temp

        val icon = current.getJSONObject("condition").getString("icon")
        currentParams["icon"] = "https:$icon"

        val text = current.getJSONObject("condition").getString("text")
        currentParams["text"] = text

        val windKph = current.getString("wind_kph")
        currentParams["windSpeed"] = windKph

        val humidity = current.getString("humidity")
        currentParams["humidity"] = "$humidity%"

        val feelsLike = current.getString("feelslike_c")
        currentParams["feelsLike"] = feelsLike

    }

    /**
     * геттер для доступа к полю
     */
    fun getCurrentParams(): HashMap<String, String> {
        return currentParams
    }

    fun deleteCurrentParams() {
        currentParams.clear()
    }

}