package com.example.guru2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import android.widget.TextView
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Calendar
import java.util.TimeZone
import android.widget.Toast
import kotlinx.coroutines.*
import javax.net.ssl.HttpsURLConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.*
import kotlinx.coroutines.launch

class WeathercastActivity : AppCompatActivity() {

    private lateinit var apiKey: String


    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weathercast)

        // 이전 액티비티로부터 위치 정보 가져오기
        latitude = intent.getDoubleExtra("latitude", 0.0)
        longitude = intent.getDoubleExtra("longitude", 0.0)

        apiKey = intent.getStringExtra("API_KEY") ?: ""

        // 위치 정보를 사용하여 날씨 정보 가져오기
        getWeatherInfo(latitude, longitude)

        //툴바 적용
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        val ab = supportActionBar!!
        ab.setDisplayShowTitleEnabled(false)
        ab.setDisplayHomeAsUpEnabled(true)

    }


    //툴바 뒤로가기 버튼 동작
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getWeatherInfo(latitude: Double, longitude: Double) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                Log.d("WeathercastActivity", "getWeatherInfo 함수 시작")
                // 날씨 정보 가져오기
                val weatherInfo = getWeatherData(latitude, longitude)
                runOnUiThread {
                    displayWeatherInfo(weatherInfo)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("WeathercastActivity", "getWeatherInfo 함수 오류: $e")
            }
        }
    }

    private fun getWeatherData(latitude: Double, longitude: Double): WeatherInfo {
        val client = OkHttpClient()
        val api = "https://api.openweathermap.org/data/2.5/weather?lat=$latitude&lon=$longitude&appid=$apiKey&lang=kr&units=metric"

        val request = Request.Builder()
            .url(api)
            .build()

        val response = client.newCall(request).execute()
        val jsonData = response.body?.string() ?: ""
        val jsonObject = JSONObject(jsonData)

        val name = jsonObject.getString("name")
        val description = jsonObject.getJSONArray("weather").getJSONObject(0).getString("description")
        val temp = jsonObject.getJSONObject("main").getDouble("temp")
        val feelsLike = jsonObject.getJSONObject("main").getDouble("feels_like")
        val tempMin = jsonObject.getJSONObject("main").getDouble("temp_min")
        val tempMax = jsonObject.getJSONObject("main").getDouble("temp_max")
        val humidity = jsonObject.getJSONObject("main").getInt("humidity")
        val pressure = jsonObject.getJSONObject("main").getInt("pressure")
        val windDeg = jsonObject.getJSONObject("wind").getInt("deg")
        val windSpeed = jsonObject.getJSONObject("wind").getDouble("speed")

        Log.d("WeathercastActivity", "getWeatherData 함수 호출")
        return WeatherInfo(name, description, temp, feelsLike, tempMin, tempMax, humidity, pressure, windDeg, windSpeed)
    }

    private fun displayWeatherInfo(weatherInfo: WeatherInfo) {
        val description = findViewById<TextView>(R.id.textViewDescription)
        val currentTemp = findViewById<TextView>(R.id.textViewCurrentTemp)
        val feelsLikeTemp = findViewById<TextView>(R.id.textViewFeelsLike)
        val minTemp = findViewById<TextView>(R.id.textViewMinTemp)
        val maxTemp = findViewById<TextView>(R.id.textViewMaxTemp)
        val humidity = findViewById<TextView>(R.id.textViewHumidity)
        val pressure = findViewById<TextView>(R.id.textViewPressure)
        val windDeg = findViewById<TextView>(R.id.textViewWindDeg)
        val windSpeed = findViewById<TextView>(R.id.textViewWindSpeed)

        description.text = "날씨는 ${weatherInfo.description} 입니다."
        currentTemp.text = "현재 온도는 ${weatherInfo.temp} ℃"
        feelsLikeTemp.text = "체감 온도는 ${weatherInfo.feelsLike} ℃"
        minTemp.text = "최저 기온은 ${weatherInfo.tempMin} ℃"
        maxTemp.text = "최고 기온은 ${weatherInfo.tempMax} ℃"
        humidity.text = "습도는 ${weatherInfo.humidity}%"
        pressure.text = "기압은 ${weatherInfo.pressure} hPa"
        windDeg.text = "풍향은 ${weatherInfo.windDeg}°"
        windSpeed.text = "풍속은 ${weatherInfo.windSpeed} m/s"

        Log.d("WeathercastActivity", "displayWeatherInfo 함수 호출")
    }

    data class WeatherInfo(
        val name: String,
        val description: String,
        val temp: Double,
        val feelsLike: Double,
        val tempMin: Double,
        val tempMax: Double,
        val humidity: Int,
        val pressure: Int,
        val windDeg: Int,
        val windSpeed: Double
    )
}

