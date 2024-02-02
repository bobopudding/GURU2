package com.example.guru2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import android.app.AlertDialog

class NicknameActivity : AppCompatActivity() {

    private lateinit var editTextNickname: EditText
    private lateinit var buttonConfirm: Button
    private lateinit var charCountTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nickname)

        editTextNickname = findViewById(R.id.editTextNickname)
        buttonConfirm = findViewById(R.id.buttonConfirm)
        charCountTextView = findViewById(R.id.charCountTextView)

        // 문자 수를 '0/20'으로 초기화
        charCountTextView.text = "0/20"

        // 사용자가 입력할 때마다 문자 수를 업데이트
        editTextNickname.addTextChangedListener {
            val text = editTextNickname.text.toString()
            val charCount = text.length
            charCountTextView.text = "$charCount/20"
        }

        buttonConfirm.setOnClickListener {
            val nickname = editTextNickname.text.toString().trim()

            if (nickname.isEmpty()) {
                Toast.makeText(this, "닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show()
            } else if (nickname.length > 20) {
                Toast.makeText(this, "20자를 초과했습니다.", Toast.LENGTH_SHORT).show()
            }  else if (DBHelper(this).isWordInDatabase(nickname, this)) {

                // 데이터베이스에 있는 단어를 입력한 경우
                Toast.makeText(this, "올바른 단어를 사용해주세요. :)", Toast.LENGTH_SHORT).show()
            } else {
                // 데이터베이스에 없는 단어를 입력한 경우
                AlertDialog.Builder(this).apply {
                    setMessage("\"$nickname\" 님이 맞습니까?")
                    setPositiveButton("맞아요") { _, _ ->
                        saveNickname(nickname)

                        // 닉네임을 StyleActivity로 전달
                        val styleIntent = Intent(this@NicknameActivity, StyleActivity::class.java)
                        styleIntent.putExtra("NICKNAME", nickname)
                        startActivity(styleIntent)

                        // 닉네임을 MypageActivity로 전달
                        val mypageIntent = Intent(this@NicknameActivity, MypageActivity::class.java)
                        mypageIntent.putExtra("NICKNAME", nickname)

                    }
                    setNegativeButton("변경할래요") { dialog, _ ->
                        dialog.dismiss()
                    }
                    show()
                }
            }
        }
    }

    private fun saveNickname(nickname: String) {
        // 닉네임을 'AppPref' 이름의 SharedPreferences에 저장
        val sharedPref = this.getSharedPreferences("AppPref", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("NICKNAME", nickname)
            apply()
        }
    }
}