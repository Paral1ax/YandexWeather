package com.parallax.yandexweather.api

import android.content.Context

abstract class AbstractAPI(internal open var city: String, internal open var context: Context): APICall {
    public var lat: Double = 0.0
    public var lon: Double = 0.0
}