package com.mch.myketrubikacleaner.activitys

import android.content.Intent
import com.adivery.sdk.*
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.mch.myketrubikacleaner.R
import com.mch.myketrubikacleaner.adapters.SongsAdapter
import com.mch.myketrubikacleaner.databinding.ActivitySongsBinding
import java.io.File

class SongsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySongsBinding
    private lateinit var filesAdapter: SongsAdapter
    private var filesList: MutableList<File> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
         Adivery.configure(getApplication(), "471bde9b-e371-4bfa-adc9-56fe9ccac377")
        binding = DataBindingUtil.setContentView(this, R.layout.activity_songs)

        // پیکربندی Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // حذف عنوان پیش‌فرض

        // تنظیم آیکون بازگشت
        val backIcon = binding.backIcon
        backIcon.setOnClickListener {
            finish() // بازگشت به صفحه قبلی
        }

        val bannerAd = findViewById<AdiveryBannerAdView>(R.id.banner_ad)
        Log.d("SongsActivity", "Banner Ad View initialized: $bannerAd")

        if (bannerAd != null) {
            bannerAd.setBannerAdListener(object : AdiveryAdListener() {
                override fun onAdLoaded() {
                    Log.d("SongsActivity", "Ad loaded successfully")
                }

                override fun onError(reason: String) {
                    Log.e("SongsActivity", "Ad failed to load: $reason")
                }

                override fun onAdClicked() {
                    Log.d("SongsActivity", "Ad clicked")
                }
            })

            bannerAd.loadAd()
            Log.d("SongsActivity", "Banner Ad loadAd called")
        } else {
            Log.e("SongsActivity", "AdiveryBannerAdView is null!")
        }

        // تغییر رنگ Status Bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.primaryColor)
        }

        setupRecyclerView()
        loadFiles()

//        // Request read external storage permission if not granted
//        if (ContextCompat.checkSelfPermission(
//                this,
//                Manifest.permission.READ_EXTERNAL_STORAGE
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            ActivityCompat.requestPermissions(
//                this,
//                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
//                PERMISSION_REQUEST_READ_EXTERNAL_STORAGE
//            )
//        } else {
//            // Permission already granted, load files
//            setupRecyclerView()
//            loadFiles()
//        }
    }

    private fun loadFiles() {
        val musicPaths = arrayOf(
//            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).absolutePath,
            File(Environment.getExternalStorageDirectory(), "/Rubika/Audio").absolutePath,
            File(Environment.getExternalStorageDirectory(), "/Download/Rubika").absolutePath,
            File(Environment.getExternalStorageDirectory(), "/Music/Rubika").absolutePath,
            File(Environment.getExternalStorageDirectory(), "/Music").absolutePath,
            File(Environment.getExternalStorageDirectory(), "/Rubika Downloder").absolutePath,
            File(Environment.getExternalStorageDirectory(), "/Rubika/Rubika Documents").absolutePath,
            File(Environment.getExternalStorageDirectory(), "/Download/Downloader").absolutePath,
            File(Environment.getExternalStorageDirectory(), "/روبیکا دانلودر").absolutePath,
            File(Environment.getExternalStorageDirectory(), "/Download/Rubika Downloader").absolutePath,
            File(Environment.getExternalStorageDirectory(), "/Download/RubinoPro").absolutePath,
            File(Environment.getExternalStorageDirectory(), "/Download/StatusSaver/Rubika").absolutePath,
            File(Environment.getExternalStorageDirectory(), "/RubikaDownloaders").absolutePath,
            File(Environment.getExternalStorageDirectory(), "/Download/روبیکا روبینو دانلودر/Audio").absolutePath,
            File(Environment.getExternalStorageDirectory(), "/RubikaRubinoDownloader").absolutePath,
            File(Environment.getExternalStorageDirectory(), "/Download/RubinoDownloader").absolutePath,
            File(Environment.getExternalStorageDirectory(), "/Download/RubinoDownloader").absolutePath,
            File(Environment.getExternalStorageDirectory(), "/RubinoDownloader").absolutePath,
            File(Environment.getExternalStorageDirectory(), "/RubinoDownloaders").absolutePath,
            File(Environment.getExternalStorageDirectory(), "/RubinoDownloaders/Images").absolutePath,
            File(Environment.getExternalStorageDirectory(), "/Download/RubinoDownloader/Audio").absolutePath,
            File(Environment.getExternalStorageDirectory(), "/Download/RubinoDownloaders/Audio").absolutePath,
            File(Environment.getExternalStorageDirectory(), "/روبینو دانلودر").absolutePath,
            File(Environment.getExternalStorageDirectory(), "/روبیکا روبینو دانلودر").absolutePath,
            File(Environment.getExternalStorageDirectory(), "/روبینو دانلودر/Audio").absolutePath,
            File(Environment.getExternalStorageDirectory(), "/Rubika Downloaders").absolutePath,
            File(Environment.getExternalStorageDirectory(), "/Download/Rubika Downloaders").absolutePath
            // Add other folders to the list here
        )

        for (path in musicPaths) {
            val dir = File(path)
            if (dir.exists() && dir.isDirectory) {
                val filesInDir = dir.listFiles()
                if (filesInDir != null) {
                    for (file in filesInDir) {
                        if (file.isFile && (file.extension == "mp3" || file.extension == "wav" || file.extension == "m4a")) {
                            filesList.add(file)
                        }
                    }
                }
            }
        }
        if (filesList.isEmpty()) {
            binding.songsRecyclerView.visibility = View.GONE
            binding.noSongsTextView.visibility = View.VISIBLE
        } else {
            binding.songsRecyclerView.visibility = View.VISIBLE
            binding.noSongsTextView.visibility = View.GONE
            filesAdapter.notifyDataSetChanged()
        }
    }

    private fun setupRecyclerView() {
        filesAdapter = SongsAdapter(this, filesList) { file ->
            // Handle file click
            playMusic(file)
        }
        binding.songsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@SongsActivity)
            adapter = filesAdapter
        }
    }

    private fun playMusic(file: File) {
        val uri = FileProvider.getUriForFile(
            this,
            applicationContext.packageName + ".provider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "audio/*")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(intent)
    }

}