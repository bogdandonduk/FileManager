package pro.filemanager.images

import android.content.Context
import android.graphics.Color
import android.text.Html
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.signature.MediaStoreSignature
import kotlinx.coroutines.launch
import pro.filemanager.core.tools.SelectionTool
import pro.filemanager.databinding.LayoutImageItemBinding
import pro.filemanager.files.FileCore

class ImageLibraryAdapter(val context: Context, var imageItems: MutableList<ImageItem>, val layoutInflater: LayoutInflater, val hostFragment: ImageLibraryFragment) : RecyclerView.Adapter<ImageLibraryAdapter.ImageItemViewHolder>() {
    var lastSelectionModeState: Boolean = false

    class ImageBrowserAdapterDiffCallback(
            val oldItems: MutableList<ImageItem>,
            val newItems: MutableList<ImageItem>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldItems.size

        override fun getNewListSize(): Int = newItems.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                oldItems[oldItemPosition].data == newItems[newItemPosition].data

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean = oldItems[oldItemPosition].equals(newItems[newItemPosition])
    }

    fun submitItems(newItems: MutableList<ImageItem>) {
        val diffResult: DiffUtil.DiffResult = DiffUtil.calculateDiff(ImageBrowserAdapterDiffCallback(imageItems, newItems), true)

        imageItems = newItems

        diffResult.dispatchUpdatesTo(this)
    }

    fun submitItemsWithoutDiff(newItems: MutableList<ImageItem>) {
        imageItems = newItems

        notifyDataSetChanged()
    }

    class ImageItemViewHolder(val context: Context, val binding: LayoutImageItemBinding, val hostFragment: ImageLibraryFragment, val adapter: ImageLibraryAdapter) : RecyclerView.ViewHolder(binding.root) {
        lateinit var item: ImageItem

        init {
            binding.layoutImageItemRootLayout.apply {
                setOnClickListener {
                    if(this@ImageItemViewHolder::item.isInitialized) {
                        hostFragment.viewModel.MainScope?.launch {
                            @Suppress("UNCHECKED_CAST")
                            hostFragment.viewModel.selectionTool.handleClickInViewHolder(
                                    SelectionTool.CLICK_SHORT,
                                    adapterPosition,
                                    item.data,
                                    adapter as RecyclerView.Adapter<RecyclerView.ViewHolder>
                            ) {
                                FileCore.openFileOut(context, item.data)
                            }
                        }
                    }
                }

                setOnLongClickListener {
                    if(this@ImageItemViewHolder::item.isInitialized) {
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
                                                hostFragment.binding.fragmentImageLibraryBottomToolBarInclude.layoutBottomToolBarRootLayout,
                                                hostFragment.binding.fragmentImageLibraryBottomTabsBarInclude.layoutBottomTabsBarRootLayout,
                                                hostFragment.binding.fragmentImageLibraryAppBarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayout,
                                                hostFragment.binding.fragmentImageLibraryAppBarInclude.layoutSelectionBarInclude.layoutSelectionBarContentLayoutSelectionCountCb,
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageItemViewHolder {

        return ImageItemViewHolder(context, LayoutImageItemBinding.inflate(layoutInflater, parent, false), hostFragment, this)
    }

    override fun onBindViewHolder(holder: ImageItemViewHolder, position: Int) {
        holder.item = imageItems[position]

        hostFragment.viewModel.MainScope?.launch {
            if(!holder.item.data.endsWith(".gif", true)) {
                ImageCore.glideBitmapRequestBuilder
                        .load(holder.item.data)
                        .override(holder.binding.layoutImageItemThumbnail.width, holder.binding.layoutImageItemThumbnail.height)
                        .signature(MediaStoreSignature(ImageCore.MIME_TYPE, imageItems[position].dateModified, 0))
                        .into(holder.binding.layoutImageItemThumbnail)
            } else {
                ImageCore.glideGifRequestBuilder
                        .load(holder.item.data)
                        .override(holder.binding.layoutImageItemThumbnail.width, holder.binding.layoutImageItemThumbnail.height)
                        .signature(MediaStoreSignature(ImageCore.MIME_TYPE, imageItems[position].dateModified, 0))
                        .into(holder.binding.layoutImageItemThumbnail)
            }

            holder.binding.layoutImageItemTitle.text = holder.item.displayName

            holder.binding.layoutImageItemRootLayout.post {
                holder.binding.layoutImageItemRootLayout.width.let {
                    holder.binding.layoutImageItemTitle.textSize = (it / 22).toFloat()

                    holder.binding.layoutImageItemTitleContainer.visibility = View.VISIBLE
                }
            }

            hostFragment.viewModel.selectionTool.differentiateItem(
                    holder.item.data,
                    holder.binding.layoutImageItemThumbnail,
                    holder.binding.layoutImageItemIconCheck,
                    holder.binding.layoutImageItemIconUnchecked
            )
        }

        if(hostFragment.viewModel.selectionTool.selectionMode)
            hostFragment.binding.fragmentImageLibraryAppBarInclude.layoutSelectionBarInclude.layoutSelectionBarContentLayoutSelectionCountCb.text =
                    hostFragment.viewModel.selectionTool.selectedPaths.size.toString()

        if(lastSelectionModeState != hostFragment.viewModel.selectionTool.selectionMode) {
            lastSelectionModeState = hostFragment.viewModel.selectionTool.selectionMode

            hostFragment.viewModel.MainScope?.launch {
                @Suppress("UNCHECKED_CAST")
                hostFragment.viewModel.selectionTool.initSelectionState(
                        hostFragment.activity,
                        this@ImageLibraryAdapter as RecyclerView.Adapter<RecyclerView.ViewHolder>,
                        hostFragment.binding.fragmentImageLibraryBottomToolBarInclude.layoutBottomToolBarRootLayout,
                        hostFragment.binding.fragmentImageLibraryBottomTabsBarInclude.layoutBottomTabsBarRootLayout,
                        hostFragment.binding.fragmentImageLibraryAppBarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayout,
                        hostFragment.binding.fragmentImageLibraryAppBarInclude.layoutSelectionBarInclude.layoutSelectionBarContentLayoutSelectionCountCb,
                        hostFragment.viewModel.selectionTool.selectionMode,
                        hostFragment.viewModel.selectionTool.selectedPaths.size
                )
            }
        }

    }

    override fun getItemCount(): Int = imageItems.size
}