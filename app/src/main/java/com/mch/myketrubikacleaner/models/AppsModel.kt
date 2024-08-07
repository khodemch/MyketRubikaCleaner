package com.mch.myketrubikacleaner.models

import android.graphics.drawable.Drawable

data class AppsModel(
    val name: String,
    val packageName: String,
    val icon: Drawable,
    val size: Long,
) {
}