package pro.filemanager.images

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import pro.filemanager.core.tools.SelectorTool

class ImageBrowserViewModel(private var imageRepo: ImageRepo) : ViewModel() {

    suspend fun getItemsLive() = imageRepo.loadLive() as LiveData<MutableList<ImageItem>>

    var mainRvScrollPosition: Parcelable? = null

    var selectorTool: SelectorTool? = null

}