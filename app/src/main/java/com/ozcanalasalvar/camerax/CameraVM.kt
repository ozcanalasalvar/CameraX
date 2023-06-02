package com.ozcanalasalvar.camerax

import android.net.Uri
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CameraVM : ViewModel() {

    val savedUri = MutableLiveData<Uri?>(null)

    var lensFacing = CameraSelector.DEFAULT_BACK_CAMERA

    var screenAspectRatio = AspectRatio.RATIO_16_9

    var audioEnabled = MutableLiveData<Boolean>(true)

    override fun onCleared() {
        super.onCleared()
        savedUri.value = null
    }

    fun toggleSound() {
        audioEnabled.value = !audioEnabled.value!!
    }
}