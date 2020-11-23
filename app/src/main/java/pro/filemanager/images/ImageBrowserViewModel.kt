package pro.filemanager.images

import android.os.Parcelable
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import pro.filemanager.ApplicationLoader
import pro.filemanager.core.tools.SelectionTool
import pro.filemanager.images.ImageItem
import pro.filemanager.images.ImageRepo
import pro.filemanager.images.albums.ImageAlbumItem

class ImageBrowserViewModel(val imageRepo: ImageRepo, val albumItem: ImageAlbumItem?) : ViewModel(), ImageRepo.RepoSubscriber {

    private var itemsLive: MutableLiveData<MutableList<ImageItem>>? = null

    var mainRvScrollPosition: Parcelable? = null

    init {
        imageRepo.subscribe(this)
    }

    private suspend fun initItemsLive() : MutableLiveData<MutableList<ImageItem>> {
        if(itemsLive == null) {
            itemsLive = if(albumItem != null)
                MutableLiveData(albumItem.containedImages)
            else
                MutableLiveData(imageRepo.loadItems(ApplicationLoader.appContext, false))
        }

        return itemsLive!!
    }

    suspend fun getItemsLive() = initItemsLive() as LiveData<MutableList<ImageItem>>

    var selectionTool: SelectionTool? = null

    override fun onUpdate(items: MutableList<ImageItem>) {

        itemsLive?.postValue(items)
    }

    override fun onCleared() {

        imageRepo.unsubscribe(this)
    }

}