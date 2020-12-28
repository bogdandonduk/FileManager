package pro.filemanager.images

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.signature.MediaStoreSignature
import kotlinx.android.synthetic.main.fragment_image_library.*
import kotlinx.coroutines.launch
import pro.filemanager.core.tools.SelectionTool
import pro.filemanager.databinding.LayoutImageItemBinding
import pro.filemanager.files.FileCore

class ImageLibraryAdapter(val context: Context, val imageItems: MutableList<ImageItem>, val layoutInflater: LayoutInflater, val hostFragment: ImageLibraryFragment) :
    ListAdapter<ImageItem, ImageLibraryAdapter.ImageItemViewHolder>(object : DiffUtil.ItemCallback<ImageItem>() {
        override fun areItemsTheSame(oldItem: ImageItem, newItem: ImageItem): Boolean {
            return oldItem.data == newItem.data
        }

        override fun areContentsTheSame(oldItem: ImageItem, newItem: ImageItem): Boolean {
            return oldItem.equals(newItem)
        }
    }) {

    var lastSelectionModeState: Boolean = false

    init {
        submitList(imageItems)
    }

    override fun submitList(list: MutableList<ImageItem>?) {
        super.submitList(list)

        repeat(itemCount) {
            notifyItemChanged(it - 1)
        }
    }

    override fun getItemId(position: Int): Long {
        return currentList[position].hashCode().toLong()
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
                                    context,
                                    adapterPosition,
                                    item.data,
                                    adapter as RecyclerView.Adapter<RecyclerView.ViewHolder>,
                            ) {
                                FileCore.openFileOut(context, item.data)
                            }

                            hostFragment.viewModel.selectionTool.initSelectionCheckBox(hostFragment.binding.fragmentImageLibraryAppBarInclude.layoutSelectionBarInclude.layoutSelectionBarContentLayoutSelectionCountCb, adapter.itemCount)
                        }
                    }
                }

                setOnLongClickListener {
                    if(this@ImageItemViewHolder::item.isInitialized) {
                        hostFragment.viewModel.MainScope?.launch {
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
                                                hostFragment.binding.fragmentImageLibraryBottomToolBarInclude.layoutBottomToolBarRootLayout,
                                                hostFragment.binding.fragmentImageLibraryBottomTabsBarInclude.layoutBottomTabsBarRootLayout,
                                                hostFragment.binding.fragmentImageLibraryAppBarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayout,
                                                hostFragment.binding.fragmentImageLibraryAppBarInclude.layoutSelectionBarInclude.layoutSelectionBarContentLayoutSelectionCountCb,
                                                hostFragment.viewModel.selectionTool.selectionMode,
                                                hostFragment.viewModel.selectionTool.selectedPaths.size
                                        )
                                    }
                            )

                            hostFragment.viewModel.selectionTool.initSelectionCheckBox(hostFragment.binding.fragmentImageLibraryAppBarInclude.layoutSelectionBarInclude.layoutSelectionBarContentLayoutSelectionCountCb, adapter.itemCount)

                            hostFragment.fragmentImageLibraryScrollBtn.visibility = View.VISIBLE
                            hostFragment.binding.fragmentImageLibraryBottomToolBarInclude.layoutBottomToolBarRootLayout.animate().alpha(1f).setDuration(300).start()
                            hostFragment.toolBarVisible = true
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
        holder.item = currentList[position]

        hostFragment.viewModel.MainScope?.launch {
            if(!holder.item.data.endsWith(".gif", true)) {
                ImageCore.glideBitmapRequestBuilder
                        .load(holder.item.data)
                        .override(holder.binding.layoutImageItemThumbnail.width, holder.binding.layoutImageItemThumbnail.height)
                        .signature(MediaStoreSignature(ImageCore.MIME_TYPE, holder.item.dateModified, 0))
                        .into(holder.binding.layoutImageItemThumbnail)
            } else {
                ImageCore.glideGifRequestBuilder
                        .load(holder.item.data)
                        .override(holder.binding.layoutImageItemThumbnail.width, holder.binding.layoutImageItemThumbnail.height)
                        .signature(MediaStoreSignature(ImageCore.MIME_TYPE, holder.item.dateModified, 0))
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

        if(lastSelectionModeState != hostFragment.viewModel.selectionTool.selectionMode && hostFragment.viewModel.selectionTool.selectedPaths.size == itemCount) {
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

    override fun getItemCount(): Int = currentList.size
}