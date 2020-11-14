package pro.filemanager.images

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.signature.MediaStoreSignature
import kotlinx.coroutines.launch
import pro.filemanager.R
import pro.filemanager.core.tools.SelectorTool
import pro.filemanager.databinding.LayoutImageItemBinding
import pro.filemanager.files.FileManager

class ImageBrowserAdapter(val context: Context, val imageItems: MutableList<ImageItem>, val layoutInflater: LayoutInflater, val hostFragment: ImageBrowserFragment) : RecyclerView.Adapter<ImageBrowserAdapter.ImageItemViewHolder>() {

    class ImageItemViewHolder(val context: Context, val binding: LayoutImageItemBinding, val hostFragment: ImageBrowserFragment, val adapter: ImageBrowserAdapter) : RecyclerView.ViewHolder(binding.root) {
        lateinit var item: ImageItem

        init {
            binding.layoutImageItemRootLayout.apply {
                setOnClickListener {
                    hostFragment.viewModel.selectorTool?.handleClickInViewHolder(SelectorTool.CLICK_SHORT, adapterPosition) {
                        FileManager.openFile(context, item.data)
                    }
                }

                setOnLongClickListener {
                    hostFragment.viewModel.selectorTool?.handleClickInViewHolder(SelectorTool.CLICK_LONG, adapterPosition)

                    true
                }
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageItemViewHolder {
        return ImageItemViewHolder(context, LayoutImageItemBinding.inflate(layoutInflater, parent, false), hostFragment, this)
    }

    override fun onBindViewHolder(holder: ImageItemViewHolder, position: Int) {

        holder.item = imageItems[position]

        hostFragment.MainScope.launch {

            ImageCore.glideRequestBuilder
                    .load(holder.item.data)
                    .override(holder.binding.layoutImageItemThumbnail.width, holder.binding.layoutImageItemThumbnail.height)
                    .signature(MediaStoreSignature(ImageCore.MIME_TYPE, imageItems[position].dateModified.toLong(), 0))
                    .into(holder.binding.layoutImageItemThumbnail)

        }

        hostFragment.MainScope.launch {
            if(hostFragment.viewModel.selectorTool?.selectedPositions!!.contains(position)) {
                holder.binding.layoutImageItemIconCheck.visibility = View.VISIBLE
                holder.binding.layoutImageItemThumbnail.setColorFilter(Color.argb(120, 0, 0, 0))

            } else {
                holder.binding.layoutImageItemIconCheck.visibility = View.INVISIBLE
                holder.binding.layoutImageItemThumbnail.colorFilter = null
            }
        }

    }

    override fun getItemCount(): Int {
        return imageItems.size
    }
}