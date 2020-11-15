package pro.filemanager.videos

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.signature.MediaStoreSignature
import kotlinx.coroutines.launch
import pro.filemanager.HomeActivity
import pro.filemanager.R
import pro.filemanager.core.tools.SelectionTool
import pro.filemanager.databinding.LayoutVideoItemBinding
import pro.filemanager.files.FileCore
import pro.filemanager.files.FileRepo
import pro.filemanager.images.ImageCore

class VideoBrowserAdapter(val context: Context, val videoItems: MutableList<VideoItem>, val layoutInflater: LayoutInflater, val hostFragment: VideoBrowserFragment) : RecyclerView.Adapter<VideoBrowserAdapter.VideoItemViewHolder>() {

    class VideoItemViewHolder(val context: Context, val binding: LayoutVideoItemBinding, val hostFragment: VideoBrowserFragment, val adapter: VideoBrowserAdapter) : RecyclerView.ViewHolder(binding.root) {

        lateinit var item: VideoItem

        init {
            binding.layoutVideoItemRootLayout.apply {
                setOnClickListener {
                    @Suppress("UNCHECKED_CAST")
                    hostFragment.viewModel.selectionTool?.handleClickInViewHolder(SelectionTool.CLICK_SHORT, adapterPosition, adapter as RecyclerView.Adapter<RecyclerView.ViewHolder>, hostFragment.requireActivity() as HomeActivity) {
//                        FileCore.openFileOut(this@VideoItemViewHolder.context, item.data)
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoItemViewHolder {
        return VideoItemViewHolder(context, LayoutVideoItemBinding.inflate(layoutInflater, parent, false), hostFragment, this)
    }

    override fun onBindViewHolder(holder: VideoItemViewHolder, position: Int) {
        holder.item = videoItems[position]

        hostFragment.MainScope.launch {

            VideoCore.glideRequestBuilder
                    .load(holder.item.data)
                    .override(holder.binding.layoutVideoItemThumbnail.width, holder.binding.layoutVideoItemThumbnail.height)
                    .signature(MediaStoreSignature(VideoCore.MIME_TYPE, videoItems[position].dateModified.toLong(), 0))
                    .into(holder.binding.layoutVideoItemThumbnail)

            if(hostFragment.viewModel.selectionTool!!.selectionMode) {
                if(hostFragment.viewModel.selectionTool!!.selectedPositions.contains(position)) {
                    holder.binding.layoutVideoItemIconCheck.visibility = View.VISIBLE

                    holder.binding.layoutVideoItemThumbnail.setColorFilter(Color.argb(120, 0, 0, 0))

                    VideoCore.glideSimpleRequestBuilder
                            .load(R.drawable.ic_baseline_check_circle_24)
                            .into(holder.binding.layoutVideoItemIconCheck)

                    holder.binding.layoutVideoItemIconCheck.scaleX = 0f
                    holder.binding.layoutVideoItemIconCheck.scaleY = 0f

                    holder.binding.layoutVideoItemIconCheck.animate().scaleX(1f).setDuration(150).start()
                    holder.binding.layoutVideoItemIconCheck.animate().scaleY(1f).setDuration(150).start()

                } else {
                    holder.binding.layoutVideoItemThumbnail.colorFilter = null

                    holder.binding.layoutVideoItemIconCheck.visibility = View.INVISIBLE
                }
            } else {
                holder.binding.layoutVideoItemThumbnail.colorFilter = null

                holder.binding.layoutVideoItemIconCheck.visibility = View.INVISIBLE
            }

        }

    }

    override fun getItemCount(): Int {
        return videoItems.size
    }
}