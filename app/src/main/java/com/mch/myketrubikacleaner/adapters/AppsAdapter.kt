package com.mch.myketrubikacleaner.adapters

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.mch.myketrubikacleaner.R
import com.mch.myketrubikacleaner.models.AppsModel
import com.mch.myketrubikacleaner.databinding.ApprecyclerLayoutBinding
import java.io.File

class AppsAdapter(private val context: Activity, private val appsList: MutableList<AppsModel>) :
    RecyclerView.Adapter<AppsAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ApprecyclerLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.deleteAppImageView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    showDeleteConfirmationDialog(position)
                }
            }

            itemView.setOnClickListener {
                // Optionally handle item click if needed
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ApprecyclerLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val appModel = appsList[position]
        holder.binding.appsImageView.setImageDrawable(appModel.icon)
        holder.binding.appsName.text = appModel.name

        val appSizeInMb = appModel.size / (1024 * 1024)
        holder.binding.informationAppTextView.text = "$appSizeInMb MB"
    }

    override fun getItemCount(): Int {
        return appsList.size
    }

    private fun showDeleteConfirmationDialog(position: Int) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.custom_alert_dialog, null)
        val dialog = AlertDialog.Builder(context).create()
        dialog.setView(dialogView)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val title = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val message = dialogView.findViewById<TextView>(R.id.dialogMessage)
        val positiveButton = dialogView.findViewById<Button>(R.id.btnPositive)
        val negativeButton = dialogView.findViewById<Button>(R.id.btnNegative)

        title.text = "حذف فایل"
        message.text = "آیا مطمئن هستید که می خواهید این فایل را حذف کنید؟"

        positiveButton.setOnClickListener {
            val filePath = appsList[position].packageName
            val file = File(filePath)
            if (file.exists()) {
                file.delete()
                removeAppFromList(position)
                Toast.makeText(context, "فایل با موفقیت حذف شد", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        negativeButton.setOnClickListener {
            dialog.dismiss()
        }

        // Prevent dialog from being dismissed by clicking outside
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.show()
    }

    fun removeAppFromList(position: Int) {
        appsList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, appsList.size)
    }
}