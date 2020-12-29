package pro.filemanager.images.folders

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.signature.MediaStoreSignature
import kotlinx.coroutines.launch
import pro.filemanager.R
import pro.filemanager.core.tools.SelectionTool
import pro.filemanager.databinding.LayoutImageFolderItemBinding
import pro.filemanager.files.FileCore
import pro.filemanager.images.ImageCore
import pro.filemanager.images.ImageItem
import java.io.File

class ImageFoldersAdapter(val context: Context, var imageFolderItems: MutableList<ImageFolderItem>, val layoutInflater: LayoutInflater, val hostFragment: ImageFoldersFragment) : ListAdapter<ImageFolderItem, ImageFoldersAdapter.ImageFolderItemViewHolder>(object : DiffUtil.ItemCallback<ImageFolderItem>() {

    override fun areItemsTheSame(oldItem: ImageFolderItem, newItem: ImageFolderItem): Boolean {
        return oldItem.data == newItem.data
    }

    override fun areContentsTheSame(oldItem: ImageFolderItem, newItem: ImageFolderItem): Boolean {
        return oldItem.equals(newItem)
    }
}) {
    var lastSelectionModeState = false

    init {
        submitList(imageFolderItems)
    }

    override fun submitList(list: MutableList<ImageFolderItem>?) {
        super.submitList(list)

        currentList.forEachIndexed { i: Int, _: ImageFolderItem ->
            notifyItemChanged(i)
        }
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
                                    context,
                                    adapterPosition,
                                    item.data,
                                    adapter as RecyclerView.Adapter<RecyclerView.ViewHolder>,
                            ) {
                                hostFragment.navController.navigate(R.id.action_imageFoldersFragment_to_imageLibraryFragment, bundleOf(
                                        FileCore.KEY_ARGUMENT_FOLDER_PARCELABLE to item
                                ))
                            }

                            hostFragment.viewModel.selectionTool.initSelectionCheckBox(hostFragment.binding.fragmentImageFoldersAppBarInclude.layoutSelectionBarInclude.layoutSelectionBarContentLayoutSelectionCountCb, adapter.itemCount)
                        }
                    }
                }

                setOnLongClickListener {
                    if(this@ImageFolderItemViewHolder::item.isInitialized) {
                        hostFragment.viewModel.MainScope?.launch {
                            if(!hostFragment.viewModel.selectionTool.selectionMode) {
                                hostFragment.binding.fragmentImageFoldersBottomToolBarInclude.layoutBottomToolBarFoldersRootLayout.visibility = View.VISIBLE
                            }

                            @Suppress("UNCHECKED_CAST")
                            hostFragment.viewModel.selectionTool.handleClickInViewHolder(
                                    SelectionTool.CLICK_LONG,
                                    context,
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

                            hostFragment.viewModel.selectionTool.initSelectionCheckBox(hostFragment.binding.fragmentImageFoldersAppBarInclude.layoutSelectionBarInclude.layoutSelectionBarContentLayoutSelectionCountCb, adapter.itemCount)
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
        holder.item = currentList[position]

        hostFragment.viewModel.MainScope?.launch {
            if (!holder.item.containedItems.first().data.endsWith(".gif", true)) {
                ImageCore.glideBitmapRequestBuilder
                        .load(holder.item.containedItems.first().data)
                        .override(holder.binding.layoutImageFolderItemContentLayout.width, holder.binding.layoutImageFolderItemThumbnail.height)
                        .into(holder.binding.layoutImageFolderItemThumbnail)
            } else {
                ImageCore.glideGifRequestBuilder
                        .load(holder.item.containedItems.first().data).signature(MediaStoreSignature(ImageCore.MIME_TYPE, holder.item.containedImages.first().dateModified, 0))
                        .override(holder.binding.layoutImageFolderItemThumbnail.width, holder.binding.layoutImageFolderItemThumbnail.height)
                        .into(holder.binding.layoutImageFolderItemThumbnail)
            }

            hostFragment.viewModel.selectionTool.differentiateItem(
                    holder.item.data,
                    holder.binding.layoutImageFolderItemThumbnail,
                    holder.binding.layoutImageFolderItemIconCheck,
                    holder.binding.layoutImageFolderItemIconUnchecked
            )

            FileCore.findExternalRoots(context).run {
                if(!this.isNullOrEmpty()) {
                    this.forEach {
                        if(holder.item.data.contains(it)) {
                            holder.binding.layoutImageFolderItemSdCardIcon.visibility = View.VISIBLE
                        } else {
                            holder.binding.layoutImageFolderItemSdCardIcon.visibility = View.INVISIBLE
                        }
                    }
                }
            }
        }

        holder.binding.layoutImageFolderItemCard.post {
            holder.binding.layoutImageFolderItemCard.width.let {
                holder.binding.layoutImageFolderItemTitle.textSize = (it / 30).toFloat()
                holder.binding.layoutImageFolderItemCount.textSize = (it / 40).toFloat()
            }
        }

        holder.binding.layoutImageFolderItemTitle.text = holder.item.displayName
        holder.binding.layoutImageFolderItemCount.text = holder.item.containedItems.size.toString()

        if(lastSelectionModeState != hostFragment.viewModel.selectionTool.selectionMode && hostFragment.viewModel.selectionTool.selectedPaths.size == itemCount) {
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

        if(hostFragment.viewModel.selectionTool.selectionMode && hostFragment.binding.fragmentImageFoldersAppBarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayout.visibility == View.VISIBLE) {
            hostFragment.binding.fragmentImageFoldersAppBarInclude.layoutSelectionBarInclude.layoutSelectionBarContentLayoutSelectionCountCb.text =
                    hostFragment.viewModel.selectionTool.selectedPaths.size.toString()
        }
    }

    override fun getItemCount(): Int = currentList.size

}