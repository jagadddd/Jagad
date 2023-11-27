package com.project.kalpataru

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.View
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.project.kalpataru.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private var _binding : ActivityMainBinding? = null
    private val binding get() = _binding!!
    private val apiKey = "2dbf726a758b40e2a4d101106202810"
    private val apiUrl = "https://api.weatherapi.com/v1/forecast.json?key=$apiKey&q=-7.8011945,110.364917&days=8&aqi=yes&alerts"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Glide.with(this)
            .load(R.drawable.bottom_nav)
            .into(binding.imageView)

        // Initialize Volley request queue
        val requestQueue = Volley.newRequestQueue(this)

        // Create a JSON request to fetch data from the API
        val jsonObjectRequest = JsonObjectRequest (
            Request.Method.GET, apiUrl, null,
            { response ->
                // Handle the API response here
                val location = response.getJSONObject("location")
                val current = response.getJSONObject("current")
                val airQuality = current.getJSONObject("air_quality")
                val region = location.getString("region")
                val name = location.getString("name")
                val tempInCelcius = current.getDouble("temp_c")
                val lastUpdate = current.getString("last_updated")
                val humidity = current.getInt("humidity")
                val pressureIn = current.getDouble("pressure_in")
                val windDegree = current.getInt("wind_degree")

                val getPm25 = airQuality.getDouble("pm2_5")
                val getCo = airQuality.getDouble("co")
                val getNo2 = airQuality.getDouble("no2")
                val getO3 = airQuality.getDouble("o3")
                val getSo2 = airQuality.getDouble("so2")
                val getPm10 = airQuality.getDouble("pm10")

                // Log the parsed data
                binding.apply {
                    kabupaten.text = "Di kota $name"
                    suhu.text = "$tempInCelcius °C"
                    latestTemperature.text = "Update terakhir ${formatDate(lastUpdate)}"
                    kelembabanUdara.text = "$humidity %"
                    indexUdara.text = "Index udara $pressureIn"
                    kualitasUdaraInNumber.text = "$windDegree"

                    pm25.text = getPm25.toString()
                    co.text = getCo.toString()
                    no2.text = getNo2.toString()
                    o3.text = getO3.toString()
                    so2.text = getSo2.toString()
                    pm10.text = getPm10.toString()
                    hc.text = humidity.toString()

                    // Find the maximum value
                    val maxVal = maxOf(getPm25, getCo, getNo2, getO3, getSo2, getPm10, humidity.toDouble())

                    // Calculate the scaling factor
                    val scalingFactor = 100.0 / maxVal

                    // Scale the values and set them to percentages
                    val pm25Percent = getPm25 * scalingFactor
                    val coPercent = getCo * scalingFactor
                    val no2Percent = getNo2 * scalingFactor
                    val o3Percent = getO3 * scalingFactor
                    val so2Percent = getSo2 * scalingFactor
                    val pm10Percent = getPm10 * scalingFactor
                    val humidityPercent = humidity.toDouble() * scalingFactor

                    // Define your percentage values in a map
                    val percentageMap = mapOf(
                        "pm25Percentage" to pm25Percent,
                        "coPercentage" to coPercent,
                        "no2Percentage" to no2Percent,
                        "o3Percentage" to o3Percent,
                        "so2Percentage" to so2Percent,
                        "pm10Percentage" to pm10Percent,
                        "humidityPercentage" to humidityPercent
                    )

                    // Define the maximum percentage (100%)
                    val maxPercentage = 100.0

                    // Define the maximum height in dp when the percentage is 100%
                    val maxDpHeight = 150

                    setPercentageHeights(percentageMap, maxPercentage, maxDpHeight)

                    progressBar.visibility = View.GONE
                }
            },
            { error ->
                // Handle error
                Log.e("WeatherData", "Error: ${error.message}")
            }
        )

        // Add the request to the request queue
        requestQueue.add(jsonObjectRequest)


    }

    private fun formatDate(inputDate: String): String {
        // Input date format
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        // Output date format
        val outputFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())

        try {
            // Parse the input date string into a Date object
            val date = inputFormat.parse(inputDate)

            // Format the Date object to the desired output format
            return outputFormat.format(date)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return "" // Return an empty string in case of an error
    }

    private fun setPercentageHeights(percentageMap: Map<String, Double>, maxPercentage: Double, maxDpHeight: Int) {
        for ((viewId, percentage) in percentageMap) {
            // Calculate the height in dp based on the percentage
            val heightDp = (percentage / maxPercentage) * maxDpHeight

            // Convert dp to pixels
            val heightPixels = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                heightDp.toFloat(),
                resources.displayMetrics
            ).toInt()

            // Set the calculated height to the view
            when (viewId) {
                "pm25Percentage" -> {
                    binding.pm25Percentage.layoutParams.height = heightPixels
                    binding.pm25Percentage.requestLayout()
                }
                "pm10Percentage" -> {
                    binding.pm10Percentage.layoutParams.height = heightPixels
                    binding.pm10Percentage.requestLayout()
                }
                "coPercentage" -> {
                    binding.coPercentage.layoutParams.height = heightPixels
                    binding.coPercentage.requestLayout()
                }
                "hcPercentage" -> {
                    binding.hcPercentage.layoutParams.height = heightPixels
                    binding.hcPercentage.requestLayout()
                }
                "no2Percentage" -> {
                    binding.no2Percentage.layoutParams.height = heightPixels
                    binding.no2Percentage.requestLayout()
                }
                "o3Percentage" -> {
                    binding.o3Percentage.layoutParams.height = heightPixels
                    binding.o3Percentage.requestLayout()
                }
                "so2Percentage" -> {
                    binding.so2Percentage.layoutParams.height = heightPixels
                    binding.so2Percentage.requestLayout()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}