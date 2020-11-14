package pro.filemanager.docs

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class DocBrowserViewModel(private var docRepo: DocRepo) : ViewModel() {

    suspend fun getItemsLive() = docRepo.loadLive() as LiveData<MutableList<DocItem>>

}