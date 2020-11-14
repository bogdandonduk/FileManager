package pro.filemanager.videos

object VideoSimpleManualInjector {

    fun provideViewModelFactory() : VideoBrowserViewModelFactory {
        val imageRepo = VideoRepo.getInstance()

        return VideoBrowserViewModelFactory(imageRepo)
    }

}