package com.example.guru2

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import android.content.Context
import android.location.LocationListener
import android.location.LocationManager
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.JsonHttpResponseHandler
import cz.msebera.android.httpclient.Header
import cz.msebera.android.httpclient.client.params.ClientPNames
import cz.msebera.android.httpclient.params.HttpConnectionParams
import cz.msebera.android.httpclient.params.HttpParams
import org.json.JSONObject
import cz.msebera.android.httpclient.HttpEntity
import cz.msebera.android.httpclient.entity.StringEntity
import cz.msebera.android.httpclient.protocol.HTTP
import org.json.JSONException
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.loopj.android.http.RequestParams
import android.util.Log
import android.location.Geocoder
import java.io.IOException
import java.util.*



class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private val LOCATION_PERMISSION_REQUEST_CODE = 100

    companion object {
        const val API_KEY: String = "94a991754841d780d8efb651991af19f"
        const val WEATHER_URL: String = "https://api.openweathermap.org/data/2.5/weather"
        const val MIN_TIME: Long = 5000
        const val MIN_DISTANCE: Float = 1000F
        const val WEATHER_REQUEST: Int = 102
    }

    private lateinit var weatherState: TextView
    private lateinit var temperature: TextView
    private lateinit var weatherIcon: ImageView

    private lateinit var mLocationManager: LocationManager
    private lateinit var mLocationListener: LocationListener


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 레이아웃의 View를 참조하는 코드 추가
        temperature = findViewById(R.id.textViewTemperature)
        weatherState = findViewById(R.id.textTemperatureInfo)
        weatherIcon = findViewById(R.id.imageWeatherIcon)

        // 위치 서비스 클라이언트 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val imageViewRefresh = findViewById<ImageView>(R.id.imageViewRefresh)
        imageViewRefresh.setOnClickListener {
            Log.d("MainActivity", "Refresh button clicked.")
            startLocationUpdates()
        }

        // 위치 요청 객체 설정
        locationRequest = LocationRequest.create().apply {
            interval = 10000 // 10초 간격으로 업데이트
            fastestInterval = 5000 // 가장 빠른 업데이트 간격은 5초
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }




        // 위치 콜백 설정
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    // 업데이트된 위치 정보 사용
                    val textViewLocation = findViewById<TextView>(R.id.textViewLocation)
                    textViewLocation.text = "위도: ${location.latitude}, 경도: ${location.longitude}"
                    // 날씨 정보 가져오기
                    getWeatherInCurrentLocation(location)

                    // 위치 업데이트 후 주소 업데이트
                    getAddressFromLocation(location.latitude, location.longitude)
                }
            }
        }
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            // 권한 확인 로그
            Log.d("MainActivity", "Location permission is granted.")
            // 권한이 이미 있는 경우, 위치 업데이트 시작
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()
            } else {
                Toast.makeText(this, "위치 권한이 거부되었습니다. 위치를 찾을 수 없습니다.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getWeatherInCurrentLocation(location: Location) {
        Log.d("MainActivity", "Getting weather for location: Latitude ${location.latitude}, Longitude ${location.longitude}")
        // HttpURLConnection을 사용하여 웹 서비스에 GET 요청 보내기
        val thread = Thread {
            try {
                val url = URL("$WEATHER_URL?lat=${location.latitude}&lon=${location.longitude}&appid=$API_KEY")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connect()

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    // JSON 파싱 및 WeatherData 객체 생성
                    val jsonResponse = JSONObject(response.toString())
                    val weatherData = WeatherData().fromJson(jsonResponse)
                    if (weatherData != null) {
                        runOnUiThread {
                            updateWeather(weatherData)
                        }
                    }
                } else {
                    Log.e("MainActivity", "Error in network call, response code: $responseCode")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        thread.start()
    }

    private fun doNetworking(params: RequestParams) {
        val client = AsyncHttpClient()

        client.get(WEATHER_URL, params, object : JsonHttpResponseHandler() {
            override fun onSuccess(
                statusCode: Int,
                headers: Array<out Header>?,
                response: JSONObject?
            ) {
                val weatherData = WeatherData().fromJson(response)
                if (weatherData != null) {
                    updateWeather(weatherData)
                }
            }
        })
    }

    private fun updateWeather(weather: WeatherData) {
        Log.d("MainActivity", "Updating weather on UI. Temperature: ${weather.tempString}")
        runOnUiThread {
            temperature.text = weather.tempString + " ℃"
            weatherState.text = weather.weatherType
            val resourceID = resources.getIdentifier(weather.icon, "drawable", packageName)
            weatherIcon.setImageResource(resourceID)
        }
    }

    private fun getAddressFromLocation(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(this, Locale.KOREA)
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val addressParts = addresses[0].getAddressLine(0).split(" ")
                val extractedAddress = "${addressParts[2]} ${addressParts[3]}"
                val textViewLocation = findViewById<TextView>(R.id.textViewLocation)
                textViewLocation.text = extractedAddress

                Log.d("MainActivity", "Address: $extractedAddress")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }





    override fun onPause() {
        super.onPause()
        if (this::mLocationManager.isInitialized) {
            mLocationManager.removeUpdates(mLocationListener)
        }
    }

}