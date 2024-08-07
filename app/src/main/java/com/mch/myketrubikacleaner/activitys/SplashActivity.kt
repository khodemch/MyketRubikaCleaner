package com.mch.myketrubikacleaner.activitys

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.mch.myketrubikacleaner.R

class SplashActivity : AppCompatActivity() {
    private lateinit var textViewVersion: TextView
    private lateinit var logoImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // تغییر رنگ Status Bar
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.primaryColor)
        }

        logoImageView = findViewById(R.id.logo)
        textViewVersion = findViewById(R.id.showVersionApp)

        // نمایش نسخه برنامه
        displayVersionInfo()

        // شروع انیمیشن
        startAnimation()
    }

    override fun onResume() {
        super.onResume()

        // بررسی اتصال به اینترنت و VPN
        if (!NetworkUtil.isNetworkAvailable(this)) {
            val intent = Intent(this, NoInternetActivity::class.java)
            startActivity(intent)
            finish()
        } else if (NetworkUtil.isUsingVPN(this)) {
            proceedToMain()
        } else {
            proceedToMain()
        }
    }

    private fun proceedToMain() {
        Handler().postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 3000) // مدت زمان نمایش splash screen
    }

    private fun getVersionInfo(): Pair<String, Int> {
        var versionName = ""
        var versionCode = -1

        try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            versionName = packageInfo.versionName
            versionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toInt()
            } else {
                packageInfo.versionCode
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        return Pair(versionName, versionCode)
    }

    private fun displayVersionInfo() {
        val (versionName) = getVersionInfo()
        val versionText = "نسخه: $versionName\n"

        textViewVersion.text = versionText
    }


    private fun startAnimation() {
        // ترجمه در محور Y
        val translateY = ObjectAnimator.ofFloat(logoImageView, "translationY", -200f, 0f)

        // چرخش
        val rotate = ObjectAnimator.ofFloat(logoImageView, "rotation", 0f, 360f)

        // بزرگ و کوچک شدن
        val scaleX = ObjectAnimator.ofFloat(logoImageView, "scaleX", 0.5f, 1.5f, 1.0f)
        val scaleY = ObjectAnimator.ofFloat(logoImageView, "scaleY", 0.5f, 1.5f, 1.0f)

        // محو شدن و ظاهر شدن
        val fadeOut = ObjectAnimator.ofFloat(logoImageView, "alpha", 1.0f, 0.0f)
        val fadeIn = ObjectAnimator.ofFloat(logoImageView, "alpha", 0.0f, 1.0f)

        // ترکیب انیمیشن‌ها
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(translateY, rotate, scaleX, scaleY, fadeOut, fadeIn)
        animatorSet.duration = 2000
        animatorSet.interpolator = AccelerateDecelerateInterpolator()
        animatorSet.start()
    }
}
