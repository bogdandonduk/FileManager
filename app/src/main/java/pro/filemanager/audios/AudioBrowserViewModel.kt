package pro.filemanager.audios

import android.os.Parcelable
import androidx.lifecycle.*

class AudioBrowserViewModel(var audioRepo: AudioRepo) : ViewModel(), AudioRepo.RepoSubscriber {

    private var itemsLive: MutableLiveData<MutableList<AudioItem>>? = null

    var mainRvScrollPosition: Parcelable? = null

    init {
        audioRepo.subscribe(this)
    }

    private suspend fun initItemsLive() : MutableLiveData<MutableList<AudioItem>> {
        if(itemsLive == null) {
            itemsLive = MutableLiveData(audioRepo.loadItems())
        }

        return itemsLive!!
    }

    suspend fun getItemsLive() = initItemsLive() as LiveData<MutableList<AudioItem>>

    override fun onUpdate(items: MutableList<AudioItem>) {

        itemsLive?.postValue(items)
    }

    override fun onCleared() {
        super.onCleared()

        audioRepo.unsubscribe(this)
    }
}