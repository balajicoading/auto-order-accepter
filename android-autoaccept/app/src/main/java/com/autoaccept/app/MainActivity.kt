package com.autoaccept.app

import android.Manifest
import android.app.Activity
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
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadPreferences()
        setupListeners()
        requestNotificationPermission()
    }

    override fun onResume() {
        super.onResume()
        binding.switchEnable.isChecked = Prefs.isEnabled(this)
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
            if (isChecked && !isAccessibilityServiceEnabled()) {
                binding.switchEnable.isChecked = false
                openAccessibilitySettings()
                return@setOnCheckedChangeListener
            }

            if (isChecked && Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                requestMediaProjectionPermission()
            }

            savePreferences()
            Prefs.setEnabled(this, isChecked)
            OrderAccessibilityService.instance?.refreshLoop()
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

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_NOTIFICATION_PERMISSION
                )
            }
        }
    }

    private fun requestMediaProjectionPermission() {
        val projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(projectionManager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                OrderAccessibilityService.projectionResultCode = resultCode
                OrderAccessibilityService.projectionData = data
                Toast.makeText(this, "Screen capture permission granted", Toast.LENGTH_SHORT).show()
            } else {
                binding.switchEnable.isChecked = false
                Toast.makeText(this, "Screen capture permission required", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
