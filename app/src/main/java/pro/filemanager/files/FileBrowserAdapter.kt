package pro.filemanager.files

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.signature.MediaStoreSignature
import pro.filemanager.R
import pro.filemanager.databinding.LayoutFileItemBinding
import pro.filemanager.images.ImageCore
import pro.filemanager.videos.VideoCore
import java.io.File

class FileBrowserAdapter(val context: Context, val files: Array<File>, val layoutInflater: LayoutInflater, val fileBrowserFragment: FileBrowserFragment) : RecyclerView.Adapter<FileBrowserAdapter.FileItemViewHolder>() {

    class FileItemViewHolder(val context: Context, val binding: LayoutFileItemBinding, val fileBrowserFragment: FileBrowserFragment) : RecyclerView.ViewHolder(binding.root) {
        lateinit var file: File
        var mimeType: String? = null

        init {
            binding.layoutFileItemRootLayout.apply {
                setOnClickListener {
                    if(file.isDirectory) {
                        fileBrowserFragment.navigate(file.absolutePath)
                    } else {
                        FileCore.openFileOut(this@FileItemViewHolder.context, file.absolutePath)
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
//        holder.mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(holder.file.absolutePath))
//
//        holder.binding.layoutFileItemThumbnailLayout.post {
//            holder.binding.layoutFileItemTitle.text = holder.file.name
//            holder.binding.layoutFileItemTitle.textSize = (holder.binding.layoutFileItemThumbnailLayout.width / 15).toFloat()
//        }
//
//        if(holder.file.isDirectory) {
//            ImageCore.glideSimpleRequestBuilder
//                    .load(R.drawable.ic_folder)
//                    .centerInside()
//                    .into(holder.binding.layoutFileItemThumbnail)
//        } else if(holder.mimeType != null) {
//            if(holder.mimeType!!.contains("image/", true)) {
//                if(!holder.file.absolutePath.endsWith(".gif", true)) {
//                    ImageCore.glideBitmapRequestBuilder
//                            .load(holder.file.absolutePath)
//                            .override(holder.binding.layoutFileItemImageInclude.layoutImageItemThumbnail.width, holder.binding.layoutFileItemImageInclude.layoutImageItemThumbnail.height)
//                            .signature(MediaStoreSignature(ImageCore.MIME_TYPE, files[position].lastModified(), 0))
//                            .into(holder.binding.layoutFileItemImageInclude.layoutImageItemThumbnail)
//                } else {
//                    ImageCore.glideGifRequestBuilder
//                            .load(holder.file.absolutePath)
//                            .override(holder.binding.layoutFileItemImageInclude.layoutImageItemThumbnail.width, holder.binding.layoutFileItemImageInclude.layoutImageItemThumbnail.height)
//                            .signature(MediaStoreSignature(ImageCore.MIME_TYPE, files[position].lastModified().toLong(), 0))
//                            .into(holder.binding.layoutFileItemImageInclude.layoutImageItemThumbnail)
//                }
//            } else if(holder.mimeType!!.contains("video/", true)) {
//                VideoCore.glideRequestBuilder
//                        .load(holder.file.absolutePath)
//                        .override(holder.binding.layoutFileItemVideoInclude.layoutVideoItemThumbnail.width, holder.binding.layoutFileItemVideoInclude.layoutVideoItemThumbnail.height)
//                        .signature(MediaStoreSignature(VideoCore.MIME_TYPE, files[position].lastModified(), 0))
//                        .into(holder.binding.layoutFileItemVideoInclude.layoutVideoItemThumbnail)
//            }
//        }
//
//        if(holder.file.isHidden) {
//            holder.binding.layoutFileItemThumbnail.alpha = 0.5f
//        } else {
//            holder.binding.layoutFileItemThumbnail.alpha = 1f
//        }
    }

    override fun getItemCount(): Int {
        return files.size
    }
}