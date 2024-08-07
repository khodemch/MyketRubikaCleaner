package com.mch.myketrubikacleaner.activitys

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.mch.myketrubikacleaner.R

class NoInternetActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_no_internet)

        // تغییر رنگ Status Bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.primaryColor)
        }

        val retryButton: Button = findViewById(R.id.retryButton)
        retryButton.setOnClickListener {
            if (NetworkUtil.isNetworkAvailable(this)) {
                Toast.makeText(this, "اینترنت متصل است", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, SplashActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "اینترنت متصل نیست", Toast.LENGTH_SHORT).show()
            }
        }
    }
}