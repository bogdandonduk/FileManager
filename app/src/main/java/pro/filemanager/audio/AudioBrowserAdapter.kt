package pro.filemanager.audio

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.signature.MediaStoreSignature
import kotlinx.coroutines.launch
import pro.filemanager.HomeActivity
import pro.filemanager.core.tools.SelectionTool
import pro.filemanager.databinding.LayoutAudioItemBinding
import pro.filemanager.databinding.LayoutImageItemBinding
import pro.filemanager.files.FileCore
import pro.filemanager.images.ImageCore

class AudioBrowserAdapter(val context: Context, var audioItems: MutableList<AudioItem>, val layoutInflater: LayoutInflater, val hostFragment: AudioBrowserFragment) : RecyclerView.Adapter<AudioBrowserAdapter.ImageItemViewHolder>() {

    class ImageItemViewHolder(val context: Context, val binding: LayoutAudioItemBinding, val hostFragment: AudioBrowserFragment, val adapter: AudioBrowserAdapter) : RecyclerView.ViewHolder(binding.root) {
        lateinit var item: AudioItem

        init {
            binding.layoutAudioItemRootLayout.apply {
                setOnClickListener {
                    hostFragment.viewModel.MainScope?.launch {
                        @Suppress("UNCHECKED_CAST")
                        hostFragment.viewModel.selectionTool?.handleClickInViewHolder(
                                SelectionTool.CLICK_SHORT,
                                adapterPosition,
                                adapter as RecyclerView.Adapter<RecyclerView.ViewHolder>,
                                hostFragment.requireActivity() as HomeActivity,
                                hostFragment.binding.fragmentAudioBrowserToolbarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayoutSelectionCountCb,
                                hostFragment.binding.fragmentAudioBrowserToolbarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayout,
                                hostFragment.binding.fragmentAudioBrowserBottomToolBarInclude.layoutBottomToolBarRootLayout,
                                hostFragment.binding.fragmentAudioBrowserBottomTabsBarInclude.layoutBottomTabsBarRootLayout) {
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
                                hostFragment.binding.fragmentAudioBrowserToolbarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayoutSelectionCountCb,
                                hostFragment.binding.fragmentAudioBrowserToolbarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayout,
                                hostFragment.binding.fragmentAudioBrowserBottomToolBarInclude.layoutBottomToolBarRootLayout,
                                hostFragment.binding.fragmentAudioBrowserBottomTabsBarInclude.layoutBottomTabsBarRootLayout
                        )
                    }
                    true
                }
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageItemViewHolder {
        return ImageItemViewHolder(context, LayoutAudioItemBinding.inflate(layoutInflater, parent, false), hostFragment, this)
    }

    override fun onBindViewHolder(holder: ImageItemViewHolder, position: Int) {
        holder.item = audioItems[position]

        hostFragment.viewModel.MainScope?.launch {

            holder.binding.layoutAudioItemTitle.text = holder.item.displayName

            holder.binding.layoutAudioItemRootLayout.post {
                holder.binding.layoutAudioItemRootLayout.width.let {
                    holder.binding.layoutAudioItemTitle.textSize = (it / 22).toFloat()

                    holder.binding.layoutAudioItemTitleContainer.visibility = View.VISIBLE
                }
            }

            hostFragment.viewModel.selectionTool?.differentiateItem(position, holder.binding.layoutAudioItemThumbnail, holder.binding.layoutAudioItemIconCheck, holder.binding.layoutAudioItemIconUnchecked)

        }

    }

    override fun getItemCount(): Int = audioItems.size
}