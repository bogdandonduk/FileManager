package pro.filemanager.videos

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class VideoBrowserViewModel(private var videoRepo: VideoRepo) : ViewModel() {

    suspend fun getItemsLive() = videoRepo.loadLive() as LiveData<MutableList<VideoItem>>

    var mainRvScrollPosition: Parcelable? = null
}