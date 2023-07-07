package com.camera.cameraApp

import android.graphics.Bitmap

object BitmapCache {

    private var bitmap: Bitmap? = null

    fun setBitmap(bitmap: Bitmap) {
        this.bitmap = bitmap
    }

    fun getBitmap(): Bitmap? {
        return bitmap
    }
}