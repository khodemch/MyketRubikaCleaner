package com.mch.myketrubikacleaner.activitys

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.mch.myketrubikacleaner.database.Feedback
import com.mch.myketrubikacleaner.database.FeedbackViewModel
import com.mch.myketrubikacleaner.R
import com.mch.myketrubikacleaner.databinding.ActivityFeedbackBinding

class FeedbackActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFeedbackBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_feedback)

        // تغییر رنگ Status Bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.primaryColor)
        }

        val btnPositiveFeedback = binding.btnPositiveFeedback
        val btnNegativeFeedback = binding.btnNegativeFeedback
        val exitButton = binding.exitButton

        btnPositiveFeedback.setOnClickListener {
            openMyketRatingPage()
        }

        btnNegativeFeedback.setOnClickListener {
            showSupportDialog()
        }

        exitButton.setOnClickListener {
            finish()
        }
    }

    private fun openMyketRatingPage() {
        val packageName = "com.mch.myketrubikacleaner" // جایگزین با نام بسته‌ی برنامه‌ی خود
        val url = "myket://comment?id=$packageName"
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
        }

        // بررسی وجود مایکت روی دستگاه
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            // نمایش پیغام خطا یا دیالوگ برای نصب مایکت
            showInstallMyketDialog()
        }
    }

    private fun showInstallMyketDialog() {
        // نمایش دیالوگ برای نصب مایکت
        AlertDialog.Builder(this)
            .setTitle("نصب مایکت")
            .setMessage("برای امتیازدهی و ارسال نظر، لطفاً مایکت را نصب کنید.")
            .setPositiveButton("دانلود") { _, _ ->
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://myket.ir")
                }
                startActivity(intent)
            }
            .setNegativeButton("بستن") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showSupportDialog() {
        // Inflate custom dialog layout
        val dialogView = LayoutInflater.from(this).inflate(R.layout.custom_support_dialog, null)
        val dialog = AlertDialog.Builder(this, R.style.YourAlertDialogTheme).apply {
            setView(dialogView)
        }.create()

        // Initialize views from custom dialog layout
        val title = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val btnWhatsApp = dialogView.findViewById<Button>(R.id.btnWhatsApp)
        val btnTelegram = dialogView.findViewById<Button>(R.id.btnTelegram)

        // Set title and message
        title.text = "کاربر عزیز 🌹💚 اگر مسئله ایی هنگام کار با برنامه داری یا انتقاد و پیشنهادی داری حتما بهم پیام بده. با تشکر"

        // Handle button clicks
        btnWhatsApp.setOnClickListener {
            openWhatsApp()
            dialog.dismiss()
        }

        btnTelegram.setOnClickListener {
            openTelegram()
            dialog.dismiss()
        }

        // Set dialog view and show
        dialog.setView(dialogView)
        dialog.show()
    }

    private fun openWhatsApp() {
        val contact = "989903076472" // شماره تلفن یا آیدی واتساپ شما
        val whatsappUri = "https://wa.me/$contact"

        // ایجاد Intent برای باز کردن واتساپ
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(whatsappUri))

        // بررسی نصب بودن واتساپ
        val packageManager = packageManager
        val resolveInfoList =
            packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)

        var isWhatsAppInstalled = false
        for (resolveInfo in resolveInfoList) {
            if (resolveInfo.activityInfo.packageName == "com.whatsapp" ||
                resolveInfo.activityInfo.packageName == "com.whatsapp.w4b" || // WhatsApp Business
                resolveInfo.activityInfo.packageName == "com.gbwhatsapp"
            ) {  // GB WhatsApp
                isWhatsAppInstalled = true
                intent.setPackage(resolveInfo.activityInfo.packageName)
                break
            }
        }

        if (isWhatsAppInstalled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // اندروید 12 به بالا
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // اندروید 10 تا 12
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            } else {
                // اندروید 10 به پایین
                // تنظیمات خاص اگر نیاز باشد
            }
            startActivity(intent)
        } else {
            // اگر واتساپ نصب نیست، باز کردن لینک واتساپ وب
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(whatsappUri))
            startActivity(webIntent)
        }
    }


    private fun openTelegram() {
        val username = "RubikaCleanerSupport" // نام کاربری تلگرام شما
        val telegramUri = "tg://resolve?domain=$username"
        val webUri = "https://t.me/$username"

        // ایجاد Intent برای باز کردن تلگرام
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(telegramUri))

        // بررسی نصب بودن تلگرام
        val packageManager = packageManager
        val resolveInfoList =
            packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)

        var isTelegramInstalled = false
        for (resolveInfo in resolveInfoList) {
            if (resolveInfo.activityInfo.packageName == "org.telegram.messenger" ||
                resolveInfo.activityInfo.packageName == "org.telegram.messenger.web" ||
                resolveInfo.activityInfo.packageName == "org.telegram.plus" ||
                resolveInfo.activityInfo.packageName == "org.telegram.messenger.beta" ||
                resolveInfo.activityInfo.packageName == "org.telegram.messenger.wear"
            ) {
                isTelegramInstalled = true
                intent.setPackage(resolveInfo.activityInfo.packageName)
                break
            }
        }

        if (isTelegramInstalled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // اندروید 12 به بالا
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // اندروید 10 تا 12
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            } else {
                // اندروید 10 به پایین
                // تنظیمات خاص اگر نیاز باشد
            }
            startActivity(intent)
        } else {
            // اگر تلگرام نصب نیست، باز کردن وبسایت تلگرام
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUri))
            startActivity(webIntent)
        }
    }

    override fun onBackPressed() {
        // بستن FeedbackActivity و بازگشت به برنامه اصلی
        super.onBackPressed()
    }
}