package pro.filemanager

import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import pro.filemanager.databinding.FragmentHomeBinding
import pro.filemanager.files.FileManager
import kotlin.math.abs

class HomeFragment : Fragment() {

    lateinit var binding: FragmentHomeBinding
    lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        binding.fragmentHomeRootLayout.visibility = View.INVISIBLE

        binding.fragmentHome1percentDeterminer.post {
            binding.fragmentHome1percentDeterminer.width.let {
                binding.fragmentHomeAudiosBtn.layoutHomeTileRootLayout.setPadding(0, it, 0, 0)
                binding.fragmentHomeVideosBtn.layoutHomeTileRootLayout.setPadding(0, it, 0, 0)
                binding.fragmentHomeDocsBtn.layoutHomeTileRootLayout.setPadding(0, it, 0, 0)
                binding.fragmentHomeAppsBtn.layoutHomeTileRootLayout.setPadding(0, it, 0, 0)
                binding.fragmentHomeImagesBtn.layoutHomeTileRootLayout.setPadding(0, it, 0, 0)
                binding.fragmentHomeTransferPcBtn.layoutHomeTileRootLayout.setPadding(0, it, 0, 0)
                binding.fragmentHomeCloudBtn.layoutHomeTileRootLayout.setPadding(0, it, 0, 0)
                binding.fragmentHomeTransferAppsBtn.layoutHomeTileRootLayout.setPadding(0, it, 0, 0)
                binding.fragmentHomeNamelessBtn.layoutHomeTileRootLayout.setPadding(0, it, 0, 0)
            }

            binding.fragmentHomeRootLayout.visibility = View.VISIBLE
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = Navigation.findNavController(binding.root)

        binding.fragmentHomeInternalBtn.setOnClickListener {

            try {
                navController.navigate(R.id.action_homeFragment_to_fileBrowserFragment, bundleOf(
                        FileManager.KEY_ARGUMENT_PATH to FileManager.getInternalRootPath(),
                        FileManager.KEY_ARGUMENT_APP_BAR_TITLE to requireActivity().resources.getString(R.string.title_internal_storage)
                ))
            } catch (e: Exception) {

            }

        }

        binding.fragmentHomeAudiosBtn.layoutHomeTileRootLayoutContent.setOnClickListener {
            navController.navigate(R.id.action_homeFragment_to_audioBrowserFragment)
        }

        binding.fragmentHomeVideosBtn.layoutHomeTileRootLayoutContent.setOnClickListener {
            navController.navigate(R.id.action_homeFragment_to_videoBrowserFragment)
        }

        binding.fragmentHomeDocsBtn.layoutHomeTileRootLayoutContent.setOnClickListener {
            navController.navigate(R.id.action_homeFragment_to_docBrowserFragment)
        }

        binding.fragmentHomeImagesBtn.layoutHomeTileRootLayoutContent.setOnClickListener {
            navController.navigate(R.id.action_homeFragment_to_imageBrowserFragment)
        }

        binding.fragmentHomeExternalBtn.setOnClickListener {
            navController.navigate(R.id.action_homeFragment_to_fileBrowserFragment, bundleOf(
                    FileManager.KEY_ARGUMENT_PATH to FileManager.externalRootPath,
                    FileManager.KEY_ARGUMENT_APP_BAR_TITLE to requireActivity().resources.getString(R.string.title_external_storage)
            ))
        }
    }

}