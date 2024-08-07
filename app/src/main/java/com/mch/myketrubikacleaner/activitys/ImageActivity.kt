package com.mch.myketrubikacleaner.activitys

import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import com.adivery.sdk.*
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.mch.myketrubikacleaner.models.ImageModel
import com.mch.myketrubikacleaner.R
import com.mch.myketrubikacleaner.adapters.ImageAdapter
import com.mch.myketrubikacleaner.databinding.ActivityImageBinding
import java.io.File

class ImageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityImageBinding
    private lateinit var imageAdapter: ImageAdapter
    private var imageList: MutableList<ImageModel> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Adivery.configure(getApplication(), "471bde9b-e371-4bfa-adc9-56fe9ccac377")
        binding = DataBindingUtil.setContentView(this, R.layout.activity_image)

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

        // تغییر رنگ Status Bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.primaryColor)
        }

        imageAdapter = ImageAdapter(this, imageList)
        binding.imageRecyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.imageRecyclerView.adapter = imageAdapter

        loadImagesFromFolders()
    }


    private fun loadImagesFromFolders() {
        val folders = listOf(
            File(Environment.getExternalStorageDirectory(), "/Rubika/Pictures"),
            File(Environment.getExternalStorageDirectory(), "/Download/Rubika"),
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
            // Add other folders to the list here
        )

        for (folder in folders) {
            if (folder.exists() && folder.isDirectory) {
                folder.listFiles()?.forEach { file ->
                    if (file.isFile && (file.extension == "jpg" || file.extension == "png" || file.extension == "jpeg"
                                || file.extension == "jfif" || file.extension == "pjpeg" || file.extension == "pjp"
                                || file.extension == "svg" || file.extension == "bmp" || file.extension == "ico"
                                || file.extension == "cur" || file.extension == "tif" || file.extension == "tiff"
                                || file.extension == "avif")) {
                        imageList.add(ImageModel(file.path.hashCode(), file.path))
                    }
                }
            }
        }

        if (imageList.isEmpty()) {
            binding.imageRecyclerView.visibility = View.GONE
            binding.noImagesTextView.visibility = View.VISIBLE
        } else {
            binding.imageRecyclerView.visibility = View.VISIBLE
            binding.noImagesTextView.visibility = View.GONE
            imageAdapter.notifyDataSetChanged()
        }
    }
}

