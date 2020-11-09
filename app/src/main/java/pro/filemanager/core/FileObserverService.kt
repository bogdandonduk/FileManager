package pro.filemanager.core

import android.app.Service
import android.content.Intent
import android.os.FileObserver
import android.os.IBinder
import android.util.Log

class FileObserverService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("TAG", "onStartCommand: STARTED")

        return START_REDELIVER_INTENT
    }

    class Observer(path: String) : FileObserver(path) {
        override fun onEvent(event: Int, path: String?) {

        }

    }
}