package com.example.guru2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import androidx.appcompat.app.AlertDialog
import com.example.guru2.databinding.ActivityMypageBinding
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.util.Log
import android.widget.TextView


class MypageActivity : AppCompatActivity() {

    private val PICK_IMAGE_REQUEST = 1
    private lateinit var binding: ActivityMypageBinding
    private lateinit var textNickname: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMypageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // changeprofile ImageView를 클릭하면 사진 선택 다이얼로그 표시
        binding.changeprofile.setOnClickListener {
            showConfirmationDialog()
        }

        //툴바 적용
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        val ab = supportActionBar!!
        ab.setDisplayShowTitleEnabled(false)
        ab.setDisplayHomeAsUpEnabled(true)

        val sharedPref = getSharedPreferences("AppPref", MODE_PRIVATE)
        val nickname = sharedPref.getString("NICKNAME", "사용자") ?: "사용자"
        textNickname = findViewById(R.id.textNickname)
        textNickname.text = "$nickname"

    }

    private fun showConfirmationDialog() {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("프로필 사진 변경")
        alertDialog.setMessage("선택한 사진으로 프로필을 변경하시겠습니까?")
        alertDialog.setPositiveButton("확인") { dialog, which ->
            openImagePicker()
        }
        alertDialog.setNegativeButton("취소") { dialog, which ->
            // 사용자가 취소를 누른 경우 아무 작업도 필요하지 않음
        }
        alertDialog.show()
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            val selectedImage = data.data
            val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImage)

            // 이미지 크기를 조정하여 imageProfile ImageView에 설정
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 113, 113, false)

            // 이미지를 동그라미 모양으로 자르기
            val circularBitmap = getCircularBitmap(scaledBitmap)

            binding.imageProfile.setImageBitmap(circularBitmap)

            // 사용자에게 프로필 사진 변경 완료 메시지 표시
            showProfileChangedMessage()
        }
    }

    // 비트맵을 동그라미 모양으로 자르는 함수
    private fun getCircularBitmap(bitmap: Bitmap): Bitmap {
        val outputBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(outputBitmap)

        val paint = Paint()
        val rect = Rect(0, 0, bitmap.width, bitmap.height)
        val rectF = RectF(rect)

        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = Color.BLACK
        canvas.drawOval(rectF, paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)

        // 로그 추가
        Log.d("CircularBitmap", "Bitmap converted to circular shape")


        return outputBitmap
    }

    private fun showProfileChangedMessage() {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("프로필 사진 변경 완료")
        alertDialog.setMessage("프로필 사진이 성공적으로 변경되었습니다.")
        alertDialog.setPositiveButton("확인") { dialog, which ->
            // 사용자가 확인을 누른 경우 아무 작업도 필요하지 않음
        }
        alertDialog.show()
    }


    //툴바 뒤로가기 버튼 동작
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}