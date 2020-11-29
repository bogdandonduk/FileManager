package pro.filemanager.audio.albums

import android.content.Context
import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pro.filemanager.ApplicationLoader
import pro.filemanager.audio.AudioRepo
import pro.filemanager.core.tools.SearchTool
import pro.filemanager.core.tools.SelectionTool

class AudioAlbumsViewModel(var audioRepo: AudioRepo) : ViewModel(), AudioRepo.AlbumSubscriber {

    var IOScope = CoroutineScope(IO)
    var MainScope: CoroutineScope? = CoroutineScope(Main)

    var searchInProgress = false

    private var itemsLive: MutableLiveData<MutableList<AudioAlbumItem>>? = null
    var selectionTool: SelectionTool? = null

    var mainListRvState: Parcelable? = null

    var isSearchViewEnabled = false
    var currentSearchText = ""


    init {
        audioRepo.subscribe(this)
    }

    private suspend fun initAlbumsLive(context: Context) : MutableLiveData<MutableList<AudioAlbumItem>> {
        if(itemsLive == null) itemsLive = MutableLiveData(audioRepo.loadAlbums(audioRepo.loadItems(context, false),false))

        return itemsLive!!
    }

    suspend fun getAlbumsLive() = initAlbumsLive(ApplicationLoader.appContext) as LiveData<MutableList<AudioAlbumItem>>

    override fun onUpdate(items: MutableList<AudioAlbumItem>) {
        ApplicationLoader.ApplicationIOScope.launch {
            itemsLive?.postValue(items)
        }
    }

    override fun onCleared() {
        super.onCleared()

        audioRepo.unsubscribe(this)
    }

    fun search(context: Context, text: String?) {
        IOScope.launch {
            while(searchInProgress) {
                delay(25)
            }

            searchInProgress = true

            currentSearchText = text ?: ""

            if(!text.isNullOrEmpty()) {
                itemsLive?.postValue(SearchTool.searchAudioAlbumItems(text, audioRepo.loadAlbums(audioRepo.loadItems(context, false), false)))
            } else {
                itemsLive?.postValue(audioRepo.loadAlbums(audioRepo.loadItems(context, false), false))
            }
        }
    }
}