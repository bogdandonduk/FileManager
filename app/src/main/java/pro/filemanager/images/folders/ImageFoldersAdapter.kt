package pro.filemanager.images.folders

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import pro.filemanager.R
import pro.filemanager.core.tools.SelectionTool
import pro.filemanager.databinding.LayoutImageFolderItemBinding
import pro.filemanager.files.FileCore
import pro.filemanager.images.ImageCore

class ImageFoldersAdapter(val context: Context, var imageFolderItems: MutableList<ImageFolderItem>, val layoutInflater: LayoutInflater, val hostFragment: ImageFoldersFragment) : RecyclerView.Adapter<ImageFoldersAdapter.ImageFolderItemViewHolder>() {
    var lastSelectionModeState = false

    class ImageAlbumsAdapterDiffCallback(
            val oldItems: MutableList<ImageFolderItem>,
            val newItems: MutableList<ImageFolderItem>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldItems.size

        override fun getNewListSize(): Int = newItems.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                oldItems[oldItemPosition].data == newItems[newItemPosition].data

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean = oldItems[oldItemPosition].equals(newItems[newItemPosition])
    }

    fun submitItems(newItems: MutableList<ImageFolderItem>) {
        val diffResult: DiffUtil.DiffResult = DiffUtil.calculateDiff(ImageAlbumsAdapterDiffCallback(imageFolderItems, newItems), true)

        imageFolderItems = newItems

        diffResult.dispatchUpdatesTo(this)
    }

    fun submitItemsWithoutDiff(newItems: MutableList<ImageFolderItem>) {
        imageFolderItems = newItems

        notifyDataSetChanged()
    }

    class ImageFolderItemViewHolder(val context: Context, val binding: LayoutImageFolderItemBinding, val hostFragment: ImageFoldersFragment, val adapter: ImageFoldersAdapter) : RecyclerView.ViewHolder(binding.root) {
        lateinit var item: ImageFolderItem

        init {
            binding.layoutImageFolderItemContentLayout.apply {
                setOnClickListener {
                    if(this@ImageFolderItemViewHolder::item.isInitialized) {
                        hostFragment.viewModel.MainScope?.launch {
                            @Suppress("UNCHECKED_CAST")
                            hostFragment.viewModel.selectionTool.handleClickInViewHolder(
                                    SelectionTool.CLICK_SHORT,
                                    adapterPosition,
                                    item.data,
                                    adapter as RecyclerView.Adapter<RecyclerView.ViewHolder>
                            ) {
                                hostFragment.navController.navigate(R.id.action_imageFoldersFragment_to_imageLibraryFragment, bundleOf(
                                        FileCore.KEY_ARGUMENT_ALBUM_PARCELABLE to item
                                ))
                            }
                        }
                    }
                }

                setOnLongClickListener {
                    if(this@ImageFolderItemViewHolder::item.isInitialized) {
                        hostFragment.viewModel.MainScope?.launch {
                            @Suppress("UNCHECKED_CAST")
                            hostFragment.viewModel.selectionTool.handleClickInViewHolder(
                                    SelectionTool.CLICK_LONG,
                                    adapterPosition,
                                    item.data,
                                    adapter as RecyclerView.Adapter<RecyclerView.ViewHolder>,
                                    {
                                        hostFragment.viewModel.selectionTool.initSelectionState(
                                                hostFragment.activity,
                                                adapter,
                                                hostFragment.binding.fragmentImageFoldersBottomToolBarInclude.layoutBottomToolBarFoldersRootLayout,
                                                hostFragment.binding.fragmentImageFoldersBottomTabsBarInclude.layoutBottomTabsBarRootLayout,
                                                hostFragment.binding.fragmentImageFoldersAppBarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayout,
                                                hostFragment.binding.fragmentImageFoldersAppBarInclude.layoutSelectionBarInclude.layoutSelectionBarContentLayoutSelectionCountCb,
                                                hostFragment.viewModel.selectionTool.selectionMode,
                                                hostFragment.viewModel.selectionTool.selectedPaths.size
                                        )
                                    }
                            )
                        }
                    }

                    true
                }
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageFolderItemViewHolder =
            ImageFolderItemViewHolder(context, LayoutImageFolderItemBinding.inflate(layoutInflater, parent, false), hostFragment, this)


    override fun onBindViewHolder(holder: ImageFolderItemViewHolder, position: Int) {
        holder.item = imageFolderItems[position]

        hostFragment.viewModel.MainScope?.launch {
            if (!holder.item.containedItems.first().data.endsWith(".gif", true)) {
                ImageCore.glideBitmapRequestBuilder
                        .load(holder.item.containedItems.first().data)
                        .override(holder.binding.layoutImageFolderItemContentLayout.width, holder.binding.layoutImageFolderItemThumbnail.height)
                        .into(holder.binding.layoutImageFolderItemThumbnail)
            } else {
                ImageCore.glideGifRequestBuilder
                        .load(holder.item.containedItems.first().data)
                        .override(holder.binding.layoutImageFolderItemThumbnail.width, holder.binding.layoutImageFolderItemThumbnail.height)
                        .into(holder.binding.layoutImageFolderItemThumbnail)
            }

            holder.binding.layoutImageFolderItemCard.post {
                holder.binding.layoutImageFolderItemTitle.text = holder.item.displayName
                holder.binding.layoutImageFolderItemTitle.textSize = (holder.binding.layoutImageFolderItemCard.width / 30).toFloat()
                holder.binding.layoutImageFolderItemCount.text = holder.item.containedItems.size.toString()
                holder.binding.layoutImageFolderItemCount.textSize = (holder.binding.layoutImageFolderItemCard.width / 40).toFloat()
            }

            hostFragment.viewModel.selectionTool.differentiateItem(
                    holder.item.data,
                    holder.binding.layoutImageFolderItemThumbnail,
                    holder.binding.layoutImageFolderItemIconCheck,
                    holder.binding.layoutImageFolderItemIconUnchecked
            )
        }

        if(hostFragment.viewModel.selectionTool.selectionMode && hostFragment.binding.fragmentImageFoldersAppBarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayout.visibility == View.VISIBLE) {
            hostFragment.binding.fragmentImageFoldersAppBarInclude.layoutSelectionBarInclude.layoutSelectionBarContentLayoutSelectionCountCb.text =
                    hostFragment.viewModel.selectionTool.selectedPaths.size.toString()
        }

        if(lastSelectionModeState != hostFragment.viewModel.selectionTool.selectionMode) {
            lastSelectionModeState = hostFragment.viewModel.selectionTool.selectionMode

            hostFragment.viewModel.MainScope?.launch {
                @Suppress("UNCHECKED_CAST")
                hostFragment.viewModel.selectionTool.initSelectionState(
                        hostFragment.activity,
                        this@ImageFoldersAdapter as RecyclerView.Adapter<RecyclerView.ViewHolder>,
                        hostFragment.binding.fragmentImageFoldersBottomToolBarInclude.layoutBottomToolBarFoldersRootLayout,
                        hostFragment.binding.fragmentImageFoldersBottomTabsBarInclude.layoutBottomTabsBarRootLayout,
                        hostFragment.binding.fragmentImageFoldersAppBarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayout,
                        hostFragment.binding.fragmentImageFoldersAppBarInclude.layoutSelectionBarInclude.layoutSelectionBarContentLayoutSelectionCountCb,
                        hostFragment.viewModel.selectionTool.selectionMode,
                        hostFragment.viewModel.selectionTool.selectedPaths.size
                )
            }
        }
    }

    override fun getItemCount(): Int = imageFolderItems.size

}