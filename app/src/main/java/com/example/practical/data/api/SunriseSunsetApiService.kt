package com.example.practical.data.api

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

class SunriseSunsetApiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    suspend fun fetchSolarData(latitude: Double, longitude: Double, dateStr: String): Result<SunriseSunsetResults> {
        return withContext(Dispatchers.IO) {
            try {
                val url = "https://api.sunrise-sunset.org/json?lat=$latitude&lng=$longitude&date=$dateStr&formatted=0"
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "GoldenHourPlanner/1.0")
                    .build()

                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    return@withContext Result.failure(IOException("HTTP Error ${response.code}"))
                }

                val bodyString = response.body?.string()
                    ?: return@withContext Result.failure(IOException("Empty response body"))

                val apiResponse = gson.fromJson(bodyString, SunriseSunsetApiResponse::class.java)
                if (apiResponse.status == "OK" && apiResponse.results != null) {
                    Result.success(apiResponse.results)
                } else {
                    Result.failure(Exception("API returned status: ${apiResponse.status ?: "UNKNOWN"}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
