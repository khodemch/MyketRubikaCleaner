package com.mch.myketrubikacleaner.adapters

import android.app.Activity
import android.app.RecoverableSecurityException
import android.content.ContentUris
import android.content.Context
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
import com.mch.myketrubikacleaner.databinding.FilerecyclerLayoutBinding
import com.mch.myketrubikacleaner.models.FilesModel
import java.io.File

class FilesAdapter(
    private val context: Context,
    private val filesList: MutableList<FilesModel>
) : RecyclerView.Adapter<FilesAdapter.ViewHolder>() {

    private var pendingDeleteFileUri: Uri? = null
    private var pendingDeletePosition: Int = -1
    private lateinit var deleteFileLauncher: ActivityResultLauncher<IntentSenderRequest>

    init {
        if (context is AppCompatActivity) {
            deleteFileLauncher = (context as AppCompatActivity).registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK && pendingDeleteFileUri != null && pendingDeletePosition != -1) {
                    try {
                        context.contentResolver.delete(pendingDeleteFileUri!!, null, null)
                        val position = pendingDeletePosition
                        pendingDeleteFileUri = null
                        pendingDeletePosition = -1
                        if (position != -1) {
                            updateListAfterDeletion(position)
                        }
                        Toast.makeText(context, "فایل با موفقیت حذف شد", Toast.LENGTH_SHORT).show()
                    } catch (e: SecurityException) {
                        Toast.makeText(context, "خطا در حذف فایل", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "خطا در حذف فایل", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    inner class ViewHolder(val binding: FilerecyclerLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    openFile(filesList[position])
                }
            }

            binding.deleteFileImageView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    showDeleteConfirmationDialog(filesList[position], position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = FilerecyclerLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val fileModel = filesList[position]

        // Set placeholder icon for files
        holder.binding.fileImageView.setImageResource(R.drawable.doc)

        // Use Glide to load image thumbnails
        val file = File(fileModel.path)
        val extension = file.extension.lowercase()

        if (extension in listOf("jpg", "jpeg", "gif", "png", "svg", "tiff")) {
            Glide.with(context)
                .load(file)
                .placeholder(R.drawable.doc)
                .into(holder.binding.fileImageView)
        } else {
            // Set the icon based on the file type
            when (extension) {
                "pdf" -> holder.binding.fileImageView.setImageResource(R.drawable.doc)
                "doc", "docx" -> holder.binding.fileImageView.setImageResource(R.drawable.doc)
                "xls", "xlsx" -> holder.binding.fileImageView.setImageResource(R.drawable.doc)
                "ppt", "pptx" -> holder.binding.fileImageView.setImageResource(R.drawable.doc)
                "txt", "txtx" -> holder.binding.fileImageView.setImageResource(R.drawable.doc)
                "zip", "zipx" -> holder.binding.fileImageView.setImageResource(R.drawable.doc)
                "rar", "rarx" -> holder.binding.fileImageView.setImageResource(R.drawable.doc)
                "sys", "sysx" -> holder.binding.fileImageView.setImageResource(R.drawable.doc)
                "tmp", "tmpx" -> holder.binding.fileImageView.setImageResource(R.drawable.doc)
            }
        }

        holder.binding.filesName.text = fileModel.name

        // Display the file size
        val formattedSize = formatFileSize(fileModel.size)
        holder.binding.informationFileTextView.text = formattedSize
    }

    override fun getItemCount(): Int {
        return filesList.size
    }

    private fun formatFileSize(sizeInBytes: Long): String {
        return when {
            sizeInBytes < 1024 -> "$sizeInBytes B"
            sizeInBytes < 1024 * 1024 -> "${sizeInBytes / 1024} KB"
            else -> String.format("%.2f MB", sizeInBytes / (1024.0 * 1024.0))
        }
    }

    private fun openFile(fileModel: FilesModel) {
        val file = File(fileModel.path)
        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "*/*") // All MIME types
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Handle exception if no app can handle the file
        }
    }

    private fun showDeleteConfirmationDialog(fileModel: FilesModel, position: Int) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.custom_alert_dialog, null)
        val dialog = AlertDialog.Builder(context, R.style.YourAlertDialogTheme).apply {
            setView(dialogView)
        }.create()

        val title = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val message = dialogView.findViewById<TextView>(R.id.dialogMessage)
        val positiveButton = dialogView.findViewById<Button>(R.id.btnPositive)
        val negativeButton = dialogView.findViewById<Button>(R.id.btnNegative)

        title.text = "حذف فایل"
        message.text = "آیا مطمئن هستید که می‌خواهید این فایل را حذف کنید؟"

        positiveButton.setOnClickListener {
            deleteFile(fileModel, position)
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

    private fun deleteFile(fileModel: FilesModel, position: Int) {
        val file = File(fileModel.path)
        if (file.exists()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10 (API 29) and above (Scoped Storage)
                deleteFileForAndroidQAndAbove(position, file)
            } else {
                // Below Android 10
                if (file.delete()) {
                    updateListAfterDeletion(position)
                    Toast.makeText(context, "فایل با موفقیت حذف شد", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "خطا در حذف فایل", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "فایل وجود ندارد", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteFileForAndroidQAndAbove(position: Int, file: File) {
        val filePath = file.absolutePath
        val contentResolver = context.contentResolver
        val uri = MediaStore.Files.getContentUri("external")
        val cursor = contentResolver.query(
            uri,
            arrayOf(MediaStore.Files.FileColumns._ID),
            "${MediaStore.Files.FileColumns.DATA}=?",
            arrayOf(filePath),
            null
        )

        if (cursor != null && cursor.moveToFirst()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID))
            val contentUri = ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), id)
            cursor.close()
            try {
                contentResolver.delete(contentUri, null, null)
                updateListAfterDeletion(position)
                Toast.makeText(context, "فایل با موفقیت حذف شد", Toast.LENGTH_SHORT).show()
            } catch (e: SecurityException) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val recoverableSecurityException = e as? RecoverableSecurityException
                    if (recoverableSecurityException != null) {
                        pendingDeleteFileUri = contentUri
                        pendingDeletePosition = position
                        val intentSender = recoverableSecurityException.userAction.actionIntent.intentSender
                        val intentSenderRequest = IntentSenderRequest.Builder(intentSender).build()
                        deleteFileLauncher.launch(intentSenderRequest)
                    } else {
                        Toast.makeText(context, "خطا در حذف فایل", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "خطا در حذف فایل", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            cursor?.close()
            Toast.makeText(context, "فایل یافت نشد", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateListAfterDeletion(position: Int) {
        if (position < 0 || position >= filesList.size) {
            Toast.makeText(context, "فایل یافت نشد", Toast.LENGTH_SHORT).show()
            return
        }
        filesList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, filesList.size)
    }
}