package com.mch.myketrubikacleaner.activitys

import android.os.Build
import com.adivery.sdk.*
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.mch.myketrubikacleaner.adapters.MoviesAdapter
import com.mch.myketrubikacleaner.models.MoviesModel
import com.mch.myketrubikacleaner.R
import com.mch.myketrubikacleaner.databinding.ActivityMoviesBinding
import java.io.File

class MoviesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMoviesBinding
    private lateinit var videoAdapter: MoviesAdapter
    private var videoList: MutableList<MoviesModel> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Adivery.configure(getApplication(), "61ab9370-74fd-4042-8b8d-ccab3cf5f991")
        binding = DataBindingUtil.setContentView(this, R.layout.activity_movies)

        // پیکربندی Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // حذف عنوان پیش‌فرض

        // تنظیم آیکون بازگشت
        val backIcon = binding.backIcon
        backIcon.setOnClickListener {
            finish() // بازگشت به صفحه قبلی
        }

        val bannerAd = findViewById<AdiveryBannerAdView>(R.id.banner_ad)
        Log.d("MoviesActivity", "Banner Ad View initialized: $bannerAd")

        if (bannerAd != null) {
            bannerAd.setBannerAdListener(object : AdiveryAdListener() {
                override fun onAdLoaded() {
                    Log.d("MoviesActivity", "Ad loaded successfully")
                }

                override fun onError(reason: String) {
                    Log.e("MoviesActivity", "Ad failed to load: $reason")
                }

                override fun onAdClicked() {
                    Log.d("MoviesActivity", "Ad clicked")
                }
            })

            bannerAd.loadAd()
            Log.d("MoviesActivity", "Banner Ad loadAd called")
        } else {
            Log.e("MoviesActivity", "AdiveryBannerAdView is null!")
        }

        // تغییر رنگ Status Bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.primaryColor)
        }

        videoAdapter = MoviesAdapter(this, videoList)
        binding.moviesRecyclerView.layoutManager = GridLayoutManager(this, 3)
        binding.moviesRecyclerView.adapter = videoAdapter

        loadVideosFromFolders()
    }

    private fun loadVideosFromFolders() {
        val folders = listOf(
            File(Environment.getExternalStorageDirectory(), "/Rubika/Video"),
            File(Environment.getExternalStorageDirectory(), "/Download"),
            File(Environment.getExternalStorageDirectory(), "/Movies/Rubika"),
            File(Environment.getExternalStorageDirectory(), "/Download/Rubika"),
            File(Environment.getExternalStorageDirectory(), "/Rubika Downloder"),
            File(Environment.getExternalStorageDirectory(), "/Pictures/Rubika"),
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
            File(Environment.getExternalStorageDirectory(), "/Download/Rubika Downloaders"),
            File(Environment.getExternalStorageDirectory(), "/Rubika/Rubika Video"),
            File(Environment.getExternalStorageDirectory(), "/Rubika/Rubika Documents")
            // اضافه کردن سایر پوشه‌ها به لیست در اینجا
        )

        for (folder in folders) {
            if (folder.exists() && folder.isDirectory) {
                folder.listFiles()?.forEach { file ->
                    if (file.isFile && (file.extension == "mp4" || file.extension == "avi" || file.extension == "mkv"
                                || file.extension == "webm" || file.extension == "avc" || file.extension == "flv"
                                || file.extension == "mov" || file.extension == "wmv" || file.extension == "gif")
                    ) {
                        videoList.add(MoviesModel(file.path.hashCode(), file.path, file.name))
                    }
                }
            }
        }
        if (videoList.isEmpty()) {
            binding.moviesRecyclerView.visibility = View.GONE
            binding.noMoviesTextView.visibility = View.VISIBLE
        } else {
            binding.moviesRecyclerView.visibility = View.VISIBLE
            binding.noMoviesTextView.visibility = View.GONE
            videoAdapter.notifyDataSetChanged()
        }
    }
}