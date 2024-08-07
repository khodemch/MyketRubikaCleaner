package com.mch.myketrubikacleaner.activitys

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.adivery.sdk.Adivery
import com.adivery.sdk.AdiveryAdListener
import com.adivery.sdk.AdiveryBannerAdView
import com.mch.myketrubikacleaner.R
import com.mch.myketrubikacleaner.adapters.FileListAdapter
import com.mch.myketrubikacleaner.databinding.ActivityBigFilesBinding
import java.io.File

class BigFilesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBigFilesBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var deleteButton: Button
    private lateinit var fileListAdapter: FileListAdapter
    private val fileList: MutableList<File> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Adivery.configure(getApplication(), "471bde9b-e371-4bfa-adc9-56fe9ccac377")
        binding = DataBindingUtil.setContentView(this , R.layout.activity_big_files)

        // پیکربندی Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // حذف عنوان پیش‌فرض

        // تنظیم آیکون بازگشت
        val backIcon = binding.backIcon
        backIcon.setOnClickListener {
            finish() // بازگشت به صفحه قبلی
        }

        val bannerAd = findViewById<AdiveryBannerAdView>(R.id.banner_ad)
        Log.d("ImageActivity", "Banner Ad View initialized: $bannerAd")

        if (bannerAd != null) {
            bannerAd.setBannerAdListener(object : AdiveryAdListener() {
                override fun onAdLoaded() {
                    Log.d("ImageActivity", "Ad loaded successfully")
                }

                override fun onError(reason: String) {
                    Log.e("ImageActivity", "Ad failed to load: $reason")
                }

                override fun onAdClicked() {
                    Log.d("ImageActivity", "Ad clicked")
                }
            })

            bannerAd.loadAd()
            Log.d("ImageActivity", "Banner Ad loadAd called")
        } else {
            Log.e("ImageActivity", "AdiveryBannerAdView is null!")
        }

        // Change status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.primaryColor)
        }

        recyclerView = findViewById(R.id.recycler_view)
        deleteButton = findViewById(R.id.btn_delete_selected)

        fileListAdapter = FileListAdapter(this, fileList) { file, isSelected ->
            checkEmptyState()
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = fileListAdapter

        deleteButton.setOnClickListener {
            val selectedFilesSet = fileListAdapter.getSelectedFiles() // فرض بر این است که نوع این Set<File> است
            val selectedFilesList = selectedFilesSet.toList() // تبدیل Set<File> به List<File>

            if (selectedFilesList.isEmpty()) {
                Toast.makeText(this, "فایلی برای حذف کردن انتخاب نشده است.", Toast.LENGTH_SHORT).show()
            } else {
                showDeleteConfirmationDialog(selectedFilesList) // ارسال List<File> به دیالوگ
            }
        }

        requestPermissions()
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissions = mutableListOf<String>()

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            }

            if (permissions.isNotEmpty()) {
                ActivityCompat.requestPermissions(this, permissions.toTypedArray(), 1)
            } else {
                loadFiles()
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
            } else {
                loadFiles()
            }
        }
    }

    private fun loadFiles() {
        fileList.clear()

        val folders = listOf(
            File(Environment.getExternalStorageDirectory(), "/Rubika/Pictures"),
            File(Environment.getExternalStorageDirectory(), "/Download"),
            File(Environment.getExternalStorageDirectory(), "/Pictures"),
            File(Environment.getExternalStorageDirectory(), "/Music"),
            File(Environment.getExternalStorageDirectory(), "/Download/Rubika"),
            File(Environment.getExternalStorageDirectory(), "/Movies/Rubika"),
            File(Environment.getExternalStorageDirectory(), "/Rubika Downloder"),
            File(Environment.getExternalStorageDirectory(), "/Pictures/Rubika"),
            File(Environment.getExternalStorageDirectory(), "/Rubika/Images"),
            File(Environment.getExternalStorageDirectory(), "/Rubika/Rubika Images"),
            File(Environment.getExternalStorageDirectory(), "/Download/Downloader"),
            File(Environment.getExternalStorageDirectory(), "/روبیکا دانلودر"),
            File(Environment.getExternalStorageDirectory(), "/Download/Rubika Downloader"),
            File(Environment.getExternalStorageDirectory(), "/Download/Rubino Downloader"),
            File(Environment.getExternalStorageDirectory(), "/Download/RubinoPro"),
            File(Environment.getExternalStorageDirectory(), "/Download/StatusSaver/Rubika"),
            File(Environment.getExternalStorageDirectory(), "/RubikaDownloaders"),
            File(Environment.getExternalStorageDirectory(), "/Download/روبیکا روبینو دانلودر/Images"),
            File(Environment.getExternalStorageDirectory(), "/RubikaRubinoDownloader"),
            File(Environment.getExternalStorageDirectory(), "/Download/RubinoDownloader"),
            File(Environment.getExternalStorageDirectory(), "/RubinoDownloader"),
            File(Environment.getExternalStorageDirectory(), "/RubinoDownloaders"),
            File(Environment.getExternalStorageDirectory(), "/RubinoDownloaders/Images"),
            File(Environment.getExternalStorageDirectory(), "/Download/RubinoDownloader/Images"),
            File(Environment.getExternalStorageDirectory(), "/Download/RubinoDownloaders/Images"),
            File(Environment.getExternalStorageDirectory(), "/روبینو دانلودر"),
            File(Environment.getExternalStorageDirectory(), "/روبیکا روبینو دانلودر"),
            File(Environment.getExternalStorageDirectory(), "/روبینو دانلودر/Images"),
            File(Environment.getExternalStorageDirectory(), "/Rubika Downloaders"),
            File(Environment.getExternalStorageDirectory(), "/Download/Rubika Downloaders")
        )

        folders.forEach { folder ->
            findLargeFiles(folder)
        }

        // مرتب‌سازی فایل‌ها بر اساس اندازه
        fileList.sortByDescending { it.length() }

        fileListAdapter.notifyDataSetChanged()
        checkEmptyState()
    }

    private fun findLargeFiles(dir: File) {
        if (dir.exists() && dir.isDirectory) {
            dir.listFiles()?.forEach { file ->
                if (file.isFile && file.length() > 1 * 1024 * 1024) {
                    fileList.add(file)
                }
            }
        }
    }

    private fun deleteSelectedFiles(selectedFiles: List<File>) {
        if (selectedFiles.isNotEmpty()) {
            selectedFiles.forEach { file ->
                if (file.exists()) {
                    file.delete()
                    fileList.remove(file)
                }
            }
            fileListAdapter.notifyDataSetChanged()
            checkEmptyState()
            Toast.makeText(this, "فایل‌های انتخاب شده با موفقیت حذف شدند.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkEmptyState() {
        val noBigFileTextView = findViewById<TextView>(R.id.noBigFileTextView)
        if (fileList.isEmpty()) {
            recyclerView.visibility = View.GONE
            noBigFileTextView.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            noBigFileTextView.visibility = View.INVISIBLE
        }
    }

    private fun showDeleteConfirmationDialog(selectedFiles: List<File>) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.custom_alert_dialog, null)
        val builder = AlertDialog.Builder(this, R.style.YourAlertDialogTheme).apply {
            setView(dialogView)
        }

        // Initialize views from custom dialog layout
        val title = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val message = dialogView.findViewById<TextView>(R.id.dialogMessage)
        val positiveButton = dialogView.findViewById<Button>(R.id.btnPositive)
        val negativeButton = dialogView.findViewById<Button>(R.id.btnNegative)

        // Set dialog content
        title.text = "حذف فایل‌های انتخاب شده"
        message.text = "آیا مطمئن هستید که می‌خواهید فایل‌های انتخاب شده را حذف کنید؟"

        // Create the dialog
        val dialog = builder.create()

        // Handle button clicks
        positiveButton.setOnClickListener {
            // فقط اگر فایل‌هایی انتخاب شده باشد، عملیات حذف انجام می‌شود
            if (selectedFiles.isNotEmpty()) {
                deleteSelectedFiles(selectedFiles)
            }
            dialog.dismiss()
        }

        negativeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadFiles()
        } else {
            Toast.makeText(this, "دسترسی به فایل‌ها داده نشد.", Toast.LENGTH_SHORT).show()
        }
    }
}

