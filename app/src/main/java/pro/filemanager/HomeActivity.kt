package pro.filemanager

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import pro.filemanager.core.PermissionWrapper
import pro.filemanager.core.UIManager
import pro.filemanager.databinding.ActivityHomeBinding
import pro.filemanager.files.FileCore
import java.io.File

class HomeActivity : AppCompatActivity() {

    lateinit var binding: ActivityHomeBinding
    lateinit var externalStorageRequestSuccessAction: Runnable

    lateinit var navController: NavController

    var onBackBehavior: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navController = (supportFragmentManager.findFragmentById(R.id.homeActivityContentNavHost) as NavHostFragment).navController

        binding.homeActivityNavView.setupWithNavController(navController)

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

    override fun onResume() {
        super.onResume()

        FileCore.outerIntentInProgress = false

    }
}