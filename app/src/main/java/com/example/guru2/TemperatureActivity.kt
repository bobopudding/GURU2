package com.example.guru2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.widget.Button
import android.widget.RadioButton
import android.widget.Toast
import android.widget.TextView




class TemperatureActivity : AppCompatActivity() {

    private lateinit var textViewQuestionTemperature: TextView
    private lateinit var buttonNextTemperature: Button
    private lateinit var radioButtonNormal: RadioButton
    private lateinit var radioButtonHot: RadioButton
    private lateinit var radioButtonCold: RadioButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_temperature)

        textViewQuestionTemperature = findViewById(R.id.textViewQuestionTemperature)
        buttonNextTemperature = findViewById(R.id.buttonNextTemperature)
        radioButtonNormal = findViewById(R.id.radioButtonNormal)
        radioButtonHot = findViewById(R.id.radioButtonHot)
        radioButtonCold = findViewById(R.id.radioButtonCold)

        val sharedPref = getSharedPreferences("AppPref", MODE_PRIVATE)
        val nickname = sharedPref.getString("NICKNAME", "사용자") ?: "사용자"
        textViewQuestionTemperature.text = "${nickname}님! \n 평소 체감 온도는 어떤 편인가요?"


        radioButtonNormal.setOnClickListener {
            radioButtonHot.isChecked = false
            radioButtonCold.isChecked = false
        }

        radioButtonHot.setOnClickListener {
            radioButtonNormal.isChecked = false
            radioButtonCold.isChecked = false
        }

        radioButtonCold.setOnClickListener {
            radioButtonNormal.isChecked = false
            radioButtonHot.isChecked = false
        }

        buttonNextTemperature.setOnClickListener {
            if (radioButtonNormal.isChecked || radioButtonHot.isChecked || radioButtonCold.isChecked) {

                val intent = Intent(this, StyleActivity::class.java)
                startActivity(intent)
            } else {

                Toast.makeText(this, "하나를 골라주세요", Toast.LENGTH_SHORT).show()
            }
        }

    }
}