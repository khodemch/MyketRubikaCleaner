package com.mch.myketrubikacleaner.adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.mch.myketrubikacleaner.R
import java.io.File

class FileListAdapter(
    private val context: Context,
    private val fileList: MutableList<File>,
    private val onFileSelected: (File, Boolean) -> Unit
) : RecyclerView.Adapter<FileListAdapter.ViewHolder>() {

    private val selectedFiles: MutableSet<File> = mutableSetOf()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fileName: TextView = itemView.findViewById(R.id.file_name)
        val fileSize: TextView = itemView.findViewById(R.id.file_size)
        val fileCheckbox: CheckBox = itemView.findViewById(R.id.file_checkbox)

        init {
            fileCheckbox.setOnCheckedChangeListener { _, isChecked ->
                val file = fileList[adapterPosition]
                if (isChecked) {
                    selectedFiles.add(file)
                } else {
                    selectedFiles.remove(file)
                }
                onFileSelected(file, isChecked)
            }

            itemView.setOnClickListener {
                val file = fileList[adapterPosition]
                openFile(file)
            }
        }

        private fun openFile(file: File) {
            try {
                val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(uri, context.contentResolver.getType(uri))
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "فایل باز نمی شود", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_file, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = fileList[position]
        holder.fileName.text = file.name
        holder.fileSize.text = "${file.length() / 1024 / 1024} MB" // Size in MB
        holder.fileCheckbox.isChecked = selectedFiles.contains(file)
    }

    override fun getItemCount(): Int = fileList.size

    fun getSelectedFiles(): Set<File> = selectedFiles
}
