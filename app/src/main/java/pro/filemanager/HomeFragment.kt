package pro.filemanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import pro.filemanager.databinding.FragmentHomeBinding
import pro.filemanager.files.FileCore

class HomeFragment : Fragment() {

    lateinit var binding: FragmentHomeBinding
    lateinit var navController: NavController
    lateinit var activity: HomeActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity = requireActivity() as HomeActivity

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        binding.fragmentHomeRootLayout.visibility = View.INVISIBLE

        binding.fragmentHomeAudiosBtn.layoutHomeTileRootLayoutContent.post {

            binding.fragmentHomeAudiosBtn.layoutHomeTileRootLayoutContent.width.let {
                val textSize = (it / 20).toFloat()

                binding.fragmentHomeAudiosBtn.layoutHomeTileTitle.textSize = textSize
                binding.fragmentHomeAudiosBtn.layoutHomeTileTitle.text = requireContext().resources.getString(R.string.title_audio)

                binding.fragmentHomeVideosBtn.layoutHomeTileTitle.textSize = textSize
                binding.fragmentHomeVideosBtn.layoutHomeTileTitle.text = requireContext().resources.getString(R.string.title_video)

                binding.fragmentHomeDocsBtn.layoutHomeTileTitle.textSize = textSize
                binding.fragmentHomeDocsBtn.layoutHomeTileTitle.text = requireContext().resources.getString(R.string.title_docs)

                binding.fragmentHomeAppsBtn.layoutHomeTileTitle.textSize = textSize
                binding.fragmentHomeAppsBtn.layoutHomeTileTitle.text = requireContext().resources.getString(R.string.title_apps)

                binding.fragmentHomeImagesBtn.layoutHomeTileTitle.textSize = textSize
                binding.fragmentHomeImagesBtn.layoutHomeTileTitle.text = requireContext().resources.getString(R.string.title_images)

                binding.fragmentHomeApksBtn.layoutHomeTileTitle.textSize = textSize
                binding.fragmentHomeApksBtn.layoutHomeTileTitle.text = requireContext().resources.getString(R.string.title_apks)

                binding.fragmentHomeCloudBtn.layoutHomeTileTitle.textSize = textSize
                binding.fragmentHomeCloudBtn.layoutHomeTileTitle.text = requireContext().resources.getString(R.string.title_cloud)

                binding.fragmentHomeTransferPcBtn.layoutHomeTileTitle.textSize = textSize
                binding.fragmentHomeTransferPcBtn.layoutHomeTileTitle.text = requireContext().resources.getString(R.string.title_transfer_pc)

                binding.fragmentHomeTrashBtn.layoutHomeTileTitle.textSize = textSize
                binding.fragmentHomeTrashBtn.layoutHomeTileTitle.text = requireContext().resources.getString(R.string.title_trash)
            }

        }

        binding.fragmentHome1percentDeterminer.post {
            binding.fragmentHome1percentDeterminer.width.let {
                binding.fragmentHomeAudiosBtn.layoutHomeTileRootLayout.setPadding(0, it, 0, 0)
                binding.fragmentHomeVideosBtn.layoutHomeTileRootLayout.setPadding(0, it, 0, 0)
                binding.fragmentHomeDocsBtn.layoutHomeTileRootLayout.setPadding(0, it, 0, 0)
                binding.fragmentHomeAppsBtn.layoutHomeTileRootLayout.setPadding(0, it, 0, 0)
                binding.fragmentHomeImagesBtn.layoutHomeTileRootLayout.setPadding(0, it, 0, 0)
                binding.fragmentHomeApksBtn.layoutHomeTileRootLayout.setPadding(0, it, 0, 0)
                binding.fragmentHomeCloudBtn.layoutHomeTileRootLayout.setPadding(0, it, 0, 0)
                binding.fragmentHomeTransferPcBtn.layoutHomeTileRootLayout.setPadding(0, it, 0, 0)
                binding.fragmentHomeTrashBtn.layoutHomeTileRootLayout.setPadding(0, it, 0, 0)
            }

            binding.fragmentHomeRootLayout.visibility = View.VISIBLE
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = Navigation.findNavController(binding.root)

        activity.setSupportActionBar(binding.fragmentHomeLayoutBaseToolbarInclude.layoutBaseToolBarInclude.layoutBaseToolbar)
        activity.supportActionBar?.title = requireContext().resources.getString(R.string.title_multimedia)

        CoroutineScope(Main).launch {
            binding.fragmentHomeInternalBtn.setOnClickListener {
                navController.navigate(R.id.action_homeFragment_to_fileBrowserFragment, bundleOf(
                    FileCore.KEY_ARGUMENT_PATH to FileCore.KEY_INTERNAL_STORAGE,
                    FileCore.KEY_ARGUMENT_APP_BAR_TITLE to requireActivity().resources.getString(R.string.title_internal_storage)
                ))
            }

            binding.fragmentHomeExternalBtn.setOnClickListener {
                navController.navigate(R.id.action_homeFragment_to_fileBrowserFragment, bundleOf(
                    FileCore.KEY_ARGUMENT_PATH to FileCore.KEY_EXTERNAL_STORAGE,
                    FileCore.KEY_ARGUMENT_APP_BAR_TITLE to requireActivity().resources.getString(R.string.title_external_storage)
                ))
            }

            binding.fragmentHomeImagesBtn.layoutHomeTileRootLayoutContent.setOnClickListener {
                navController.navigate(R.id.action_homeFragment_to_imageBrowserFragment)
            }

        }

    }

}