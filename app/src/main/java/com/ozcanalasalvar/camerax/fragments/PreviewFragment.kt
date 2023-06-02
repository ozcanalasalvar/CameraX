package com.ozcanalasalvar.camerax.fragments

import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.MediaController
import androidx.navigation.fragment.findNavController
import com.ozcanalasalvar.camerax.R
import com.ozcanalasalvar.camerax.databinding.FragmentCameraBinding
import com.ozcanalasalvar.camerax.databinding.FragmentPreviewBinding
import com.ozcanalasalvar.camerax.utils.MediaType
import java.io.File


const val ARG_PREVIEW_TYPE = "previewType"
const val ARG_MEDIA_PATH = "mediaPath"


class PreviewFragment : Fragment() {


    private lateinit var binding: FragmentPreviewBinding

    private var mediaType: String? = null
    private var mediaUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentPreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mediaType = it.getString(ARG_PREVIEW_TYPE)
            mediaUri = Uri.parse(it.getString(ARG_MEDIA_PATH))
        }

        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        when (mediaType) {
            MediaType.IMAGE -> {
                binding.videoViewer.visibility = View.GONE
                binding.controllerView.visibility = View.GONE
                binding.ivPreview.visibility = View.VISIBLE
                binding.ivPreview.setImageURI(mediaUri)
            }

            MediaType.VIDEO -> {
                binding.videoViewer.visibility = View.VISIBLE
                binding.controllerView.visibility = View.VISIBLE
                binding.ivPreview.visibility = View.GONE
                val mc = MediaController(requireContext())
                val params: FrameLayout.LayoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
                )
                params.bottomMargin = 200
                mc.layoutParams = params


                mc.setAnchorView(binding.controllerView)



                binding.videoViewer.apply {
                    setVideoURI(mediaUri)
                    setMediaController(mc)
                    requestFocus()
                }.start()
            }
        }

        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
    }
}
