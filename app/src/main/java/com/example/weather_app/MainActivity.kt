package com.example.weather_app

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.weather_app.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import android.widget.SearchView

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        fetchWeatherData("delhi")
        setupSearchCity()
    }

    private fun setupSearchCity() {
        val searchView = binding.searchView // ensure your XML id is searchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    fetchWeatherData(query.trim())
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }

    private fun fetchWeatherData(cityName: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiInterface::class.java)

        val response = retrofit.getWeatherData(
            city = cityName,
            appid = "6dbfc02be5a7ff5fadd64da206c1d8ff",
            units = "metric"
        )

        response.enqueue(object : Callback<WeatherApp> {
            override fun onResponse(call: Call<WeatherApp>, res: Response<WeatherApp>) {
                if (res.isSuccessful && res.body() != null) {
                    val weatherData = res.body()!!

                    val temperature = weatherData.main.temp
                    val humidity = weatherData.main.humidity
                    val windSpeed = weatherData.wind.speed
                    val sunrise = formatTime(weatherData.sys.sunrise.toLong())
                    val sunset = formatTime(weatherData.sys.sunset.toLong())
                    val seaLevel = weatherData.main.pressure
                    val condition = weatherData.weather.firstOrNull()?.main ?: "Unknown"
                    val maxTemp = weatherData.main.temp_max
                    val minTemp = weatherData.main.temp_min

                    Log.d("WeatherData", "Temperature: $temperature°C")

                    binding.temp.text = "$temperature °C"
                    binding.weather.text = condition
                    binding.maxTemp.text = "Max temp: $maxTemp °C"
                    binding.minTemp.text = "Min temp: $minTemp °C"
                    binding.humidity.text = "$humidity %"
                    binding.windSpeed.text = "$windSpeed m/s"
                    binding.sunrise.text = sunrise
                    binding.sunset.text = sunset
                    binding.sea.text = "$seaLevel hPa"
                    binding.condition.text = condition
                    binding.day.text = dayName(System.currentTimeMillis())
                    binding.date.text = date()
                    binding.cityName.text = cityName

                    changeImageAccordingToWeather(condition)
                } else {
                    Log.e("WeatherData", "Failed: ${res.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<WeatherApp>, t: Throwable) {
                Log.e("WeatherData", "Error: ${t.message}")
            }
        })
    }

    private fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp*1000))
    }

    private fun date(): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun changeImageAccordingToWeather(conditions: String) {
        when (conditions) {
            "Clouds", "Partly Clouds", "Overcast", "Mist", "Foggy" -> {
                binding.root.setBackgroundResource(R.drawable.cloud_background)
                binding.lottieAnimationView.setAnimation(R.raw.cloud)
            }
            "Clear Sky", "Sunny", "Clear" -> {
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }
            "Light Rain", "Drizzle", "Moderate Rain", "Showers", "Heavy Rain" -> {
                binding.root.setBackgroundResource(R.drawable.rain_background)
                binding.lottieAnimationView.setAnimation(R.raw.rain)
            }
            "Light Snow", "Moderate Snow", "Heavy Snow", "Blizzard" -> {
                binding.root.setBackgroundResource(R.drawable.snow_background)
                binding.lottieAnimationView.setAnimation(R.raw.snow)
            }
        }
        binding.lottieAnimationView.playAnimation()
    }

    private fun dayName(timestamp: Long): String {
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
