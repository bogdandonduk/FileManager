package pro.filemanager.files

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import pro.filemanager.R
import pro.filemanager.databinding.LayoutDocItemBinding
import pro.filemanager.databinding.LayoutFileItemBinding
import java.io.File

class FileBrowserAdapter(val context: Context, val files: Array<File>, val layoutInflater: LayoutInflater) : RecyclerView.Adapter<FileBrowserAdapter.FileItemViewHolder>() {

    class FileItemViewHolder(val context: Context, val binding: LayoutFileItemBinding) : RecyclerView.ViewHolder(binding.root) {
        lateinit var file: File

        init {
            binding.layoutFileItemRootLayout.setOnClickListener {
                if(file.isDirectory) {

                } else {

                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileItemViewHolder {
        return FileItemViewHolder(context, LayoutFileItemBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: FileItemViewHolder, position: Int) {

        holder.file = files[position]
        holder.binding.layoutFileItemFilename.text = holder.file.name

    }

    override fun getItemCount(): Int {
        return files.size
    }
}