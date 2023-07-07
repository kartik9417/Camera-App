package com.camera.cameraApp

import android.graphics.Bitmap
import android.net.Uri

object VideoCache {

    private var videoUri: Uri? = null

    fun setVideoUri(videoUri: Uri?) {
        this.videoUri = videoUri
    }

    fun getVideoUri(): Uri? {
        return videoUri
    }
}