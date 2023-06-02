package com.ozcanalasalvar.camerax.fragments

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.ozcanalasalvar.camerax.CameraVM
import com.ozcanalasalvar.camerax.R
import com.ozcanalasalvar.camerax.databinding.FragmentCameraBinding
import com.ozcanalasalvar.camerax.utils.DateFormats
import com.ozcanalasalvar.camerax.utils.DateFormatter
import com.ozcanalasalvar.camerax.utils.getAspectRationString
import com.ozcanalasalvar.camerax.utils.getDimensionRatioString
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class CameraFragment : Fragment() {
    private val viewModel: CameraVM by viewModels()

    private lateinit var binding: FragmentCameraBinding

    private var imageCapture: ImageCapture? = null

    private lateinit var cameraExecutor: ExecutorService

    private lateinit var scaleGestureDetector: ScaleGestureDetector

    private var camera: Camera? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.viewFinder.setOnTouchListener { view, motionEvent ->
            scaleGestureDetector.onTouchEvent(motionEvent)
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    binding.pnlFlashOptions.visibility = View.GONE
                    binding.pnlRatioOptions.visibility = View.GONE
                    binding.zoomSeekWrapper.visibility = View.VISIBLE
                    startTouchTimer()
                    return@setOnTouchListener true
                }

                MotionEvent.ACTION_UP -> {
                    val factory = binding.viewFinder.meteringPointFactory
                    val point = factory.createPoint(motionEvent.x, motionEvent.y)
                    val action = FocusMeteringAction.Builder(point).build()
                    camera?.cameraControl?.startFocusAndMetering(action)

                    return@setOnTouchListener true
                }

                else -> return@setOnTouchListener false

            }
        }

        updateRatioView()

        binding.photoButton.setOnClickListener { takePhoto() }
        binding.pnlFLash.setOnClickListener(flashClickListener)
        binding.ivFlashOff.setOnClickListener(flashChangeListener)
        binding.ivFlashOn.setOnClickListener(flashChangeListener)
        binding.ivFlashAuto.setOnClickListener(flashChangeListener)
        binding.pnlFacing.setOnClickListener(facingChangeListener)

        binding.pnlRatio.setOnClickListener(ratioClickListener)
        binding.tvRatio169.setOnClickListener(ratioChangeListener)
        binding.tvRatio43.setOnClickListener(ratioChangeListener)

        binding.zoomSeekBar.setOnSeekBarChangeListener(zoomSeekListener)
        scaleGestureDetector = ScaleGestureDetector(requireContext(), zoomListener)


        binding.swCameraOption.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                findNavController().navigate(R.id.action_cameraFragment_to_videoFragment)
            }
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        bindCameraUseCases()

        observeUriState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateRatioView()
    }

    private fun observeUriState() {
        viewModel.savedUri.observe(requireActivity()) { uri ->
            uri?.let { showPreview(uri) }
            Log.w("savedUri", uri.toString())
        }
    }

    private fun takePhoto() {

        binding.captureProgress.visibility = View.VISIBLE

        val imageCapture = imageCapture ?: return


        val contentValues = ContentValues().apply {
            put(
                MediaStore.MediaColumns.DISPLAY_NAME,
                "CameraX-capture-" + DateFormatter.getCurrentDate(DateFormats.backend)
            )
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            val appName = requireContext().resources.getString(R.string.app_name)
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/${appName}")

        }
        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            requireActivity().contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()

        imageCapture.takePicture(outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                    viewModel.savedUri.value = null
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    output.savedUri?.let { showPreview(it) }
                    viewModel.savedUri.value = output.savedUri
                    binding.captureProgress.visibility = View.GONE
                }
            })
    }

    private fun bindCameraUseCases() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        binding.viewFinder.updateLayoutParams<ConstraintLayout.LayoutParams> {
            val orientation = resources.configuration.orientation
            dimensionRatio =
                viewModel.screenAspectRatio.getDimensionRatioString((orientation == Configuration.ORIENTATION_PORTRAIT))
        }

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview =
                Preview.Builder().setTargetAspectRatio(viewModel.screenAspectRatio).build().also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setFlashMode(imageCapture?.flashMode ?: ImageCapture.FLASH_MODE_AUTO)
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .setTargetAspectRatio(viewModel.screenAspectRatio).build()

            updateFlashView()

            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    this, viewModel.lensFacing, preview, imageCapture
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun flipCamera() {
        var rotationDegree = 0f
        if (viewModel.lensFacing === CameraSelector.DEFAULT_FRONT_CAMERA) {
            viewModel.lensFacing = CameraSelector.DEFAULT_BACK_CAMERA
            rotationDegree = 180f
        } else if (viewModel.lensFacing === CameraSelector.DEFAULT_BACK_CAMERA) {
            viewModel.lensFacing = CameraSelector.DEFAULT_FRONT_CAMERA
            rotationDegree = -180f
        }
        bindCameraUseCases()

        binding.ivFacing.animate().rotation(rotationDegree).setDuration(500).start()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }


    private val flashClickListener = View.OnClickListener {
        binding.pnlRatioOptions.visibility = View.GONE
        binding.pnlFlashOptions.visibility = when (binding.pnlFlashOptions.visibility) {
            View.VISIBLE -> View.GONE
            View.GONE -> View.VISIBLE
            else -> binding.pnlFlashOptions.visibility
        }
    }

    private val flashChangeListener = View.OnClickListener {
        when (it.id) {
            binding.ivFlashOff.id -> {
                imageCapture?.flashMode = ImageCapture.FLASH_MODE_OFF
                binding.pnlFlashOptions.visibility = View.GONE
                updateFlashView()
            }

            binding.ivFlashOn.id -> {
                imageCapture?.flashMode = ImageCapture.FLASH_MODE_ON
                binding.pnlFlashOptions.visibility = View.GONE
                updateFlashView()
            }

            binding.ivFlashAuto.id -> {
                imageCapture?.flashMode = ImageCapture.FLASH_MODE_AUTO
                binding.pnlFlashOptions.visibility = View.GONE
                updateFlashView()
            }
        }
    }

    private fun updateFlashView() {
        binding.ivFlash.setImageResource(
            when (imageCapture?.flashMode) {
                ImageCapture.FLASH_MODE_OFF -> {
                    R.drawable.ic_flash_off
                }

                ImageCapture.FLASH_MODE_ON -> {
                    R.drawable.ic_flash_on
                }

                ImageCapture.FLASH_MODE_AUTO -> {
                    R.drawable.ic_flash_auto
                }

                else -> R.drawable.ic_flash_off
            }
        )
    }


    private val ratioClickListener = View.OnClickListener {
        binding.pnlFlashOptions.visibility = View.GONE
        binding.pnlRatioOptions.visibility = when (binding.pnlRatioOptions.visibility) {
            View.VISIBLE -> View.GONE
            View.GONE -> View.VISIBLE
            else -> binding.pnlRatioOptions.visibility
        }
    }


    private val facingChangeListener = View.OnClickListener {
        flipCamera()
    }

    private val zoomListener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scale = camera?.cameraInfo?.zoomState?.value?.zoomRatio?.times(detector.scaleFactor)
            scale?.let { value ->
                camera?.cameraControl?.setZoomRatio(value)
                val result = if (value < 1.5) 0.0f else value
                binding.zoomSeekBar.setOnSeekBarChangeListener(null)
                binding.zoomSeekBar.progress = (result * 10).toInt()
                binding.zoomSeekBar.setOnSeekBarChangeListener(zoomSeekListener)
            }

            binding.zoomSeekWrapper.visibility = View.VISIBLE
            startTouchTimer()

            return true
        }
    }

    private val zoomSeekListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(p0: SeekBar?, progress: Int, p2: Boolean) {
            camera?.cameraControl?.setLinearZoom(progress / 100.toFloat())
            startTouchTimer()
        }

        override fun onStartTrackingTouch(p0: SeekBar?) {

        }

        override fun onStopTrackingTouch(p0: SeekBar?) {

        }

    }

    private val ratioChangeListener = View.OnClickListener {
        var ratio = 0
        when (it.id) {
            binding.tvRatio169.id -> {
                ratio = AspectRatio.RATIO_16_9
            }

            binding.tvRatio43.id -> {
                ratio = AspectRatio.RATIO_4_3
            }
        }

        if (viewModel.screenAspectRatio != ratio) {
            viewModel.screenAspectRatio = ratio
            updateRatioView()
            lifecycleScope.launch {
                delay(500L)
                bindCameraUseCases()
            }

        }

        binding.pnlRatioOptions.visibility = View.GONE

    }

    private fun updateRatioView() {
        val orientation = resources.configuration.orientation
        binding.tvRatio.text =
            viewModel.screenAspectRatio.getAspectRationString(orientation == Configuration.ORIENTATION_PORTRAIT)

        binding.tvRatio169.text =
            if (orientation == Configuration.ORIENTATION_PORTRAIT) "9:16" else "16:9"
        binding.tvRatio43.text =
            if (orientation == Configuration.ORIENTATION_PORTRAIT) "3:4" else "4:3"
    }


    private fun showPreview(uri: Uri) {
        binding.previewImage.setImageURI(uri)
        binding.pnlPreview.visibility = View.VISIBLE
    }


    private var timer: CountDownTimer? = null
    private fun startTouchTimer(duration: Long = 1000) {
        timer?.cancel()
        timer = null
        timer = object : CountDownTimer(duration, 1000) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                binding.zoomSeekWrapper.visibility = View.INVISIBLE
            }
        }.start()
    }


    companion object {
        private const val TAG = "MRCCamera"
    }
}