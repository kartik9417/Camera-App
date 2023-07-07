package com.camera.cameraApp

import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.GridView
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.myapplication.R
import ja.burhanrashid52.photoeditor.PhotoEditor
import ja.burhanrashid52.photoeditor.PhotoEditorView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class ImageEditor : AppCompatActivity() {
    private lateinit var capturedImage: Drawable
    private lateinit var mPhotoEditor:PhotoEditor
    private lateinit var color:ColorDrawable
    private lateinit var saveButton: Button
    private var drawModeCheck: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_editor)
        val textButton = findViewById<ImageButton>(R.id.textButton)
        val drawButton = findViewById<ImageButton>(R.id.drawButton)
        val stickerButton = findViewById<ImageButton>(R.id.stickerButton)
        val undoButton = findViewById<ImageButton>(R.id.undoButton)
        val redoButton = findViewById<ImageButton>(R.id.redoButton)
        val colorBar: LinearLayout = findViewById(R.id.colorBar)
        val toolbar:LinearLayout = findViewById(R.id.toolBar)
        val mPhotoEditorView = findViewById<PhotoEditorView>(R.id.photoEditorView)
        val doneButton:Button = findViewById(R.id.done_button)
        val mEmojiTypeFace = Typeface.createFromAsset(assets, "emojione-android.ttf")
        val scope = CoroutineScope(Dispatchers.Main)
        saveButton = findViewById(R.id.saveButton)
        requestPermission()
        if (Build.VERSION.SDK_INT > 32) {
            saveButton.setOnClickListener{
                val coroutineScope = CoroutineScope(Dispatchers.Main)
                coroutineScope.launch {
                    saveImage()
                }
            }
        }
        scope.launch {
            val bitmap = BitmapCache.getBitmap()
            capturedImage = BitmapDrawable(resources, bitmap)
            mPhotoEditorView.source.setImageDrawable(capturedImage)
        }

        mPhotoEditor = PhotoEditor.Builder(this, mPhotoEditorView)
            .setPinchTextScalable(true)
            .setClipSourceImage(true)
            .setDefaultEmojiTypeface(mEmojiTypeFace)
            .build()
        mPhotoEditor.brushSize = 50f

        for (i in 0 until colorBar.childCount) {
            val childView = colorBar.getChildAt(i) as ImageButton
            // Use the childView as needed
            color = childView.background as ColorDrawable
            childView.setOnClickListener {
                color = childView.background as ColorDrawable
                mPhotoEditor.brushColor = color.color
            }
        }

        textButton.setOnClickListener {
            saveButton.visibility = View.GONE
            showTextDialog()
        }

        drawButton.setOnClickListener{
            saveButton.visibility = View.GONE
            if(!drawModeCheck){
                mPhotoEditor.setBrushDrawingMode(true)
                doneButton.visibility = View.VISIBLE
                colorBar.visibility = View.VISIBLE
                toolbar.visibility = View.GONE
            }
            else{
                mPhotoEditor.setBrushDrawingMode(false)
            }
            drawModeCheck = !drawModeCheck
        }

        stickerButton.setOnClickListener {
            saveButton.visibility = View.GONE
            showEmojiDialog()
        }

        undoButton.setOnClickListener {
            mPhotoEditor.undo()
        }

        redoButton.setOnClickListener {
            mPhotoEditor.redo()
        }
        doneButton.setOnClickListener{
            if (drawModeCheck){
                mPhotoEditor.setBrushDrawingMode(false)
                colorBar.visibility = View.GONE
                toolbar.visibility = View.VISIBLE
                doneButton.visibility = View.GONE
            }
            drawModeCheck = !drawModeCheck
            saveButton.visibility = View.VISIBLE
        }
    }
    private fun showEmojiDialog() {
        val dialog = Dialog(this)
        val stickerGridView = LayoutInflater.from(this).inflate(R.layout.sticker_gridview,null)
        dialog.setContentView(stickerGridView)
        val gridView = dialog.findViewById<GridView>(R.id.stickerGridview)
        val names = StickersList.getList()
        val stickerGridAdaptor = StickerGridAdaptor(this,names)
        gridView.adapter = stickerGridAdaptor
        gridView.onItemClickListener = AdapterView.OnItemClickListener {
                adapterView, view, position, id ->

            val selectedSticker = adapterView.getItemAtPosition(position) as String
            mPhotoEditor.addEmoji(selectedSticker)
            dialog.hide()
            saveButton.visibility = View.VISIBLE
        }

        dialog.setOnDismissListener{
            saveButton.visibility = View.VISIBLE
        }

        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }
    private fun showTextDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.text_input_layout)
        val editTextInput = dialog.findViewById<EditText>(R.id.editTextInput)
        val colorBar = dialog.findViewById<LinearLayout>(R.id.colorBar)
        var color:ColorDrawable? = null
        editTextInput.background = null
        for (i in 0 until colorBar.childCount) {
            val childView = colorBar.getChildAt(i) as ImageButton
            color = childView.background as ColorDrawable
            childView.setOnClickListener {
                color = childView.background as ColorDrawable
                editTextInput.setTextColor(color!!.color)
            }
            }

        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        val buttonSubmit = dialog.findViewById<Button>(R.id.done_button)
        buttonSubmit.setOnClickListener {
            dialog.dismiss()
        }
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setOnDismissListener{
            val userInput = editTextInput.text.toString()
            dialog.dismiss()
            if(userInput != "")
                mPhotoEditor.addText(userInput,color!!.color)
            saveButton.visibility = View.VISIBLE
        }
        dialog.show()
    }

    private fun saveImage(){
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "IMG_$timeStamp.jpg"
        val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath
        val customDirectory = File("$storageDir/Camera App")
        if (!customDirectory.exists()) {
            customDirectory.mkdirs()
        }
        val imageFile = File(customDirectory, imageFileName)

        mPhotoEditor.saveAsFile(imageFile.absolutePath, object : PhotoEditor.OnSaveListener {
            override fun onSuccess(imagePath: String) {
                Toast.makeText(this@ImageEditor, "Image Saved Successfully",Toast.LENGTH_SHORT).show()
                scanImageFile(this@ImageEditor,imageFile.absolutePath)
                finish()
            }

            override fun onFailure(exception: Exception) {
                Toast.makeText(this@ImageEditor, "Image not saved +${exception}",Toast.LENGTH_SHORT).show()
                finish()
            }
        })
    }

    fun scanImageFile(context: Context, imagePath: String) {
        MediaScannerConnection.scanFile(
            context,
            arrayOf(imagePath),
            arrayOf("image/*")
        ) { path, uri ->

        }
    }

    private fun requestPermission(){
        if (Build.VERSION.SDK_INT < 33) {
            requestStorageWritePermissionIfMissing {granted ->
                if(granted){
                    saveButton.setOnClickListener{
                        val coroutineScope = CoroutineScope(Dispatchers.Main)
                        coroutineScope.launch {
                            saveImage()
                        }
                    }
                }
                else{
                    Toast.makeText(this,"Allow Storage Permission in Settings", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun requestStorageWritePermissionIfMissing(onResult: ((Boolean) -> Unit)){
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            onResult(true)
        }
        else
            registerForActivityResult(ActivityResultContracts.RequestPermission()){
                onResult(it)
            }.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }
}