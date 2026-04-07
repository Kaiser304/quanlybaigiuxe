package com.example.quanlybaigiuxe1

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.quanlybaigiuxe1.databinding.ActivityVehicleInBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.text.SimpleDateFormat
import java.util.*

class VehicleInActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVehicleInBinding
    private lateinit var db: DatabaseHelper
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val CAMERA_PERMISSION_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityVehicleInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper(this)
        updateTimeDisplay()

        // 1. Kiểm tra quyền và tự động bật camera ngay lập tức
        if (checkPermission()) {
            startCamera()
        } else {
            requestPermission()
        }

        binding.btnBack.setOnClickListener { finish() }

        binding.btnScan.setOnClickListener {
            startCamera() // Cho phép khởi động lại nếu có lag
            Toast.makeText(this, "Đang khởi động lại Camera...", Toast.LENGTH_SHORT).show()
        }

        binding.btnLuu.setOnClickListener { saveVehicleData() }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Thiết lập Preview
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }

            // Thiết lập Bộ phân tích hình ảnh (OCR)
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy ->
                        processImageProxy(imageProxy)
                    }
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalyzer)
            } catch (e: Exception) {
                Log.e("CameraX", "Không thể khởi động camera: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    for (block in visionText.textBlocks) {
                        val textRaw = block.text.uppercase().replace(Regex("[^A-Z0-9]"), "")
                        val regex = Regex("[0-9]{2}[A-Z]{1,2}[0-9]{4,6}")

                        if (regex.containsMatchIn(textRaw)) {
                            val plate = regex.find(textRaw)?.value ?: ""
                            runOnUiThread {
                                // Chỉ cập nhật nếu biển số mới khác biển số cũ để tránh nháy màn hình
                                if (binding.edtBienSo.text.toString() != plate) {
                                    binding.edtBienSo.setText(plate)
                                }
                            }
                            // Không gọi stopCamera() để camera luôn trong tư thế sẵn sàng
                        }
                    }
                }
                .addOnCompleteListener { imageProxy.close() }
        } else {
            imageProxy.close()
        }
    }

    // --- CÁC HÀM HỖ TRỢ ---

    private fun checkPermission() = ContextCompat.checkSelfPermission(
        this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        }
    }

    private fun updateTimeDisplay() {
        val currentTime = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date())
        binding.txtTimeIn.text = "Thời gian vào: $currentTime"
    }

    private fun saveVehicleData() {
        val plate = binding.edtBienSo.text.toString().trim()
        val type = if (binding.rbXeMay.isChecked) "Xe máy" else "Ô tô"
        if (plate.isEmpty()) {
            Toast.makeText(this, "Chưa nhận diện được biển số!", Toast.LENGTH_SHORT).show()
            return
        }
        val timeIn = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date())
        if (db.insertVehicle(plate, type, timeIn, "IN") != -1L) {
            Toast.makeText(this, "Đã lưu xe: $plate", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}