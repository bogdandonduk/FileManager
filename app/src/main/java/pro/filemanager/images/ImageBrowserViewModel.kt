package pro.filemanager.images

import android.content.Context
import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import pro.filemanager.ApplicationLoader
import pro.filemanager.core.tools.SearchTool
import pro.filemanager.core.tools.SelectionTool
import pro.filemanager.images.albums.ImageAlbumItem

class ImageBrowserViewModel(val imageRepo: ImageRepo, val albumItem: ImageAlbumItem?) : ViewModel(), ImageRepo.ItemSubscriber {

    var IOScope: CoroutineScope? = CoroutineScope(IO)
    var MainScope: CoroutineScope? = CoroutineScope(Main)

    var searchInProgress = false

    private var itemsLive: MutableLiveData<MutableList<ImageItem>>? = null
    var mainListRvState: Parcelable? = null
    var currentSearchText: String? = null

    var selectionTool: SelectionTool? = null

    init {
        imageRepo.subscribe(this)
    }

    private suspend fun initItemsLive(context: Context) : MutableLiveData<MutableList<ImageItem>> {
        if(itemsLive == null)
            itemsLive =
                    if(albumItem != null) MutableLiveData(albumItem.containedImages) else MutableLiveData(imageRepo.loadItems(context, false))

        return itemsLive!!
    }

    suspend fun getItemsLive() = initItemsLive(ApplicationLoader.appContext) as LiveData<MutableList<ImageItem>>

    override fun onUpdate(items: MutableList<ImageItem>) {
        itemsLive?.postValue(items)
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
            while(searchInProgress) {
                delay(25)
            }

            searchInProgress = true

            if(!text.isNullOrEmpty()) {
                currentSearchText = text

                if(albumItem != null) {
                    itemsLive?.postValue(SearchTool.searchImageItems(text, albumItem.containedImages))
                } else {
                    itemsLive?.postValue(SearchTool.searchImageItems(text, imageRepo.loadItems(context, false)))
                }
            } else {
                itemsLive?.postValue(imageRepo.loadItems(context, false))
            }

        }

    }

}