package pro.filemanager.audios

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AudioBrowserViewModel(private var audioRepo: AudioRepo) : ViewModel() {

    private var itemsLive: MutableLiveData<MutableList<AudioItem>>? = null

    private suspend fun initItemsLive() : MutableLiveData<MutableList<AudioItem>> {
        if(itemsLive == null) {
            itemsLive = audioRepo.loadLive()
        }

        return itemsLive!!
    }

    suspend fun getItemsLive() = initItemsLive() as LiveData<MutableList<AudioItem>>

    fun deleteRow() {
        val items: MutableList<AudioItem> = mutableListOf()

        items.addAll(itemsLive!!.value!!)

        items.shuffle()

        itemsLive!!.postValue(items)
    }
}