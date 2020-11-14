package pro.filemanager.audios

object AudioSimpleManualInjector {

    fun provideViewModelFactory() : AudioBrowserViewModelFactory {
        val audioRepo = AudioRepo.getInstance()

        return AudioBrowserViewModelFactory(audioRepo)
    }

}