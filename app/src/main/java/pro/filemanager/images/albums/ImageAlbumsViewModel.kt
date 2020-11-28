package pro.filemanager.images.albums

import android.content.Context
import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import pro.filemanager.ApplicationLoader
import pro.filemanager.audio.albums.AudioAlbumItem
import pro.filemanager.core.tools.SearchTool
import pro.filemanager.core.tools.SelectionTool
import pro.filemanager.images.ImageRepo

class ImageAlbumsViewModel(var imageRepo: ImageRepo) : ViewModel(), ImageRepo.AlbumSubscriber {

    var IOScope: CoroutineScope? = CoroutineScope(Dispatchers.IO)
    var MainScope: CoroutineScope? = CoroutineScope(Dispatchers.Main)

    private var itemsLive: MutableLiveData<MutableList<ImageAlbumItem>>? = null
    var selectionTool: SelectionTool? = null

    var searchInProgress = false

    var mainListRvState: Parcelable? = null
    var currentSearchText: String? = null

    init {
        imageRepo.subscribe(this)
    }

    private suspend fun initAlbumsLive(context: Context) : MutableLiveData<MutableList<ImageAlbumItem>> {
        if(itemsLive == null) itemsLive = MutableLiveData(imageRepo.loadAlbums(imageRepo.loadItems(context, false),false))

        return itemsLive!!
    }

    suspend fun getAlbumsLive() = initAlbumsLive(ApplicationLoader.appContext) as LiveData<MutableList<ImageAlbumItem>>

    override fun onUpdate(items: MutableList<ImageAlbumItem>) {
        ApplicationLoader.ApplicationIOScope.launch {
            itemsLive?.postValue(items)
        }
    }

    override fun onCleared() {
        super.onCleared()

        imageRepo.unsubscribe(this)

        try {
            IOScope?.cancel()
            MainScope?.cancel()
        } catch (thr: Throwable) {

        }
    }

    fun search(context: Context, text: String?) {
        IOScope!!.launch {
            while (searchInProgress) {
                delay(25)
            }

            searchInProgress = true

            if(!text.isNullOrEmpty()) {
                currentSearchText = text

                itemsLive?.postValue(SearchTool.searchImageAlbumItems(text, imageRepo.loadAlbums(imageRepo.loadItems(context, false), false)))
            } else {
                itemsLive?.postValue(imageRepo.loadAlbums(imageRepo.loadItems(context, false),false))
            }
        }
    }
}