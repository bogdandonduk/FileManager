package pro.filemanager.images.albums

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import pro.filemanager.HomeActivity
import pro.filemanager.R
import pro.filemanager.core.tools.SelectionTool
import pro.filemanager.databinding.LayoutImageAlbumItemBinding
import pro.filemanager.images.ImageCore
import pro.filemanager.images.gallery.ImageGalleryFragment
import pro.filemanager.images.gallery.ImageGalleryViewModel

class ImageAlbumsAdapter(val context: Context, val imageAlbumItems: MutableList<ImageAlbumItem>, val layoutInflater: LayoutInflater, val hostFragment: ImageAlbumsFragment) : RecyclerView.Adapter<ImageAlbumsAdapter.ImageAlbumItemViewHolder>() {

    class ImageAlbumItemViewHolder(val context: Context, val binding: LayoutImageAlbumItemBinding, val hostFragment: ImageAlbumsFragment, val adapter: ImageAlbumsAdapter) : RecyclerView.ViewHolder(binding.root) {
        lateinit var item: ImageAlbumItem

        init {
            binding.layoutImageAlbumItemContentLayout.apply {
                setOnClickListener {
                    @Suppress("UNCHECKED_CAST")
                    hostFragment.viewModel.selectionTool?.handleClickInViewHolder(SelectionTool.CLICK_SHORT, adapterPosition, adapter as RecyclerView.Adapter<RecyclerView.ViewHolder>, hostFragment.requireActivity() as HomeActivity) {
                        hostFragment.requireActivity().supportFragmentManager.beginTransaction().replace(hostFragment.binding.fragmentImageAlbumsRootLayout.id, ImageGalleryFragment().apply {
                            arguments = bundleOf(ImageCore.KEY_ARGUMENT_ALBUM_PARCELABLE to item)
                        }).addToBackStack(null).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit()
                    }
                }

                setOnLongClickListener {
                    @Suppress("UNCHECKED_CAST")
                    hostFragment.viewModel.selectionTool?.handleClickInViewHolder(SelectionTool.CLICK_LONG, adapterPosition, adapter as RecyclerView.Adapter<RecyclerView.ViewHolder>, hostFragment.requireActivity() as HomeActivity)

                    true
                }
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageAlbumItemViewHolder =
            ImageAlbumItemViewHolder(context, LayoutImageAlbumItemBinding.inflate(layoutInflater, parent, false), hostFragment, this)


    override fun onBindViewHolder(holder: ImageAlbumItemViewHolder, position: Int) {
        holder.item = imageAlbumItems[position]

        hostFragment.MainScope.launch {
            if (!holder.item.containedImages.first().data.endsWith(".gif", true)) {
                ImageCore.glideBitmapRequestBuilder
                        .load(holder.item.containedImages.first().data)
                        .override(holder.binding.layoutImageAlbumItemContentLayout.width, holder.binding.layoutImageAlbumItemThumbnail.height)
                        .into(holder.binding.layoutImageAlbumItemThumbnail)
            } else {
                ImageCore.glideGifRequestBuilder
                        .load(holder.item.containedImages.first().data)
                        .override(holder.binding.layoutImageAlbumItemThumbnail.width, holder.binding.layoutImageAlbumItemThumbnail.height)
                        .into(holder.binding.layoutImageAlbumItemThumbnail)
            }

            holder.binding.layoutImageAlbumItemCard.post {
                holder.binding.layoutImageAlbumItemTitle.text = holder.item.displayName
                holder.binding.layoutImageAlbumItemTitle.textSize = (holder.binding.layoutImageAlbumItemCard.width / 30).toFloat()
                holder.binding.layoutImageAlbumItemCount.text = holder.item.containedImages.size.toString()
                holder.binding.layoutImageAlbumItemCount.textSize = (holder.binding.layoutImageAlbumItemCard.width / 40).toFloat()
            }

            if (hostFragment.viewModel.selectionTool!!.selectionMode) {
                if (hostFragment.viewModel.selectionTool!!.selectedPositions.contains(position)) {
                    holder.binding.layoutImageAlbumItemIconCheck.visibility = View.VISIBLE

                    holder.binding.layoutImageAlbumItemThumbnail.setColorFilter(Color.argb(120, 0, 0, 0))

                    ImageCore.glideSimpleRequestBuilder
                            .load(R.drawable.ic_baseline_check_circle_24)
                            .into(holder.binding.layoutImageAlbumItemIconCheck)

                    holder.binding.layoutImageAlbumItemIconCheck.scaleX = 0f
                    holder.binding.layoutImageAlbumItemIconCheck.scaleY = 0f

                    holder.binding.layoutImageAlbumItemIconCheck.animate().scaleX(1f).setDuration(150).start()
                    holder.binding.layoutImageAlbumItemIconCheck.animate().scaleY(1f).setDuration(150).start()

                } else {
                    holder.binding.layoutImageAlbumItemThumbnail.colorFilter = null

                    holder.binding.layoutImageAlbumItemIconCheck.visibility = View.INVISIBLE
                }
            } else {
                holder.binding.layoutImageAlbumItemThumbnail.colorFilter = null

                holder.binding.layoutImageAlbumItemIconCheck.visibility = View.INVISIBLE
            }

        }

    }

    override fun getItemCount(): Int = imageAlbumItems.size

}