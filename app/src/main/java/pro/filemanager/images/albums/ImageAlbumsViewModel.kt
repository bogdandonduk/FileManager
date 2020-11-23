package pro.filemanager.images.albums

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.launch
import pro.filemanager.ApplicationLoader
import pro.filemanager.core.tools.SelectionTool
import pro.filemanager.images.ImageItem
import pro.filemanager.images.ImageRepo
import pro.filemanager.images.albums.ImageAlbumItem

class ImageAlbumsViewModel(var imageRepo: ImageRepo) : ViewModel(), ImageRepo.RepoSubscriber {

    private var itemsLive: MutableLiveData<MutableList<ImageAlbumItem>>? = null

    var mainListRvState: Parcelable? = null

    init {
        imageRepo.subscribe(this)

    }

    private suspend fun initAlbumsLive() : MutableLiveData<MutableList<ImageAlbumItem>> {
        if(itemsLive == null) {
            itemsLive = MutableLiveData(imageRepo.loadAlbums(ApplicationLoader.appContext, false))
        }

        return itemsLive!!
    }

    suspend fun getAlbumsLive() = initAlbumsLive() as LiveData<MutableList<ImageAlbumItem>>

    var selectionTool: SelectionTool? = null

    override fun onUpdate(items: MutableList<ImageItem>) {
        ApplicationLoader.ApplicationIOScope.launch {
            itemsLive?.postValue(imageRepo.loadAlbums(ApplicationLoader.appContext, false))
        }
    }

    override fun onCleared() {
        super.onCleared()

        imageRepo.unsubscribe(this)
    }

}