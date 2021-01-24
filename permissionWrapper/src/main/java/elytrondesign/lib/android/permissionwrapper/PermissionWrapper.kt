package elytrondesign.lib.android.permissionwrapper

import android.Manifest.permission.*
import android.Manifest.permission_group.*
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.ViewGroup
import elytrondesign.lib.android.dialogwrapper.DialogWrapper

/*
    A wrapper encapsulating permission requests and result handling.
    It also shows rationale dialogs and takes user to application details when "Deny and do not ask again" option is selected.
*/

object PermissionWrapper {

    private const val SHARED_PREFERENCES_SUFFIX = "elytrondesign.permissionwrapper"
    private const val KEY_SHARED_PREFERENCES_DO_NOT_ASK_AGAIN = "doNotAskAgain"

    var lastRequestResultGrantedAction: (() -> Unit)? = null
    var lastRequestResultDeniedAction: (() -> Unit)? = null

    var userSentToAppSettings = false

    val codesMap = mutableMapOf(
            STORAGE to 1,
            READ_EXTERNAL_STORAGE to 11,
            WRITE_EXTERNAL_STORAGE to 12,
    )

    private fun getSPreferences(context: Context) : SharedPreferences = context.getSharedPreferences(context.packageName + SHARED_PREFERENCES_SUFFIX, Context.MODE_PRIVATE)

    fun handleUserReturnFromAppSettingsForStorageGroup(activity: Activity, grantedAction: (() -> Unit)? = null) {
        if(userSentToAppSettings) {
            userSentToAppSettings = false

            if(checkStorageGroup(activity))
                grantedAction?.invoke()
            else if(!checkStorageGroup(activity))
                activity.onBackPressed()
        }
    }

    fun handleUserReturnFromAppSettingsForStorageRead(activity: Activity, grantedAction: (() -> Unit)? = null) {
        if(userSentToAppSettings) {
            userSentToAppSettings = false

            if(checkStorageRead(activity))
                grantedAction?.invoke()
            else if(!checkStorageRead(activity))
                activity.onBackPressed()
        }
    }

    fun handleUserReturnFromAppSettingsForStorageWrite(activity: Activity, grantedAction: (() -> Unit)? = null) {
        if(userSentToAppSettings) {
            userSentToAppSettings = false

            if(checkStorageWrite(activity))
                grantedAction?.invoke()
            else if(!checkStorageWrite(activity))
                activity.onBackPressed()
        }
    }

    fun checkStorageGroup(context: Context) : Boolean {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            (context.checkSelfPermission(READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    context.checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        } else true
    }

    fun checkStorageRead(context: Context) : Boolean {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            (context.checkSelfPermission(READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        } else true
    }

    fun checkStorageWrite(context: Context) : Boolean {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            (context.checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        } else true
    }

    fun requestStorageGroup(activity: Activity, parentLayout: ViewGroup, deniedAction: () -> Unit = {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                DialogWrapper.buildAlertDialog(
                    activity,
                    parentLayout,
                    App.context.resources.getString(R.string.no_storage_permission),
                    App.context.resources.getString(R.string.denied_message_no_storage_permission),
                    true,
                    App.context.resources.getString(R.string.button_grant),
                    {
                        lastRequestResultGrantedAction = alreadyGrantedOrLessThanApi23Action
                        activity.requestPermissions(
                            arrayOf(
                                READ_EXTERNAL_STORAGE,
                                WRITE_EXTERNAL_STORAGE
                            ), codesMap[STORAGE]!!
                        )
                    },
                    App.context.resources.getString(R.string.button_deny),
                    {
                        activity.onBackPressed()
                    },
                    {
                        activity.onBackPressed()
                    }
                ).show()
            }
        }, doNotAskAgainAction: () -> Unit = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                DialogWrapper.buildAlertDialog(
                    activity,
                    parentLayout,
                    App.context.resources.getString(R.string.no_storage_permission),
                    App.context.resources.getString(R.string.do_not_ask_again_message_no_storage_permission),
                    true,
                    App.context.resources.getString(R.string.button_grant),
                    {
                        userSentToAppSettings = true
                        openAppDetailsSettings(activity)
                    },
                    App.context.resources.getString(R.string.button_deny),
                    {
                        activity.onBackPressed()
                    },
                    {
                        activity.onBackPressed()
                    }
                ).show()
            }
        }, alreadyGrantedOrLessThanApi23Action: () -> Unit
    ) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(activity.checkSelfPermission(READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || activity.checkSelfPermission(WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                if(activity.shouldShowRequestPermissionRationale(READ_EXTERNAL_STORAGE) || activity.shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) {
                    deniedAction.invoke()
                } else if(
                    getSPreferences(activity).getBoolean(READ_EXTERNAL_STORAGE + KEY_SHARED_PREFERENCES_DO_NOT_ASK_AGAIN, false) ||
                    getSPreferences(activity).getBoolean(WRITE_EXTERNAL_STORAGE + KEY_SHARED_PREFERENCES_DO_NOT_ASK_AGAIN, false)
                ) {
                    doNotAskAgainAction.invoke()
                } else {
                    lastRequestResultGrantedAction = alreadyGrantedOrLessThanApi23Action
                    activity.requestPermissions(arrayOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE), codesMap[STORAGE]!!)
                }
            } else {
                alreadyGrantedOrLessThanApi23Action.invoke()
            }
        } else {
            alreadyGrantedOrLessThanApi23Action.invoke()
        }
    }

    fun requestStorageRead(activity: Activity, parentLayout: ViewGroup, deniedAction: () -> Unit = {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            DialogWrapper.buildAlertDialog(
                    activity,
                    parentLayout,
                    App.context.resources.getString(R.string.no_storage_permission),
                    App.context.resources.getString(R.string.denied_message_no_storage_permission),
                    true,
                    App.context.resources.getString(R.string.button_grant),
                    {
                        lastRequestResultGrantedAction = alreadyGrantedOrLessThanApi23Action
                        activity.requestPermissions(arrayOf(READ_EXTERNAL_STORAGE), codesMap[READ_EXTERNAL_STORAGE]!!)
                    },
                    App.context.resources.getString(R.string.button_deny),
                    {
                        activity.onBackPressed()
                    },
                    {
                        activity.onBackPressed()
                    }
            ).show()
        }
    }, doNotAskAgainAction: () -> Unit = {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            DialogWrapper.buildAlertDialog(
                    activity,
                    parentLayout,
                    App.context.resources.getString(R.string.no_storage_permission),
                    App.context.resources.getString(R.string.do_not_ask_again_message_no_storage_permission),
                    true,
                    App.context.resources.getString(R.string.button_grant),
                    {
                        userSentToAppSettings = true
                        openAppDetailsSettings(activity)
                    },
                    App.context.resources.getString(R.string.button_deny),
                    {
                        activity.onBackPressed()
                    },
                    {
                        activity.onBackPressed()
                    }
            ).show()
        }
    }, alreadyGrantedOrLessThanApi23Action: () -> Unit) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(activity.checkSelfPermission(READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || activity.checkSelfPermission(WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if(activity.shouldShowRequestPermissionRationale(READ_EXTERNAL_STORAGE) || activity.shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) {
                    deniedAction.invoke()
                } else if(
                    getSPreferences(activity).getBoolean(READ_EXTERNAL_STORAGE + KEY_SHARED_PREFERENCES_DO_NOT_ASK_AGAIN, false) ||
                    getSPreferences(activity).getBoolean(WRITE_EXTERNAL_STORAGE + KEY_SHARED_PREFERENCES_DO_NOT_ASK_AGAIN, false)
                ) {
                    doNotAskAgainAction.invoke()
                } else {
                    lastRequestResultGrantedAction = alreadyGrantedOrLessThanApi23Action
                    activity.requestPermissions(arrayOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE), codesMap[STORAGE]!!)
                }
            } else {
                alreadyGrantedOrLessThanApi23Action.invoke()
            }
        } else {
            alreadyGrantedOrLessThanApi23Action.invoke()
        }
    }

    fun requestStorageWrite(activity: Activity, parentLayout: ViewGroup, deniedAction: () -> Unit = {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            DialogWrapper.buildAlertDialog(
                    activity,
                    parentLayout,
                    App.context.resources.getString(R.string.no_storage_permission),
                    App.context.resources.getString(R.string.denied_message_no_storage_permission),
                    true,
                    App.context.resources.getString(R.string.button_grant),
                    {
                        lastRequestResultGrantedAction = alreadyGrantedOrLessThanApi23Action
                        activity.requestPermissions(arrayOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE), codesMap[WRITE_EXTERNAL_STORAGE]!!)
                    },
                    App.context.resources.getString(R.string.button_deny),
                    {
                        activity.onBackPressed()
                    },
                    {
                        activity.onBackPressed()
                    }
            ).show()
        }
    }, doNotAskAgainAction: () -> Unit = {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            DialogWrapper.buildAlertDialog(
                    activity,
                    parentLayout,
                    App.context.resources.getString(R.string.no_storage_permission),
                    App.context.resources.getString(R.string.do_not_ask_again_message_no_storage_permission),
                    true,
                    App.context.resources.getString(R.string.button_grant),
                    {
                        userSentToAppSettings = true
                        openAppDetailsSettings(activity)
                    },
                    App.context.resources.getString(R.string.button_deny),
                    {
                        activity.onBackPressed()
                    },
                    {
                        activity.onBackPressed()
                    }
            ).show()
        }
    }, alreadyGrantedOrLessThanApi23Action: () -> Unit) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(activity.checkSelfPermission(READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || activity.checkSelfPermission(WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if(activity.shouldShowRequestPermissionRationale(READ_EXTERNAL_STORAGE) || activity.shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) {
                    deniedAction.invoke()
                } else if(
                    getSPreferences(activity).getBoolean(READ_EXTERNAL_STORAGE + KEY_SHARED_PREFERENCES_DO_NOT_ASK_AGAIN, false) ||
                    getSPreferences(activity).getBoolean(WRITE_EXTERNAL_STORAGE + KEY_SHARED_PREFERENCES_DO_NOT_ASK_AGAIN, false)
                ) {
                    doNotAskAgainAction.invoke()
                } else {
                    lastRequestResultGrantedAction = alreadyGrantedOrLessThanApi23Action
                    activity.requestPermissions(arrayOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE), codesMap[STORAGE]!!)
                }
            } else {
                alreadyGrantedOrLessThanApi23Action.invoke()
            }
        } else {
            alreadyGrantedOrLessThanApi23Action.invoke()
        }
    }

    fun handleStorageGroupRequestResult(activity: Activity, requestCode: Int, grantResults: IntArray, grantedAction: (() -> Unit)? = null, deniedAction: (() -> Unit)? = null) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(requestCode == codesMap[STORAGE]) {
                if(grantResults.size == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    getSPreferences(activity).edit().remove(STORAGE + KEY_SHARED_PREFERENCES_DO_NOT_ASK_AGAIN).apply()
                    grantedAction?.invoke()
                } else {
                    if(!activity.shouldShowRequestPermissionRationale(READ_EXTERNAL_STORAGE) || !activity.shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE))
                        getSPreferences(activity).edit().putBoolean(STORAGE + KEY_SHARED_PREFERENCES_DO_NOT_ASK_AGAIN, true).apply()
                    deniedAction?.invoke()
                }
            }
        }
    }

    fun handleStorageReadRequestResult(activity: Activity, requestCode: Int, grantResults: IntArray, grantedAction: (() -> Unit)? = null, deniedAction: (() -> Unit)? = null) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(requestCode == codesMap[READ_EXTERNAL_STORAGE]) {
                if(grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getSPreferences(activity).edit().remove(READ_EXTERNAL_STORAGE + KEY_SHARED_PREFERENCES_DO_NOT_ASK_AGAIN).apply()
                    grantedAction?.invoke()
                } else {
                    if(!activity.shouldShowRequestPermissionRationale(READ_CALENDAR))
                        getSPreferences(activity).edit().putBoolean(READ_EXTERNAL_STORAGE + KEY_SHARED_PREFERENCES_DO_NOT_ASK_AGAIN, true).apply()
                    deniedAction?.invoke()
                }
            }
        }
    }

    fun handleStorageWriteRequestResult(activity: Activity, requestCode: Int, grantResults: IntArray, grantedAction: (() -> Unit)? = null, deniedAction: (() -> Unit)? = null) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(requestCode == codesMap[WRITE_EXTERNAL_STORAGE]) {
                if(grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getSPreferences(activity).edit().remove(WRITE_CALENDAR + KEY_SHARED_PREFERENCES_DO_NOT_ASK_AGAIN).apply()
                    grantedAction?.invoke()
                } else {
                    if(!activity.shouldShowRequestPermissionRationale(WRITE_CALENDAR))
                        getSPreferences(activity).edit().putBoolean(WRITE_CALENDAR + KEY_SHARED_PREFERENCES_DO_NOT_ASK_AGAIN, true).apply()
                    deniedAction?.invoke()
                }
            }
        }
    }

    private fun openAppDetailsSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }

        context.startActivity(intent)
    }
}