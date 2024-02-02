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
import android.content.Intent
import android.widget.ImageButton
import android.os.StrictMode
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import javax.net.ssl.HttpsURLConnection
import com.google.android.gms.maps.model.LatLng
import java.util.*
import okhttp3.OkHttpClient
import okhttp3.Request as OkHttpRequest
import android.app.Activity
import android.widget.Button










class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private val LOCATION_PERMISSION_REQUEST_CODE = 100

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0


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
    private lateinit var weatherImage: ImageView
    private lateinit var characterImage: ImageView


    private lateinit var mLocationManager: LocationManager
    private lateinit var mLocationListener: LocationListener

    private var location: Location? = null // 위치 정보를 저장할 변수

    private val WEATHERCAST_REQUEST_CODE = 101  // 원하는 코드로 설정


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)




        val Forecast = findViewById<ImageButton>(R.id.ImageButtonForecast)
        Forecast.setOnClickListener {
                val apiKey = "94a991754841d780d8efb651991af19f"
                val intent = Intent(this, WeathercastActivity::class.java)
                // 위치 정보를 WeathercastActivity로 전달
                location?.let {
                    intent.putExtra("latitude", it.latitude)
                    intent.putExtra("longitude", it.longitude)
                }
                intent.putExtra("API_KEY", apiKey)
                startActivity(intent)
            }

            val Mypage = findViewById<ImageButton>(R.id.ImageButtonMyPage)
            Mypage.setOnClickListener {
                val intent = Intent(this@MainActivity, MypageActivity::class.java)
                startActivity(intent)
            }


            // 레이아웃의 View를 참조하는 코드 추가
            temperature = findViewById(R.id.textViewTemperature)
            weatherState = findViewById(R.id.textTemperatureInfo)
            weatherIcon = findViewById(R.id.imageWeatherIcon)
            weatherImage = findViewById(R.id.imageWeatherImage)
            characterImage = findViewById(R.id.imageCharacterImage)

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
                        textViewLocation.text =
                            "위도: ${location.latitude}, 경도: ${location.longitude}"
                        // 날씨 정보 가져오기
                        getWeatherInCurrentLocation(location)

                        // 위치 업데이트 후 주소 업데이트
                        getAddressFromLocation(location.latitude, location.longitude)

                        // 대기질 데이터를 가져와서 textViewAirQuality를 업데이트합니다.
                        getAirQuality(location.latitude, location.longitude)

                        // onLocationChanged 호출
                        onLocationChanged(location)

                        // getTimeZoneId 호출
                        val latLng = LatLng(location.latitude, location.longitude)
                        val timeZoneId = getTimeZoneId(latLng)
                        Log.d(
                            "MainActivity",
                            "getTimeZoneId - Latitude: ${latLng.latitude}, Longitude: ${latLng.longitude}, TimeZoneId: $timeZoneId"
                        )

                    }
                }
            }
        }





    private fun startLocationUpdates() {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            } else {
                // 권한 확인 로그
                Log.d("MainActivity", "Location permission is granted.")
                // 권한이 이미 있는 경우, 위치 업데이트 시작
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if (requestCode == WEATHERCAST_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
                val minTemp = data?.getDoubleExtra("minTemp", 0.0)
                val maxTemp = data?.getDoubleExtra("maxTemp", 0.0)

                Log.d("MainActivity", "메인 액티비티에서 전달받은 최저 온도: $minTemp, 최고 온도: $maxTemp")


            }
        }


        override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
        ) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationUpdates()
                } else {
                    Toast.makeText(this, "위치 권한이 거부되었습니다. 위치를 찾을 수 없습니다.", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }


        private fun getWeatherInCurrentLocation(location: Location) {
            Log.d(
                "MainActivity",
                "Getting weather for location: Latitude ${location.latitude}, Longitude ${location.longitude}"
            )
            // HttpURLConnection을 사용하여 웹 서비스에 GET 요청 보내기
            val thread = Thread {
                try {
                    val url =
                        URL("$WEATHER_URL?lat=${location.latitude}&lon=${location.longitude}&appid=$API_KEY")
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


        private fun getAirQuality(latitude: Double, longitude: Double) {
            val apiKey = "94a991754841d780d8efb651991af19f"
            val airQualityUrl =
                "https://api.openweathermap.org/data/2.5/air_pollution?lat=$latitude&lon=$longitude&appid=$apiKey"

            Log.d("MainActivity", "getAirQuality 시작")


            val thread = Thread {
                try {
                    val url = URL(airQualityUrl)
                    val connection = url.openConnection() as HttpsURLConnection
                    connection.requestMethod = "GET"
                    connection.connect()

                    val responseCode = connection.responseCode
                    Log.d("MainActivity", "HTTP 응답 코드: $responseCode")
                    if (responseCode == HttpsURLConnection.HTTP_OK) {
                        val reader = BufferedReader(InputStreamReader(connection.inputStream))
                        val response = StringBuilder()
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            response.append(line)
                        }
                        Log.d("MainActivity", "미세먼지 API 응답 데이터: $response")
                        val jsonResponse = JSONObject(response.toString())

                        val components =
                            jsonResponse.optJSONObject("list")?.optJSONArray("components")
                        val pm10 = components?.optJSONObject(1)?.optInt("value", -1) ?: -1
                        val airQualityText = when {
                            pm10 <= 20 -> "매우 좋음"
                            pm10 <= 50 -> "좋음"
                            pm10 <= 100 -> "보통"
                            pm10 <= 200 -> "나쁨"
                            else -> "매우 나쁨"
                        }

                        // 미세먼지 정보 출력
                        val airQualityString = "미세먼지: $airQualityText"
                        runOnUiThread {
                            val textViewAirQuality = findViewById<TextView>(R.id.textViewAirQuality)
                            textViewAirQuality.text = airQualityString
                        }

                    } else {
                        Log.e("MainActivity", "네트워크 호출 오류, 응답 코드: $responseCode")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            thread.start()
        }

        // 위치 변경이 감지될 때 호출되는 함수
        private fun onLocationChanged(location: Location) {
            this.location = location // 위치 정보 업데이트

            // 현재 위치의 좌표를 LatLng 객체로 변환
            val latLng = LatLng(location.latitude, location.longitude)

            // Google Time Zone API를 사용하여 시간대 정보 가져오기
            val timeZoneId = getTimeZoneId(latLng)

            // 시간대 정보를 기반으로 현재 시간 계산
            val currentTime = Calendar.getInstance(TimeZone.getTimeZone(timeZoneId))
            val month = currentTime.get(Calendar.MONTH) + 1 // 월은 0부터 시작하므로 +1
            val day = currentTime.get(Calendar.DAY_OF_MONTH)

            // 날짜를 표시하는 TextView 업데이트
            val textViewDate = findViewById<TextView>(R.id.textViewDate)
            textViewDate.text = "오늘은 ${month}월 ${day}일"
            Log.d(
                "MainActivity",
                "onLocationChanged - Latitude: ${location.latitude}, Longitude: ${location.longitude}, TimeZoneId: $timeZoneId"
            )
        }


        // LatLng 좌표를 기반으로 시간대 ID 가져오기
        private fun getTimeZoneId(latLng: LatLng): String {
            val timeZoneUrl =
                "https://maps.googleapis.com/maps/api/timezone/json?location=${latLng.latitude},${latLng.longitude}&timestamp=${System.currentTimeMillis() / 1000}&key=AIzaSyB5YW9mo5wNEh-bSoeh29fslzzX54VFh_0"

            val httpClient = OkHttpClient()
            val request = OkHttpRequest.Builder()
                .url(timeZoneUrl)
                .build()

            try {
                val response = httpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    val jsonResponse = JSONObject(response.body?.string() ?: "")
                    val timeZoneId = jsonResponse.optString("timeZoneId")

                    Log.d(
                        "MainActivity",
                        "getTimeZoneId - Latitude: ${latLng.latitude}, Longitude: ${latLng.longitude}, TimeZoneId: $timeZoneId"
                    )

                    return timeZoneId
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return TimeZone.getDefault().id
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
                val temperatureString = "${weather.tempString}℃"
                temperature.text = temperatureString
                weatherState.text = weather.weatherType
                val iconResourceID = resources.getIdentifier(weather.icon, "drawable", packageName)
                weatherIcon.setImageResource(iconResourceID)
                val imageResourceID =
                    resources.getIdentifier(weather.image, "drawable", packageName)
                weatherImage.setImageResource(imageResourceID)


                // WeatherData에서 가져온 온도를 Double로 변환합니다.
                val temperature = weather.tempString.toDoubleOrNull() ?: 0.0

                // 선택한 스타일 불러오기
                val sharedPref = getSharedPreferences("AppPref", MODE_PRIVATE)
                val selectedStyle = sharedPref.getString("SELECTED_STYLE", "")

                val resourceId = when (selectedStyle) {
                    "오피스룩" -> when {
                        temperature <= 5 -> R.drawable.office_1
                        temperature in 6.0..11.0 -> R.drawable.office_2
                        temperature in 12.0..19.0 -> R.drawable.office_3
                        temperature in 20.0..26.0 -> R.drawable.office_4
                        else -> R.drawable.office_5
                    }

                    "캐주얼" -> when {
                        temperature <= 5 -> R.drawable.casual_1
                        temperature in 6.0..11.0 -> R.drawable.casual_2
                        temperature in 12.0..19.0 -> R.drawable.casual_3
                        temperature in 20.0..26.0 -> R.drawable.casual_4
                        else -> R.drawable.casual_5
                    }

                    "걸리시" -> when {
                        temperature <= 5 -> R.drawable.girlish_1
                        temperature in 6.0..11.0 -> R.drawable.girlish_2
                        temperature in 12.0..19.0 -> R.drawable.girlish_3
                        temperature in 20.0..26.0 -> R.drawable.girlish_4
                        else -> R.drawable.girlish_5
                    }

                    else -> R.drawable.office_1
                }

                // 설정된 이미지 리소스를 characterImage에 적용
                characterImage.setImageResource(resourceId)

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
