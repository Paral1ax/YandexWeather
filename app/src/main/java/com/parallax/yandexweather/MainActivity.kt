package com.parallax.yandexweather

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.loader.content.AsyncTaskLoader
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.parallax.yandexweather.api.CurrentAPI
import com.parallax.yandexweather.api.TodayAPI
import com.parallax.yandexweather.api.WeekAPI
import kotlinx.coroutines.*
import org.w3c.dom.Text
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {

    private lateinit var temp: TextView
    private lateinit var humidity: TextView
    private lateinit var speed: TextView
    private lateinit var perception: TextView
    private lateinit var refresh: Button  //кнопка для обновления
    private lateinit var weatherPhoto: ImageView
    private lateinit var error: TextView
    private lateinit var place: TextView
    private lateinit var locationRequest: LocationRequest
    private lateinit var menu: Button
    lateinit var downloading: TextView
    private var latitude = 0.0
    private var longitude = 0.0
    private val context: Context = this
    private lateinit var currentApi: CurrentAPI
    private lateinit var weekApi: WeekAPI
    private lateinit var todayApi: TodayAPI

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY;
        locationRequest.interval = 5000;
        locationRequest.fastestInterval = 2000;

        currentApi = CurrentAPI("Москва", context, latitude, longitude)
        weekApi = WeekAPI("Москва", context, latitude, longitude)
        todayApi = TodayAPI("Москва", context, latitude, longitude)

        CoroutineScope(Dispatchers.IO).launch {
                getCurrentLocationForecast()
            }

        setContentView(R.layout.activity_main)

        perception = findViewById(R.id.perception)  //ищем вьюшки
        speed = findViewById(R.id.wind_speed)
        humidity = findViewById(R.id.humidity)
        temp = findViewById(R.id.degrees)
        refresh = findViewById(R.id.refresh)
        weatherPhoto = findViewById(R.id.weather_type)
        error = findViewById(R.id.ErrorOcured)
        place = findViewById(R.id.city_country)
        downloading = findViewById(R.id.downloadingText)
        menu = findViewById(R.id.options)

        startProgram()

        onMenuClick()

    }

    /**
     * Так как я еще не опытный
     * не знал как синхронизировать тот факт, что
     * когда мы получаем наши координаты, нам после этого надо
     * создавать инстанц, или хотя бы настраивать поля с координатами и вызывать метод для получения данных по координатам
     * но callback с широтой и долготой приходит позднее вызова метода
     * координаты остаются нулевыми
     * поэтому сделал такую проверку
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun startProgram() {
        downloading.visibility = TextView.INVISIBLE
        refresh.setOnClickListener {
            if (latitude != 0.0 && longitude != 0.0) {  //если широта и долгота вернулись в колбеке
                currentApi.lat = latitude
                currentApi.lon = longitude

                weekApi.lat = latitude
                weekApi.lon = longitude

                todayApi.lat = latitude
                todayApi.lon = longitude

                if (todayApi.getTime().isEmpty() && weekApi.getWeekWeather().isEmpty() && currentApi.getCurrentParams().isEmpty()) {  //если списки пустые
                    downloading.text = "Загрузка, пожалуйста подождите"  //и мы раньше уже не обращались к апи
                    downloading.visibility = TextView.VISIBLE

                    CoroutineScope(Dispatchers.IO).launch {
                        currentApi.getDataByAPI(false)
                        weekApi.getDataByAPI(false)
                        todayApi.getDataByAPI(false)
                    }
                    Thread.sleep(1000)
                    downloading.text = "Готово, обновите страницу"

                } else {  //иначе просто биндим во вью
                    if (currentApi.getCurrentParams().isNotEmpty() && weekApi.getWeekWeather().isNotEmpty()) {
                        bindingDataInViews(currentApi.getCurrentParams()) //устанавливаем данные во вьюшки для отображения погоды сегодня
                        initTodayForecastRecyclerview(todayApi.getTime(), todayApi.getIcon(), todayApi.getTemp())
                        initFourDaysForecastRecyclerview(weekApi.getWeekTemperature(), weekApi.getWeekTime(), weekApi.getWeekWeather())
                    }
                    downloading.visibility = TextView.INVISIBLE
                }
            } else {  //иначе говорим что не все готово (тут ждем колбек с координатами)
                downloading.text = "Идет предварительная настройка"
                downloading.visibility = TextView.VISIBLE
            }
        }
    }

    /**
     * возвращаться назад через кнопку назад андройда
     */
    private fun onMenuClick() {
        menu.setOnClickListener {
            val intent = Intent(this, SelectCityActivity::class.java)

            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        try {

        } catch (e: Exception) {
            Log.d("CurLog", "On Resume method $e")
        }
    }

    /**
     * Устанавливаем значения в сегодняшние поля
     */
    private fun bindingDataInViews(todayParams: HashMap<String, String>) {
        place.text = todayParams.getValue("location")
        temp.text = todayParams.getValue("temp")
        humidity.text = todayParams.getValue("humidity")
        speed.text = todayParams.getValue("windSpeed")
        perception.text = todayParams.getValue("feelsLike")
        Glide.with(this).load(todayParams.getValue("icon")).into(weatherPhoto)
    }

    /**
     * инициализируем ресайклер прогноза погоды на несколько дней вперед
     */
    private fun initFourDaysForecastRecyclerview(temperature: ArrayList<String>, time: ArrayList<String>, image: ArrayList<String>) {
        try {
            val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            val recycler: RecyclerView = findViewById(R.id.week_weather)
            recycler.layoutManager = layoutManager
            val adapter: NextDaysWeatherRecyclerAdapter = NextDaysWeatherRecyclerAdapter(time, image, temperature, this)
            recycler.adapter = adapter
        } catch (e: Exception) {
            Log.d("CurLog", "Unknown exception on initFourDaysForecastRecyclerview method: $e")
        }
    }

    /**
     * инициализируем ресайклер прогноза погоды на сегодня
     */
    private fun initTodayForecastRecyclerview(time: ArrayList<String>, weather: ArrayList<String>, temp: ArrayList<String>) {
        try {
            val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            val recyclerview: RecyclerView = findViewById(R.id.today_weather)
            recyclerview.layoutManager = layoutManager
            val adapter = TodayWeatherAdapter(time, weather, temp, this)
            recyclerview.adapter = adapter
        }
        catch (e: Exception) {
            Log.d("CurLog", "Unknown exception on initTodayForecastRecyclerview method: $e")
        }
    }

    /**
     * получаем координаты
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCurrentLocationForecast() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (isGPSEnable()) {
                    LocationServices.getFusedLocationProviderClient(this@MainActivity)
                        .requestLocationUpdates(locationRequest, object : LocationCallback() {
                            override fun onLocationResult(locationResult: LocationResult) {
                                super.onLocationResult(locationResult)
                                LocationServices.getFusedLocationProviderClient(this@MainActivity)
                                    .removeLocationUpdates(this)
                                if (locationResult.locations.size > 0) {
                                    val index = locationResult.locations.size - 1
                                    latitude = locationResult.locations[index].latitude
                                    longitude = locationResult.locations[index].longitude
                                }
                            }
                        }, Looper.getMainLooper())
                } else {
                    turnOnGPS()
                }
            }
            else requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1);
        }
    }

    /**
     * получаем состояние GPS
     */
    private fun isGPSEnable(): Boolean {
        var locationManager: LocationManager? = null
        var isEnabled = false

        if (locationManager == null) {
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        }

        isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        return isEnabled

    }

    /**
     * Если выключен - включаем
     */
    private fun turnOnGPS() {
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)

        val result: Task<LocationSettingsResponse> = LocationServices.getSettingsClient(
            applicationContext
        )
            .checkLocationSettings(builder.build())

        result.addOnCompleteListener(OnCompleteListener<LocationSettingsResponse?> { task ->
            try {
                val response = task.getResult(ApiException::class.java)
                Toast.makeText(this@MainActivity, "GPS is already tured on", Toast.LENGTH_SHORT)
                    .show()
            } catch (e: ApiException) {
                when (e.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                        val resolvableApiException = e as ResolvableApiException
                        resolvableApiException.startResolutionForResult(this@MainActivity, 2)
                    } catch (ex: SendIntentException) {
                        ex.printStackTrace()
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {}
                }
            }
        })
    }


}