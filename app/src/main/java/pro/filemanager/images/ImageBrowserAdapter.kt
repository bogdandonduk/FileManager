package pro.filemanager.images

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.signature.MediaStoreSignature
import kotlinx.coroutines.launch
import pro.filemanager.core.tools.SelectionTool
import pro.filemanager.databinding.LayoutImageItemBinding
import pro.filemanager.files.FileCore

class ImageBrowserAdapter(val context: Context, var imageItems: MutableList<ImageItem>, val layoutInflater: LayoutInflater, val hostFragment: ImageBrowserFragment) : RecyclerView.Adapter<ImageBrowserAdapter.ImageItemViewHolder>() {

    class ImageItemViewHolder(val context: Context, val binding: LayoutImageItemBinding, val hostFragment: ImageBrowserFragment, val adapter: ImageBrowserAdapter) : RecyclerView.ViewHolder(binding.root) {
        lateinit var item: ImageItem

        init {
            binding.layoutImageItemRootLayout.apply {
                setOnClickListener {
                    if(this@ImageItemViewHolder::item.isInitialized) {
                        hostFragment.viewModel.MainScope?.launch {
                            @Suppress("UNCHECKED_CAST")
                            hostFragment.viewModel.selectionTool?.handleClickInViewHolder(
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageItemViewHolder {
        if(false) {
            hostFragment.viewModel.MainScope?.launch {
                @Suppress("UNCHECKED_CAST")
                hostFragment.viewModel.selectionTool?.initSelectionState(
                        hostFragment.activity,
                        this@ImageBrowserAdapter as RecyclerView.Adapter<RecyclerView.ViewHolder>,
                        hostFragment.binding.fragmentImageBrowserBottomToolBarInclude.layoutBottomToolBarRootLayout,
                        hostFragment.binding.fragmentImageBrowserBottomTabsBarInclude.layoutBottomTabsBarRootLayout,
                        hostFragment.binding.fragmentImageBrowserToolbarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayout,
                        hostFragment.binding.fragmentImageBrowserToolbarInclude.layoutSelectionBarInclude.layoutSelectionBarContentLayoutSelectionCountCb,
                        hostFragment.viewModel.selectionTool!!.selectionMode,
                        hostFragment.viewModel.selectionTool!!.selectedPaths.size
                )
            }
        }
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

            hostFragment.viewModel.selectionTool?.differentiateItem(
                    holder.item.data,
                    holder.binding.layoutImageItemThumbnail,
                    holder.binding.layoutImageItemIconCheck,
                    holder.binding.layoutImageItemIconUnchecked
            )

        }

        hostFragment.viewModel.MainScope?.launch {
            hostFragment.viewModel.MainScope?.launch {
                @Suppress("UNCHECKED_CAST")
                hostFragment.viewModel.selectionTool?.initSelectionState(
                        hostFragment.activity,
                        this@ImageBrowserAdapter as RecyclerView.Adapter<RecyclerView.ViewHolder>,
                        hostFragment.binding.fragmentImageBrowserBottomToolBarInclude.layoutBottomToolBarRootLayout,
                        hostFragment.binding.fragmentImageBrowserBottomTabsBarInclude.layoutBottomTabsBarRootLayout,
                        hostFragment.binding.fragmentImageBrowserToolbarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayout,
                        hostFragment.binding.fragmentImageBrowserToolbarInclude.layoutSelectionBarInclude.layoutSelectionBarContentLayoutSelectionCountCb,
                        hostFragment.viewModel.selectionTool!!.selectionMode,
                        hostFragment.viewModel.selectionTool!!.selectedPaths.size
                )
            }
        }
    }

    override fun getItemCount(): Int = imageItems.size
}