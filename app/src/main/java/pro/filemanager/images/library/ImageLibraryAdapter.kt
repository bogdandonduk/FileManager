package pro.filemanager.images.library

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.signature.MediaStoreSignature
import kotlinx.coroutines.launch
import pro.filemanager.core.tools.SelectionTool
import pro.filemanager.core.wrappers.GlideWrapper
import pro.filemanager.core.wrappers.MimeTypeWrapper
import pro.filemanager.databinding.LayoutImageLibraryItemBinding
import pro.filemanager.files.FileCore
import java.util.*

class ImageLibraryAdapter(val context: Context, val imageItems: MutableList<ImageLibraryItem>, val layoutInflater: LayoutInflater, val hostFragment: ImageLibraryFragment) :
    ListAdapter<ImageLibraryItem, ImageLibraryAdapter.ImageLibraryItemViewHolder>(object : DiffUtil.ItemCallback<ImageLibraryItem>() {
        override fun areItemsTheSame(oldItem: ImageLibraryItem, newItem: ImageLibraryItem): Boolean {
            return oldItem.data == newItem.data
        }

        override fun areContentsTheSame(oldItem: ImageLibraryItem, newItem: ImageLibraryItem): Boolean {
            return oldItem.equals(newItem)
        }
    }) {

    var lastSelectionModeState: Boolean = false

    init {
        submitList(imageItems)
    }

    override fun submitList(list: MutableList<ImageLibraryItem>?) {
        super.submitList(list)

        currentList.forEachIndexed { i: Int, _: ImageLibraryItem ->
            notifyItemChanged(i)
        }
    }

    override fun getItemId(position: Int): Long {
        return UUID.nameUUIDFromBytes(currentList[position].data.toByteArray()).mostSignificantBits
    }

    class ImageLibraryItemViewHolder(val context: Context, val binding: LayoutImageLibraryItemBinding, val hostFragment: ImageLibraryFragment, val adapter: ImageLibraryAdapter) : RecyclerView.ViewHolder(binding.root) {
        lateinit var item: ImageLibraryItem

        private fun isItemInitialized() : Boolean = this::item.isInitialized

        init {
            binding.layoutImageItemRootLayout.apply {
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
                                    {
                                        hostFragment.viewModel.selectionTool.initSelectionState(
                                                hostFragment.activity,
                                                hostFragment.binding.root,
                                                adapter,
                                                hostFragment.binding.fragmentImageLibraryBottomToolbarInclude.layoutBottomToolbarRootLayout,
                                                hostFragment.binding.fragmentImageLibraryAppBarInclude.baseToolbar,
                                                hostFragment.binding.fragmentImageLibraryBottomTabsBarInclude.layoutBottomTabsBarRootLayout,
                                                hostFragment.binding.fragmentImageLibrarySelectionBarInclude.layoutSelectionBarRootLayout,
                                                hostFragment.binding.fragmentImageLibrarySelectionBarInclude.layoutSelectionBarSelectionCountCb,
                                                hostFragment.viewModel.selectionTool.selectionMode,
                                                hostFragment.viewModel.selectionTool.selectedPaths.size
                                        )

                                        hostFragment.viewModel.selectionTool.initSelectionCheckBox(
                                                hostFragment.binding.fragmentImageLibrarySelectionBarInclude.layoutSelectionBarSelectionCountCb,
                                                adapter.itemCount
                                        )
                                    }
                            ) {
                                FileCore.openFileOut(context, item.data)
                            }

                        }
                    }
                }

                setOnLongClickListener {
                    if(isItemInitialized()) {
                        hostFragment.viewModel.mainScope.launch {
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
                                                adapter,
                                                hostFragment.binding.fragmentImageLibraryBottomToolbarInclude.layoutBottomToolbarRootLayout,
                                                hostFragment.binding.fragmentImageLibraryAppBarInclude.baseToolbar,
                                                hostFragment.binding.fragmentImageLibraryBottomTabsBarInclude.layoutBottomTabsBarRootLayout,
                                                hostFragment.binding.fragmentImageLibrarySelectionBarInclude.layoutSelectionBarRootLayout,
                                                hostFragment.binding.fragmentImageLibrarySelectionBarInclude.layoutSelectionBarSelectionCountCb,
                                                hostFragment.viewModel.selectionTool.selectionMode,
                                                hostFragment.viewModel.selectionTool.selectedPaths.size
                                        )

                                        hostFragment.viewModel.selectionTool.initSelectionCheckBox(
                                                hostFragment.binding.fragmentImageLibrarySelectionBarInclude.layoutSelectionBarSelectionCountCb,
                                                adapter.itemCount
                                        )

                                        hostFragment.binding.fragmentImageLibraryBottomToolbarInclude.layoutBottomToolbarRootLayout.animate().alpha(1f).setDuration(300).start()
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageLibraryItemViewHolder =
            ImageLibraryItemViewHolder(context, LayoutImageLibraryItemBinding.inflate(layoutInflater, parent, false), hostFragment, this)

    override fun onBindViewHolder(holder: ImageLibraryItemViewHolder, position: Int) {
        holder.item = currentList[position]

        hostFragment.viewModel.mainScope.launch {
            if(!holder.item.data.endsWith(".gif", true)) {
                GlideWrapper.bitmapRequestBuilder
                    .load(holder.item.data)
                    .override(
                            holder.binding.layoutImageItemThumbnail.width,
                            holder.binding.layoutImageItemThumbnail.height
                    )
                    .signature(
                        MediaStoreSignature(
                            MimeTypeWrapper.IMAGE_GENERIC_MIME_TYPE,
                            holder.item.dateModified,
                            0
                        )
                    )
                    .into(holder.binding.layoutImageItemThumbnail)
            } else {
                GlideWrapper.gifRequestBuilder
                    .load(holder.item.data)
                    .override(
                            holder.binding.layoutImageItemThumbnail.width,
                            holder.binding.layoutImageItemThumbnail.height
                    )
                    .signature(
                        MediaStoreSignature(
                            MimeTypeWrapper.IMAGE_GENERIC_MIME_TYPE,
                            holder.item.dateModified,
                            0
                        )
                    )
                    .into(holder.binding.layoutImageItemThumbnail)
            }


            holder.binding.layoutImageItemTitleContainer.post {
                holder.binding.layoutImageItemTitleContainer.height.let {
                    holder.binding.layoutImageItemTitle.textSize = (it / 5).toFloat()

                    holder.binding.layoutImageItemTitleContainer.visibility = View.VISIBLE
                }
            }

            holder.binding.layoutImageItemTitle.text = holder.item.displayName

            hostFragment.viewModel.selectionTool.differentiateItem(
                    context,
                    holder.item.data,
                    holder.binding.layoutImageItemRootLayoutBgColorFilterHolder,
                    holder.binding.layoutImageItemIconCheck,
                    holder.binding.layoutImageItemIconUnchecked
            )

            FileCore.findExternalRoots(context).run {
                if(!this.isNullOrEmpty()) {
                    this.forEach {
                        if(holder.item.data.contains(it))
                            holder.binding.layoutImageItemSdCardIcon.visibility = View.VISIBLE
                        else
                            holder.binding.layoutImageItemSdCardIcon.visibility = View.INVISIBLE
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = currentList.size
}