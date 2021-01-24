package pro.filemanager

import android.app.Application
import android.content.Context
import elytrondesign.lib.android.permissionwrapper.PermissionWrapper
import kotlinx.coroutines.launch
import pro.filemanager.core.wrappers.CoroutineWrapper
import pro.filemanager.files.FileCore
import pro.filemanager.images.ImageRepo

class ApplicationLoader : Application() {

    companion object {
        lateinit var appContext: Context

        fun loadAll() {
            loadImages()
//            loadVideos()
//            loadAudios()
//            loadDocs()
//            loadApks()
            findExternalRoots()
        }

        fun loadImages(context: Context = appContext) {
            if(PermissionWrapper.checkStorageGroup(context)) {
                CoroutineWrapper.globalIOScope.launch {
                    ImageRepo.getSingleton().loadAll(context, false)
                }
            }
        }

//        fun loadVideos(context: Context = appContext) {
//            if(PermissionWrapper.checkExternalStoragePermissions(context)) {
//                CoroutineWrapper.globalIOScope.launch {
//                    VideoRepo.getSingleton().loadAll(context, false)
//                }
//            }
//        }
//
//        fun loadAudios(context: Context = appContext) {
//            if(PermissionWrapper.checkExternalStoragePermissions(context)) {
//                CoroutineWrapper.globalIOScope.launch {
//                    AudioRepo.getSingleton().loadAll(context, false)
//                }
//            }
//        }
//
//        fun loadDocs(context: Context = appContext) {
//            if(PermissionWrapper.checkExternalStoragePermissions(context)) {
//                CoroutineWrapper.globalIOScope.launch {
//                    DocRepo.getSingleton().loadAll(context, false)
//                }
//            }
//        }
//
//        fun loadApks(context: Context = appContext) {
//            if(PermissionWrapper.checkExternalStoragePermissions(context)) {
//                CoroutineWrapper.globalIOScope.launch {
//                    ApkRepo.getSingleton().loadAll(context, false)
//                }
//            }
//        }

        fun findExternalRoots(context: Context = appContext) {
            if(PermissionWrapper.checkStorageGroup(context)) {
                CoroutineWrapper.globalIOScope.launch {
                    FileCore.findExternalRoots(context)
                }
            }
        }

        fun releaseLists() {

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