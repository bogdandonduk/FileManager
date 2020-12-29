package pro.filemanager.core.generics

import android.database.ContentObserver
import android.os.Handler
import kotlinx.coroutines.launch
import pro.filemanager.ApplicationLoader
import android.content.Context
import android.util.Log
import pro.filemanager.images.ImageRepo

class BaseContentObserver(val context: Context, val viewModel: BaseViewModel, val handler: Handler) : ContentObserver(handler) {

    override fun onChange(selfChange: Boolean) {
        ApplicationLoader.ApplicationIOScope.launch {
            viewModel.assignItemsLive(context, true)
            ImageRepo.getSingleton().loadAll(context, true)
        }
    }

}