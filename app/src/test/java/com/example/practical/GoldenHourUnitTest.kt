package com.example.practical

import com.example.practical.data.api.SunriseSunsetApiResponse
import com.example.practical.util.DateTimeUtils
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Calendar

class GoldenHourUnitTest {

    @Test
    fun testParseUtcIsoString_validInput() {
        val utcIso = "2025-05-18T06:30:00+00:00"
        val date = DateTimeUtils.parseUtcIsoString(utcIso)
        assertNotNull(date)

        val cal = Calendar.getInstance().apply { time = date!! }
        // Verify in UTC calendar that hour is 6 and minute is 30
        cal.timeZone = java.util.TimeZone.getTimeZone("UTC")
        assertEquals(6, cal.get(Calendar.HOUR_OF_DAY))
        assertEquals(30, cal.get(Calendar.MINUTE))
    }

    @Test
    fun testParseUtcIsoString_invalidInput() {
        val date = DateTimeUtils.parseUtcIsoString("invalid_date_string")
        assertNull(date)
    }

    @Test
    fun testMorningGoldenHourRange() {
        val utcIso = "2025-05-18T06:00:00+00:00"
        val sunriseDate = DateTimeUtils.parseUtcIsoString(utcIso)
        assertNotNull(sunriseDate)

        val range = DateTimeUtils.getMorningGoldenHourRange(sunriseDate)
        assertNotNull(range)

        val diffMs = range!!.second.time - range.first.time
        // Must be exactly 1 hour (3600000 ms)
        assertEquals(3600000L, diffMs)
    }

    @Test
    fun testEveningGoldenHourRange() {
        val utcIso = "2025-05-18T18:00:00+00:00"
        val sunsetDate = DateTimeUtils.parseUtcIsoString(utcIso)
        assertNotNull(sunsetDate)

        val range = DateTimeUtils.getEveningGoldenHourRange(sunsetDate)
        assertNotNull(range)

        val diffMs = range!!.second.time - range.first.time
        // Must be exactly 1 hour (3600000 ms)
        assertEquals(3600000L, diffMs)
    }

    @Test
    fun testFormatDayLength_seconds() {
        // 45210 seconds = 12h 33m 30s
        val formatted = DateTimeUtils.formatDayLength("45210")
        assertEquals("12h 33m 30s", formatted)
    }

    @Test
    fun testFormatDayLength_formattedString() {
        val formatted = DateTimeUtils.formatDayLength("12:33:30")
        assertEquals("12h 33m", formatted)
    }

    @Test
    fun testCalculateCountdown() {
        val nowMs = 1000000L
        val targetMs = nowMs + (2 * 3600 * 1000) + (15 * 60 * 1000) + (30 * 1000) // + 2h 15m 30s
        val countdownStr = DateTimeUtils.calculateCountdown(nowMs, targetMs)
        assertEquals("02h 15m 30s", countdownStr)
    }

    @Test
    fun testSunriseSunsetApiResponseParsing() {
        val json = """
            {
                "results": {
                    "sunrise": "2025-05-18T05:32:10+00:00",
                    "sunset": "2025-05-18T19:45:20+00:00",
                    "solar_noon": "2025-05-18T12:38:45+00:00",
                    "day_length": "51190",
                    "civil_twilight_begin": "2025-05-18T05:01:00+00:00",
                    "civil_twilight_end": "2025-05-18T20:16:30+00:00"
                },
                "status": "OK"
            }
        """.trimIndent()

        val gson = Gson()
        val response = gson.fromJson(json, SunriseSunsetApiResponse::class.java)
        assertEquals("OK", response.status)
        assertNotNull(response.results)
        assertEquals("2025-05-18T05:32:10+00:00", response.results?.sunrise)
        assertEquals("2025-05-18T19:45:20+00:00", response.results?.sunset)
        assertEquals("51190", response.results?.dayLength)
    }
}
