package com.example.guru2

import org.json.JSONException
import org.json.JSONObject

class WeatherData {

    lateinit var tempString: String
    lateinit var icon: String
    lateinit var image: String
    lateinit var weatherType: String
    private var weatherId: Int = 0
    private var tempInt: Int = 0

    fun fromJson(jsonObject: JSONObject?): WeatherData? {
        try {
            val weatherData = WeatherData()
            weatherData.weatherId = jsonObject?.getJSONArray("weather")?.getJSONObject(0)?.getInt("id")!!
            weatherData.weatherType = jsonObject.getJSONArray("weather").getJSONObject(0).getString("main")
            weatherData.icon = updateWeatherIcon(weatherData.weatherId)
            weatherData.image = updateWeatherImage(weatherData.weatherId)
            val roundedTemp: Int = (jsonObject.getJSONObject("main").getDouble("temp") - 273.15).toInt()
            weatherData.tempString = roundedTemp.toString()
            weatherData.tempInt = roundedTemp
            return weatherData
        } catch (e: JSONException) {
            e.printStackTrace()
            return null
        }
    }

    private fun updateWeatherIcon(condition: Int): String {
        if (condition in 200..299) {
            return "thunderstorm"
        } else if (condition in 300..499) {
            return "lightrain"
        } else if (condition in 500..599) {
            return "rain"
        } else if (condition in 600..700) {
            return "snow"
        } else if (condition in 701..771) {
            return "fog"
        } else if (condition in 772..799) {
            return "overcast"
        } else if (condition == 800) {
            return "clear"
        } else if (condition in 801..804) {
            return "cloudy"
        } else if (condition in 900..902) {
            return "thunderstorm"
        }
        if (condition == 903) {
            return "snow"
        }
        if (condition == 904) {
            return "clear"
        }
        return if (condition in 905..1000) {
            "thunderstorm"
        } else "dunno"
    }

    private fun updateWeatherImage(condition: Int): String {
        if (condition in 200..299) {
            return "illust_thunderstorm"
        } else if (condition in 300..499) {
            return "illust_lightrain"
        } else if (condition in 500..599) {
            return "illust_rain"
        } else if (condition in 600..700) {
            return "illust_snow"
        } else if (condition in 701..771) {
            return "illust_fog"
        } else if (condition in 772..799) {
            return "illust_overcast"
        } else if (condition == 800) {
            return "illust_clear"
        } else if (condition in 801..804) {
            return "illust_cloudy"
        } else if (condition in 900..902) {
            return "illust_thunderstorm"
        }
        if (condition == 903) {
            return "illust_snow"
        }
        if (condition == 904) {
            return "illust_clear"
        }
        return if (condition in 905..1000) {
            "illust_thunderstorm"
        } else "dunno"
    }
}