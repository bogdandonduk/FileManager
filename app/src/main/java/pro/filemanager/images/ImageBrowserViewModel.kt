package pro.filemanager.images

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import pro.filemanager.audios.AudioItem
import pro.filemanager.core.tools.SelectionTool

class ImageBrowserViewModel(var imageRepo: ImageRepo) : ViewModel(), ImageRepo.RepoSubscriber {

    private var itemsLive: MutableLiveData<MutableList<ImageItem>>? = null

    var mainRvScrollPosition: Parcelable? = null

    init {
        imageRepo.subscribe(this)
    }

    private suspend fun initItemsLive() : MutableLiveData<MutableList<ImageItem>> {
        if(itemsLive == null) {
            itemsLive = MutableLiveData(imageRepo.loadItems())
        }

        return itemsLive!!
    }

    suspend fun getItemsLive() = initItemsLive() as LiveData<MutableList<ImageItem>>

    var selectionTool: SelectionTool? = null

    override fun onUpdate(items: MutableList<ImageItem>) {

        itemsLive?.postValue(items)
    }

    override fun onCleared() {
        super.onCleared()

        imageRepo.unsubscribe(this)
    }

}