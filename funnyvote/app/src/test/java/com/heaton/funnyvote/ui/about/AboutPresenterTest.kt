package com.heaton.funnyvote.ui.about

import com.heaton.funnyvote.ui.account.AccountContract
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import com.heaton.funnyvote.ui.about.AboutContract.View

import org.mockito.Mockito.verify

class AboutPresenterTest {
    private lateinit var presenter: AboutPresenter
    @Mock
    private lateinit var view: AboutContract.View

    @Before
    @Throws(Exception::class)
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun createPresenter_setsThePresenterToView() {
        // Get a reference to the class under test
        presenter = AboutPresenter(view)

        // Then the presenter is set to the userPageView
        verify<AboutContract.View>(view).setPresenter(presenter)
        presenter.start()
    }

    @Test
    fun intentToAllSubItem() {
        presenter = AboutPresenter(view)
        presenter.IntentToAbout()
        verify<View>(view).showAbout()
        presenter.IntentToAppStore()
        verify<View>(view).showAppStore()
        presenter.IntentToAuthorInfo()
        verify<View>(view).showAuthorInfo()
        presenter.IntentToIntroduction()
        verify<View>(view).showIntroduction()
        presenter.IntentToLicence()
        verify<View>(view).showLicence()
        presenter.IntentToProblem()
        verify<View>(view).showProblem()
        presenter.IntentToShareApp()
        verify<View>(view).showShareApp()
    }
}
