package pro.filemanager.images

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.signature.MediaStoreSignature
import kotlinx.coroutines.launch
import pro.filemanager.databinding.LayoutImageItemBinding

class ImageBrowserAdapter(val context: Context, val imageItems: MutableList<ImageItem>, val layoutInflater: LayoutInflater, val hostFragment: ImageBrowserFragment) : RecyclerView.Adapter<ImageBrowserAdapter.ImageItemViewHolder>() {

    class ImageItemViewHolder(val context: Context, val binding: LayoutImageItemBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.layoutImageItemRootLayout.setOnClickListener {

            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageItemViewHolder {
        return ImageItemViewHolder(context, LayoutImageItemBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: ImageItemViewHolder, position: Int) {

        hostFragment.MainScope.launch {
            ImageManager.glideImageRequestBuilder
                    .load(imageItems[position].data)
                    .override(holder.binding.layoutImageItemThumbnail.width, holder.binding.layoutImageItemThumbnail.height)
                    .signature(MediaStoreSignature(ImageManager.MIME_TYPE, imageItems[position].dateModified.toLong(), 0))
                    .into(holder.binding.layoutImageItemThumbnail)
        }

    }

    override fun getItemCount(): Int {
        return imageItems.size
    }
}