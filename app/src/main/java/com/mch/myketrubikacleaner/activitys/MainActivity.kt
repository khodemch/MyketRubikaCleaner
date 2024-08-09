package com.mch.myketrubikacleaner.activitys

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.adivery.sdk.Adivery
import com.adivery.sdk.AdiveryAdListener
import com.adivery.sdk.AdiveryBannerAdView
import com.google.android.material.navigation.NavigationView
import com.mch.myketrubikacleaner.R
import com.mch.myketrubikacleaner.adapters.MainAdapter
import com.mch.myketrubikacleaner.databinding.ActivityMainBinding
import com.mch.myketrubikacleaner.models.ImageModel
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.AppMetricaConfig
import java.io.RandomAccessFile


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var showAvailableMemoryTextView: TextView
    private lateinit var showTotalMemoryTextView: TextView
    private lateinit var showUsedMemoryTextView: TextView
    private lateinit var circularProgressBar: CircularProgressBar
    private val REQUEST_CODE = 100
    private lateinit var drawerLayout: DrawerLayout

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Adivery.configure(getApplication(), "2043f331-5c27-4d27-bea7-ee40ef4f3e87")
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        // Creating an extended library configuration.
        val config = AppMetricaConfig.newConfigBuilder("c66aedd6-e3e0-46d8-aec2-79e6728a6718").build()
        // Initializing the AppMetrica SDK.
        AppMetrica.activate(this, config)

//        getVersionName()
//        getVersionCode()
//        showVersionInfo()


        val bannerAd = findViewById<AdiveryBannerAdView>(R.id.banner_ad)
        Log.d("MainActivity", "Banner Ad View initialized: $bannerAd")

        if (bannerAd != null) {
            bannerAd.setBannerAdListener(object : AdiveryAdListener() {
                override fun onAdLoaded() {
                    Log.d("MainActivity", "Ad loaded successfully")
                }

                override fun onError(reason: String) {
                    Log.e("MainActivity", "Ad failed to load: $reason")
                }

                override fun onAdClicked() {
                    Log.d("MainActivity", "Ad clicked")
                }
            })

            bannerAd.loadAd()
            Log.d("MainActivity", "Banner Ad loadAd called")
        } else {
            Log.e("MainActivity", "AdiveryBannerAdView is null!")
        }

        // تغییر رنگ Status Bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.primaryColor)
        }


        // بررسی اتصال به اینترنت
        if (!isInternetAvailable()) {
            val intent = Intent(this, NoInternetActivity::class.java)
            startActivity(intent)
            finish()
            return
        } else {
            binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

            // تنظیم DrawerLayout و NavigationView
            drawerLayout = findViewById(R.id.drawer_layout)
            val logo: ImageView = findViewById(R.id.logo)
            val navigationView: NavigationView = findViewById(R.id.navigation_view)

            // تنظیم کلیک لیسنر برای ImageView
            logo.setOnClickListener {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    drawerLayout.openDrawer(GravityCompat.END)
                }
            }

            // تنظیم کلیک لیسنر برای آیتم‌های Navigation Drawer
            navigationView.setNavigationItemSelectedListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.bigFiles -> {
                        val intent = Intent(this, BigFilesActivity::class.java)
                        startActivity(intent)
                        true
                    }

                    R.id.shareAppItem -> {
                        shareApp()
                        true
                    }
                    R.id.rateItem -> {
                        val intent = Intent(this, FeedbackActivity::class.java)
                        startActivity(intent)
                        true
                    }
                    R.id.guideItem -> {
                        // انجام عملیات مورد نظر برای آیتم راهنما
                        showGuideDialog()
                        true
                    }
                    R.id.supportItem -> {
                        showSupportDialog()
                        true
                    }

                    R.id.exitItem -> {
                        // انجام عملیات مورد نظر برای آیتم خروج
                        showExitDialog()
                        true
                    }
                    else -> false
                }
            }

            // بررسی مجوزها
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                checkPermissions()
            } else {
                // دسترسی به فایل‌ها
                accessFiles()
            }

            // تنظیم RecyclerView
            val imageList = mutableListOf<ImageModel>().apply {
                add(ImageModel(R.drawable.pictures, "/path/to/pictures"))
                add(ImageModel(R.drawable.movies, "/path/to/movies"))
                add(ImageModel(R.drawable.songs, "/path/to/songs"))
                add(ImageModel(R.drawable.apps, "/path/to/apps"))
                add(ImageModel(R.drawable.voices, "/path/to/voices"))
                add(ImageModel(R.drawable.doc, "/path/to/pdfs"))
            }

            val recyclerView: RecyclerView = binding.mainRecyclerView
            recyclerView.layoutManager = LinearLayoutManager(this)
            val adapter = MainAdapter(this, imageList)
            recyclerView.adapter = adapter

            // تنظیم TextView ها و CircularProgressBar
            showAvailableMemoryTextView = binding.showAvailableMemoryTextView
            showTotalMemoryTextView = binding.showTotalMemoryTextView
            showUsedMemoryTextView = binding.showUsedMemoryTextView
            circularProgressBar = binding.circularProgressBar

            // دریافت اطلاعات حافظه داخلی و RAM دستگاه
            val totalInternalMemory = getTotalInternalMemory()
            val availableInternalMemory = getAvailableInternalMemory()
            val totalRAM = getTotalRAM()
            val availableRAM = getAvailableRAM()

            // محاسبه و نمایش اطلاعات حافظه
            val totalMemory = (totalInternalMemory + totalRAM) / (1024.0 * 1024.0 * 1024.0)
            val availableMemory =
                (availableInternalMemory + availableRAM) / (1024.0 * 1024.0 * 1024.0)
            val usedMemory = totalMemory - availableMemory
            val usedMemoryPercentage = (usedMemory / totalMemory) * 100

            showAvailableMemoryTextView.text = "%.2f GB".format(availableMemory)
            showTotalMemoryTextView.text = "%.2f GB".format(totalMemory)
            showUsedMemoryTextView.text = "%.2f GB".format(usedMemory)
            circularProgressBar.progress = usedMemoryPercentage.toFloat()
        }
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

    // تابع برای دریافت مقدار کل حافظه داخلی
    private fun getTotalInternalMemory(): Long {
        val statFs = StatFs(Environment.getDataDirectory().absolutePath)
        return statFs.blockCountLong * statFs.blockSizeLong
    }

    // تابع برای دریافت مقدار حافظه داخلی در دسترس
    private fun getAvailableInternalMemory(): Long {
        val statFs = StatFs(Environment.getDataDirectory().absolutePath)
        return statFs.availableBlocksLong * statFs.blockSizeLong
    }

    // تابع برای دریافت مقدار کل حافظه RAM
    @SuppressLint("PrivateApi")
    private fun getTotalRAM(): Long {
        try {
            val reader = RandomAccessFile("/proc/meminfo", "r")
            val load = reader.readLine().replace(Regex("\\D+"), "")
            reader.close()
            return load.toLong() * 1024
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0
    }

    // تابع برای دریافت مقدار حافظه RAM در دسترس
    private fun getAvailableRAM(): Long {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo.availMem
    }

    private fun checkPermissions() {
        val permissionsNeeded = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13 و بالاتر
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.READ_MEDIA_IMAGES)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.READ_MEDIA_VIDEO)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.READ_MEDIA_AUDIO)
            }
        } else {
            // Android 12 و پایین‌تر
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        if (permissionsNeeded.isNotEmpty()) {
            if (permissionsNeeded.any { ActivityCompat.shouldShowRequestPermissionRationale(this, it) }) {
                showCustomPermissionDialog(permissionsNeeded.toTypedArray())
            } else {
                ActivityCompat.requestPermissions(this, permissionsNeeded.toTypedArray(), REQUEST_CODE)
            }
        } else {
            // مجوزها قبلاً اعطا شده‌اند
            accessFiles()
        }
    }

    private fun showCustomPermissionDialog(permissions: Array<String>) {
        // Inflate custom dialog layout
        val dialogView = LayoutInflater.from(this).inflate(R.layout.custom_alert_dialog, null)
        val dialog = AlertDialog.Builder(this, R.style.YourAlertDialogTheme).apply {
            setView(dialogView)
        }.create()

        // Initialize views from custom dialog layout
        val title = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val message = dialogView.findViewById<TextView>(R.id.dialogMessage)
        val positiveButton = dialogView.findViewById<Button>(R.id.btnPositive)
        positiveButton.text = "رد کردن"
        val negativeButton = dialogView.findViewById<Button>(R.id.btnNegative)
        negativeButton.text = "اجازه دادن"

        // Set title and message (optional, since they are set in the XML)
        title.text = "دسترسی‌های لازم"
        message.text = "این برنامه نیاز به دسترسی به حافظه دارد تا بتواند فایل‌های شما را مدیریت کند."

        // Handle button clicks
        positiveButton.setOnClickListener {
            dialog.dismiss()
            Toast.makeText(this, "لطفاً دسترسی‌های لازم را قبول کنید.", Toast.LENGTH_SHORT).show()
            checkPermissions()
        }

        negativeButton.setOnClickListener {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)
            dialog.dismiss()
        }

        // Prevent dialog from being dismissed by clicking outside
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        // Set dialog view and show
        dialog.setView(dialogView)
        dialog.show()
    }






//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            // مجوزها اعطا شده‌اند
//            accessFiles()
//        }
//    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // مجوزها اعطا شده‌اند
                accessFiles()
            } else {
                // مجوزها رد شده‌اند
                Toast.makeText(this, "لطفاً دسترسی‌های لازم را قبول کنید.", Toast.LENGTH_SHORT).show()
                checkPermissions()
            }
        }
    }

    private fun accessFiles() {
        val images: MutableList<String> = ArrayList()

        // دریافت URI برای دسترسی به تصاویر در حافظه خارجی
        val uriExternal: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val uriInternal: Uri = MediaStore.Images.Media.INTERNAL_CONTENT_URI

        // پروژکشن مورد نیاز برای درخواست تصاویر
        val projection = arrayOf(MediaStore.MediaColumns.DATA)

        // دریافت تصاویر از حافظه خارجی
        var cursor: Cursor? = contentResolver.query(uriExternal, projection, null, null, null)
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val imagePath: String = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA))
                images.add(imagePath)
            }
            cursor.close()
        }

        // دریافت تصاویر از حافظه داخلی
        cursor = contentResolver.query(uriInternal, projection, null, null, null)
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val imagePath: String = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA))
                images.add(imagePath)
            }
            cursor.close()
        }

        // نمایش تعداد تصاویر پیدا شده
//        Toast.makeText(this, "Found ${images.size} images", Toast.LENGTH_SHORT).show()
    }


    private fun showExitDialog() {
        // Inflate custom dialog layout
        val dialogView = LayoutInflater.from(this).inflate(R.layout.custom_alert_dialog, null)
        val dialog = AlertDialog.Builder(this).create()

        // Initialize views from custom dialog layout
        val title = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val message = dialogView.findViewById<TextView>(R.id.dialogMessage)
        val positiveButton = dialogView.findViewById<Button>(R.id.btnPositive)
        val negativeButton = dialogView.findViewById<Button>(R.id.btnNegative)

        // Set title and message
        title.text = "خروج از برنامه"
        message.text = "آیا می‌خواهید از برنامه خارج شوید؟"

        // Handle button clicks
        positiveButton.setOnClickListener {
            finish()
        }

        negativeButton.setOnClickListener {
            dialog.dismiss()
        }

        // Set dialog view and show
        dialog.setView(dialogView)
        dialog.show()
    }


    private fun shareApp() {
        try {
            val appPackageName = packageName // این نام بسته برنامه است
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            val shareMessage = "برنامه مورد علاقه خود را از لینک زیر دریافت کنید:\nhttps://myket.ir/app/$appPackageName/"
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(shareIntent, "اشتراک‌گذاری برنامه با"))
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("ShareApp", e.message.toString())
        }
    }

    private fun showSupportDialog() {
        // Inflate custom dialog layout
        val dialogView = LayoutInflater.from(this).inflate(R.layout.custom_support_dialog, null)
        val dialog = AlertDialog.Builder(this).create()

        // Initialize views from custom dialog layout
        val title = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val btnWhatsApp = dialogView.findViewById<Button>(R.id.btnWhatsApp)
        val btnTelegram = dialogView.findViewById<Button>(R.id.btnTelegram)

        // Set title and message
        title.text = "❕ برنامه مورد نظرتان را برای پشتیبانی انتخاب کنید:"

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


    private fun showGuideDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.custom_alert_dialog, null)
        val dialog = AlertDialog.Builder(this, R.style.YourAlertDialogTheme).apply {
            setView(dialogView)
        }.create()

        val title = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val message = dialogView.findViewById<TextView>(R.id.dialogMessage)
        val positiveButton = dialogView.findViewById<Button>(R.id.btnPositive)
        positiveButton.text = "فهمیدم"
        val negativeButton = dialogView.findViewById<Button>(R.id.btnNegative)
        negativeButton.visibility = View.INVISIBLE

        title.text = "کاربر عزیز!"
        message.text = "این برنامه فایل هایی که از طریق برنامه روبیکا ذخیره شده اند را به شما نمایش میدهد. با استفاده از این برنامه شما میتوانید فایل های زائد را مشاهده و حذف کنید تا فضای گوشی تان آزاد شود و باعث سریع تر شدن دستگاه تان شود."

        positiveButton.setOnClickListener {
            dialog.dismiss()
        }

        // Prevent dialog from being dismissed by clicking outside
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

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
        val resolveInfoList = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)

        var isWhatsAppInstalled = false
        for (resolveInfo in resolveInfoList) {
            if (resolveInfo.activityInfo.packageName == "com.whatsapp" ||
                resolveInfo.activityInfo.packageName == "com.whatsapp.w4b" || // WhatsApp Business
                resolveInfo.activityInfo.packageName == "com.gbwhatsapp") {  // GB WhatsApp
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
        val resolveInfoList = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)

        var isTelegramInstalled = false
        for (resolveInfo in resolveInfoList) {
            if (resolveInfo.activityInfo.packageName == "org.telegram.messenger" ||
                resolveInfo.activityInfo.packageName == "org.telegram.messenger.web" ||
                resolveInfo.activityInfo.packageName == "org.telegram.plus" ||
                resolveInfo.activityInfo.packageName == "org.telegram.messenger.beta" ||
                resolveInfo.activityInfo.packageName == "org.telegram.messenger.wear") {
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
        // هدایت کاربر به FeedbackActivity هنگام فشار دادن دکمه بازگشت
        val intent = Intent(this, FeedbackActivity::class.java)
        startActivity(intent)
        finish()
    }


//    // تابع برای دریافت نام ورژن برنامه
//    fun getVersionName(): String {
//        return try {
//            val packageInfo = applicationContext.packageManager.getPackageInfo(applicationContext.packageName, 0)
//            packageInfo.versionName
//        } catch (e: PackageManager.NameNotFoundException) {
//            e.printStackTrace()
//            "نامشخص"
//        }
//    }
//
//    // تابع برای دریافت کد ورژن برنامه
//    fun getVersionCode(): Int {
//        return try {
//            val packageInfo = applicationContext.packageManager.getPackageInfo(applicationContext.packageName, 0)
//            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
//                packageInfo.longVersionCode.toInt() // برای API level 28 و بالاتر
//            } else {
//                packageInfo.versionCode // برای API level 27 و پایین‌تر
//            }
//        } catch (e: PackageManager.NameNotFoundException) {
//            e.printStackTrace()
//            -1
//        }
//    }
//
//    // تابع برای نمایش اطلاعات نسخه و کد ورژن به صورت یک Toast
//    fun showVersionInfo() {
//        val versionName = getVersionName()
//        val versionCode = getVersionCode()
//
//        val toastMessage = "نسخه: $versionName\nکد ورژن: $versionCode"
//        Toast.makeText(applicationContext, toastMessage, Toast.LENGTH_LONG).show()
//    }



}