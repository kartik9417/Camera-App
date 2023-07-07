package com.camera.cameraApp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.myapplication.R

class StickerGridAdaptor(context: Context,private val stickerCodes: List<String>): ArrayAdapter<String>(context,0) {

    override fun getCount(): Int {
        return stickerCodes.size
    }

    override fun getItem(position: Int): String? {
        return stickerCodes[position]
    }

    override fun getItemId(p0: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        var view = convertView

        if (view == null){
            view = LayoutInflater.from(context).inflate(R.layout.sticker_grid_item,null)
        }
        val stickerTextView = view?.findViewById<TextView>(R.id.sticker_item_name)
        if (stickerTextView != null) {
            stickerTextView.text = stickerCodes[position]
        }
        return view!!
    }
}