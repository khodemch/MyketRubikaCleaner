package com.mch.myketrubikacleaner.activitys

import android.os.Build
import android.os.Bundle
import com.adivery.sdk.*
import android.os.Environment
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.mch.myketrubikacleaner.adapters.FilesAdapter
import com.mch.myketrubikacleaner.models.FilesModel
import com.mch.myketrubikacleaner.R
import com.mch.myketrubikacleaner.databinding.ActivityFilesBinding
import java.io.File

class FilesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFilesBinding
    private lateinit var filesAdapter: FilesAdapter
    private val filesList: MutableList<FilesModel> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Adivery.configure(getApplication(), "61ab9370-74fd-4042-8b8d-ccab3cf5f991")
        binding = DataBindingUtil.setContentView(this, R.layout.activity_files)

        // پیکربندی Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // حذف عنوان پیش‌فرض

        // تنظیم آیکون بازگشت
        val backIcon = binding.backIcon
        backIcon.setOnClickListener {
            finish() // بازگشت به صفحه قبلی
        }

        val bannerAd = findViewById<AdiveryBannerAdView>(R.id.banner_ad)
        Log.d("FilesActivity", "Banner Ad View initialized: $bannerAd")

        if (bannerAd != null) {
            bannerAd.setBannerAdListener(object : AdiveryAdListener() {
                override fun onAdLoaded() {
                    Log.d("FilesActivity", "Ad loaded successfully")
                }

                override fun onError(reason: String) {
                    Log.e("FilesActivity", "Ad failed to load: $reason")
                }

                override fun onAdClicked() {
                    Log.d("FilesActivity", "Ad clicked")
                }
            })

            bannerAd.loadAd()
            Log.d("FilesActivity", "Banner Ad loadAd called")
        } else {
            Log.e("FilesActivity", "AdiveryBannerAdView is null!")
        }

        // تغییر رنگ Status Bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.primaryColor)
        }

        // Setup RecyclerView and Adapter
        filesAdapter = FilesAdapter(this, filesList)
        binding.filesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.filesRecyclerView.adapter = filesAdapter

        loadAllFiles()
    }

    private fun loadAllFiles() {
        // Define directories to search
        val directories = listOf(
            File(Environment.getExternalStorageDirectory(), "/Rubika/Documents") ,
            File(Environment.getExternalStorageDirectory(), "/Download") ,
            File(Environment.getExternalStorageDirectory(), "/Download/Rubika") ,
            File(Environment.getExternalStorageDirectory(), "/Rubika Downloder") ,
            File(Environment.getExternalStorageDirectory(), "/Rubika/Rubika Documents"),
            File(Environment.getExternalStorageDirectory(), "/Download/Downloader"),
            File(Environment.getExternalStorageDirectory(), "/روبیکا دانلودر"),
            File(Environment.getExternalStorageDirectory(), "/Download/Rubika Downloader"),
            File(Environment.getExternalStorageDirectory(), "/Download/Rubino Downloader"),
            File(Environment.getExternalStorageDirectory(), "/Download/RubinoPro"),
            File(Environment.getExternalStorageDirectory(), "/Download/StatusSaver/Rubika"),
            File(Environment.getExternalStorageDirectory(), "/RubikaDownloaders"),
            File(Environment.getExternalStorageDirectory(), "/Download/روبیکا روبینو دانلودر/Documents"),
            File(Environment.getExternalStorageDirectory(), "/RubikaRubinoDownloader"),
            File(Environment.getExternalStorageDirectory(), "/Download/RubinoDownloader"),
            File(Environment.getExternalStorageDirectory(), "/Download/RubinoDownloader"),
            File(Environment.getExternalStorageDirectory(), "/RubinoDownloader"),
            File(Environment.getExternalStorageDirectory(), "/RubinoDownloaders"),
            File(Environment.getExternalStorageDirectory(), "/RubinoDownloaders/Documents"),
            File(Environment.getExternalStorageDirectory(), "/Download/RubinoDownloader/Documents"),
            File(Environment.getExternalStorageDirectory(), "/Download/RubinoDownloaders/Documents"),
            File(Environment.getExternalStorageDirectory(), "/روبینو دانلودر"),
            File(Environment.getExternalStorageDirectory(), "/روبیکا روبینو دانلودر"),
            File(Environment.getExternalStorageDirectory(), "/روبینو دانلودر/Documents"),
            File(Environment.getExternalStorageDirectory(), "/Rubika Downloaders"),
            File(Environment.getExternalStorageDirectory(), "/Download/Rubika Downloaders")
            // Add other folders here
        )

//        val excludedExtensions = listOf("apk", "mp3", "jpg", "jpeg", "gif", "svg", "tiff", "png", "mp4", "mkv", "avi", "webm", "avc", "flv", "mov", "wmv")
        val excludedExtensions = listOf("flv")

        for (directory in directories) {
            if (directory.exists() && directory.isDirectory) {
                Log.d("FilesActivity", "Checking directory: ${directory.absolutePath}")
                val files = directory.listFiles()
                if (files != null) {
                    for (file in files) {
                        if (file.isFile && file.extension.lowercase() !in excludedExtensions) {
                            Log.d("FilesActivity", "Found file: ${file.absolutePath}")
                            filesList.add(FilesModel(file.name, file.absolutePath, file.length()))
                        }
                    }
                } else {
                    Log.e("FilesActivity", "No files found in directory: ${directory.absolutePath}")
                }
            }
        }

        if (filesList.isEmpty()) {
            binding.filesRecyclerView.visibility = View.GONE
            binding.noFilesTextView.visibility = View.VISIBLE
        } else {
            binding.filesRecyclerView.visibility = View.VISIBLE
            binding.noFilesTextView.visibility = View.GONE
            filesAdapter.notifyDataSetChanged()
        }
    }
}