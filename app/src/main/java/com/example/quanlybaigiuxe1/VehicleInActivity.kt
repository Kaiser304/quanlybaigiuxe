package com.example.quanlybaigiuxe1

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.quanlybaigiuxe1.databinding.ActivityVehicleInBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.text.SimpleDateFormat
import java.util.*

class VehicleInActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVehicleInBinding
    private lateinit var db: DatabaseHelper // Đã đổi tên 'db' cho gọn nhưng vẫn giữ logic khởi tạo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        binding = ActivityVehicleInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper(this) // Khởi tạo để hết lỗi Unresolved reference 'db'

        updateTimeDisplay()

        // Sự kiện quay lại
        binding.btnBack.setOnClickListener { finish() }

        // Sự kiện bấm nút Quét biển số
        binding.btnScan.setOnClickListener {
            startCamera()
        }

        // Sự kiện Lưu dữ liệu
        binding.btnLuu.setOnClickListener {
            saveVehicleData()
        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
            != android.content.pm.PackageManager.PERMISSION_GRANTED) {

            // Nếu chưa có quyền, thì yêu cầu người dùng cấp quyền
            androidx.core.app.ActivityCompat.requestPermissions(
                this, arrayOf(android.Manifest.permission.CAMERA), 101
            )
        }
    }

    private fun updateTimeDisplay() {
        val currentTime = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date())
        binding.txtTimeIn.text = "Thời gian vào: $currentTime"
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }

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
                binding.viewFinder.visibility = View.VISIBLE // Hiện khung camera đã thêm trong XML
            } catch (e: Exception) {
                Log.e("CameraX", "Lỗi: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun stopCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        val cameraProvider = cameraProviderFuture.get()
        cameraProvider.unbindAll()
        binding.viewFinder.visibility = View.GONE // Ẩn camera sau khi quét xong
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    for (block in visionText.textBlocks) {
                        val textRaw = block.text.uppercase().replace(" ", "").replace("-", "").replace(".", "")

                        // Regex bóc tách biển số Việt Nam (VD: 67K112345)
                        val regex = Regex("[0-9]{2}[A-Z][0-9]{1,2}[0-9]{4,5}")
                        if (regex.containsMatchIn(textRaw)) {
                            val plate = regex.find(textRaw)?.value ?: ""
                            binding.edtBienSo.setText(plate)
                            stopCamera()
                            break
                        }
                    }
                }
                .addOnCompleteListener { imageProxy.close() }
        }
    }

    private fun saveVehicleData() {
        val plate = binding.edtBienSo.text.toString().trim()

        // Sửa lỗi rbXeMay: Kiểm tra RadioButton thay vì lấy text từ EditText cũ
        val type = if (binding.rbXeMay.isChecked) "Xe máy" else "Ô tô"

        if (plate.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập hoặc quét biển số", Toast.LENGTH_SHORT).show()
            return
        }

        val timeIn = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date())
        val result = db.insertVehicle(plate, type, timeIn, "IN")

        if (result != -1L) {
            Toast.makeText(this, "Gửi xe thành công!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Lỗi lưu dữ liệu!", Toast.LENGTH_SHORT).show()
        }
    }
}