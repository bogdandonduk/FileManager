package pro.filemanager.images

import android.database.ContentObserver
import android.os.Handler
import kotlinx.coroutines.launch
import pro.filemanager.ApplicationLoader
import pro.filemanager.core.generics.BaseViewModel
import android.content.Context
import android.util.Log

class ImageContentObserver(val context: Context, val viewModel: BaseViewModel, val handler: Handler) : ContentObserver(handler) {

    override fun onChange(selfChange: Boolean) {

        Log.d("TAG", "onChange: ")

        ApplicationLoader.ApplicationIOScope.launch {
            viewModel.assignItemsLive(context, true)
            ImageRepo.getSingleton().loadAll(context, true)
        }
    }
}