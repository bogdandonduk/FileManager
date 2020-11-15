package pro.filemanager.audios

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import pro.filemanager.ApplicationLoader
import pro.filemanager.HomeActivity
import pro.filemanager.R
import pro.filemanager.databinding.LayoutAudioItemBinding
import pro.filemanager.files.FileCore

class AudioBrowserAdapter(val context: Context, val layoutInflater: LayoutInflater, val hostFragment: AudioBrowserFragment) : RecyclerView.Adapter<AudioBrowserAdapter.AudioItemViewHolder>() {

    class AudioItemViewHolder(val context: Context, val binding: LayoutAudioItemBinding, val hostFragment: AudioBrowserFragment) : RecyclerView.ViewHolder(binding.root) {
        lateinit var item: AudioItem

        init {
            binding.layoutAudioItemRootLayout.setOnClickListener {
                ApplicationLoader.ApplicationIOScope.launch {
//                    FileCore.openFileOut(context, item.data)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioItemViewHolder {
        return AudioItemViewHolder(context, DataBindingUtil.inflate(layoutInflater, R.layout.layout_audio_item, parent, false), hostFragment)
    }

    override fun onBindViewHolder(holder: AudioItemViewHolder, position: Int) {

        holder.item = hostFragment.itemsLive!!.value!![position]
        holder.binding.audioItem = holder.item
    }

    override fun getItemCount(): Int {
        return hostFragment.itemsLive!!.value!!.size
    }
}