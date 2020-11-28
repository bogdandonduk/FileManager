package pro.filemanager.audio.albums

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import pro.filemanager.HomeActivity
import pro.filemanager.R
import pro.filemanager.core.tools.SelectionTool
import pro.filemanager.databinding.LayoutAudioAlbumItemBinding
import pro.filemanager.databinding.LayoutImageAlbumItemBinding
import pro.filemanager.images.ImageCore

class AudioAlbumsAdapter(val context: Context, var audioAlbumItems: MutableList<AudioAlbumItem>, val layoutInflater: LayoutInflater, val hostFragment: AudioAlbumsFragment) : RecyclerView.Adapter<AudioAlbumsAdapter.ImageAlbumItemViewHolder>() {

    class ImageAlbumItemViewHolder(val context: Context, val binding: LayoutAudioAlbumItemBinding, val hostFragment: AudioAlbumsFragment, val adapter: AudioAlbumsAdapter) : RecyclerView.ViewHolder(binding.root) {
        lateinit var item: AudioAlbumItem

        init {
            binding.layoutAudioAlbumItemContentLayout.apply {
                setOnClickListener {
                    hostFragment.MainScope.launch {
                        @Suppress("UNCHECKED_CAST")
                        hostFragment.viewModel.selectionTool?.handleClickInViewHolder(
                                SelectionTool.CLICK_SHORT,
                                adapterPosition,
                                adapter as RecyclerView.Adapter<RecyclerView.ViewHolder>,
                                hostFragment.requireActivity() as HomeActivity,
                                hostFragment.binding.fragmentAudioAlbumsToolbarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayoutSelectionCountCb,
                                hostFragment.binding.fragmentAudioAlbumsToolbarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayout,
                                hostFragment.binding.fragmentAudioAlbumsBottomToolBarInclude.layoutBottomToolBarRootLayout,
                                hostFragment.binding.fragmentAudioAlbumsBottomTabsBarInclude.layoutBottomTabsBarRootLayout
                        ) {
                            hostFragment.navController.navigate(R.id.action_audioAlbumsFragment_to_audioBrowserFragment, bundleOf(
                                    ImageCore.KEY_ARGUMENT_ALBUM_PARCELABLE to item
                            ))
                        }
                    }
                }

                setOnLongClickListener {
                    hostFragment.MainScope.launch {
                        @Suppress("UNCHECKED_CAST")
                        hostFragment.viewModel.selectionTool?.handleClickInViewHolder(
                                SelectionTool.CLICK_LONG,
                                adapterPosition,
                                adapter as RecyclerView.Adapter<RecyclerView.ViewHolder>,
                                hostFragment.requireActivity() as HomeActivity,
                                hostFragment.binding.fragmentAudioAlbumsToolbarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayoutSelectionCountCb,
                                hostFragment.binding.fragmentAudioAlbumsToolbarInclude.layoutSelectionBarInclude.layoutSelectionBarRootLayout,
                                hostFragment.binding.fragmentAudioAlbumsBottomToolBarInclude.layoutBottomToolBarRootLayout,
                                hostFragment.binding.fragmentAudioAlbumsBottomTabsBarInclude.layoutBottomTabsBarRootLayout
                        )
                    }

                    true
                }
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageAlbumItemViewHolder =
            ImageAlbumItemViewHolder(context, LayoutAudioAlbumItemBinding.inflate(layoutInflater, parent, false), hostFragment, this)


    override fun onBindViewHolder(holder: ImageAlbumItemViewHolder, position: Int) {
        holder.item = audioAlbumItems[position]

            holder.binding.layoutAudioAlbumItemCard.post {
                holder.binding.layoutAudioAlbumItemTitle.text = holder.item.displayName
                holder.binding.layoutAudioAlbumItemTitle.textSize = (holder.binding.layoutAudioAlbumItemCard.width / 30).toFloat()
                holder.binding.layoutAudioAlbumItemCount.text = holder.item.containedImages.size.toString()
                holder.binding.layoutAudioAlbumItemCount.textSize = (holder.binding.layoutAudioAlbumItemCard.width / 40).toFloat()
            }

            hostFragment.viewModel.selectionTool?.differentiateItem(position, holder.binding.layoutAudioAlbumItemThumbnail, holder.binding.layoutAudioAlbumItemIconCheck, holder.binding.layoutAudioAlbumItemIconUnchecked)

    }

    override fun getItemCount(): Int = audioAlbumItems.size

}