package com.ozcanalasalvar.camerax

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.ozcanalasalvar.camerax.databinding.ActivityCameraBinding
import com.ozcanalasalvar.camerax.fragments.CameraVM

class CameraActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private val viewModel: CameraVM by viewModels()

    private lateinit var binding: ActivityCameraBinding

    @SuppressLint("ClickableViewAccessibility") override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        window.navigationBarColor = Color.parseColor("#80000000")
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initNavController()
        intent.extras.let {
            when (it?.getString(CAPTURE_TYPE, CAMERA)) {
                CAMERA -> {}
                VIDEO -> {
                    if ((navController.currentDestination?.id ?: -1) != R.id.videoFragment) {
                        navController.navigate(R.id.action_cameraFragment_to_videoFragment)
                    }
                }
                else -> throw Exception("Please pass valid camera type")
            }
        }

    }

    private fun initNavController() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.cameraNavHost) as NavHostFragment
        navController = navHostFragment.navController
    }

    companion object {
        const val CAPTURE_TYPE = "capture_type"
        const val CAMERA = "camera"
        const val VIDEO = "video"
    }

}