package pro.filemanager.images.albums

import android.content.Context
import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.launch
import pro.filemanager.ApplicationLoader
import pro.filemanager.core.tools.SearchTool
import pro.filemanager.core.tools.SelectionTool
import pro.filemanager.images.ImageRepo

class ImageAlbumsViewModel(var imageRepo: ImageRepo) : ViewModel(), ImageRepo.AlbumSubscriber {

    private var itemsLive: MutableLiveData<MutableList<ImageAlbumItem>>? = null
    var selectionTool: SelectionTool? = null

    var mainListRvState: Parcelable? = null
    var currentSearchText: String? = null

    init {
        imageRepo.subscribe(this)
    }

    private suspend fun initAlbumsLive(context: Context, forceLoad: Boolean = false) : MutableLiveData<MutableList<ImageAlbumItem>> {
        if(!forceLoad) {
            if(itemsLive == null) {
                itemsLive = MutableLiveData(imageRepo.loadAlbums(imageRepo.loadItems(context, false),false))
            }
        } else {
            itemsLive = MutableLiveData(imageRepo.loadAlbums(imageRepo.loadItems(context, false),false))
        }

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
    }

    fun search(context: Context, text: String?) {
        ApplicationLoader.ApplicationIOScope.launch {
            if(text != null) {
                mainListRvState = null
                currentSearchText = text
                itemsLive?.postValue(SearchTool.searchImageAlbumItems(text, imageRepo.loadAlbums(imageRepo.loadItems(context, false), false)))
            } else {
                mainListRvState = null

                initAlbumsLive(context, true)
            }
        }
    }
}