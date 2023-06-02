package com.ozcanalasalvar.camerax

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.ozcanalasalvar.camerax.databinding.ActivityCameraBinding
import com.ozcanalasalvar.camerax.utils.Permissions

class CameraActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private val viewModel: CameraVM by viewModels()

    private lateinit var binding: ActivityCameraBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        window.navigationBarColor = Color.parseColor("#80000000")
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initNavController()

        binding.btnRequestPermission.setOnClickListener { requestPermissions() }

        if (!Permissions.isPermissionTaken(this)) {
            requestPermissions()
        }
    }

    private fun initNavController() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.cameraNavHost) as NavHostFragment
        navController = navHostFragment.navController
    }

    private fun requestPermissions() {
        permissionLauncher.launch(Permissions.requestList)
    }


    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val hasPermission = permissions.entries.all { it.value }
            binding.pnlPermission.visibility =
                if (!hasPermission) View.VISIBLE else View.GONE
        }

}