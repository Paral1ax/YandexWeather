package com.parallax.yandexweather.api

interface APICall {
    fun getDataByAPI(byCity: Boolean)
}