package com.autoaccept.app

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.autoaccept.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    companion object {
        private const val REQUEST_NOTIFICATION_PERMISSION = 1
        private const val REQUEST_MEDIA_PROJECTION = 2
        private const val PERMISSION_STEP_NONE = 0
        private const val PERMISSION_STEP_NOTIFICATION = 1
        private const val PERMISSION_STEP_ACCESSIBILITY = 2
        private const val PERMISSION_STEP_SCREEN_CAPTURE = 3
        private const val PERMISSION_STEP_COMPLETE = 4
    }

    private var currentPermissionStep = PERMISSION_STEP_NONE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadPreferences()
        setupListeners()
        checkPermissionStatus()
    }

    override fun onResume() {
        super.onResume()
        binding.switchEnable.isChecked = Prefs.isEnabled(this)
        if (currentPermissionStep > PERMISSION_STEP_NONE) {
            continuePermissionFlow()
        }
    }

    private fun loadPreferences() {
        binding.editStartX.setText(Prefs.getStartX(this).toString())
        binding.editStartY.setText(Prefs.getStartY(this).toString())
        binding.editEndX.setText(Prefs.getEndX(this).toString())
        binding.editEndY.setText(Prefs.getEndY(this).toString())
        binding.editDuration.setText(Prefs.getDuration(this).toString())
        binding.editCloseX.setText(Prefs.getCloseX(this).toString())
        binding.editCloseY.setText(Prefs.getCloseY(this).toString())
        binding.editThreshold.setText(Prefs.getThreshold(this).toString())
        binding.switchEnable.isChecked = Prefs.isEnabled(this)
    }

    private fun setupListeners() {
        binding.switchEnable.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                startPermissionFlow()
            } else {
                savePreferences()
                Prefs.setEnabled(this, isChecked)
                OrderAccessibilityService.instance?.refreshLoop()
            }
        }

        binding.editStartX.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) savePreferences()
        }
        binding.editStartY.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) savePreferences()
        }
        binding.editEndX.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) savePreferences()
        }
        binding.editEndY.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) savePreferences()
        }
        binding.editDuration.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) savePreferences()
        }
        binding.editCloseX.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) savePreferences()
        }
        binding.editCloseY.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) savePreferences()
        }
        binding.editThreshold.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) savePreferences()
        }

        binding.btnTestSwipe.setOnClickListener {
            savePreferences()
            val service = OrderAccessibilityService.instance
            if (service != null) {
                service.performSwipe(
                    Prefs.getStartX(this),
                    Prefs.getStartY(this),
                    Prefs.getEndX(this),
                    Prefs.getEndY(this),
                    Prefs.getDuration(this)
                )
                Toast.makeText(this, "Swipe executed", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Service not running", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnTestClose.setOnClickListener {
            savePreferences()
            val service = OrderAccessibilityService.instance
            if (service != null) {
                service.performTap(
                    Prefs.getCloseX(this),
                    Prefs.getCloseY(this)
                )
                Toast.makeText(this, "Tap executed", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Service not running", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun savePreferences() {
        binding.editStartX.text.toString().toIntOrNull()?.let { Prefs.setStartX(this, it) }
        binding.editStartY.text.toString().toIntOrNull()?.let { Prefs.setStartY(this, it) }
        binding.editEndX.text.toString().toIntOrNull()?.let { Prefs.setEndX(this, it) }
        binding.editEndY.text.toString().toIntOrNull()?.let { Prefs.setEndY(this, it) }
        binding.editDuration.text.toString().toLongOrNull()?.let { Prefs.setDuration(this, it) }
        binding.editCloseX.text.toString().toIntOrNull()?.let { Prefs.setCloseX(this, it) }
        binding.editCloseY.text.toString().toIntOrNull()?.let { Prefs.setCloseY(this, it) }
        binding.editThreshold.text.toString().toDoubleOrNull()?.let { Prefs.setThreshold(this, it) }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val service = "${packageName}/.OrderAccessibilityService"
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        return enabledServices?.contains(service) == true
    }

    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
        Toast.makeText(this, "Enable AutoAccept service", Toast.LENGTH_LONG).show()
    }

    private fun checkPermissionStatus() {
        val hasNotification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        val hasAccessibility = isAccessibilityServiceEnabled()

        if (!hasNotification || !hasAccessibility) {
            binding.switchEnable.isChecked = false
        }
    }

    private fun startPermissionFlow() {
        currentPermissionStep = PERMISSION_STEP_NOTIFICATION
        continuePermissionFlow()
    }

    private fun continuePermissionFlow() {
        when (currentPermissionStep) {
            PERMISSION_STEP_NOTIFICATION -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        showPermissionDialog(
                            title = "Permission 1 of 3: Notifications",
                            message = "This app needs notification permission to alert you when orders are detected and auto-accepted.\n\nClick 'Continue' to grant this permission.",
                            onContinue = { requestNotificationPermission() }
                        )
                    } else {
                        currentPermissionStep = PERMISSION_STEP_ACCESSIBILITY
                        continuePermissionFlow()
                    }
                } else {
                    currentPermissionStep = PERMISSION_STEP_ACCESSIBILITY
                    continuePermissionFlow()
                }
            }
            PERMISSION_STEP_ACCESSIBILITY -> {
                if (!isAccessibilityServiceEnabled()) {
                    showPermissionDialog(
                        title = "Permission 2 of 3: Accessibility Service",
                        message = "This app needs accessibility permission to:\n• Detect order notifications\n• Read order details from the screen\n• Automatically tap 'Accept' buttons\n\nClick 'Continue' to open Accessibility Settings.",
                        onContinue = { openAccessibilitySettings() }
                    )
                } else {
                    currentPermissionStep = PERMISSION_STEP_SCREEN_CAPTURE
                    continuePermissionFlow()
                }
            }
            PERMISSION_STEP_SCREEN_CAPTURE -> {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                    showPermissionDialog(
                        title = "Permission 3 of 3: Screen Capture",
                        message = "This app needs screen capture permission to read order information from notifications.\n\nYour screen content is NOT recorded or saved.\n\nClick 'Continue' to grant this permission.",
                        onContinue = { requestMediaProjectionPermission() }
                    )
                } else {
                    currentPermissionStep = PERMISSION_STEP_COMPLETE
                    continuePermissionFlow()
                }
            }
            PERMISSION_STEP_COMPLETE -> {
                currentPermissionStep = PERMISSION_STEP_NONE
                savePreferences()
                Prefs.setEnabled(this, true)
                OrderAccessibilityService.instance?.refreshLoop()
                Toast.makeText(this, "✓ All permissions granted! Auto-Accept is now active.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showPermissionDialog(title: String, message: String, onContinue: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Continue") { _, _ -> onContinue() }
            .setNegativeButton("Cancel") { _, _ ->
                currentPermissionStep = PERMISSION_STEP_NONE
                binding.switchEnable.isChecked = false
                Toast.makeText(this, "Permission setup cancelled", Toast.LENGTH_SHORT).show()
            }
            .setCancelable(false)
            .show()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_NOTIFICATION_PERMISSION
            )
        }
    }

    private fun requestMediaProjectionPermission() {
        val projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(projectionManager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_NOTIFICATION_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    currentPermissionStep = PERMISSION_STEP_ACCESSIBILITY
                    continuePermissionFlow()
                } else {
                    currentPermissionStep = PERMISSION_STEP_NONE
                    binding.switchEnable.isChecked = false
                    Toast.makeText(this, "Notification permission is required", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                OrderAccessibilityService.projectionResultCode = resultCode
                OrderAccessibilityService.projectionData = data
                currentPermissionStep = PERMISSION_STEP_COMPLETE
                continuePermissionFlow()
            } else {
                currentPermissionStep = PERMISSION_STEP_NONE
                binding.switchEnable.isChecked = false
                Toast.makeText(this, "Screen capture permission is required", Toast.LENGTH_LONG).show()
            }
        }
    }
}
