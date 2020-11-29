package pro.filemanager.images

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.RotateAnimation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.signature.MediaStoreSignature
import kotlinx.coroutines.launch
import pro.filemanager.HomeActivity
import pro.filemanager.R
import pro.filemanager.core.tools.SelectionTool
import pro.filemanager.databinding.LayoutImageItemBinding
import pro.filemanager.files.FileCore

class ImageBrowserAdapter(val context: Context, var imageItems: MutableList<ImageItem>, val layoutInflater: LayoutInflater, val hostFragment: ImageBrowserFragment) : RecyclerView.Adapter<ImageBrowserAdapter.ImageItemViewHolder>() {

    class ImageItemViewHolder(val context: Context, val binding: LayoutImageItemBinding, val hostFragment: ImageBrowserFragment, val adapter: ImageBrowserAdapter) : RecyclerView.ViewHolder(binding.root) {
        lateinit var item: ImageItem

        init {
            binding.layoutImageItemRootLayout.apply {
                setOnClickListener {
                    hostFragment.viewModel.MainScope?.launch {
                        @Suppress("UNCHECKED_CAST")
                        hostFragment.viewModel.selectionTool?.handleClickInViewHolder(
                                SelectionTool.CLICK_SHORT,
                                adapterPosition,
                                adapter as RecyclerView.Adapter<RecyclerView.ViewHolder>,
                                hostFragment.requireActivity() as HomeActivity,
                                hostFragment.binding.fragmentImageBrowserToolbarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayoutSelectionCountCb,
                                hostFragment.binding.fragmentImageBrowserToolbarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayout,
                                hostFragment.binding.fragmentImageBrowserBottomToolBarInclude.layoutBottomToolBarRootLayout,
                                hostFragment.binding.fragmentImageBrowserBottomTabsBarInclude.layoutBottomTabsBarRootLayout) {
                            FileCore.openFileOut(context, item.data)
                        }
                    }
                }

                setOnLongClickListener {
                    hostFragment.viewModel.MainScope?.launch {
                        @Suppress("UNCHECKED_CAST")
                        hostFragment.viewModel.selectionTool?.handleClickInViewHolder(
                                SelectionTool.CLICK_LONG,
                                adapterPosition,
                                adapter as RecyclerView.Adapter<RecyclerView.ViewHolder>,
                                hostFragment.requireActivity() as HomeActivity,
                                hostFragment.binding.fragmentImageBrowserToolbarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayoutSelectionCountCb,
                                hostFragment.binding.fragmentImageBrowserToolbarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayout,
                                hostFragment.binding.fragmentImageBrowserBottomToolBarInclude.layoutBottomToolBarRootLayout,
                                hostFragment.binding.fragmentImageBrowserBottomTabsBarInclude.layoutBottomTabsBarRootLayout
                        )
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
                        .signature(MediaStoreSignature(ImageCore.MIME_TYPE, imageItems[position].dateModified.toLong(), 0))
                        .into(holder.binding.layoutImageItemThumbnail)
            } else {
                ImageCore.glideGifRequestBuilder
                        .load(holder.item.data)
                        .override(holder.binding.layoutImageItemThumbnail.width, holder.binding.layoutImageItemThumbnail.height)
                        .signature(MediaStoreSignature(ImageCore.MIME_TYPE, imageItems[position].dateModified.toLong(), 0))
                        .into(holder.binding.layoutImageItemThumbnail)
            }

            holder.binding.layoutImageItemTitle.text = holder.item.displayName

            holder.binding.layoutImageItemRootLayout.post {
                holder.binding.layoutImageItemRootLayout.width.let {
                    holder.binding.layoutImageItemTitle.textSize = (it / 22).toFloat()

                    holder.binding.layoutImageItemTitleContainer.visibility = View.VISIBLE
                }
            }

            hostFragment.viewModel.selectionTool?.differentiateItem(position, holder.binding.layoutImageItemThumbnail, holder.binding.layoutImageItemIconCheck, holder.binding.layoutImageItemIconUnchecked)

        }

    }

    override fun getItemCount(): Int = imageItems.size
}