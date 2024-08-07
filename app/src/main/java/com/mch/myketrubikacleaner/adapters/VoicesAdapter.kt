package com.mch.myketrubikacleaner.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.mch.myketrubikacleaner.R
import com.mch.myketrubikacleaner.models.VoicesModel
import com.mch.myketrubikacleaner.databinding.VoicerecyclerLayoutBinding
import java.io.File

class VoicesAdapter(
    private val context: Context,
    private val voicesList: MutableList<VoicesModel>,
    private val playVoiceCallback: (String) -> Unit,
    private val stopVoiceCallback: () -> Unit
) : RecyclerView.Adapter<VoicesAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: VoicerecyclerLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.deleteVoiceImageView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    showDeleteConfirmationDialog(position)
                }
            }

            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val voiceModel = voicesList[position]
                    playVoiceCallback(voiceModel.path)
                }
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = VoicerecyclerLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val voiceModel = voicesList[position]
        holder.binding.voiceImageView.setImageResource(R.drawable.voices) // Placeholder for voice icon
        holder.binding.voicesName.text = voiceModel.name

        // Display the voice file size
        val formattedSize = formatFileSize(voiceModel.size)
        holder.binding.informationVoiceTextView.text = formattedSize
    }

    override fun getItemCount(): Int {
        return voicesList.size
    }

    private fun showDeleteConfirmationDialog(position: Int) {
        // Inflate custom dialog layout
        val dialogView = LayoutInflater.from(context).inflate(R.layout.custom_alert_dialog, null)
        val dialog = AlertDialog.Builder(context, R.style.YourAlertDialogTheme).apply {
            setView(dialogView)
        }.create()

        // Initialize views from custom dialog layout
        val title = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val message = dialogView.findViewById<TextView>(R.id.dialogMessage)
        val positiveButton = dialogView.findViewById<Button>(R.id.btnPositive)
        val negativeButton = dialogView.findViewById<Button>(R.id.btnNegative)

        // Set title and message
        title.text = "حذف فایل صوتی"
        message.text = "آیا مطمئن هستید که می‌خواهید این فایل صوتی را حذف کنید؟"

        // Handle button clicks
        positiveButton.setOnClickListener {
            deleteVoice(position)
            dialog.dismiss()
        }

        negativeButton.setOnClickListener {
            dialog.dismiss()
        }

        // Prevent dialog from being dismissed by clicking outside
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        // Show the dialog
        dialog.show()
    }

    private fun deleteVoice(position: Int) {
        val voiceFile = File(voicesList[position].path)
        if (voiceFile.exists()) {
            val deleted = voiceFile.delete()
            if (deleted) {
                voicesList.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, voicesList.size)
                Toast.makeText(context, "فایل صوتی با موفقیت حذف شد", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "خطا در حذف فایل صوتی", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun formatFileSize(sizeInBytes: Long): String {
        return if (sizeInBytes < 1024) {
            "$sizeInBytes B"
        } else if (sizeInBytes < 1024 * 1024) {
            "${sizeInBytes / 1024} KB"
        } else {
            String.format("%.2f MB", sizeInBytes / (1024.0 * 1024.0))
        }
    }
}