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
import com.mch.myketrubikacleaner.databinding.MovierecyclerLayoutBinding
import com.mch.myketrubikacleaner.models.MoviesModel
import java.io.File

class MoviesAdapter(
    private val context: Context,
    private val items: MutableList<MoviesModel>
) : RecyclerView.Adapter<MoviesAdapter.ItemViewHolder>() {

    private var pendingDeleteVideoUri: Uri? = null
    private var pendingDeletePosition: Int = -1
    private lateinit var deleteVideoLauncher: ActivityResultLauncher<IntentSenderRequest>

    inner class ItemViewHolder(val binding: MovierecyclerLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    init {
        if (context is AppCompatActivity) {
            deleteVideoLauncher = (context as AppCompatActivity).registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK && pendingDeleteVideoUri != null && pendingDeletePosition != -1) {
                    try {
                        context.contentResolver.delete(pendingDeleteVideoUri!!, null, null)
                        val position = pendingDeletePosition
                        pendingDeleteVideoUri = null
                        pendingDeletePosition = -1
                        if (position != -1) {
                            updateListAfterDeletion(position)
                        }
                        Toast.makeText(context, "ویدیو با موفقیت حذف شد", Toast.LENGTH_SHORT).show()
                    } catch (e: SecurityException) {
                        Toast.makeText(context, "خطا در حذف ویدیو", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "خطا در حذف ویدیو", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = MovierecyclerLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val videoModel = items[position]
        Glide.with(context).load(File(videoModel.path)).into(holder.binding.showMovieImageView)

        holder.binding.deleteMovieImageView.setOnClickListener {
            showDeleteConfirmationDialog(position)
        }

        holder.binding.showMovieImageView.setOnClickListener {
            playVideo(videoModel.path)
        }
    }

    override fun getItemCount(): Int = items.size

    private fun showDeleteConfirmationDialog(position: Int) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.custom_alert_dialog, null)
        val dialog = AlertDialog.Builder(context, R.style.YourAlertDialogTheme).apply {
            setView(dialogView)
        }.create()

        val title = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val message = dialogView.findViewById<TextView>(R.id.dialogMessage)
        val positiveButton = dialogView.findViewById<Button>(R.id.btnPositive)
        val negativeButton = dialogView.findViewById<Button>(R.id.btnNegative)

        title.text = "حذف ویدیو"
        message.text = "آیا مطمئن هستید که می‌خواهید این ویدیو را حذف کنید؟"

        positiveButton.setOnClickListener {
            deleteVideo(position)
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

    private fun deleteVideo(position: Int) {
        if (position < 0 || position >= items.size) {
            Toast.makeText(context, "ویدیو یافت نشد", Toast.LENGTH_SHORT).show()
            return
        }

        val videoFile = File(items[position].path)
        if (videoFile.exists()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                deleteVideoForAndroidQAndAbove(position, videoFile)
            } else {
                val deleted = videoFile.delete()
                if (deleted) {
                    updateListAfterDeletion(position)
                    Toast.makeText(context, "ویدیو با موفقیت حذف شد", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "خطا در حذف ویدیو", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "ویدیو موجود نیست", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteVideoForAndroidQAndAbove(position: Int, videoFile: File) {
        val videoPath = items[position].path
        val contentResolver = context.contentResolver
        val uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val cursor = contentResolver.query(
            uri,
            arrayOf(MediaStore.Video.Media._ID),
            "${MediaStore.Video.Media.DATA}=?",
            arrayOf(videoPath),
            null
        )

        if (cursor != null && cursor.moveToFirst()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID))
            val contentUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
            cursor.close()

            try {
                contentResolver.delete(contentUri, null, null)
                updateListAfterDeletion(position)
                Toast.makeText(context, "ویدیو با موفقیت حذف شد", Toast.LENGTH_SHORT).show()
            } catch (e: SecurityException) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val recoverableSecurityException = e as? RecoverableSecurityException
                    if (recoverableSecurityException != null) {
                        pendingDeleteVideoUri = contentUri
                        pendingDeletePosition = position
                        val intentSender = recoverableSecurityException.userAction.actionIntent.intentSender
                        val intentSenderRequest = IntentSenderRequest.Builder(intentSender).build()
                        deleteVideoLauncher.launch(intentSenderRequest)
                    } else {
                        Toast.makeText(context, "خطا در حذف ویدیو", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "خطا در حذف ویدیو", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            cursor?.close()
            Toast.makeText(context, "ویدیو یافت نشد", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateListAfterDeletion(position: Int) {
        if (position < 0 || position >= items.size) {
            Toast.makeText(context, "ویدیو یافت نشد", Toast.LENGTH_SHORT).show()
            return
        }

        items.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, items.size)
    }

    private fun playVideo(path: String) {
        val videoFile = File(path)
        if (videoFile.exists()) {
            val uri = FileProvider.getUriForFile(context, context.applicationContext.packageName + ".provider", videoFile)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(uri, "video/*")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.startActivity(intent)
        } else {
            Toast.makeText(context, "ویدیو موجود نیست", Toast.LENGTH_SHORT).show()
        }
    }
}