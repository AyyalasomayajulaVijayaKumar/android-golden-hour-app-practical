package com.example.practical.ui.viewmodel

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.practical.data.api.SunriseSunsetApiService
import com.example.practical.data.api.SunriseSunsetResults
import com.example.practical.data.db.AppDatabase
import com.example.practical.data.db.PlanEntity
import com.example.practical.data.repository.PlanRepository
import com.example.practical.ui.auth.AuthManager
import com.example.practical.util.DateTimeUtils
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = SunriseSunsetApiService()
    private val database = AppDatabase.getInstance(application)
    private val planRepository = PlanRepository(database.planDao())
    val authManager = AuthManager(application)

    private val _selectedDate = MutableLiveData<String>(DateTimeUtils.getTodayApiDateStr())
    val selectedDate: LiveData<String> = _selectedDate

    private val _location = MutableLiveData<Location?>()
    val location: LiveData<Location?> = _location

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _solarResults = MutableLiveData<SunriseSunsetResults?>()
    val solarResults: LiveData<SunriseSunsetResults?> = _solarResults

    private val _morningGoldenHour = MutableLiveData<String>("N/A")
    val morningGoldenHour: LiveData<String> = _morningGoldenHour

    private val _eveningGoldenHour = MutableLiveData<String>("N/A")
    val eveningGoldenHour: LiveData<String> = _eveningGoldenHour

    private val _formattedSunrise = MutableLiveData<String>("N/A")
    val formattedSunrise: LiveData<String> = _formattedSunrise

    private val _formattedSunset = MutableLiveData<String>("N/A")
    val formattedSunset: LiveData<String> = _formattedSunset

    private val _formattedSolarNoon = MutableLiveData<String>("N/A")
    val formattedSolarNoon: LiveData<String> = _formattedSolarNoon

    private val _formattedDayLength = MutableLiveData<String>("N/A")
    val formattedDayLength: LiveData<String> = _formattedDayLength

    private val _formattedCivilTwilightBegin = MutableLiveData<String>("N/A")
    val formattedCivilTwilightBegin: LiveData<String> = _formattedCivilTwilightBegin

    private val _formattedCivilTwilightEnd = MutableLiveData<String>("N/A")
    val formattedCivilTwilightEnd: LiveData<String> = _formattedCivilTwilightEnd

    private val _countdownEventName = MutableLiveData<String>("Next Solar Event")
    val countdownEventName: LiveData<String> = _countdownEventName

    private val _countdownTimerText = MutableLiveData<String>("--h --m --s")
    val countdownTimerText: LiveData<String> = _countdownTimerText

    private val _currentUser = MutableLiveData<FirebaseUser?>(authManager.getCurrentUser())
    val currentUser: LiveData<FirebaseUser?> = _currentUser

    val allPlans: LiveData<List<PlanEntity>> = planRepository.allPlans.asLiveData()

    private var countdownJob: Job? = null
    private var nextTargetDate: Date? = null

    // Default coordinates (e.g. San Francisco) if location unavailable
    private var currentLat: Double = 37.7749
    private var currentLng: Double = -122.4194
    private var locationReceived = false

    fun setLocation(loc: Location?) {
        if (loc != null) {
            currentLat = loc.latitude
            currentLng = loc.longitude
            _location.value = loc
            locationReceived = true
        } else if (!locationReceived) {
            // Standard fallback location
            val fallbackLoc = Location("default").apply {
                latitude = currentLat
                longitude = currentLng
            }
            _location.value = fallbackLoc
        }
        loadSolarData()
    }

    fun setSelectedDate(dateStr: String) {
        _selectedDate.value = dateStr
        loadSolarData()
    }

    fun updateCurrentUser() {
        _currentUser.value = authManager.getCurrentUser()
    }

    fun loadSolarData() {
        val dateStr = _selectedDate.value ?: DateTimeUtils.getTodayApiDateStr()
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            val result = apiService.fetchSolarData(currentLat, currentLng, dateStr)
            _isLoading.value = false

            result.onSuccess { results ->
                _solarResults.value = results
                processSolarResults(results)
            }.onFailure { exception ->
                _errorMessage.value = "Failed to load solar data: ${exception.localizedMessage}"
            }
        }
    }

    private fun processSolarResults(results: SunriseSunsetResults) {
        val sunriseDate = DateTimeUtils.parseUtcIsoString(results.sunrise)
        val sunsetDate = DateTimeUtils.parseUtcIsoString(results.sunset)
        val solarNoonDate = DateTimeUtils.parseUtcIsoString(results.solarNoon)
        val twilightBeginDate = DateTimeUtils.parseUtcIsoString(results.civilTwilightBegin)
        val twilightEndDate = DateTimeUtils.parseUtcIsoString(results.civilTwilightEnd)

        _formattedSunrise.value = DateTimeUtils.formatToLocalTimeString(sunriseDate)
        _formattedSunset.value = DateTimeUtils.formatToLocalTimeString(sunsetDate)
        _formattedSolarNoon.value = DateTimeUtils.formatToLocalTimeString(solarNoonDate)
        _formattedDayLength.value = DateTimeUtils.formatDayLength(results.dayLength)
        _formattedCivilTwilightBegin.value = DateTimeUtils.formatToLocalTimeString(twilightBeginDate)
        _formattedCivilTwilightEnd.value = DateTimeUtils.formatToLocalTimeString(twilightEndDate)

        // Golden Hours calculation
        val morningRange = DateTimeUtils.getMorningGoldenHourRange(sunriseDate)
        if (morningRange != null) {
            _morningGoldenHour.value = "${DateTimeUtils.formatToLocalTimeString(morningRange.first)} - ${DateTimeUtils.formatToLocalTimeString(morningRange.second)}"
        } else {
            _morningGoldenHour.value = "N/A"
        }

        val eveningRange = DateTimeUtils.getEveningGoldenHourRange(sunsetDate)
        if (eveningRange != null) {
            _eveningGoldenHour.value = "${DateTimeUtils.formatToLocalTimeString(eveningRange.first)} - ${DateTimeUtils.formatToLocalTimeString(eveningRange.second)}"
        } else {
            _eveningGoldenHour.value = "N/A"
        }

        // Setup Countdown
        setupCountdown(sunriseDate, sunsetDate)
    }

    private fun setupCountdown(sunriseDate: Date?, sunsetDate: Date?) {
        countdownJob?.cancel()
        if (sunriseDate == null || sunsetDate == null) {
            _countdownEventName.value = "Solar Event"
            _countdownTimerText.value = "Unavailable"
            return
        }

        val now = Date()
        if (now.before(sunriseDate)) {
            _countdownEventName.value = "Countdown to Sunrise"
            nextTargetDate = sunriseDate
        } else if (now.after(sunriseDate) && now.before(sunsetDate)) {
            _countdownEventName.value = "Countdown to Sunset"
            nextTargetDate = sunsetDate
        } else {
            // After sunset: next event is tomorrow's sunrise (add 24h to today's sunrise)
            _countdownEventName.value = "Countdown to Next Sunrise"
            nextTargetDate = Date(sunriseDate.time + 24 * 60 * 60 * 1000)
        }

        startCountdownTimer()
    }

    private fun startCountdownTimer() {
        countdownJob = viewModelScope.launch {
            while (true) {
                val target = nextTargetDate
                if (target == null) {
                    _countdownTimerText.value = "N/A"
                    break
                }
                val nowMs = System.currentTimeMillis()
                val targetMs = target.time
                if (targetMs <= nowMs) {
                    _countdownTimerText.value = "Event started!"
                    // Reload data for upcoming events
                    loadSolarData()
                    break
                } else {
                    _countdownTimerText.value = DateTimeUtils.calculateCountdown(nowMs, targetMs)
                }
                delay(1000)
            }
        }
    }

    fun addPlan(dateStr: String, placeName: String, note: String) {
        if (placeName.isBlank()) {
            _errorMessage.value = "Place name cannot be empty"
            return
        }
        viewModelScope.launch {
            val plan = PlanEntity(dateStr = dateStr, placeName = placeName, note = note)
            planRepository.insertPlan(plan)
        }
    }

    fun updatePlan(plan: PlanEntity) {
        if (plan.placeName.isBlank()) {
            _errorMessage.value = "Place name cannot be empty"
            return
        }
        viewModelScope.launch {
            planRepository.updatePlan(plan)
        }
    }

    fun deletePlan(plan: PlanEntity) {
        viewModelScope.launch {
            planRepository.deletePlan(plan)
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}
