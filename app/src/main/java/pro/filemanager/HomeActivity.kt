package pro.filemanager

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import kotlinx.coroutines.launch
import pro.filemanager.core.wrappers.PermissionWrapper
import pro.filemanager.core.tools.ShareTool
import pro.filemanager.databinding.ActivityHomeBinding
import pro.filemanager.files.FileCore

class HomeActivity : AppCompatActivity() {

    lateinit var binding: ActivityHomeBinding
    lateinit var navController: NavController

    var currentOnBackBehavior: Runnable? = null

    lateinit var externalStorageRequestSuccessAction: Runnable

    lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navController = (supportFragmentManager.findFragmentById(R.id.homeActivityContentNavHost) as NavHostFragment).navController

        binding.homeActivityMainNavView.setupWithNavController(navController)

        handler = Handler(Looper.getMainLooper())
    }

    override fun onResume() {
        super.onResume()

        FileCore.outerIntentInProgress = false
        ShareTool.outerSharingIntentInProgress = false
    }

    override fun onBackPressed() {
        if(currentOnBackBehavior != null)
            currentOnBackBehavior!!.run()
        else
            super.onBackPressed()
    }

    fun requestExternalStoragePermission(action: Runnable = Runnable {}) {
        externalStorageRequestSuccessAction = action
        PermissionWrapper.requestExternalStorage(this, externalStorageRequestSuccessAction)
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        PermissionWrapper.handleExternalStorageRequestResult(this, requestCode, grantResults,
                externalStorageRequestSuccessAction,
                {
                    onBackPressed()
                })
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