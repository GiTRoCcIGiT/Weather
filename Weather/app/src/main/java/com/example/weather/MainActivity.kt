package com.example.weather

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class MainActivity : AppCompatActivity() {
    private lateinit var cityTextView: TextView
    private lateinit var temperatureTextView: TextView
    private lateinit var editTextCity: EditText
    private lateinit var textViewCity: TextView
    private lateinit var textViewTemperature: TextView
    private lateinit var buttonSearch: Button
    private lateinit var weatherAPI: WeatherAPI // Объявляем переменную weatherAPI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Инициализируем weatherAPI с помощью Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("https://danepubliczne.imgw.pl/api/data/synop/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        weatherAPI = retrofit.create(WeatherAPI::class.java) // Инициализация weatherAPI

        editTextCity = findViewById<EditText>(R.id.editTextCity)
        buttonSearch = findViewById<Button>(R.id.buttonSearch)
        textViewCity = findViewById<TextView>(R.id.textViewCity)
        textViewTemperature = findViewById<TextView>(R.id.textViewTemperature)


        buttonSearch.setOnClickListener {
            val cityName = editTextCity.text.toString()
            if (cityName.isNotEmpty()) {
                fetchWeather(cityName) // Just call the function with cityName
            } else {
                Toast.makeText(this, "Please enter a city name", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchWeather(cityName: String) {
        val call = weatherAPI.getWeatherByCity(cityName)
        call.enqueue(object : Callback<List<WeatherData>> {
            override fun onResponse(call: Call<List<WeatherData>>, response: Response<List<WeatherData>>) {
                if (response.isSuccessful) {
                    val weatherDataList = response.body()
                    Log.d("WeatherData", "Response: $weatherDataList")
                    if (!weatherDataList.isNullOrEmpty()) {
                        // Filter the weather data list to find the data for the searched city
                        val weatherData = weatherDataList.find { it.stacja == cityName }
                        if (weatherData != null) {
                            // Update TextViews with weather data for the searched city
                            textViewCity.text = weatherData.stacja
                            textViewTemperature.text = weatherData.temperatura
                        } else {
                            Toast.makeText(this@MainActivity, "No weather data available for $cityName", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@MainActivity, "No weather data available for $cityName", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Error handling for weather data retrieval
                    Toast.makeText(this@MainActivity, "Failed to get weather data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<WeatherData>>, t: Throwable) {
                // Error handling for request execution failure
                Toast.makeText(this@MainActivity, "Failed to fetch weather data: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }



}

interface WeatherAPI {
    @GET("station")
    fun getWeatherByCity(@Query("stacja") city: String): Call<List<WeatherData>>
}

data class WeatherData(val stacja: String, val temperatura: String)