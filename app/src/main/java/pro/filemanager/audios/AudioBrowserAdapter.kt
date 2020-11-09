package pro.filemanager.audios

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import pro.filemanager.R
import pro.filemanager.databinding.LayoutAudioItemBinding

class AudioBrowserAdapter(val context: Context, val audioItems: MutableList<AudioItem>, val layoutInflater: LayoutInflater) : RecyclerView.Adapter<AudioBrowserAdapter.AudioItemViewHolder>() {

    class AudioItemViewHolder(val context: Context, val binding: LayoutAudioItemBinding) : RecyclerView.ViewHolder(binding.root) {
        lateinit var item: AudioItem

        init {
            binding.layoutAudioItemRootLayout.setOnClickListener {
                AudioManager.openInOtherMusicPlayer(context, item.data)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioItemViewHolder {
        return AudioItemViewHolder(context, DataBindingUtil.inflate(layoutInflater, R.layout.layout_audio_item, parent, false))
    }

    override fun onBindViewHolder(holder: AudioItemViewHolder, position: Int) {

        holder.item = audioItems[position]
        holder.binding.audioItem = holder.item
    }

    override fun getItemCount(): Int {
        return audioItems.size
    }
}