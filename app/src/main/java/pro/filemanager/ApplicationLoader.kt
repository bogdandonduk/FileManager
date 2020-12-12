package pro.filemanager

import android.app.Application
import android.content.Context
import android.os.FileObserver
import android.os.Parcelable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import pro.filemanager.images.ImageRepo
import kotlinx.coroutines.launch
import pro.filemanager.core.PermissionWrapper
import pro.filemanager.files.FileCore

class ApplicationLoader : Application() {

    companion object {
        lateinit var appContext: Context

        val ApplicationIOScope = CoroutineScope(IO)
        val ApplicationMainScope = CoroutineScope(Main)

        val transientParcelables: MutableMap<String, Parcelable?> = mutableMapOf()
        val transientStrings: MutableMap<String, String?> = mutableMapOf()
        var isUserSentToAppDetailsSettings = false

        lateinit var fileObserver: FileObserver

        fun loadAll() {
            loadImages()
            findExternalRoots()
        }

        fun loadImages(context: Context = appContext) {
            if(PermissionWrapper.checkExternalStoragePermissions(context)) {
                ApplicationIOScope.launch {
                    ImageRepo.getSingleton().loadAll(context, false)
                }
            }
        }

        fun findExternalRoots(context: Context = appContext) {
            if(PermissionWrapper.checkExternalStoragePermissions(context)) {
                ApplicationIOScope.launch {
                    FileCore.findExternalRoots(context)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        appContext = this

        loadAll()

//        fileObserver = pro.filemanager.core.FileObserver(path)
//
//        fileObserver.startWatching()

    }

}