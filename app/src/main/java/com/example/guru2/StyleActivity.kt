package com.example.guru2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.widget.Toast
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.view.View
import android.graphics.Color

class StyleActivity : AppCompatActivity() {

    private var selectedStyle: String? = null
    private var selectedButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_style)

        // findViewById를 사용하여 뷰를 참조
        val textViewStyleQuestion = findViewById<TextView>(R.id.textViewStyleQuestion)
        val buttonNextStyle = findViewById<Button>(R.id.buttonNextStyle)
        val buttonGroupStyle = findViewById<LinearLayout>(R.id.buttonGroupStyle)

        // SharedPreferences에서 닉네임을 불러옴
        val sharedPref = getSharedPreferences("AppPref", MODE_PRIVATE)
        val nickname = sharedPref.getString("NICKNAME", "사용자") ?: "사용자"

        val buttonOfficeLook = findViewById<Button>(R.id.buttonOfficeLook)
        val buttonCasual = findViewById<Button>(R.id.buttonCasual)
        val buttonGirly = findViewById<Button>(R.id.buttonGirly)

        // 불러온 닉네임을 TextView에 설정함.
        textViewStyleQuestion.text = "${nickname}님이\n추구하시는 패션 스타일이 궁금해요!"

        // 버튼들에 대한 리스너 설정
        val buttons = listOf(buttonOfficeLook, buttonCasual, buttonGirly)
        buttons.forEach { button ->
            button.setOnClickListener {
                // 이전에 선택된 버튼의 색상을 초기 상태로 되돌립니다
                selectedButton?.setBackgroundColor(Color.parseColor("#D8D5DB"))

                // 새로 클릭된 버튼이 현재 선택된 버튼이 아니라면 새 버튼을 선택합니다
                if (selectedButton != button) {
                    button.setBackgroundColor(Color.parseColor("#88C6F7"))
                    selectedButton = button
                    selectedStyle = button.text.toString()
                } else {
                    // 이미 선택된 버튼을 다시 클릭하면 선택을 해제합니다
                    selectedButton = null
                    selectedStyle = null
                }
            }
        }

        // '다음으로' 버튼에 대한 리스너 설정
        buttonNextStyle.setOnClickListener {
            if (selectedStyle == null) {
                Toast.makeText(this, "하나를 선택해주세요", Toast.LENGTH_SHORT).show()
            } else {
                    val intent = Intent(this@StyleActivity, MainActivity::class.java)
                intent.putExtra("selectedStyle", selectedStyle)
                startActivity(intent)
                }
            }
        }
    }