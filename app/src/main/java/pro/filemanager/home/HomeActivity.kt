package pro.filemanager.home

import android.content.ComponentCallbacks2
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import elytrondesign.lib.android.permissionwrapper.PermissionWrapper
import pro.filemanager.ApplicationLoader
import pro.filemanager.R
import pro.filemanager.core.tools.ShareTool
import pro.filemanager.core.ui.FragmentWrapper
import pro.filemanager.databinding.ActivityHomeBinding
import pro.filemanager.files.FileCore

class HomeActivity : AppCompatActivity(), ComponentCallbacks2 {

    lateinit var binding: ActivityHomeBinding
    var currentOnBackBehavior: (() -> Unit)? = null

    lateinit var handler: Handler

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)

        ApplicationLoader.releaseLists()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handler = Handler(Looper.getMainLooper())

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(supportFragmentManager.findFragmentById(R.id.activityHomeRootDrawerLayout) == null) {
            supportFragmentManager.beginTransaction().add(R.id.activityHomeRootDrawerLayout, HomeFragment(), FragmentWrapper.NAME_HOME_FRAGMENT).commit()
        }
    }

    override fun onResume() {
        super.onResume()

        FileCore.openingInProgress = false
        ShareTool.sharingInProgress = false
    }

    override fun onBackPressed() {
        currentOnBackBehavior?.invoke() ?: super.onBackPressed()
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        PermissionWrapper.handleStorageGroupRequestResult(this, requestCode, grantResults,
                {
                    PermissionWrapper.lastRequestResultGrantedAction?.invoke()
                }
        ) {
            onBackPressed()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if(item.itemId == android.R.id.home) {
            onBackPressed()

            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
}