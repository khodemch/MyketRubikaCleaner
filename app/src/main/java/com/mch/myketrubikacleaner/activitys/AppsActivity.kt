package com.mch.myketrubikacleaner.activitys

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.adivery.sdk.Adivery
import com.adivery.sdk.AdiveryAdListener
import com.adivery.sdk.AdiveryBannerAdView
import com.mch.myketrubikacleaner.models.AppsModel
import com.mch.myketrubikacleaner.R
import com.mch.myketrubikacleaner.adapters.AppsAdapter
import com.mch.myketrubikacleaner.databinding.ActivityAppsBinding
import java.io.File

class AppsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAppsBinding
    private lateinit var appsAdapter: AppsAdapter
    private var appsList: MutableList<AppsModel> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Adivery.configure(application, "61ab9370-74fd-4042-8b8d-ccab3cf5f991")
        binding = DataBindingUtil.setContentView(this, R.layout.activity_apps)

        // پیکربندی Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // حذف عنوان پیش‌فرض

        // تنظیم آیکون بازگشت
        val backIcon = binding.backIcon
        backIcon.setOnClickListener {
            finish() // بازگشت به صفحه قبلی
        }

        setupBannerAd()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.primaryColor)
        }

        setupRecyclerView()
        checkPermissionsAndLoadApps()
    }

    private fun setupBannerAd() {
        val bannerAd = findViewById<AdiveryBannerAdView>(R.id.banner_ad)
        bannerAd?.setBannerAdListener(object : AdiveryAdListener() {
            override fun onAdLoaded() {
                Log.d("AppsActivity", "Ad loaded successfully")
            }

            override fun onError(reason: String) {
                Log.e("AppsActivity", "Ad failed to load: $reason")
            }

            override fun onAdClicked() {
                Log.d("AppsActivity", "Ad clicked")
            }
        })
        bannerAd?.loadAd()
    }

    private fun setupRecyclerView() {
        appsAdapter = AppsAdapter(this, appsList)
        binding.appsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@AppsActivity)
            adapter = appsAdapter
        }
    }

    private fun checkPermissionsAndLoadApps() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
//                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
//                startActivity(intent)
            } else {
                loadApps()
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_PERMISSION_READ_EXTERNAL_STORAGE)
            } else {
                loadApps()
            }
        } else {
//            loadApps()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_READ_EXTERNAL_STORAGE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadApps()
        }
    }

    private fun loadApps() {
        appsList.clear() // Clear the list before loading new apps
        loadAppsFromFolders()
    }

    private fun loadAppsFromFolders() {
        val appFolders = arrayOf(
            File(Environment.getExternalStorageDirectory(), "/Rubika/Documents"),
            File(Environment.getExternalStorageDirectory(), "/Download"),
            File(Environment.getExternalStorageDirectory(), "/Download/Rubika"),
            File(Environment.getExternalStorageDirectory(), "/Rubika Downloder"),
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
        )

        for (folder in appFolders) {
            if (folder.exists() && folder.isDirectory) {
                val filesInDir = folder.listFiles()
                filesInDir?.forEach { file ->
                    if (file.isFile && file.extension == "apk") {
                        val appName = file.nameWithoutExtension
                        val icon = ContextCompat.getDrawable(this, R.drawable.android) // Placeholder icon
                        val size = file.length()
                        if (appsList.none { it.packageName == file.path }) {
                            appsList.add(AppsModel(appName, file.path, icon!!, size))
                        }
                    } else if (file.isFile && (file.extension == "exe" || file.extension == "rar")) {
                        val appName = file.name
                        val icon = ContextCompat.getDrawable(this, R.drawable.android) // Placeholder icon
                        val size = file.length()
                        if (appsList.none { it.packageName == "${file.extension}_file" }) {
                            appsList.add(AppsModel(appName, "${file.extension}_file", icon!!, size))
                        }
                    }
                }
            }
        }

        updateRecyclerViewVisibility()
    }

    private fun updateRecyclerViewVisibility() {
        if (appsList.isEmpty()) {
            binding.appsRecyclerView.visibility = View.GONE
            binding.noAppsTextView.visibility = View.VISIBLE
        } else {
            binding.appsRecyclerView.visibility = View.VISIBLE
            binding.noAppsTextView.visibility = View.GONE
            appsAdapter.notifyDataSetChanged()
        }
    }

    companion object {
        private const val REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 1
    }
}