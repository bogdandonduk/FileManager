package pro.filemanager

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import pro.filemanager.core.PermissionWrapper
import pro.filemanager.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    lateinit var binding: ActivityHomeBinding
    lateinit var externalStorageRequestSuccessAction: Runnable
    val viewsCreatedStateKey: String = "viewsCreated"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(savedInstanceState == null) {

            binding.homeActivityRootLayout.alpha = 0f
            binding.homeActivityRootLayout.scaleX = 0.95f
            binding.homeActivityRootLayout.scaleY = 0.95f
            binding.homeActivityRootLayout.visibility = View.GONE
            binding.homeActivityRootLayout.visibility = View.VISIBLE
            binding.homeActivityRootLayout.animate().alpha(1f).setDuration(400).start()
            binding.homeActivityRootLayout.animate().scaleX(1f).setDuration(400).start()
            binding.homeActivityRootLayout.animate().scaleY(1f).setDuration(400).start()

        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putBoolean(viewsCreatedStateKey, true)
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