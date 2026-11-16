package com.example.practical

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.practical.data.db.PlanEntity
import com.example.practical.databinding.ActivityMainBinding
import com.example.practical.ui.adapter.PlanAdapter
import com.example.practical.ui.viewmodel.MainViewModel
import com.example.practical.util.DateTimeUtils
import com.example.practical.util.LocationHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var locationHelper: LocationHelper
    private lateinit var planAdapter: PlanAdapter

    private val requestLocationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
            if (fineGranted || coarseGranted) {
                fetchLocation()
            } else {
                Toast.makeText(this, "Location permission denied. Using fallback location.", Toast.LENGTH_SHORT).show()
                viewModel.setLocation(null)
            }
        }

    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (idToken != null) {
                    viewModel.authManager.handleGoogleAuth(idToken) { success, error ->
                        if (success) {
                            viewModel.updateCurrentUser()
                            Toast.makeText(this, "Signed in successfully!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Authentication failed: $error", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    // Signed in via Google client directly
                    viewModel.updateCurrentUser()
                    Toast.makeText(this, "Signed in as ${account?.displayName ?: account?.email}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: ApiException) {
                Toast.makeText(this, "Google Sign-In cancelled/failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        locationHelper = LocationHelper(this)

        setupRecyclerView()
        setupListeners()
        observeViewModel()

        checkLocationPermissionAndFetch()
    }

    private fun setupRecyclerView() {
        planAdapter = PlanAdapter(
            onEditClick = { plan -> showAddEditPlanDialog(plan) },
            onDeleteClick = { plan -> confirmDeletePlan(plan) }
        )
        binding.rvPlans.layoutManager = LinearLayoutManager(this)
        binding.rvPlans.adapter = planAdapter
    }

    private fun setupListeners() {
        binding.btnRefreshLocation.setOnClickListener {
            checkLocationPermissionAndFetch()
        }

        binding.btnPickDate.setOnClickListener {
            showDatePicker()
        }

        binding.btnAuth.setOnClickListener {
            val user = viewModel.currentUser.value
            if (user != null) {
                val client = viewModel.authManager.getGoogleSignInClient()
                viewModel.authManager.signOut(client) {
                    viewModel.updateCurrentUser()
                    Toast.makeText(this, "Signed out", Toast.LENGTH_SHORT).show()
                }
            } else {
                val client = viewModel.authManager.getGoogleSignInClient()
                val signInIntent: Intent = client.signInIntent
                googleSignInLauncher.launch(signInIntent)
            }
        }

        binding.btnAddPlan.setOnClickListener {
            showAddEditPlanDialog(null)
        }
    }

    private fun checkLocationPermissionAndFetch() {
        if (locationHelper.hasLocationPermission()) {
            fetchLocation()
        } else {
            requestLocationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun fetchLocation() {
        lifecycleScope.launch {
            binding.progressIndicator.visibility = View.VISIBLE
            val location = locationHelper.getCurrentLocation()
            viewModel.setLocation(location)
        }
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Photo-shoot Date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val dateStr = sdf.format(Date(selection))
            viewModel.setSelectedDate(dateStr)
        }

        datePicker.show(supportFragmentManager, "DATE_PICKER")
    }

    private fun observeViewModel() {
        viewModel.selectedDate.observe(this) { dateStr ->
            binding.tvSelectedDate.text = dateStr
        }

        viewModel.location.observe(this) { loc ->
            if (loc != null) {
                val latStr = String.format(Locale.getDefault(), "%.4f", loc.latitude)
                val lngStr = String.format(Locale.getDefault(), "%.4f", loc.longitude)
                binding.tvLocationCoords.text = "Lat: $latStr, Lng: $lngStr"
            } else {
                binding.tvLocationCoords.text = "Location unavailable"
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(this) { errorMsg ->
            if (errorMsg != null) {
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
                viewModel.clearErrorMessage()
            }
        }

        viewModel.morningGoldenHour.observe(this) { morningGH ->
            binding.tvMorningGoldenHour.text = morningGH
        }

        viewModel.eveningGoldenHour.observe(this) { eveningGH ->
            binding.tvEveningGoldenHour.text = eveningGH
        }

        viewModel.formattedSunrise.observe(this) { text -> binding.tvSunrise.text = text }
        viewModel.formattedSunset.observe(this) { text -> binding.tvSunset.text = text }
        viewModel.formattedSolarNoon.observe(this) { text -> binding.tvSolarNoon.text = text }
        viewModel.formattedDayLength.observe(this) { text -> binding.tvDayLength.text = text }
        viewModel.formattedCivilTwilightBegin.observe(this) { text -> binding.tvCivilTwilightBegin.text = text }
        viewModel.formattedCivilTwilightEnd.observe(this) { text -> binding.tvCivilTwilightEnd.text = text }

        viewModel.countdownEventName.observe(this) { title ->
            binding.tvCountdownTitle.text = title
        }

        viewModel.countdownTimerText.observe(this) { timerText ->
            binding.tvCountdownValue.text = timerText
        }

        viewModel.allPlans.observe(this) { plans ->
            planAdapter.submitList(plans)
            binding.tvEmptyPlans.visibility = if (plans.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.currentUser.observe(this) { user ->
            if (user != null) {
                val name = user.displayName ?: user.email ?: "Signed In User"
                binding.tvUserStatus.text = "Signed in as: $name"
                binding.btnAuth.text = "Sign Out"
            } else {
                binding.tvUserStatus.text = "Not Signed In"
                binding.btnAuth.text = "Sign In"
            }
        }
    }

    private fun showAddEditPlanDialog(planToEdit: PlanEntity?) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_plan, null)
        val etDate = dialogView.findViewById<EditText>(R.id.etPlanDate)
        val etPlaceName = dialogView.findViewById<EditText>(R.id.etPlaceName)
        val etNote = dialogView.findViewById<EditText>(R.id.etNote)

        val isEdit = planToEdit != null
        val dialogTitle = if (isEdit) getString(R.string.dialog_edit_plan) else getString(R.string.dialog_add_plan)

        if (planToEdit != null) {
            etDate.setText(planToEdit.dateStr)
            etPlaceName.setText(planToEdit.placeName)
            etNote.setText(planToEdit.note)
        } else {
            etDate.setText(viewModel.selectedDate.value ?: DateTimeUtils.getTodayApiDateStr())
        }

        AlertDialog.Builder(this)
            .setTitle(dialogTitle)
            .setView(dialogView)
            .setPositiveButton(if (isEdit) getString(R.string.btn_update) else getString(R.string.btn_save)) { _, _ ->
                val dateStr = etDate.text.toString().trim()
                val placeName = etPlaceName.text.toString().trim()
                val note = etNote.text.toString().trim()

                if (planToEdit != null) {
                    val updatedPlan = planToEdit.copy(
                        dateStr = if (dateStr.isNotBlank()) dateStr else planToEdit.dateStr,
                        placeName = placeName,
                        note = note
                    )
                    viewModel.updatePlan(updatedPlan)
                } else {
                    val targetDate = if (dateStr.isNotBlank()) dateStr else DateTimeUtils.getTodayApiDateStr()
                    viewModel.addPlan(targetDate, placeName, note)
                }
            }
            .setNegativeButton(getString(R.string.btn_cancel), null)
            .show()
    }

    private fun confirmDeletePlan(plan: PlanEntity) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete_plan))
            .setMessage("Are you sure you want to delete the plan for '${plan.placeName}'?")
            .setPositiveButton(getString(R.string.btn_delete)) { _, _ ->
                viewModel.deletePlan(plan)
                Toast.makeText(this, "Plan deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(getString(R.string.btn_cancel), null)
            .show()
    }
}
