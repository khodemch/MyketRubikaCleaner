package com.mch.myketrubikacleaner.activitys

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
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
    private val feedbackViewModel: FeedbackViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_feedback)

        // تغییر رنگ Status Bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.primaryColor)
        }

        val btnPositiveFeedback = binding.btnPositiveFeedback
        val btnNegativeFeedback = binding.btnNegativeFeedback
        val negativeFeedbackContainer = binding.negativeFeedbackContainer
        val btnSubmitNegativeFeedback = binding.btnSubmitNegativeFeedback
        val editTextNegativeFeedback = binding.editTextNegativeFeedback

        btnPositiveFeedback.setOnClickListener {
            openMyketRatingPage()
        }

        btnNegativeFeedback.setOnClickListener {
            negativeFeedbackContainer.visibility = View.VISIBLE
            editTextNegativeFeedback.visibility = View.VISIBLE
            btnSubmitNegativeFeedback.visibility = View.VISIBLE
        }

        btnSubmitNegativeFeedback.setOnClickListener {
            val feedbackContent = editTextNegativeFeedback.text.toString()
            if (feedbackContent.isNotEmpty()) {
                val feedback = Feedback(content = feedbackContent)
                feedbackViewModel.insert(feedback)
                sendNegativeFeedbackToServer(feedbackContent)
                negativeFeedbackContainer.visibility = View.GONE
                editTextNegativeFeedback.visibility = View.GONE
                btnSubmitNegativeFeedback.visibility = View.GONE
                editTextNegativeFeedback.text.clear()
            } else {
                editTextNegativeFeedback.error = "لطفا نظر خود را وارد کنید"
            }
        }

        feedbackViewModel.allFeedback.observe(this, Observer { feedbacks ->
            // Clear existing views or prepare for new data
            // در اینجا می‌توانید کارهای لازم برای حذف نمایشگرهای قبلی یا آماده‌سازی برای داده‌های جدید انجام دهید

            // نمایش تمام نظرات
            for (feedback in feedbacks) {
                Log.d("FeedbackActivity", "Feedback ID: ${feedback.id}, Content: ${feedback.content}")
                // اینجا می‌توانید نظرات را به نمایش بگذارید، برای نمونه می‌توانید آن‌ها را به یک لیست یا RecyclerView اضافه کنید
            }
        })
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

    private fun sendNegativeFeedbackToServer(feedback: String) {
        // کد برای ارسال نظر منفی به سرور یا ذخیره در دیتابیس
        // برای نمونه نمایش یک دیالوگ به کاربر
        AlertDialog.Builder(this)
            .setTitle("تشکر!")
            .setMessage("نظر شما با موفقیت ارسال شد.")
            .setPositiveButton("باشه") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .show()
    }

    override fun onBackPressed() {
        // بستن FeedbackActivity و بازگشت به برنامه اصلی
        super.onBackPressed()
    }
}