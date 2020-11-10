package pro.filemanager

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.ActionBar
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

class HomeActivity : AppCompatActivity() {

    lateinit var binding: ActivityHomeBinding
    lateinit var externalStorageRequestSuccessAction: Runnable

    lateinit var navController: NavController
    lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navController = (supportFragmentManager.findFragmentById(R.id.homeActivityContentNavHost) as NavHostFragment).navController

        appBarConfiguration = AppBarConfiguration(navController.graph, binding.homeActivityRootDrawerLayout)

        binding.homeActivityNavView.setupWithNavController(navController)
        setupActionBarWithNavController(navController, appBarConfiguration)

        if(savedInstanceState == null) {

            binding.homeActivityRootDrawerLayout.alpha = 0f
            binding.homeActivityRootDrawerLayout.scaleX = 0.97f
            binding.homeActivityRootDrawerLayout.scaleY = 0.97f
            binding.homeActivityRootDrawerLayout.visibility = View.GONE
            binding.homeActivityRootDrawerLayout.visibility = View.VISIBLE
            binding.homeActivityRootDrawerLayout.animate().alpha(1f).setDuration(400).start()
            binding.homeActivityRootDrawerLayout.animate().scaleX(1f).setDuration(400).start()
            binding.homeActivityRootDrawerLayout.animate().scaleY(1f).setDuration(400).start()

        }

    }

    override fun onSupportNavigateUp(): Boolean {
        val navController: NavController = findNavController(R.id.homeActivityContentNavHost)

        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putBoolean(UIManager.KEY_VIEWS_CREATED, true)

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

}