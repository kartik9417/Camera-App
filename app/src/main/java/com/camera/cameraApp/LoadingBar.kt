package com.camera.cameraApp

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.view.LayoutInflater
import com.example.myapplication.R

class LoadingBar(private val activity: Activity) {
    private lateinit var dialog: AlertDialog
    @SuppressLint("InflateParams")
    fun startLoading(){
        val builder:AlertDialog.Builder = AlertDialog.Builder(activity)
        val inflater:LayoutInflater = activity.layoutInflater
        builder.setView(inflater.inflate(R.layout.progress_bar,null))
        builder.setCancelable(false)

        dialog = builder.create()
        dialog.show()
    }

    fun stopLoading(){
        dialog.dismiss()
    }

}