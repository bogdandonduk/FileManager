package pro.filemanager.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class StartupReceiver() : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.startService(Intent(context, FileObserverService::class.java))
    }
}