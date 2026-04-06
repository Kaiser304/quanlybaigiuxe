package com.example.quanlybaigiuxe1

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.quanlybaigiuxe1.databinding.ActivityLayxeBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.text.SimpleDateFormat
import java.util.*

class LayxeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLayxeBinding
    private lateinit var dbHelper: DatabaseHelper

    private var finalPrice: Int = 0
    private var finalTimeOut: String = ""
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLayxeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)

        // 1. TỰ ĐỘNG MỞ CAMERA NGAY KHI VÀO MÀN HÌNH
        startCamera()

        binding.btnBack.setOnClickListener { finish() }

        // Nút quét lại (phòng trường hợp người dùng lỡ tay đóng camera)
        binding.btnScan.setOnClickListener {
            startCamera()
        }

        binding.btnThanhToan.setOnClickListener {
            val currentPlate = binding.tvBienSo.text.toString()
            // Kiểm tra xem đã quét được biển số hợp lệ chưa
            if (currentPlate.isNotEmpty() && currentPlate != "QUÉT BIỂN SỐ" && finalPrice > 0) {
                thanhToanXeRa(currentPlate)
            } else {
                Toast.makeText(this, "Vui lòng quét biển số xe trong bãi trước!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // region --- QUẢN LÝ CAMERA ---

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
                binding.viewFinder.visibility = View.VISIBLE
            } catch (e: Exception) {
                Log.e("CameraX", "Lỗi khởi động: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun stopCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        val cameraProvider = cameraProviderFuture.get()
        cameraProvider.unbindAll()
        binding.viewFinder.visibility = View.GONE
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    for (block in visionText.textBlocks) {
                        // Làm sạch chuỗi: CHỈ lấy Chữ và Số (Xóa dấu cách, gạch ngang, chấm)
                        val textRaw = block.text.uppercase().replace(Regex("[^A-Z0-9]"), "")

                        // Regex linh hoạt: 2 số - 1 hoặc 2 chữ - 4 đến 6 số
                        val regex = Regex("[0-9]{2}[A-Z]{1,2}[0-9]{4,6}")

                        if (regex.containsMatchIn(textRaw)) {
                            val plate = regex.find(textRaw)?.value ?: ""

                            // KIỂM TRA TRONG DATABASE: Có xe này đang gửi không?
                            val ticket = dbHelper.getTicketByPlate(plate)

                            if (ticket != null) {
                                // TÌM THẤY -> Cập nhật UI và dừng Camera
                                runOnUiThread {
                                    displayVehicleInfo(ticket)
                                    stopCamera()
                                }
                                break
                            }
                            // Nếu không thấy ticket, camera tiếp tục quét khung hình tiếp theo tự động
                        }
                    }
                }
                .addOnCompleteListener { imageProxy.close() }
        } else {
            imageProxy.close()
        }
    }
    // endregion

    // region --- XỬ LÝ NGHIỆP VỤ ---

    private fun displayVehicleInfo(ticket: Ticket) {
        binding.tvBienSo.text = ticket.plate
        binding.tvGioVao.text = ticket.time_in
        binding.tvLoaiXe.text = ticket.type

        val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
        val dateRa = Date()
        finalTimeOut = sdf.format(dateRa)
        binding.tvGioRa.text = finalTimeOut

        try {
            val dateVao = sdf.parse(ticket.time_in)!!
            val diff = dateRa.time - dateVao.time
            val diffMinutes = diff / (1000 * 60)

            // Hiển thị tổng thời gian gửi
            binding.tvTongThoiGian.text = "${diffMinutes / 60}h ${diffMinutes % 60}m"

            // Tính tiền: Làm tròn lên theo giờ (Dưới 1 tiếng tính 1 tiếng)
            val hoursToCharge = if (diffMinutes < 60) 1 else Math.ceil(diffMinutes / 60.0).toLong()
            val pricePerUnit = if(ticket.type == "Xe máy") 5000 else 15000
            finalPrice = (hoursToCharge * pricePerUnit).toInt()

            // Hiển thị giá tiền định dạng 10,000 VNĐ
            binding.tvGiaTien.text = String.format("%,d VNĐ", finalPrice)
        } catch (e: Exception) {
            Log.e("Calculation", "Lỗi tính tiền: ${e.message}")
            binding.tvGiaTien.text = "Lỗi tính toán"
        }
    }

    private fun thanhToanXeRa(bienSo: String) {
        val success = dbHelper.updateStatusXeRa(bienSo, finalTimeOut, finalPrice)
        if (success) {
            Toast.makeText(this, "Thanh toán thành công: ${finalPrice} VNĐ", Toast.LENGTH_SHORT).show()
            finish() // Quay lại màn hình chính
        } else {
            Toast.makeText(this, "Lỗi cập nhật dữ liệu!", Toast.LENGTH_SHORT).show()
        }
    }
    // endregion
}