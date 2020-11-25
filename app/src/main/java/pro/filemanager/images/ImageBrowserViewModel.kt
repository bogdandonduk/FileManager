package pro.filemanager.images

import android.content.Context
import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.launch
import pro.filemanager.ApplicationLoader
import pro.filemanager.core.tools.SearchTool
import pro.filemanager.core.tools.SelectionTool
import pro.filemanager.images.albums.ImageAlbumItem

class ImageBrowserViewModel(val imageRepo: ImageRepo, val albumItem: ImageAlbumItem?) : ViewModel(), ImageRepo.ItemSubscriber {

    private var itemsLive: MutableLiveData<MutableList<ImageItem>>? = null
    var mainListRvState: Parcelable? = null
    var currentSearchText: String? = null

    init {
        imageRepo.subscribe(this)
    }

    private suspend fun initItemsLive(context: Context, forceLoad: Boolean = false) : MutableLiveData<MutableList<ImageItem>> {
        if(!forceLoad) {
            if(itemsLive == null) {
                itemsLive = if(albumItem != null)
                    MutableLiveData(albumItem.containedImages)
                else
                    MutableLiveData(imageRepo.loadItems(context, false))
            }
        } else {
            itemsLive = if(albumItem != null)
                MutableLiveData(albumItem.containedImages)
            else
                MutableLiveData(imageRepo.loadItems(context, false))
        }

        return itemsLive!!
    }

    suspend fun getItemsLive() = initItemsLive(ApplicationLoader.appContext) as LiveData<MutableList<ImageItem>>

    var selectionTool: SelectionTool? = null

    override fun onUpdate(items: MutableList<ImageItem>) {
        itemsLive?.postValue(items)
    }

    override fun onCleared() {
        imageRepo.unsubscribe(this)
    }

    fun search(context: Context, text: String?) {
        ApplicationLoader.ApplicationIOScope.launch {
            if(text != null) {
                currentSearchText = text

                if(albumItem != null) {
                    itemsLive?.postValue(SearchTool.searchImageItems(text, albumItem.containedImages))
                } else {
                    itemsLive?.postValue(SearchTool.searchImageItems(text, imageRepo.loadItems(context, false)))
                }
            } else {
                initItemsLive(ApplicationLoader.appContext, true)
            }
        }
    }
}