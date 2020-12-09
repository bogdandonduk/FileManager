package pro.filemanager.core

import android.os.FileObserver
import android.util.Log
import kotlinx.coroutines.launch
import pro.filemanager.ApplicationLoader
import pro.filemanager.images.ImageRepo

class FileObserver(path: String) : FileObserver(path, ALL_EVENTS) {
    override fun onEvent(event: Int, path: String?) {
        Log.d("TAG", "onEvent: $event $path")

        ApplicationLoader.ApplicationIOScope.launch {
            ImageRepo.getSingleton().loadItemsByDateRecent(ApplicationLoader.appContext, true)
        }
    }

    override fun startWatching() {
        super.startWatching()

        Log.d("TAG", "startWatching: ")
    }
}