package com.parallax.yandexweather

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.parallax.yandexweather.api.CurrentAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class SelectCityActivity : AppCompatActivity() {

    private val cities = ArrayList<CurrentAPI>()

    private val place = ArrayList<String>()
    private val temp = ArrayList<String>()
    private val weather = ArrayList<String>()

    private lateinit var refresh: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_city)

        refresh = findViewById(R.id.refreshRecycler)
        createAndInitialise()
        getDataByAPI()

        bindData()
    }

    private fun createAndInitialise() {
        cities.add(CurrentAPI("Moscow", this))
        cities.add(CurrentAPI("Saint Petersburg", this))
        cities.add(CurrentAPI("Voronezh", this))
        cities.add(CurrentAPI("Nizhny Novgorod", this))
        cities.add(CurrentAPI("kazan", this))
        cities.add(CurrentAPI("London", this))
        cities.add(CurrentAPI("Paris", this))
        cities.add(CurrentAPI("Berlin", this))
        cities.add(CurrentAPI("Beijing", this))
    }

    private fun initCityRecyclerview() {
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        val recyclerview: RecyclerView = findViewById(R.id.selectCityRecycler)
        recyclerview.layoutManager = layoutManager
        val adapter = SelectCityRecyclerview(place, weather, temp, this)
        recyclerview.adapter = adapter
    }

    private fun getDataByAPI() {
        CoroutineScope(Dispatchers.IO).launch {
            for(i in cities) {
                i.getDataByAPI(true)
            }
        }
    }
    private fun getArraysOfData() {
        for(i in cities) {
            place.add(i.getCurrentParams().getValue("location"))
            weather.add(i.getCurrentParams().getValue("text"))
            temp.add(i.getCurrentParams().getValue("temp"))
        }
    }

    private fun bindData() {
        refresh.setOnClickListener {
            getArraysOfData()
            if (place.isNotEmpty() && temp.isNotEmpty() && weather.isNotEmpty()) {
                initCityRecyclerview()
            }
        }
    }
}