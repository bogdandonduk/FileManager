package pro.filemanager.core.wrappers

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main

object CoroutineWrapper {

    val globalIOScope = CoroutineScope(IO)
    val globalMainImmediateScope = CoroutineScope(Main.immediate)
    val globalMainScope = CoroutineScope(Main)
    val globalDefaultScope = CoroutineScope(Default)

}