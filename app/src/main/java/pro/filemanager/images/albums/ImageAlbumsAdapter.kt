package pro.filemanager.images.albums

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import pro.filemanager.HomeActivity
import pro.filemanager.R
import pro.filemanager.core.tools.SelectionTool
import pro.filemanager.databinding.LayoutImageAlbumItemBinding
import pro.filemanager.images.ImageCore

class ImageAlbumsAdapter(val context: Context, var imageAlbumItems: MutableList<ImageAlbumItem>, val layoutInflater: LayoutInflater, val hostFragment: ImageAlbumsFragment) : RecyclerView.Adapter<ImageAlbumsAdapter.ImageAlbumItemViewHolder>() {

    class ImageAlbumItemViewHolder(val context: Context, val binding: LayoutImageAlbumItemBinding, val hostFragment: ImageAlbumsFragment, val adapter: ImageAlbumsAdapter) : RecyclerView.ViewHolder(binding.root) {
        lateinit var item: ImageAlbumItem

        init {
            binding.layoutImageAlbumItemContentLayout.apply {
                setOnClickListener {
                    if(this@ImageAlbumItemViewHolder::item.isInitialized) {
                        hostFragment.viewModel.MainScope?.launch {
                            @Suppress("UNCHECKED_CAST")
                            hostFragment.viewModel.selectionTool?.handleClickInViewHolder(
                                    SelectionTool.CLICK_SHORT,
                                    adapterPosition,
                                    item.data,
                                    adapter as RecyclerView.Adapter<RecyclerView.ViewHolder>
                            ) {
                                hostFragment.navController.navigate(R.id.action_imageAlbumsFragment_to_imageBrowserFragment, bundleOf(
                                        ImageCore.KEY_ARGUMENT_ALBUM_PARCELABLE to item
                                ))
                            }
                        }
                    }
                }

                setOnLongClickListener {
                    if(this@ImageAlbumItemViewHolder::item.isInitialized) {
                        hostFragment.viewModel.MainScope?.launch {
                            @Suppress("UNCHECKED_CAST")
                            hostFragment.viewModel.selectionTool?.handleClickInViewHolder(
                                    SelectionTool.CLICK_LONG,
                                    adapterPosition,
                                    item.data,
                                    adapter as RecyclerView.Adapter<RecyclerView.ViewHolder>
                            )
                        }
                    }

                    true
                }
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageAlbumItemViewHolder =
            ImageAlbumItemViewHolder(context, LayoutImageAlbumItemBinding.inflate(layoutInflater, parent, false), hostFragment, this)


    override fun onBindViewHolder(holder: ImageAlbumItemViewHolder, position: Int) {
        holder.item = imageAlbumItems[position]

        hostFragment.viewModel.MainScope?.launch {
            if (!holder.item.containedItems.first().data.endsWith(".gif", true)) {
                ImageCore.glideBitmapRequestBuilder
                        .load(holder.item.containedItems.first().data)
                        .override(holder.binding.layoutImageAlbumItemContentLayout.width, holder.binding.layoutImageAlbumItemThumbnail.height)
                        .into(holder.binding.layoutImageAlbumItemThumbnail)
            } else {
                ImageCore.glideGifRequestBuilder
                        .load(holder.item.containedItems.first().data)
                        .override(holder.binding.layoutImageAlbumItemThumbnail.width, holder.binding.layoutImageAlbumItemThumbnail.height)
                        .into(holder.binding.layoutImageAlbumItemThumbnail)
            }

            holder.binding.layoutImageAlbumItemCard.post {
                holder.binding.layoutImageAlbumItemTitle.text = holder.item.displayName
                holder.binding.layoutImageAlbumItemTitle.textSize = (holder.binding.layoutImageAlbumItemCard.width / 30).toFloat()
                holder.binding.layoutImageAlbumItemCount.text = holder.item.containedItems.size.toString()
                holder.binding.layoutImageAlbumItemCount.textSize = (holder.binding.layoutImageAlbumItemCard.width / 40).toFloat()
            }

            hostFragment.viewModel.selectionTool?.differentiateItem(
                    holder.item.data,
                    holder.binding.layoutImageAlbumItemThumbnail,
                    holder.binding.layoutImageAlbumItemIconCheck,
                    holder.binding.layoutImageAlbumItemIconUnchecked
            )
        }

        hostFragment.viewModel.MainScope?.launch {
            @Suppress("UNCHECKED_CAST")
            hostFragment.viewModel.selectionTool?.initSelectionState(
                    hostFragment.activity,
                    this@ImageAlbumsAdapter as RecyclerView.Adapter<RecyclerView.ViewHolder>,
                    hostFragment.binding.fragmentImageAlbumsBottomToolBarInclude.layoutBottomToolBarAlbumRootLayout,
                    hostFragment.binding.fragmentImageAlbumsBottomTabsBarInclude.layoutBottomTabsBarRootLayout,
                    hostFragment.binding.fragmentImageAlbumsToolbarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayout,
                    hostFragment.binding.fragmentImageAlbumsToolbarInclude.layoutSelectionBarInclude.layoutSelectionBarContentLayoutSelectionCountCb,
                    hostFragment.viewModel.selectionTool!!.selectionMode,
                    hostFragment.viewModel.selectionTool!!.selectedPaths.size
            )
        }
    }

    override fun getItemCount(): Int = imageAlbumItems.size

}