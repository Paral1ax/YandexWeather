package com.parallax.yandexweather

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.TimeoutError
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject
import java.net.UnknownHostException

class MainActivity : AppCompatActivity() {

    private var dayAd: ArrayList<String> = ArrayList()  //список часов для ресайклера
    private var imageAd: List<Int> = ArrayList()  //список с картинками для ресайклера
    private var temperatureAd: ArrayList<String> = ArrayList()  //список температур для ресайклера
    private var weatherAd: ArrayList<String> = ArrayList()  //наименование типа погоды для ресайклера
    private var todayParams: HashMap<String, String> = HashMap()  //хэшмапа для отображения темпы, ощущения, осадков и влажности
    private lateinit var temp: TextView
    private lateinit var humidity: TextView
    private lateinit var speed: TextView
    private lateinit var perception: TextView
    private var context: Context = this  //на самом деле лишнее, пытался с корутинами метод создать, а там this это корутина
    private lateinit var refresh: Button  //кнопка для обновления
    private lateinit var weatherPhoto: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getApiDataForOneDay()  //запрос для получения погоды на сегодня
        getApiDataForOneWeek()  //запрос для получения погоды на несколько дней вперед
        setContentView(R.layout.activity_main)
        perception = findViewById(R.id.perception)  //ищем вьюшки
        speed = findViewById(R.id.wind_speed)
        humidity = findViewById(R.id.humidity)
        temp = findViewById(R.id.degrees)
        refresh = findViewById(R.id.refresh)
        weatherPhoto = findViewById(R.id.weather_type)

    }
    override fun onResume() {
        super.onResume()
        try {
            refresh.setOnClickListener{  //обработчик нажатия на кнопку обновления
                if (todayParams.isNotEmpty() && weatherAd.isNotEmpty()) {
                    bindingDataInViews()  //устанавливаем данные во вьюшки для отображения погоды сегодня
                }
            }
        } catch (e: Exception) {
            Log.d("CurLog", "On Resume method $e")
        }
    }

    /**
     * Метод для работы с api огоды на сегодня, я тренировался на нем, второй получше
     */
    private fun getApiDataForOneDay() {
        var city: String = ""  //на будущее
        val url = "https://api.openweathermap.org/data/2.5/weather?lat=55.7504461&lon=37.6174943&appid=${key}&units=metric"  //ссылка на апи
        try {
            val queue = Volley.newRequestQueue(context)  //добавляем в очередь на запрос
            val request = StringRequest(Request.Method.GET, url,  //отправляем запрос

                { response ->  //получаем ответ в виде джейсона
                    val json = JSONObject(response)
                    Log.d("CurLog", response)  //много раз логируем для дебага
                    val data = json.getJSONObject("main")  //обрабатываем джейсон

                    todayParams["temp"] = data.getString("temp")  //и кладем в мапу для отображения
                    todayParams["feels_like"] = data.getString("feels_like")
                    val humidity = data.getString("humidity")
                    todayParams["humidity"] = "$humidity%"
                    Log.d("CurLog", todayParams.getValue("temp") + " " + todayParams.getValue("feels_like") + " " + todayParams.getValue("humidity"))
                    val weather = json.getJSONArray("weather")
                    val type = weather.getJSONObject(0)
                    Log.d("CurLog", type.getString("main"))
                    todayParams["main"] = type.getString("main")
                    val wind = json.getJSONObject("wind")
                    val speed = wind.getString("speed")
                    todayParams["speed"] = speed
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
     * метод для работы с api на следующие дни, я решил взять всего 4 отметки, 3 часа вперед, 24, 48 и 72, апи предоставляет до 5 дней вперед по 3 часа каждый, я взял 25 полей, то есть 3*25 = 75 часов вперед
     */
    private fun getApiDataForOneWeek() {
        val url = "https://api.openweathermap.org/data/2.5/forecast?lat=55.7504461&lon=37.6174943&appid=75642222f41cf9a3c0f89b8636eca48f&units=metric&cnt=25"  //ссылка апи
        try {
            val queue = Volley.newRequestQueue(this)  //очередь
            val request = StringRequest(Request.Method.GET, url,  //реквест

                { response ->  //респонс
                    getWeeklyForecastTemperature(response)  //обработка джейсона и получение необходимых полей из него
                },
                {
                    Log.d("CurLog", "$it here")
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
     * Устанавливаем значения в сегодняшние поля
     */
    private fun bindingDataInViews() {
        temp.text = todayParams.getValue("temp")
        humidity.text = todayParams.getValue("humidity")
        speed.text = todayParams.getValue("speed")
        perception.text = todayParams.getValue("feels_like")
        convertWeatherToPicks()
    }

    /**
     * обработка джейсона с информацией о погоде на ближайшие дни
     */
    private fun getWeeklyForecastTemperature(response: String) {
        try {
            var start = 0
            val json = JSONObject(response)
            Log.d("CurLog", "All = $json")
            val temp = json.getJSONArray("list")
            while (start < 24) {  //делаем в цикле потому что будет 4 элемента в ресайклере, обрабатываем  и сразу добавляем в соответствующие списки
                val day = temp.getJSONObject(start)
                temperatureAd.add(getFourDaysTemperature("main", "temp", day))  //добавляем в список с температурой
                weatherAd.add(getFourDaysWeather(day))  //добавляем в список с наименованиями погоды
                start += if (start > 0) 8 else 7
            }
        } catch (e: JSONException) {
            Log.d("CurLog", "Json exception on getWeeklyForecastTemperature method: $e")
        } catch (e: Exception) {
            Log.d("CurLog", "Unknown exception on getWeeklyForecastTemperature method: $e")
        }
    }

    /**
     обобщенная обработка джейсона
     на самом деле я думал что он подойдет и под наименование погоды и под основные данные о погоде, но в наименовании погоды зачем то в апи естьс писок поверх полей, из одного элемента
     */
    private fun getFourDaysTemperature(keyWord1: String, keyWord2: String, json: JSONObject): String {
        var jsonMain = JSONObject()
        try {
            val word = json.getString(keyWord1)
             jsonMain = JSONObject(word)
        } catch (e: JSONException) {
            Log.d("CurLog", "Json exception on getFourDaysTemperature method: $e")
        } catch (e: Exception) {
            Log.d("CurLog", "Unknown exception on getFourDaysTemperature method: $e")
        }
        return jsonMain.getString(keyWord2)
    }

    /**
     * поэтому пришлось создать второй метод для обработки
     */
    private fun getFourDaysWeather(json: JSONObject): String {
        val word = json.getJSONArray("weather")
        Log.d("CurLog", "weather array = $word")
        val jsonMain = word.getJSONObject(0)
        return jsonMain.getString("main")
    }

    /**
     * не знал как сделать лучше, поэтому создал отдельную хэшмапу с наименованиями погоды и ссылками на картинки
     */
    private fun convertWeatherToPicks() {
        val weatherToPicks = mapOf(
            "Thunderstorm" to R.drawable.thunder,
            "Drizzle" to R.drawable.rain,
            "Rain" to R.drawable.rain,
            "Snow" to R.drawable.snow,
            "Smoke" to R.drawable.fog,
            "Haze" to R.drawable.fog,
            "Dust" to R.drawable.fog,
            "Fog" to R.drawable.fog,
            "Sand" to R.drawable.fog,
            "Mist" to R.drawable.fog,
            "Dust" to R.drawable.fog,
            "Ash" to R.drawable.fog,
            "Squall" to R.drawable.fog,
            "Tornado" to R.drawable.fog,
            "Clear" to R.drawable.sun,
            "Clouds" to R.drawable.cloud
        )
        try {
            imageAd = weatherAd.map { key-> weatherToPicks.getValue(key) }  //конвертим наименования погоды которые получили по апи в ссылки на картинки
            Log.d("CurLog", "imageAd = $imageAd")
            dayAd = arrayListOf("3H", "24H", "48H", "72H")
            Log.d("CurLog", "dayAd = $dayAd")

            val weatherType: ImageView = findViewById(R.id.weather_type)
            weatherType.setImageResource(imageAd[0])  //устанавливаем картинку которая сверху

            initFourDaysForecastRecyclerview()
        } catch (e: Exception) {
            Log.d("CurLog", "Unknown exception on convertWeatherToPicks method: $e")
        }  //и инициализируем ресайклер

    }

    private fun initFourDaysForecastRecyclerview() {
        try {
            val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            val recycler: RecyclerView = findViewById(R.id.week_weather)
            recycler.layoutManager = layoutManager
            val adapter: NextDaysWeatherRecyclerAdapter = NextDaysWeatherRecyclerAdapter(dayAd, imageAd,temperatureAd, this)
            recycler.adapter = adapter
        } catch (e: Exception) {
            Log.d("CurLog", "Unknown exception on initFourDaysForecastRecyclerview method: $e")
        }
    }

    companion object {
        const val key = "75642222f41cf9a3c0f89b8636eca48f"  //ключ апи
    }

}