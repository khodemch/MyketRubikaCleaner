package com.mch.myketrubikacleaner.activitys

import android.content.Intent
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
import com.adivery.sdk.Adivery
import com.adivery.sdk.AdiveryAdListener
import com.adivery.sdk.AdiveryBannerAdView
import com.mch.myketrubikacleaner.R
import com.mch.myketrubikacleaner.adapters.VoicesAdapter
import com.mch.myketrubikacleaner.databinding.ActivityVoicesBinding
import com.mch.myketrubikacleaner.models.VoicesModel
import java.io.File

class VoicesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVoicesBinding
    private lateinit var voicesAdapter: VoicesAdapter
    private var voicesList: MutableList<VoicesModel> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Adivery.configure(application, "61ab9370-74fd-4042-8b8d-ccab3cf5f991")
        binding = DataBindingUtil.setContentView(this, R.layout.activity_voices)

        // پیکربندی Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // حذف عنوان پیش‌فرض

        // تنظیم آیکون بازگشت
        val backIcon = binding.backIcon
        backIcon.setOnClickListener {
            finish() // بازگشت به صفحه قبلی
        }

        val bannerAd = findViewById<AdiveryBannerAdView>(R.id.banner_ad)
        Log.d("VoicesActivity", "Banner Ad View initialized: $bannerAd")

        if (bannerAd != null) {
            bannerAd.setBannerAdListener(object : AdiveryAdListener() {
                override fun onAdLoaded() {
                    Log.d("VoicesActivity", "Ad loaded successfully")
                }

                override fun onError(reason: String) {
                    Log.e("VoicesActivity", "Ad failed to load: $reason")
                }

                override fun onAdClicked() {
                    Log.d("VoicesActivity", "Ad clicked")
                }
            })

            bannerAd.loadAd()
            Log.d("VoicesActivity", "Banner Ad loadAd called")
        } else {
            Log.e("VoicesActivity", "AdiveryBannerAdView is null!")
        }

        // تغییر رنگ Status Bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.primaryColor)
        }

        voicesAdapter = VoicesAdapter(this, voicesList, { path -> playVoice(File(path)) }, { null })
        binding.voicesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@VoicesActivity)
            adapter = voicesAdapter
        }
        loadVoices()
    }

    private fun loadVoices() {
        val voiceFolders = arrayOf(
            File(Environment.getExternalStorageDirectory(), "/Rubika/Audio"),
            File(Environment.getExternalStorageDirectory(), "/Download/Rubika"),
            File(Environment.getExternalStorageDirectory(), "/Rubika Downloder"),
            File(Environment.getExternalStorageDirectory(), "/Rubika/Rubika Audio"),
            File(Environment.getExternalStorageDirectory(), "/Download/Downloader"),
            File(Environment.getExternalStorageDirectory(), "/روبیکا دانلودر"),
            File(Environment.getExternalStorageDirectory(), "/Download/Rubika Downloader"),
            File(Environment.getExternalStorageDirectory(), "/Download/RubinoPro"),
            File(Environment.getExternalStorageDirectory(), "/Download/StatusSaver/Rubika"),
            File(Environment.getExternalStorageDirectory(), "/RubikaDownloaders"),
            File(Environment.getExternalStorageDirectory(), "/Download/روبیکا روبینو دانلودر/Audio"),
            File(Environment.getExternalStorageDirectory(), "/RubikaRubinoDownloader"),
            File(Environment.getExternalStorageDirectory(), "/Download/RubinoDownloader"),
            File(Environment.getExternalStorageDirectory(), "/Download/RubinoDownloader"),
            File(Environment.getExternalStorageDirectory(), "/RubinoDownloader"),
            File(Environment.getExternalStorageDirectory(), "/RubinoDownloaders"),
            File(Environment.getExternalStorageDirectory(), "/RubinoDownloaders/Images"),
            File(Environment.getExternalStorageDirectory(), "/Download/RubinoDownloader/Audio"),
            File(Environment.getExternalStorageDirectory(), "/Download/RubinoDownloaders/Audio"),
            File(Environment.getExternalStorageDirectory(), "/روبینو دانلودر"),
            File(Environment.getExternalStorageDirectory(), "/روبیکا روبینو دانلودر"),
            File(Environment.getExternalStorageDirectory(), "/روبینو دانلودر/Audio"),
            File(Environment.getExternalStorageDirectory(), "/Rubika Downloaders"),
            File(Environment.getExternalStorageDirectory(), "/Download/Rubika Downloaders")
            // Add other folders here
        )

        for (folder in voiceFolders) {
            if (folder.exists() && folder.isDirectory) {
                val filesInDir = folder.listFiles()
                if (filesInDir != null) {
                    for (file in filesInDir) {
                        if (file.isFile && (file.extension == "mp3" || file.extension == "wav" || file.extension == "m4a" || file.extension == "ogg")) {
                            val title = file.name
                            val data = file.path
                            val size = file.length()
                            voicesList.add(VoicesModel(title, data, size))
                        }
                    }
                }
            }
        }

        if (voicesList.isEmpty()) {
            binding.voicesRecyclerView.visibility = View.GONE
            binding.noVoicesTextView.visibility = View.VISIBLE
        } else {
            binding.voicesRecyclerView.visibility = View.VISIBLE
            binding.noVoicesTextView.visibility = View.GONE
            voicesAdapter.notifyDataSetChanged()
        }
    }

    private fun playVoice(file: File) {
        val uri = FileProvider.getUriForFile(
            this,
            applicationContext.packageName + ".provider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "audio/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(intent)
    }
}
