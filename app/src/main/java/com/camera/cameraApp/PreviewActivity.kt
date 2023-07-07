package com.camera.cameraApp

import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.media.Image
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.example.myapplication.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class PreviewActivity : AppCompatActivity() {
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var imageCapture:ImageCapture
    private lateinit var viewFinder: PreviewView
    private lateinit var cameraProvider:ProcessCameraProvider
    private lateinit var loadingBar:LoadingBar
    private lateinit var flipButton: ImageButton
    private lateinit var captureButton:ImageButton
    private lateinit var galleryButton:ImageButton
    private var audioPermissionCheck:Boolean = false
    private var cameraSideCheck = true
    private var currentFlashMode: FlashMode = FlashMode.AUTO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)
        captureButton = findViewById(R.id.captureButton)
        flipButton = findViewById(R.id.cameraFlipButton)
        val flashButton: ImageButton = findViewById(R.id.flashLightButton)
        val videoButton: ImageButton = findViewById(R.id.video)
        galleryButton = findViewById(R.id.galleryButton)
        loadingBar = LoadingBar(this)
        if(Build.VERSION.SDK_INT > 28){
            galleryButton.setOnClickListener {
                val intent = Intent(Intent.ACTION_DEFAULT)
                intent.type = "image/*"
                startActivity(intent)
            }
        }
        videoButton.setOnClickListener{
            val intent  = Intent(this,VideoPreviewActivity::class.java)
            startActivity(intent)
            finish()
        }
        flashButton.setOnClickListener {
            toggleFlash(flashButton)
        }
        viewFinder = findViewById(R.id.viewFinder)
        viewFinder.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        cameraExecutor = Executors.newSingleThreadExecutor()
        requestPermission()
    }
    override fun onStart() {
        super.onStart()
        viewFinder.post {
            startCamera()
        }
    }
    private fun requestPermission(){
        requestCameraPermissionIfMissing { granted ->
            if (granted){
                startCamera()
                captureButton.setOnClickListener { capturePhoto() }
                 flipButton.setOnClickListener {
                     cameraSideCheck = !cameraSideCheck
                     startCamera()
                 }
             }
            else
                Toast.makeText(this,"Allow Camera Permission in Settings", Toast.LENGTH_SHORT).show()
        }

        requestAudioPermissionIfMissing { granted ->
            audioPermissionCheck = granted
        }

        if (Build.VERSION.SDK_INT < 29) {
            requestStorageReadPermissionIfMissing { granted ->
                if(granted){
                    galleryButton.setOnClickListener {
                        val intent = Intent(Intent.ACTION_DEFAULT)
                        intent.type = "image/*"
                        startActivity(intent)
                    }
                }
                else
                    Toast.makeText(this,"Allow Storage Permission in Settings", Toast.LENGTH_SHORT).show()
            }
        }

        if (Build.VERSION.SDK_INT < 33) {
            requestStorageWritePermissionIfMissing {

            }
        }
    }

    private fun requestCameraPermissionIfMissing(onResult: ((Boolean) -> Unit)){
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
            onResult(true)
        }
        else
            registerForActivityResult(ActivityResultContracts.RequestPermission()){
                onResult(it)
            }.launch(android.Manifest.permission.CAMERA)
    }

    private fun requestAudioPermissionIfMissing(onResult: ((Boolean) -> Unit)){
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED){
            onResult(true)
        }
        else
            registerForActivityResult(ActivityResultContracts.RequestPermission()){
                onResult(it)
            }.launch(android.Manifest.permission.RECORD_AUDIO)
    }

    private fun requestStorageReadPermissionIfMissing(onResult: ((Boolean) -> Unit)){
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            onResult(true)
        }
        else
            registerForActivityResult(ActivityResultContracts.RequestPermission()){
                onResult(it)
            }.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
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

    private fun startCamera(){
        val processCameraProvider = ProcessCameraProvider.getInstance(this)
        processCameraProvider.addListener({
            try {
                cameraProvider = processCameraProvider.get( )
                val preview = Preview.Builder().build()
                preview.setSurfaceProvider(viewFinder.surfaceProvider)

                //Image Capture
                imageCapture = ImageCapture.Builder().setFlashMode(ImageCapture.FLASH_MODE_AUTO).build()
                var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                if(!cameraSideCheck){
                    cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                }

                cameraProvider.unbindAll()
                val camera = cameraProvider.bindToLifecycle(this, cameraSelector,preview, imageCapture)
                setFocusAndZoomListener(viewFinder, camera)

            }catch (e: Exception){
                Toast.makeText(this,"Error starting the camera",Toast.LENGTH_SHORT).show()
            }

        },ContextCompat.getMainExecutor(this))
    }

    private fun capturePhoto(){
        val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P){
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }



        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(contentResolver,MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues)
            .build()



//        imageCapture.takePicture(
//            outputOptions,
//            ContextCompat.getMainExecutor(this),
//            object : ImageCapture.OnImageSavedCallback{
//                override fun onError(exception: ImageCaptureException) {
//                    Log.e("PreviewActivity","Photo capture failed ${exception.message}",exception)
//                }
//
//                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
//                    val msg = "Photo capture succeeded: ${outputFileResults.savedUri}"
//                    File(outputFileResults.savedUri)
//                    Toast.makeText(baseContext,msg, Toast.LENGTH_SHORT).show()
//                    Log.d("PreviewActivity",msg)
//                }
//            }
//        )
        loadingBar.startLoading()
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback(){
                override fun onError(exception: ImageCaptureException) {
                    Log.e("PreviewActivity","Photo capture failed ${exception.message}",exception)
                }

                @androidx.camera.core.ExperimentalGetImage
                override fun onCaptureSuccess(imageProxy: ImageProxy) {
                    val scope = CoroutineScope(Dispatchers.Main)
                    scope.launch {
                        val image: Image? = imageProxy.image
                        val buffer = image!!.planes[0].buffer
                        val bytes = ByteArray(buffer.remaining())
                        buffer[bytes]
                        var myBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null)
                        if (!cameraSideCheck){
                            myBitmap = rotateBitmap(myBitmap,270f)
                        }
                        else{
                            myBitmap = rotateBitmap(myBitmap,90f)
                        }
                        BitmapCache.setBitmap(myBitmap)
                        imageProxy.close()
                        val intent = Intent(applicationContext, ImageEditor::class.java)
                        startActivity(intent)
                        loadingBar.stopLoading()
                    }
//                    scope.launch() {
//                        val image: Image? = imageProxy.image
//                        val buffer = image!!.planes[0].buffer
//                        val bytes = ByteArray(buffer.remaining())
//                        buffer[bytes]
//                        val myBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null)
//                        var fileOutputStream: FileOutputStream? = null
//                        val file = File(cacheDir, "bitmap.png")
//                        try {
//                            fileOutputStream = FileOutputStream(file)
//
////                            generateBitmap(myBitmap, fileOutputStream)
//                            myBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
//                            withContext(Dispatchers.IO) {
//                                fileOutputStream.flush()
//                            }
//                            imageProxy.close()
//                            val intent = Intent(applicationContext, ImageEditor::class.java)
//                            intent.putExtra("bitmapFilePath", file.absolutePath)
//                            startActivity(intent)
//
//
////                        myBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
////                            fileOutputStream.flush()
//                        } catch (e: IOException) {
//                            e.printStackTrace()
//                        } finally {
//                            if (fileOutputStream != null) {
//                                try {
//                                    fileOutputStream.close()
//                                } catch (e: IOException) {
//                                    e.printStackTrace()
//                                }
//                            }
//                            loadingBar.stopLoading()
//                        }
//                    }
                }
            }
        )
    }

    private fun setFocusAndZoomListener(viewF:PreviewView,camera: Camera){
        val scaleGestureDetector = ScaleGestureDetector(this,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    val scale = camera.cameraInfo.zoomState.value!!.zoomRatio * detector.scaleFactor
                    camera.cameraControl.setZoomRatio(scale)
                    return true
                }
            })
        viewF.setOnTouchListener { view, event ->
            view.performClick()
            scaleGestureDetector.onTouchEvent(event)
            if (event.action == MotionEvent.ACTION_DOWN) {
                val factory = viewF.meteringPointFactory
                val point = factory.createPoint(event.x, event.y)
                val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF)
                    .setAutoCancelDuration(5, java.util.concurrent.TimeUnit.SECONDS)
                    .build()

                val future = camera.cameraControl.startFocusAndMetering(action)
                future.addListener({
                    try {
                        val result = future.get()
                        if (result.isFocusSuccessful) {
                            Log.d("PreviewActivity", "Focus successful")
                        } else {
                            Log.d("PreviewActivity", "Focus failed")
                        }
                    } catch (e: Exception) {
                        Log.e("PreviewActivity", "Error focusing: ${e.message}")
                    }
                }, ContextCompat.getMainExecutor(this))
            }
            return@setOnTouchListener true
        }
    }

    private fun getFlashMode(flashButton:ImageButton){
        val flashOn: Drawable? = ContextCompat.getDrawable(this,R.drawable.camera_flash_on)
        val flashOff: Drawable? = ContextCompat.getDrawable(this,R.drawable.camera_flash_off)
        val flashAuto: Drawable? = ContextCompat.getDrawable(this,R.drawable.camera_flash_auto)
        when (currentFlashMode) {
            FlashMode.AUTO -> {
                imageCapture.flashMode = ImageCapture.FLASH_MODE_AUTO
                flashButton.setImageDrawable(flashAuto)
            }
            FlashMode.ON -> {
                imageCapture.flashMode = ImageCapture.FLASH_MODE_ON
                flashButton.setImageDrawable(flashOn)
            }
            FlashMode.OFF -> {
                imageCapture.flashMode = ImageCapture.FLASH_MODE_OFF
                flashButton.setImageDrawable(flashOff)
            }
        }
    }

    private fun toggleFlash(flashButton: ImageButton){
        currentFlashMode = when(currentFlashMode){
            FlashMode.AUTO -> FlashMode.ON
            FlashMode.ON -> FlashMode.OFF
            FlashMode.OFF -> FlashMode.AUTO
        }
        getFlashMode(flashButton)
    }

    fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    }
}