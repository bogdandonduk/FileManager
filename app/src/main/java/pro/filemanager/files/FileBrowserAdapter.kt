package pro.filemanager.files

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import pro.filemanager.databinding.LayoutFileItemBinding
import java.io.File

class FileBrowserAdapter(val context: Context, val files: Array<File>, val layoutInflater: LayoutInflater, val fileBrowserFragment: FileBrowserFragment) : RecyclerView.Adapter<FileBrowserAdapter.FileItemViewHolder>() {

    class FileItemViewHolder(val context: Context, val binding: LayoutFileItemBinding, val fileBrowserFragment: FileBrowserFragment) : RecyclerView.ViewHolder(binding.root) {
        lateinit var file: File

        init {
            binding.layoutFileItemRootLayout.apply {
                setOnClickListener {
                    if(file.isDirectory) {
                        fileBrowserFragment.navigate(file.absolutePath)
                    } else {
//                        FileCore.openFileOut(this@FileItemViewHolder.context, file.absolutePath)
                    }
                }

                setOnLongClickListener {

                    true
                }
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileItemViewHolder {
        return FileItemViewHolder(context, LayoutFileItemBinding.inflate(layoutInflater, parent, false), fileBrowserFragment)
    }

    override fun onBindViewHolder(holder: FileItemViewHolder, position: Int) {

        holder.file = files[position]
        holder.binding.layoutFileItemFilename.text = holder.file.name

    }

    override fun getItemCount(): Int {
        return files.size
    }
}