package pro.filemanager.apps.all

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.signature.MediaStoreSignature
import kotlinx.coroutines.launch
import pro.filemanager.apps.AppItem
import pro.filemanager.core.tools.SelectionTool
import pro.filemanager.core.wrappers.GlideWrapper
import pro.filemanager.core.wrappers.MimeTypeWrapper
import pro.filemanager.databinding.LayoutImageLibraryItemBinding
import pro.filemanager.files.FileCore
import java.util.*

class AllAppsAdapter(val context: Context, val appItems: MutableList<AppItem>, val layoutInflater: LayoutInflater, val hostFragment: AllAppsFragment) :
    ListAdapter<AppItem, AllAppsAdapter.AppItemViewHolder>(object : DiffUtil.ItemCallback<AppItem>() {
        override fun areItemsTheSame(oldItem: AppItem, newItem: AppItem): Boolean {
            return oldItem.data == newItem.data
        }

        override fun areContentsTheSame(oldItem: AppItem, newItem: AppItem): Boolean {
            return oldItem.equals(newItem)
        }
    }) {

    init {
        submitList(appItems)
    }

    override fun submitList(list: MutableList<AppItem>?) {
        super.submitList(list)

        currentList.forEachIndexed { i: Int, _: AppItem ->
            notifyItemChanged(i)
        }
    }

    override fun getItemId(position: Int): Long {
        return UUID.nameUUIDFromBytes(currentList[position].data.toByteArray()).mostSignificantBits
    }

    class AppItemViewHolder(val context: Context, val binding: LayoutImageLibraryItemBinding, val hostFragment: AllAppsFragment, val adapter: AllAppsAdapter) : RecyclerView.ViewHolder(binding.root) {
        lateinit var item: AppItem

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppItemViewHolder =
            AppItemViewHolder(context, LayoutImageLibraryItemBinding.inflate(layoutInflater, parent, false), hostFragment, this)

    override fun onBindViewHolder(holder: AppItemViewHolder, position: Int) {
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
                            holder.item.dateInstalled,
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
                            holder.item.dateInstalled,
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