package pro.filemanager.docs

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import pro.filemanager.core.tools.SelectionTool
import pro.filemanager.images.ImageItem
import pro.filemanager.images.ImageRepo

class DocBrowserViewModel(var docRepo: DocRepo) : ViewModel(), DocRepo.RepoSubscriber {

    private var itemsLive: MutableLiveData<MutableList<DocItem>>? = null

    var mainRvScrollPosition: Parcelable? = null

    init {
        docRepo.subscribe(this)
    }

    private suspend fun initItemsLive() : MutableLiveData<MutableList<DocItem>> {
        if(itemsLive == null) {
            itemsLive = MutableLiveData(docRepo.loadItems())
        }

        return itemsLive!!
    }

    suspend fun getItemsLive() = initItemsLive() as LiveData<MutableList<DocItem>>

    var selectionTool: SelectionTool? = null

    override fun onUpdate(items: MutableList<DocItem>) {

        itemsLive?.postValue(items)
    }

    override fun onCleared() {
        super.onCleared()

        docRepo.unsubscribe(this)
    }

}