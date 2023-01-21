package com.example.iType

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.fragmentstutorial.*
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceContour
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import org.opencv.android.OpenCVLoader
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


interface FragmentControler {
    fun clickRightButton()
    fun clickLeftButton()
    fun clickUpButton()
    fun clickDownButton()
}
class MainActivity : AppCompatActivity(), KeyBoardFragment.KeyboardFragmentListener,
    WordsFragment.WordsListListener, WordsFragment.WrodsFragmentListenr ,
    OptionsFragment.OptionsFragmentListener{

    private lateinit var keyBoardFragment: KeyBoardFragment
    private lateinit var sentenceTextView: TextView
    private var seq :String = ""
    private lateinit var seq2WordsEngine: Seq2WordsEngine
    private lateinit var wordsList : Queue<Word>
    private lateinit var textToSpeech: TextToSpeech
    private lateinit var wordsFragment : WordsFragment
    private lateinit var optionsFragment: OptionsFragment
    private lateinit var listener: FragmentControler
    private lateinit var previewView: PreviewView
    private lateinit var testImage : ImageView

    private var currentFrag: Char = 'K'
    private var x: Float = 0.0f
    private var y :Float = 0.0f
    private var width :Float = 0.0f
    private var height : Float = 0.0f

    private var xR: Float = 0.0f
    private var yR :Float = 0.0f
    private var widthR :Float = 0.0f
    private var heightR : Float = 0.0f

    private lateinit var classifier: Classifier
    private var start =  0L
    private var end = 0L
    private val INPUT_SIZE: Int = 150
    private val mModelPath = "model20.tflite"
    private val mLabelPath = "label.txt"
    private var lensFacing: Int = CameraSelector.LENS_FACING_FRONT
    private var cameraSelector: CameraSelector? = null
    private lateinit var cameraExecutor: ExecutorService
    private var bitmapFrame: Bitmap? = null
    private var imageRotationDegrees: Int = 0

    private var startedTyping = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        seq2WordsEngine = Seq2WordsEngine(assets)
        previewView = findViewById(R.id.preview_view)
        testImage = findViewById(R.id.test_image)

        initClassifier()
        keyBoardFragment = KeyBoardFragment()
        wordsFragment  = WordsFragment()
        optionsFragment = OptionsFragment()
        supportFragmentManager.beginTransaction().replace(R.id.frag_view, keyBoardFragment).commit()
        textToSpeech = TextToSpeech(
            applicationContext
        ) { i ->
            if (i != TextToSpeech.ERROR) {
                textToSpeech.language = Locale.UK
            }
        }
        cameraExecutor = Executors.newSingleThreadExecutor()
        cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        OpenCVLoader.initDebug()
        if (allPermissionsGranted()) {
            bindCameraUseCases()
            println("allPermissionsGranted")
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
            println("allPermissionsnotGranted")
        }

        listener = if (keyBoardFragment is FragmentControler) {
            keyBoardFragment
        } else {
            throw RuntimeException(
                keyBoardFragment.toString()
                    .toString() + " must implement FragmentAListener"
            )
        }
        sentenceTextView = findViewById(R.id.sentence_text_view)

    }

    override fun onInputKeyboardSent(input: CharSequence?) {
        seq += input.toString()
    }

    override fun getWordlist(): Queue<Word> {
        return wordsList
    }

    override fun onInputWordsSent(input: CharSequence?) {
        val t = sentenceTextView.text.toString() + " " + input.toString()
        sentenceTextView.text = t
        supportFragmentManager.beginTransaction().replace(R.id.frag_view, optionsFragment).commit()
        listener = if (optionsFragment is FragmentControler) {
            optionsFragment
        } else {
            throw RuntimeException(
                keyBoardFragment.toString()
                    .toString() + " must implement FragmentAListener"
            )
        }

    }

    override fun onInputOptionsSent(input: CharSequence?) {
        if (input.toString().equals("1")) {
            val t = sentenceTextView.text.toString()
            textToSpeech.speak(t, TextToSpeech.QUEUE_FLUSH, null)
            sentenceTextView.text = ""
            seq = ""
            supportFragmentManager.beginTransaction().replace(R.id.frag_view, keyBoardFragment).commit()
            currentFrag = 'K'
            listener = if (keyBoardFragment is FragmentControler) {
                keyBoardFragment
            } else {
                throw RuntimeException(
                    keyBoardFragment.toString()
                        .toString() + " must implement FragmentAListener"
                )
            }
        }
        else if(input.toString().equals("3")){
            seq = ""
            supportFragmentManager.beginTransaction().replace(R.id.frag_view, keyBoardFragment).commit()

        }
        else if(input.toString().equals("2")){
            supportFragmentManager.beginTransaction().replace(R.id.frag_view, wordsFragment).commit()

        }
        else{

        }

    }

    @SuppressLint("UnsafeExperimentalUsageError", "UnsafeOptInUsageError")
    private fun bindCameraUseCases()  = previewView.post{

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener ({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                .setTargetRotation(previewView.display.rotation)
                .build()
            println("setTargetRotation")
            println(previewView.display.rotation)
            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .setImageQueueDepth(1)
                .build()

            imageAnalysis.setAnalyzer(cameraExecutor, ImageAnalysis.Analyzer { image ->
                if(bitmapFrame == null)
                {
                    imageRotationDegrees = image.imageInfo.rotationDegrees

                    println("imageRotationDegrees")
                    println(imageRotationDegrees)
                    println("imageAnalysis.targetRotation ")
                    println(imageAnalysis.targetRotation )
                    bitmapFrame = Bitmap.createBitmap(
                        image.width, image.height, Bitmap.Config.ARGB_8888)
                }
                image.use { bitmapFrame!!.copyPixelsFromBuffer(image.planes[0].buffer)  }
                start = System.currentTimeMillis()
                runFaceContourDetection()
            })
            val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                this as LifecycleOwner, cameraSelector, preview, imageAnalysis)
        }, ContextCompat.getMainExecutor(this))
    }

    private fun runFaceContourDetection(){
        val image = bitmapFrame?.let { InputImage.fromBitmap(it, 270) }
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .build()
        val detector = FaceDetection.getClient(options)
        if (image != null) {
            println(image != null)
            detector.process(image)
                .addOnSuccessListener { faces ->
                    println(faces.size)
                    processFaceContourDetectionResult(faces)
                    println("this image ")
                    if (bitmapFrame != null){
                        println(bitmapFrame?.height)
                    }
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                }
        }
    }
    private fun processFaceContourDetectionResult(faces: List<Face>) {
        if (faces.size == 0) {
            println("no face detected")
            return
        }
        for (i in faces.indices) {
            val face = faces[i]
            var eyeContour = face.getContour(FaceContour.LEFT_EYE)
            eyeContour!!.points.sortBy { it.x }
            var m2 = eyeContour.points.get((eyeContour.points.size-1)/2).y

            var eyeBrow = face.getContour(FaceContour.LEFT_EYEBROW_BOTTOM)
            var minX = eyeBrow!!.points.get(0).x
            var maxX = eyeBrow!!.points.get(0).x
            var minY = eyeBrow!!.points.get(0).y
            var maxY = eyeBrow!!.points.get(0).y

            for (point in eyeBrow!!.points) {
                if (point.x < minX){
                    minX = point.x
                }
                if (point.x > maxX){
                    maxX = point.x
                }
                if (point.y < minY){
                    minY = point.y
                }
                if (point.y > minY){
                    maxY = point.y
                }
            }
            var bY = m2 - minY
            var b = m2 + bY
            x = minX
            y = minY
            width = maxX
            height = b

            eyeContour = face.getContour(FaceContour.RIGHT_EYE)
            eyeContour!!.points.sortBy { it.x }
            m2 = eyeContour.points.get((eyeContour.points.size-1)/2).y
            eyeBrow = face.getContour(FaceContour.RIGHT_EYEBROW_BOTTOM)
            minX = eyeBrow!!.points.get(0).x
            maxX = eyeBrow!!.points.get(0).x
            minY = eyeBrow!!.points.get(0).y
            maxY = eyeBrow!!.points.get(0).y
            for (point in eyeBrow!!.points) {
                if (point.x < minX){
                    minX = point.x
                }
                if (point.x > maxX){
                    maxX = point.x
                }
                if (point.y < minY){
                    minY = point.y
                }
                if (point.y > minY){
                    maxY = point.y
                }

                println("point xr is " + point.x.toInt())
            }
            bY = m2 - minY
            b = m2 + bY
            xR = minX
            yR = minY
            widthR = maxX
            heightR = b
            set_im()
        }
    }
    private fun set_im(){
        if((start - end) > 200)
        {
            if (x > 0 && y > 0 && width > 0 && height > 0 && xR > 0 && yR > 0 && widthR > 9 && heightR > 0 ) {
                if (bitmapFrame != null) {
                    val matFrame = rotateBitmap(bitmapFrame!!, 270F)
                    val bFrame = applyGrayscaleEffect(matFrame!!)
                    if (x > 0 && y > 0 && width > 0 && height > 0) {
                        if (bitmapFrame != null) {
                            val croppedBmp: Bitmap = Bitmap.createBitmap(
                                bFrame!!,
                                x.toInt(),
                                y.toInt(),
                                (widthR.toInt() - x.toInt()),
                                (height.toInt() - y.toInt())
                            )
                            val croppedLeft: Bitmap = Bitmap.createBitmap(
                                bFrame!!,
                                x.toInt(),
                                y.toInt(),
                                (width.toInt() - x.toInt()),
                                (height.toInt() - y.toInt())
                            )
                            val croppedRight: Bitmap = Bitmap.createBitmap(
                                bFrame!!,
                                xR.toInt(),
                                yR.toInt(),
                                (widthR.toInt() - xR.toInt()),
                                (heightR.toInt() - yR.toInt())
                            )

                            var scaledBitmap =
                                Bitmap.createScaledBitmap(croppedBmp, 300, 150, false)
//                            scaledBitmap = applyGrayscaleEffect(scaledBitmap)
                            val resultR = classifier.recognizeImage(croppedRight)

                            val result = classifier.recognizeImage(croppedLeft)
                            val action = when{
                                (result.get(0).title == "up" ) && (resultR.get(0).title == "up" )-> {
                                    listener.clickUpButton()
                                    end = System.currentTimeMillis()
                                    startedTyping = true

                                }
                                (result.get(0).title == "down") && (resultR.get(0).title == "down") -> {
                                     listener.clickDownButton()
                                    end = System.currentTimeMillis()
                                }
                                (result.get(0).title == "right") &&  (resultR.get(0).title == "right")-> {
                                   listener.clickLeftButton()
                                    end = System.currentTimeMillis()
                                    startedTyping = true
                                }
                                (result.get(0).title == "left")  && (resultR.get(0).title == "left")-> {
                                   listener.clickRightButton()
                                    end = System.currentTimeMillis()
                                    startedTyping = true
                                }
                                else -> {
                                    if((start - end) > 2000 && startedTyping){
                                        if (currentFrag == 'K' ){
                                            wordsList = Seq2WordsEngine.getResponse(seq)
                                            supportFragmentManager.beginTransaction().replace(R.id.frag_view, wordsFragment).commit()
                                            listener = if (wordsFragment is FragmentControler) {
                                                wordsFragment
                                            } else {
                                                throw RuntimeException(
                                                    keyBoardFragment.toString()
                                                        .toString() + " must implement FragmentAListener"
                                                )
                                            }
                                            currentFrag = 'W'
                                            end = System.currentTimeMillis()
                                        }
                                        else
                                        {

                                        }
                                    }
                                    else {

                                    }

                                }
                            }
                            testImage.setImageBitmap(scaledBitmap!!)
                        }

                    }
                }
            }
        }
    }

    private fun initClassifier() {
        println("the classifier initialized sec")
        classifier = Classifier(assets, mModelPath, mLabelPath, INPUT_SIZE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                bindCameraUseCases()
            } else {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }
    companion object {

        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

}