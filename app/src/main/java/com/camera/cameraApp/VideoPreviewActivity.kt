package com.camera.cameraApp

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.util.Consumer
import com.example.myapplication.R
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class VideoPreviewActivity : AppCompatActivity() {
    private lateinit var videoCapture: VideoCapture<Recorder>
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var viewFinder: PreviewView
    private lateinit var cameraProvider:ProcessCameraProvider
    private lateinit var recording:Recording
    private lateinit var camera:Camera
    private lateinit var captureButton:ImageButton
    private lateinit var recorderOnButton:Drawable
    private lateinit var timerTextView: TextView
    private var audioPermissionCheck: Boolean = false
    private var cameraSideCheck = true
    private var currentFlashMode: FlashMode = FlashMode.OFF
    private var recorderRunningCheck: Boolean = false
    private var timerCount:Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_preview)
        captureButton = findViewById(R.id.videoCaptureButton)
        val flipButton: ImageButton = findViewById(R.id.videoFlipButton)
        val flashButton: ImageButton = findViewById(R.id.videoFlashLightButton)
        val photoButton: ImageButton = findViewById(R.id.photo1)
        val galleryButton:ImageButton = findViewById(R.id.galleryButton)
        timerTextView = findViewById(R.id.timerTextView)
        if (Build.VERSION.SDK_INT > 32) {
            captureButton.setOnClickListener {
                recorderOnButton = ContextCompat.getDrawable(this, R.drawable.record_start)!!
                if (!recorderRunningCheck) {
                    val recorderOffButton: Drawable? =
                        ContextCompat.getDrawable(this, R.drawable.record_end)
                    recorderRunningCheck = true
                    captureButton.setImageDrawable(recorderOffButton)
                    recordVideo()
                } else {
                    videoTimer.cancel()
                    recorderRunningCheck = false
                    captureButton.setImageDrawable(recorderOnButton)
                    recording.stop()
                    camera.cameraControl.enableTorch(false)
                    timerCount = 0
                    timerTextView.text = getString(R.string._0sec)
                }
            }
        }
        if(Build.VERSION.SDK_INT > 28){
            galleryButton.setOnClickListener {
                val intent = Intent(Intent.ACTION_DEFAULT)
                intent.type = "video/*"
                startActivity(intent)
            }
        }
            photoButton.setOnClickListener{
            val intent  = Intent(this,PreviewActivity::class.java)
            startActivity(intent)
            finish()
        }
        flashButton.setOnClickListener {
            toggleFlash(flashButton)
        }

        galleryButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_DEFAULT)
            intent.type = "video/*"
            startActivity(intent)
        }
        viewFinder = findViewById(R.id.videoViewFinder)
        viewFinder.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        cameraExecutor = Executors.newSingleThreadExecutor()
        requestPermission()

        flipButton.setOnClickListener {
            cameraSideCheck = !cameraSideCheck
            startCamera()
        }
    }
    override fun onStart() {
        super.onStart()
        viewFinder.post {
            startCamera()
        }
    }
    private fun requestPermission(){
        requestCameraPermissionIfMissing{ granted ->
            if(granted)
                startCamera()
            else
                Toast.makeText(this,"Allow Camera Permission in Settings", Toast.LENGTH_SHORT).show()
        }

        requestAudioPermissionIfMissing { granted ->
            audioPermissionCheck = granted
        }
        if (Build.VERSION.SDK_INT < 33) {
            requestStorageWritePermissionIfMissing { granted ->
                if (granted) {
                } else {
                    Toast.makeText(this, "Allow Storage Permission in Settings", Toast.LENGTH_SHORT)
                        .show()
                }
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

                var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                if(!cameraSideCheck){
                    cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                }

                //Video Capture
                val qualitySelector = QualitySelector.fromOrderedList(
                    getResolutions(cameraSelector,cameraProvider),
                    FallbackStrategy.lowerQualityOrHigherThan(Quality.SD))

                val recorder = Recorder.Builder()
                    .setQualitySelector(qualitySelector)
                    .build()

                videoCapture = VideoCapture.withOutput(recorder)

                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(this, cameraSelector,preview, videoCapture)
                setFocusAndZoomListener(viewFinder, camera)

            }catch (e: Exception){
                 Toast.makeText(this,"Error starting the camera",Toast.LENGTH_SHORT).show()
            }

        },ContextCompat.getMainExecutor(this))
    }


    @SuppressLint("MissingPermission")
    private fun recordVideo(){
        val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/Camera App")
            }
        }

        val mediaStoreOutputOptions = MediaStoreOutputOptions
            .Builder(contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()

        if (audioPermissionCheck){
            recording = videoCapture.output
                .prepareRecording(this, mediaStoreOutputOptions)
                .withAudioEnabled()
                .start(ContextCompat.getMainExecutor(this), recordingListener)
            videoTimer.start()
            if(currentFlashMode == FlashMode.ON)
                camera.cameraControl.enableTorch(true)
            else
                camera.cameraControl.enableTorch(false)
        }
        else{
            recording = videoCapture.output
                .prepareRecording(this, mediaStoreOutputOptions)
                .start(ContextCompat.getMainExecutor(this), recordingListener)
            videoTimer.start()
            if(currentFlashMode == FlashMode.ON)
                camera.cameraControl.enableTorch(true)
            else
                camera.cameraControl.enableTorch(false)
        }

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
                val factory = viewFinder.meteringPointFactory
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
        when (currentFlashMode) {
            FlashMode.ON -> {
                flashButton.setImageDrawable(flashOn)
            }
            FlashMode.OFF -> {
                flashButton.setImageDrawable(flashOff)
            }

            else -> {
                flashButton.setImageDrawable(flashOff)
            }
        }
    }

    private fun toggleFlash(flashButton: ImageButton){
        currentFlashMode = when(currentFlashMode){
            FlashMode.ON -> FlashMode.OFF
            FlashMode.OFF -> FlashMode.ON
            else -> {
                FlashMode.OFF
            }
        }
        getFlashMode(flashButton)
    }

    private fun getResolutions(
        selector: CameraSelector,
        provider: ProcessCameraProvider
    ): List<Quality> {
        return selector.filter(provider.availableCameraInfos).firstOrNull()
            ?.let { camInfo ->
                QualitySelector.getSupportedQualities(camInfo)
            } ?: emptyList()
    }

    private val recordingListener = Consumer<VideoRecordEvent> { event ->
        when(event) {
            is VideoRecordEvent.Start -> {
            }
            is VideoRecordEvent.Finalize -> {
                if (!event.hasError()) {

                    "Video capture succeeded: ${event.outputResults.outputUri}"
                }
                else {
                    recording.close()

                    Toast.makeText(this, "Video capture error occurred", Toast.LENGTH_SHORT)
                        .show()
                }
                }

        }

    }

    val videoTimer = object : CountDownTimer(15000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            "${timerCount}s".also { timerTextView.text = it }
            timerCount++
        }

        override fun onFinish() {
            recorderRunningCheck = false
            captureButton.setImageDrawable(recorderOnButton)
            recording.stop()
            camera.cameraControl.enableTorch(false)
            timerCount = 0
            timerTextView.text = getString(R.string._0sec)
        }
    }
}