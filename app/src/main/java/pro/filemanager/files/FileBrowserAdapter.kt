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

class FileBrowserAdapter(val context: Context, val fileItems: MutableList<FileItem>, val layoutInflater: LayoutInflater) : RecyclerView.Adapter<FileBrowserAdapter.FileItemViewHolder>() {

    class FileItemViewHolder(val context: Context, val binding: LayoutFileItemBinding) : RecyclerView.ViewHolder(binding.root) {
        lateinit var item: FileItem

        init {
            binding.layoutFileItemRootLayout.setOnClickListener {

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileItemViewHolder {
        return FileItemViewHolder(context, DataBindingUtil.inflate(layoutInflater, R.layout.layout_file_item, parent, false))
    }

    override fun onBindViewHolder(holder: FileItemViewHolder, position: Int) {

        holder.item = fileItems[position]
        holder.binding.fileItem = holder.item
    }

    override fun getItemCount(): Int {
        return fileItems.size
    }
}