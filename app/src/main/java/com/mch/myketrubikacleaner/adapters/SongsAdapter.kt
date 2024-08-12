package com.mch.myketrubikacleaner.adapters

import android.app.Activity
import android.app.RecoverableSecurityException
import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mch.myketrubikacleaner.R
import com.mch.myketrubikacleaner.databinding.SongsrecyclerLayoutBinding
import java.io.File

class SongsAdapter(
    private val context: Context,
    private val filesList: MutableList<File>,
    private val onItemClick: (File) -> Unit
) : RecyclerView.Adapter<SongsAdapter.ViewHolder>() {

    private var pendingDeleteSongUri: Uri? = null
    private var pendingDeletePosition: Int = -1
    private lateinit var deleteSongLauncher: ActivityResultLauncher<IntentSenderRequest>

    init {
        if (context is AppCompatActivity) {
            deleteSongLauncher = (context as AppCompatActivity).registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK && pendingDeleteSongUri != null && pendingDeletePosition != -1) {
                    try {
                        context.contentResolver.delete(pendingDeleteSongUri!!, null, null)
                        val position = pendingDeletePosition
                        pendingDeleteSongUri = null
                        pendingDeletePosition = -1
                        if (position != -1) {
                            updateListAfterDeletion(position)
                        }
                        Toast.makeText(context, "موسیقی با موفقیت حذف شد", Toast.LENGTH_SHORT).show()
                    } catch (e: SecurityException) {
                        Toast.makeText(context, "خطا در حذف موسیقی", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "خطا در حذف موسیقی", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    inner class ViewHolder(val binding: SongsrecyclerLayoutBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        init {
            itemView.setOnClickListener(this)
            binding.deleteSongImageView.setOnClickListener {
                showDeleteConfirmationDialog(adapterPosition)
            }
        }

        override fun onClick(view: View) {
            val file = filesList[adapterPosition]
            onItemClick(file)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SongsrecyclerLayoutBinding.inflate(
            LayoutInflater.from(context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = filesList[position]
        holder.binding.songsName.text = file.name

        // Load music cover image using Glide
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(file.path)
        val coverBytes = retriever.embeddedPicture
        retriever.release()

        if (coverBytes != null) {
            Glide.with(context)
                .asBitmap()
                .load(coverBytes)
                .into(holder.binding.songImageView)
        } else {
            Glide.with(context)
                .load(R.drawable.icsong) // Default image if no cover found
                .placeholder(R.drawable.icsong)
                .error(R.drawable.icsong)
                .into(holder.binding.songImageView)
        }
    }

    override fun getItemCount(): Int {
        return filesList.size
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

        title.text = "حذف موسیقی"
        message.text = "آیا مطمئن هستید که می‌خواهید این موسیقی را حذف کنید؟"

        positiveButton.setOnClickListener {
            deleteMusic(position)
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

    private fun deleteMusic(position: Int) {
        val musicFile = filesList[position]
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            deleteMusicForAndroidQAndAbove(position, musicFile)
        } else {
            val deleted = musicFile.delete()
            if (deleted) {
                updateListAfterDeletion(position)
                Toast.makeText(context, "موسیقی با موفقیت حذف شد", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "خطا در حذف موسیقی", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteMusicForAndroidQAndAbove(position: Int, file: File) {
        val contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val contentResolver = context.contentResolver
        val selection = "${MediaStore.Audio.Media.DATA} = ?"
        val selectionArgs = arrayOf(file.absolutePath)

        val cursor = contentResolver.query(
            contentUri,
            arrayOf(MediaStore.Audio.Media._ID),
            selection,
            selectionArgs,
            null
        )

        if (cursor != null && cursor.moveToFirst()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
            val uri = ContentUris.withAppendedId(contentUri, id)
            cursor.close()

            try {
                contentResolver.delete(uri, null, null)
                updateListAfterDeletion(position)
                Toast.makeText(context, "موسیقی با موفقیت حذف شد", Toast.LENGTH_SHORT).show()
            } catch (e: SecurityException) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val recoverableException = e as? RecoverableSecurityException
                    if (recoverableException != null) {
                        pendingDeleteSongUri = uri
                        pendingDeletePosition = position
                        val intentSender = recoverableException.userAction.actionIntent.intentSender
                        val intentSenderRequest = IntentSenderRequest.Builder(intentSender).build()
                        deleteSongLauncher.launch(intentSenderRequest)
                    } else {
                        Toast.makeText(context, "خطا در حذف موسیقی", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "خطا در حذف موسیقی", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            cursor?.close()
            Toast.makeText(context, "موسیقی یافت نشد", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateListAfterDeletion(position: Int) {
        if (position < 0 || position >= filesList.size) {
            Toast.makeText(context, "موسیقی یافت نشد", Toast.LENGTH_SHORT).show()
            return
        }
        filesList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, filesList.size)
    }
}
