package pro.filemanager.images

object ImageSimpleManualInjector {

    fun provideViewModelFactory() : ImageBrowserViewModelFactory {
        val imageRepo = ImageRepo.getInstance()

        return ImageBrowserViewModelFactory(imageRepo)
    }

}