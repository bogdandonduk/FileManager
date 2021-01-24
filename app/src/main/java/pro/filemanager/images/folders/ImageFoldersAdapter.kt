package pro.filemanager.images.folders

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.signature.MediaStoreSignature
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.coroutines.launch
import pro.filemanager.core.tools.SelectionTool
import pro.filemanager.core.wrappers.GlideWrapper
import pro.filemanager.core.wrappers.MimeTypeWrapper
import pro.filemanager.databinding.LayoutImageFolderItemBinding
import pro.filemanager.files.FileCore
import pro.filemanager.apps.all.AllAppsFragment
import java.util.*

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

    override fun getItemId(position: Int): Long {
        return UUID.nameUUIDFromBytes(currentList[position].data.toByteArray()).mostSignificantBits
    }

    class ImageFolderItemViewHolder(val context: Context, val binding: LayoutImageFolderItemBinding, val hostFragment: ImageFoldersFragment, val adapter: ImageFoldersAdapter) : RecyclerView.ViewHolder(binding.root) {
        lateinit var item: ImageFolderItem

        fun isItemInitialized() : Boolean = this::item.isInitialized

        init {
            binding.layoutImageFolderItemContentLayout.apply {
                setOnClickListener {
                    if(isItemInitialized()) {
                        hostFragment.viewModel.mainScope.launch {
                            @Suppress("UNCHECKED_CAST")
                            hostFragment.viewModel.selectionTool.handleClickInViewHolder(
                                    SelectionTool.CLICK_SHORT,
                                    context,
                                    adapterPosition,
                                    item.data,
                                    adapter as RecyclerView.Adapter<RecyclerView.ViewHolder>,
                            ) {
                                hostFragment.activity.supportFragmentManager.beginTransaction().replace(hostFragment.activity.activityHomeRootDrawerLayout.id, AllAppsFragment().apply {
                                    arguments = bundleOf(FileCore.KEY_ARGUMENT_FOLDER_PARCELABLE to item)
                                }).addToBackStack(null).commit()
                            }
                        }
                    }
                }

                setOnLongClickListener {
                    if(isItemInitialized()) {
                        hostFragment.viewModel.mainScope.launch {
                            if(!hostFragment.viewModel.selectionTool.selectionMode) {
                                hostFragment.binding.fragmentImageFoldersBottomToolBarInclude.layoutBottomToolbarRootLayout.visibility = View.VISIBLE
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
                                                hostFragment.binding.root,
                                                hostFragment.binding.fragmentImageFoldersList.adapter!!,
                                                hostFragment.binding.fragmentImageFoldersBottomToolBarInclude.layoutBottomToolbarRootLayout,
                                                hostFragment.binding.fragmentImageFoldersAppBarInclude.baseToolbar,
                                                hostFragment.binding.fragmentImageFoldersBottomTabsBarInclude.layoutBottomTabsBarRootLayout,
                                                hostFragment.binding.fragmentImageFoldersSelectionBarInclude.layoutSelectionBarRootLayout,
                                                hostFragment.binding.fragmentImageFoldersSelectionBarInclude.layoutSelectionBarSelectionCountCb,
                                                hostFragment.viewModel.selectionTool.selectionMode,
                                                hostFragment.viewModel.selectionTool.selectedPaths.size
                                        )

                                        hostFragment.binding.fragmentImageFoldersBottomToolBarInclude.layoutBottomToolbarRootLayout.animate().alpha(1f).setDuration(300).start()
                                        hostFragment.toolBarVisible = true
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
        holder.item = currentList[position]

        hostFragment.viewModel.mainScope.launch {
            holder.item.containedImages.first().data.run {
                if(!this.endsWith(".gif", true)) {
                    GlideWrapper.bitmapRequestBuilder
                            .load(this)
                            .override(
                                    holder.binding.layoutImageFolderItemThumbnail.width,
                                    holder.binding.layoutImageFolderItemThumbnail.height
                            )
                            .signature(
                                    MediaStoreSignature(
                                            MimeTypeWrapper.IMAGE_GENERIC_MIME_TYPE,
                                            holder.item.containedImages.first().dateModified,
                                            0
                                    )
                            )
                            .into(holder.binding.layoutImageFolderItemThumbnail)
                } else {
                    GlideWrapper.gifRequestBuilder
                            .load(this)
                            .override(
                                    holder.binding.layoutImageFolderItemThumbnail.width,
                                    holder.binding.layoutImageFolderItemThumbnail.height
                            )
                            .signature(
                                    MediaStoreSignature(
                                            MimeTypeWrapper.IMAGE_GENERIC_MIME_TYPE,
                                            holder.item.containedImages.first().dateModified,
                                            0
                                    )
                            )
                            .into(holder.binding.layoutImageFolderItemThumbnail)
                }
            }

            holder.binding.layoutImageFolderItemCard.post {
                holder.binding.layoutImageFolderItemCard.width.let {
                    holder.binding.layoutImageFolderItemTitle.textSize = (it / 30).toFloat()
                    holder.binding.layoutImageFolderItemCount.textSize = (it / 40).toFloat()
                }
            }

            holder.binding.layoutImageFolderItemTitle.text = holder.item.displayName
            holder.binding.layoutImageFolderItemCount.text = holder.item.containedLibraryItems.size.toString()

            hostFragment.viewModel.selectionTool.differentiateItem(
                    context,
                    holder.item.data,
                    holder.binding.layoutImageFolderItemRootLayout,
                    holder.binding.layoutImageFolderItemIconCheck,
                    holder.binding.layoutImageFolderItemIconUnchecked
            )

            FileCore.findExternalRoots(context).run {
                if(!this.isNullOrEmpty()) {
                    this.forEach {
                        if(holder.item.data.contains(it))
                            holder.binding.layoutImageFolderItemSdCardIcon.visibility = View.VISIBLE
                        else
                            holder.binding.layoutImageFolderItemSdCardIcon.visibility = View.INVISIBLE
                    }
                }
            }

            if(position == 0) hostFragment.scrollDownBtnInitializer.invoke()
        }
    }

    override fun getItemCount(): Int = currentList.size
}