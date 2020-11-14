package pro.filemanager.docs

object DocSimpleManualInjector {

    fun provideViewModelFactory() : DocBrowserViewModelFactory {
        val docRepo = DocRepo.getInstance()

        return DocBrowserViewModelFactory(docRepo)
    }

}