package pro.filemanager.video

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import pro.filemanager.core.tools.SelectionTool

class VideoBrowserViewModel(var videoRepo: VideoRepo) : ViewModel(), VideoRepo.RepoSubscriber {

    private var itemsLive: MutableLiveData<MutableList<VideoItem>>? = null

    var mainRvScrollPosition: Parcelable? = null

    init {
        videoRepo.subscribe(this)
    }

    private suspend fun initItemsLive() : MutableLiveData<MutableList<VideoItem>> {
        if(itemsLive == null) {
            itemsLive = MutableLiveData(videoRepo.loadItems())
        }

        return itemsLive!!
    }

    suspend fun getItemsLive() = initItemsLive() as LiveData<MutableList<VideoItem>>

    var selectionTool: SelectionTool? = null

    override fun onUpdate(items: MutableList<VideoItem>) {

        itemsLive?.postValue(items)
    }

    override fun onCleared() {
        super.onCleared()

        videoRepo.unsubscribe(this)
    }

}