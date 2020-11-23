package pro.filemanager.images.albums

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import pro.filemanager.HomeActivity
import pro.filemanager.R
import pro.filemanager.core.tools.SelectionTool
import pro.filemanager.databinding.LayoutImageAlbumItemBinding
import pro.filemanager.images.ImageCore
import pro.filemanager.images.ImageRepo

class ImageAlbumsAdapter(val context: Context, val imageAlbumItems: MutableList<ImageAlbumItem>, val layoutInflater: LayoutInflater, val hostFragment: ImageAlbumsFragment) : RecyclerView.Adapter<ImageAlbumsAdapter.ImageAlbumItemViewHolder>() {

    class ImageAlbumItemViewHolder(val context: Context, val binding: LayoutImageAlbumItemBinding, val hostFragment: ImageAlbumsFragment, val adapter: ImageAlbumsAdapter) : RecyclerView.ViewHolder(binding.root) {
        lateinit var item: ImageAlbumItem

        init {
            binding.layoutImageAlbumItemContentLayout.apply {
                setOnClickListener {
                    @Suppress("UNCHECKED_CAST")
                    hostFragment.viewModel.selectionTool?.handleClickInViewHolder(SelectionTool.CLICK_SHORT, adapterPosition, adapter as RecyclerView.Adapter<RecyclerView.ViewHolder>, hostFragment.requireActivity() as HomeActivity) {
                        hostFragment.navController.navigate(R.id.action_imageAlbumsFragment_to_imageBrowserFragment, bundleOf(
                                ImageCore.KEY_ARGUMENT_ALBUM_PARCELABLE to item
                        ))
                    }
                }

                setOnLongClickListener {
                    @Suppress("UNCHECKED_CAST")
                    hostFragment.viewModel.selectionTool?.handleClickInViewHolder(SelectionTool.CLICK_LONG, adapterPosition, adapter as RecyclerView.Adapter<RecyclerView.ViewHolder>, hostFragment.requireActivity() as HomeActivity)

                    true
                }
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageAlbumItemViewHolder =
            ImageAlbumItemViewHolder(context, LayoutImageAlbumItemBinding.inflate(layoutInflater, parent, false), hostFragment, this)


    override fun onBindViewHolder(holder: ImageAlbumItemViewHolder, position: Int) {
        holder.item = imageAlbumItems[position]

        hostFragment.MainScope.launch {
            if (!holder.item.containedImages.first().data.endsWith(".gif", true)) {
                ImageCore.glideBitmapRequestBuilder
                        .load(holder.item.containedImages.first().data)
                        .override(holder.binding.layoutImageAlbumItemContentLayout.width, holder.binding.layoutImageAlbumItemThumbnail.height)
                        .into(holder.binding.layoutImageAlbumItemThumbnail)
            } else {
                ImageCore.glideGifRequestBuilder
                        .load(holder.item.containedImages.first().data)
                        .override(holder.binding.layoutImageAlbumItemThumbnail.width, holder.binding.layoutImageAlbumItemThumbnail.height)
                        .into(holder.binding.layoutImageAlbumItemThumbnail)
            }

            holder.binding.layoutImageAlbumItemCard.post {
                holder.binding.layoutImageAlbumItemTitle.text = holder.item.displayName
                holder.binding.layoutImageAlbumItemTitle.textSize = (holder.binding.layoutImageAlbumItemCard.width / 30).toFloat()
                holder.binding.layoutImageAlbumItemCount.text = holder.item.containedImages.size.toString()
                holder.binding.layoutImageAlbumItemCount.textSize = (holder.binding.layoutImageAlbumItemCard.width / 40).toFloat()
            }

            hostFragment.viewModel.selectionTool?.differentiateItem(position, holder.binding.layoutImageAlbumItemThumbnail, holder.binding.layoutImageAlbumItemIconCheck, holder.binding.layoutImageAlbumItemIconUnchecked)

        }

    }

    override fun getItemCount(): Int = imageAlbumItems.size

}