package com.mch.myketrubikacleaner.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mch.myketrubikacleaner.models.ImageModel
import com.mch.myketrubikacleaner.activitys.AppsActivity
import com.mch.myketrubikacleaner.activitys.FilesActivity
import com.mch.myketrubikacleaner.activitys.ImageActivity
import com.mch.myketrubikacleaner.activitys.MoviesActivity
import com.mch.myketrubikacleaner.activitys.SongsActivity
import com.mch.myketrubikacleaner.activitys.VoicesActivity
import com.mch.myketrubikacleaner.databinding.MainrecyclerLayoutBinding
import com.adivery.sdk.Adivery
import com.adivery.sdk.AdiveryListener

class MainAdapter(val context: Context, var imageList: MutableList<ImageModel>) :
    RecyclerView.Adapter<MainAdapter.MyViewHolder>() {

    private var currentPosition: Int = -1

    init {
        Adivery.addGlobalListener(object : AdiveryListener() {
            override fun onInterstitialAdLoaded(placementId: String) {
                // تبلیغ میان‌صفحه‌ای بارگذاری شده
            }

            override fun onInterstitialAdShown(placementId: String) {
                // تبلیغ میان‌صفحه‌ای نمایش داده شد
            }

            override fun onInterstitialAdClosed(placementId: String) {
                // تبلیغ میان‌صفحه‌ای بسته شد
                goToActivity(currentPosition)
            }
        })
    }

    inner class MyViewHolder(val binding: MainrecyclerLayoutBinding) : ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        MyViewHolder(MainrecyclerLayoutBinding.inflate(LayoutInflater.from(context), parent, false))

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val imageModel = imageList[position]
        holder.binding.pictureImageView.setImageResource(imageModel.imageResId)
        holder.binding.itemNamesTextView.text = when (position) {
            0 -> "تصاویر"
            1 -> "فیلم ها"
            2 -> "موسیقی ها"
            3 -> "برنامه ها"
            4 -> "صدا ها"
            5 -> "فایل ها و اسناد"
            else -> ""
        }
    }

    override fun getItemCount(): Int = imageList.size

    open inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            val position = adapterPosition
            val placementId = "471bde9b-e371-4bfa-adc9-56fe9ccac377"
            currentPosition = position

            if (Adivery.isLoaded(placementId)) {
                Adivery.showAd(placementId)
            } else {
                Adivery.prepareInterstitialAd(context, placementId)
                goToActivity(position)
            }
        }
    }

    private fun goToActivity(position: Int) {
        val intent: Intent? = when (position) {
            0 -> Intent(context, ImageActivity::class.java)
            1 -> Intent(context, MoviesActivity::class.java)
            2 -> Intent(context, SongsActivity::class.java)
            3 -> Intent(context, AppsActivity::class.java)
            4 -> Intent(context, VoicesActivity::class.java)
            5 -> Intent(context, FilesActivity::class.java)
            else -> null
        }
        intent?.let { context.startActivity(it) }
    }
}