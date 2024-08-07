package com.mch.myketrubikacleaner.adapters

import android.app.Activity
import android.app.RecoverableSecurityException
import android.content.Context
import android.content.ActivityNotFoundException
import android.content.ContentUris
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mch.myketrubikacleaner.R
import com.mch.myketrubikacleaner.models.ImageModel
import com.mch.myketrubikacleaner.databinding.ImagerecyclerLayoutBinding
import java.io.File

class ImageAdapter(
    private val context: Context,
    private val items: MutableList<ImageModel>
) : RecyclerView.Adapter<ImageAdapter.ItemViewHolder>() {

    private var pendingDeleteImageUri: Uri? = null
    private var pendingDeletePosition: Int = -1
    private lateinit var deleteImageLauncher: ActivityResultLauncher<IntentSenderRequest>

    init {
        if (context is AppCompatActivity) {
            deleteImageLauncher = (context as AppCompatActivity).registerForActivityResult(
                ActivityResultContracts.StartIntentSenderForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK && pendingDeleteImageUri != null && pendingDeletePosition != -1) {
                    try {
                        context.contentResolver.delete(pendingDeleteImageUri!!, null, null)
                        val position = pendingDeletePosition
                        pendingDeleteImageUri = null
                        pendingDeletePosition = -1
                        if (position != -1) {
                            updateListAfterDeletion(position)
                        }
                        Toast.makeText(context, "تصویر با موفقیت حذف شد", Toast.LENGTH_SHORT).show()
                    } catch (e: SecurityException) {
                        Toast.makeText(context, "خطا در حذف تصویر", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "خطا در حذف تصویر", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    inner class ItemViewHolder(val binding: ImagerecyclerLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ImagerecyclerLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val imageModel = items[position]
        Glide.with(context).load(File(imageModel.path)).into(holder.binding.showImageView)

        holder.binding.showImageView.setOnClickListener {
            openImageWithExternalApp(imageModel.path)
        }

        holder.binding.deleteImageView.setOnClickListener {
            showDeleteConfirmationDialog(position)
        }
    }

    override fun getItemCount(): Int = items.size

    private fun openImageWithExternalApp(imagePath: String) {
        val file = File(imagePath)
        val uri = FileProvider.getUriForFile(context, context.packageName + ".provider", file)

        val intent = Intent().apply {
            action = Intent.ACTION_VIEW
            setDataAndType(uri, "image/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "هیچ برنامه‌ای برای باز کردن این فایل یافت نشد", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDeleteConfirmationDialog(position: Int) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.custom_alert_dialog, null)
        val dialog = AlertDialog.Builder(context, R.style.YourAlertDialogTheme).apply {
            setView(dialogView)
        }.create()

        val title = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val message = dialogView.findViewById<TextView>(R.id.dialogMessage)
        val positiveButton = dialogView.findViewById<Button>(R.id.btnPositive)
        val negativeButton = dialogView.findViewById<Button>(R.id.btnNegative)

        title.text = "حذف تصویر"
        message.text = "آیا مطمئن هستید که می‌خواهید این تصویر را حذف کنید؟"

        positiveButton.setOnClickListener {
            deleteImage(position)
            dialog.dismiss()
        }

        negativeButton.setOnClickListener {
            dialog.dismiss()
        }

        // Prevent dialog from being dismissed by clicking outside
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        dialog.setView(dialogView)
        dialog.show()
    }

    private fun deleteImage(position: Int) {
        if (position < 0 || position >= items.size) {
            Toast.makeText(context, "تصویر یافت نشد", Toast.LENGTH_SHORT).show()
            return
        }

        val imageFile = File(items[position].path)
        if (imageFile.exists()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                deleteImageForAndroidQAndAbove(position, imageFile)
            } else {
                val deleted = imageFile.delete()
                if (deleted) {
                    updateListAfterDeletion(position)
                    Toast.makeText(context, "تصویر با موفقیت حذف شد", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "خطا در حذف تصویر", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "تصویر موجود نیست", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteImageForAndroidQAndAbove(position: Int, imageFile: File) {
        val imagePath = items[position].path
        val contentResolver = context.contentResolver
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val cursor = contentResolver.query(
            uri,
            arrayOf(MediaStore.Images.Media._ID),
            "${MediaStore.Images.Media.DATA}=?",
            arrayOf(imagePath),
            null
        )

        if (cursor != null && cursor.moveToFirst()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
            val contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
            cursor.close()

            try {
                contentResolver.delete(contentUri, null, null)
                updateListAfterDeletion(position)
                Toast.makeText(context, "تصویر با موفقیت حذف شد", Toast.LENGTH_SHORT).show()
            } catch (e: SecurityException) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val recoverableSecurityException = e as? RecoverableSecurityException
                    if (recoverableSecurityException != null) {
                        pendingDeleteImageUri = contentUri
                        pendingDeletePosition = position
                        val intentSender = recoverableSecurityException.userAction.actionIntent.intentSender
                        val intentSenderRequest = IntentSenderRequest.Builder(intentSender).build()
                        deleteImageLauncher.launch(intentSenderRequest)
                    } else {
                        Toast.makeText(context, "خطا در حذف تصویر", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "خطا در حذف تصویر", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            cursor?.close()
            Toast.makeText(context, "تصویر یافت نشد", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateListAfterDeletion(position: Int) {
        if (position < 0 || position >= items.size) {
            Toast.makeText(context, "تصویر یافت نشد", Toast.LENGTH_SHORT).show()
            return
        }

        items.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, items.size)
    }
}
