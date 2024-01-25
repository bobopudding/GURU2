package com.example.guru2

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.Toast
import android.widget.TextView



class GenderActivity : AppCompatActivity() {

    private lateinit var radioButtonMale: RadioButton
    private lateinit var radioButtonFemale: RadioButton
    private lateinit var buttonNext: Button
    private lateinit var nickname: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gender)

        // XML에서 라디오 버튼과 다음 버튼을 가져옴
        radioButtonMale = findViewById(R.id.radioButtonMale)
        radioButtonFemale = findViewById(R.id.radioButtonFemale)
        buttonNext = findViewById(R.id.buttonNext)

        // XML에서 라디오 버튼과 다음 버튼을 가져옴
        radioButtonMale = findViewById(R.id.radioButtonMale)
        radioButtonFemale = findViewById(R.id.radioButtonFemale)
        buttonNext = findViewById(R.id.buttonNext)

        val sharedPref = getSharedPreferences("AppPref", MODE_PRIVATE)
        nickname = sharedPref.getString("NICKNAME", "사용자") ?: "사용자"

        // "닉네임반영" 텍스트뷰의 텍스트를 변경하여 닉네임을 표시
        val textViewGenderPrompt = findViewById<TextView>(R.id.textViewGenderPrompt)
        textViewGenderPrompt.text = "'$nickname'님의 성별을 알려주세요!"

        // 라디오 버튼 클릭 이벤트 처리
        radioButtonMale.setOnClickListener {
            radioButtonFemale.isChecked = false
        }

        radioButtonFemale.setOnClickListener {
            radioButtonMale.isChecked = false
        }

        // 다음 버튼 클릭 이벤트 처리
        buttonNext.setOnClickListener {
            if (!isGenderSelected()) {
                showToast("하나를 선택해주세요")
            } else if (radioButtonMale.isChecked) {
                showToast("남성 패션은 서비스 준비 중입니다 :(")
            } else if (radioButtonFemale.isChecked) {
                // 여성 선택 시 다음 화면으로 이동
                val intent = Intent(this, TemperatureActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun isGenderSelected(): Boolean {
        return radioButtonMale.isChecked || radioButtonFemale.isChecked
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    }
}