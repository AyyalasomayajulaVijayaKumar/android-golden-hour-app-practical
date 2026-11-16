package com.example.practical.data.api

import com.google.gson.annotations.SerializedName

data class SunriseSunsetApiResponse(
    @SerializedName("results") val results: SunriseSunsetResults?,
    @SerializedName("status") val status: String?
)

data class SunriseSunsetResults(
    @SerializedName("sunrise") val sunrise: String?,
    @SerializedName("sunset") val sunset: String?,
    @SerializedName("solar_noon") val solarNoon: String?,
    @SerializedName("day_length") val dayLength: String?,
    @SerializedName("civil_twilight_begin") val civilTwilightBegin: String?,
    @SerializedName("civil_twilight_end") val civilTwilightEnd: String?,
    @SerializedName("nautical_twilight_begin") val nauticalTwilightBegin: String?,
    @SerializedName("nautical_twilight_end") val nauticalTwilightEnd: String?,
    @SerializedName("astronomical_twilight_begin") val astronomicalTwilightBegin: String?,
    @SerializedName("astronomical_twilight_end") val astronomicalTwilightEnd: String?
)
