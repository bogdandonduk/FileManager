package pro.filemanager

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import pro.filemanager.core.generics.BaseSectionFragment
import pro.filemanager.databinding.FragmentHomeBinding
import pro.filemanager.images.ImageCore

class HomeFragment : BaseSectionFragment() {

    lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        try {
            navController = Navigation.findNavController(binding.root)

            activity.setSupportActionBar(binding.fragmentHomeLayoutBaseToolbarInclude.layoutBaseToolBarInclude.layoutBaseToolbar)
            activity.supportActionBar?.title = requireContext().resources.getString(R.string.title_multimedia)

            binding.fragmentHomeRootLayout.visibility = View.INVISIBLE

            binding.fragmentHomeAudiosBtn.layoutHomeTileRootLayoutContent.post {

                binding.fragmentHomeAudiosBtn.layoutHomeTileRootLayoutContent.width.let {
                    val textSize = (it / 22).toFloat()

                    binding.fragmentHomeAudiosBtn.layoutHomeTileTitle.textSize = textSize
                    binding.fragmentHomeAudiosBtn.layoutHomeTileTitle.text = requireContext().resources.getString(R.string.title_audio)
                    ImageCore.glideSimpleRequestBuilder
                            .load(R.drawable.ic_audio)
                            .into(binding.fragmentHomeAudiosBtn.layoutHomeTileIcon)

//                binding.fragmentHomeVideosBtn.layoutHomeTileTitle.textSize = textSize
//                binding.fragmentHomeVideosBtn.layoutHomeTileTitle.text = requireContext().resources.getString(R.string.title_video)

//                binding.fragmentHomeDocsBtn.layoutHomeTileTitle.textSize = textSize
//                binding.fragmentHomeDocsBtn.layoutHomeTileTitle.text = requireContext().resources.getString(R.string.title_docs)

//                binding.fragmentHomeAppsBtn.layoutHomeTileTitle.textSize = textSize
//                binding.fragmentHomeAppsBtn.layoutHomeTileTitle.text = requireContext().resources.getString(R.string.title_apps)

                    binding.fragmentHomeImagesBtn.layoutHomeTileTitle.textSize = textSize
                    binding.fragmentHomeImagesBtn.layoutHomeTileTitle.text = requireContext().resources.getString(R.string.title_images)

//                binding.fragmentHomeApksBtn.layoutHomeTileTitle.textSize = textSize
//                binding.fragmentHomeApksBtn.layoutHomeTileTitle.text = requireContext().resources.getString(R.string.title_apks)

//                binding.fragmentHomeCloudBtn.layoutHomeTileTitle.textSize = textSize
//                binding.fragmentHomeCloudBtn.layoutHomeTileTitle.text = requireContext().resources.getString(R.string.title_cloud)
//
//                binding.fragmentHomeTransferPcBtn.layoutHomeTileTitle.textSize = textSize
//                binding.fragmentHomeTransferPcBtn.layoutHomeTileTitle.text = requireContext().resources.getString(R.string.title_transfer_pc)
//
//                binding.fragmentHomeTrashBtn.layoutHomeTileTitle.textSize = textSize
//                binding.fragmentHomeTrashBtn.layoutHomeTileTitle.text = requireContext().resources.getString(R.string.title_trash)
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

//            binding.fragmentHomeInternalBtn.setOnClickListener {
//                navController.navigate(R.id.action_homeFragment_to_fileBrowserFragment, bundleOf(
//                    FileCore.KEY_ARGUMENT_PATH to FileCore.KEY_INTERNAL_STORAGE,
//                    FileCore.KEY_ARGUMENT_APP_BAR_TITLE to requireActivity().resources.getString(R.string.title_internal_storage)
//                ))
//            }
//
//            binding.fragmentHomeExternalBtn.setOnClickListener {
//                navController.navigate(R.id.action_homeFragment_to_fileBrowserFragment, bundleOf(
//                    FileCore.KEY_ARGUMENT_PATH to FileCore.KEY_EXTERNAL_STORAGE,
//                    FileCore.KEY_ARGUMENT_APP_BAR_TITLE to requireActivity().resources.getString(R.string.title_external_storage)
//                ))
//            }

            binding.fragmentHomeImagesBtn.layoutHomeTileRootLayoutContent.setOnClickListener {
                navController.navigate(R.id.action_homeFragment_to_imageLibraryFragment)
            }
        } catch(thr: Throwable) {

        }
    }
}