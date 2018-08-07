package com.heaton.funnyvote.ui.about;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;

public class AboutPresenterTest {
    private AboutPresenter presenter;
    @Mock
    private AboutContract.View view;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void createPresenter_setsThePresenterToView() {
        // Get a reference to the class under test
        presenter = new AboutPresenter(view);

        // Then the presenter is set to the userPageView
        verify(view).setPresenter(presenter);
        presenter.start();
    }

    @Test
    public void intentToAllSubItem() {
        presenter = new AboutPresenter(view);
        presenter.IntentToAbout();
        verify(view).showAbout();
        presenter.IntentToAppStore();
        verify(view).showAppStore();
        presenter.IntentToAuthorInfo();
        verify(view).showAuthorInfo();
        presenter.IntentToIntroduction();
        verify(view).showIntroduction();
        presenter.IntentToLicence();
        verify(view).showLicence();
        presenter.IntentToProblem();
        verify(view).showProblem();
        presenter.IntentToShareApp();
        verify(view).showShareApp();
    }
}
