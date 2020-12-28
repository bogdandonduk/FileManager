package pro.filemanager.core.wrappers

import android.content.Context

object PreferencesWrapper {
    const val KEY_SP_NAME =  "pro.filemanager.sp"

    fun putString(context: Context, key: String, value: String) {
        context.getSharedPreferences(KEY_SP_NAME, Context.MODE_PRIVATE).edit().putString(key, value).apply()
    }

    fun getString(context: Context, key: String, defValue: String) : String =
            context.getSharedPreferences(KEY_SP_NAME, Context.MODE_PRIVATE).getString(key, defValue)!!

}