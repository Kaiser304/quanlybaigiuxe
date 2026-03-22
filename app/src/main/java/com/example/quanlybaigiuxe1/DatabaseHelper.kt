package com.example.quanlybaigiuxe1

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.content.ContentValues

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "ParkingDB", null, 2) { // Tăng version lên 2 để cập nhật cấu trúc mới

    override fun onCreate(db: SQLiteDatabase) {

        // 1. Bảng User
        val createUserTable = """
            CREATE TABLE User (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE,
                password TEXT,
                fullname TEXT
            )
        """.trimIndent()

        // 2. Bảng Xe
        // Thêm status để biết xe nào đang ở trong bãi (1) hay đã ra (0)
        val createVehicleTable = """
            CREATE TABLE Vehicle (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                plate TEXT,
                type TEXT, -- "Xe máy" hoặc "Ô tô"
                status INTEGER DEFAULT 1 
            )
        """.trimIndent()

        // 3. Bảng Vé gửi xe
        // Thêm 'type' để biết loại xe khi tính tiền và 'status' để lọc xe chưa ra
        val createTicketTable = """
            CREATE TABLE Ticket (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                plate TEXT,
                type TEXT,          -- Cần loại xe để áp giá tiền khác nhau
                time_in TEXT,       -- Định dạng: YYYY-MM-DD HH:MM:SS
                time_out TEXT,      -- Để trống khi xe chưa ra
                price INTEGER DEFAULT 0,
                status INTEGER DEFAULT 1 -- 1: Đang gửi, 0: Đã thanh toán và ra khỏi bãi
            )
        """.trimIndent()

        db.execSQL(createUserTable)
        db.execSQL(createVehicleTable)
        db.execSQL(createTicketTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Xóa bảng cũ và tạo lại khi nâng cấp version
        db.execSQL("DROP TABLE IF EXISTS User")
        db.execSQL("DROP TABLE IF EXISTS Vehicle")
        db.execSQL("DROP TABLE IF EXISTS Ticket")
        onCreate(db)
    }

    fun getTicketByPlate(plate: String): Ticket? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Ticket WHERE plate = ? AND status = 1", arrayOf(plate))
        var ticket: Ticket? = null

        if (cursor.moveToFirst()) {
            // Lấy chỉ số cột an toàn
            val idIdx = cursor.getColumnIndex("id")
            val plateIdx = cursor.getColumnIndex("plate")
            val typeIdx = cursor.getColumnIndex("type")
            val timeInIdx = cursor.getColumnIndex("time_in")
            val timeOutIdx = cursor.getColumnIndex("time_out")
            val priceIdx = cursor.getColumnIndex("price")
            val statusIdx = cursor.getColumnIndex("status")

            // Truyền đầy đủ các tham số vào constructor của Ticket
            ticket = Ticket(
                id = cursor.getInt(idIdx),
                plate = cursor.getString(plateIdx) ?: "",
                type = cursor.getString(typeIdx) ?: "",       // Thêm giá trị cho type
                time_in = cursor.getString(timeInIdx) ?: "",
                time_out = cursor.getString(timeOutIdx) ?: "", // Thêm giá trị cho time_out
                price = cursor.getInt(priceIdx),              // Thêm giá trị cho price
                status = cursor.getInt(statusIdx)
            )
        }
        cursor.close()
        return ticket
    }
    fun updateStatusXeRa(plate: String, timeOut: String, price: Int): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()

        values.put("status", 0)      // 0 là đã lấy xe ra
        values.put("time_out", timeOut) // Cập nhật giờ xe ra thực tế
        values.put("price", price)      // Lưu số tiền đã tính toán được

        // Cập nhật đúng xe có biển số này và đang ở trong bãi (status = 1)
        val result = db.update("Ticket", values, "plate = ? AND status = 1", arrayOf(plate))
        return result > 0
    }
    fun insertSampleTicket(plate: String): Long {
        val db = this.writableDatabase
        val values = ContentValues()

        // Lấy thời gian hiện tại đúng định dạng để LayxeActivity parse được
        val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
        val currentTime = sdf.format(Date())

        values.put("plate", plate)         // Cột biển số
        values.put("time_in", currentTime) // Cột giờ vào
        values.put("status", 1)            // 1 = Xe đang ở trong bãi

        // Chèn vào bảng Ticket
        return db.insert("Ticket", null, values)
    }
    // hàm đếm số chỗ trống còn lại trong bãi
    fun getOccupiedCountByType(vehicleType: String): Int {
        val db = this.readableDatabase
        // Truy vấn đếm xe dựa trên loại xe và trạng thái đang trong bãi
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM Ticket WHERE type = ? AND status = 1",
            arrayOf(vehicleType)
        )
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        return count
    }
    fun insertVehicle(plate: String, type: String, timeIn: String, cardId: String, statusString: String): Long {
        val db = this.writableDatabase
        val values = ContentValues()

        // Chuyển đổi trạng thái từ chữ "IN" sang số 1 (đang ở trong bãi)
        val statusCode = if (statusString == "IN") 1 else 0

        values.put("plate", plate)         // Cột biển số
        values.put("type", type)           // Cột loại xe (Xe máy/Ô tô)
        values.put("time_in", timeIn)      // Cột giờ vào
        values.put("status", statusCode)   // Cột trạng thái (1 = Trong bãi)
        values.put("price", 0)             // Mặc định giá tiền là 0 khi mới vào
        values.put("time_out", "")         // Chưa có giờ ra

        // Lưu ý: Nếu database của bạn có cột cardId, hãy thêm dòng dưới đây:
        // values.put("card_id", cardId)

        // Chèn dữ liệu vào bảng Ticket
        return db.insert("Ticket", null, values)
    }
}