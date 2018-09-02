package com.heaton.funnyvote.ui.createvote;

import com.heaton.funnyvote.data.VoteData.VoteDataRepository;
import com.heaton.funnyvote.data.VoteData.VoteDataSource;
import com.heaton.funnyvote.data.user.UserDataRepository;
import com.heaton.funnyvote.data.user.UserDataSource;
import com.heaton.funnyvote.database.Option;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.database.VoteData;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CreateVoteActivityPresenterTest {
    private static User user;
    private static final String voteCode = "CODE_123";
    private static final VoteData voteData = new VoteData();
    private static List<Option> optionList = new ArrayList<>();

    @Mock
    private UserDataRepository userDataRepository;
    @Mock
    private VoteDataRepository voteDataRepository;
    @Mock
    private CreateVoteContract.ActivityView activityView;
    @Mock
    private CreateVoteContract.SettingFragmentView settingFragmentView;
    @Mock
    private CreateVoteContract.OptionFragmentView optionFragmentView;

    private CreateVoteActivityPresenter presenter;

    @Captor
    private ArgumentCaptor<VoteDataSource.GetVoteDataCallback> getVoteDataCallbackArgumentCaptor;

    @Captor
    private ArgumentCaptor<UserDataSource.GetUserCallback> getUserCallbackArgumentCaptor;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        user = mock(User.class);
        when(user.getUserName()).thenReturn("Heaton");
    }

    @Test
    public void createPresenter_setsThePresenterToView() {
        // Get a reference to the class under test
        presenter = new CreateVoteActivityPresenter(voteDataRepository, userDataRepository
                , activityView, optionFragmentView, settingFragmentView);

        // Then the presenter is set to the view
        verify(activityView).setPresenter(presenter);
        presenter.setOptionFragmentView(optionFragmentView);
        presenter.setSettingFragmentView(settingFragmentView);
    }

    @Test
    public void getUserAndInitialDefaultView() {
        presenter = new CreateVoteActivityPresenter(voteDataRepository, userDataRepository
                , activityView, optionFragmentView, settingFragmentView);
        presenter.start();
        verify(userDataRepository).getUser(getUserCallbackArgumentCaptor.capture(), eq(false));
        getUserCallbackArgumentCaptor.getValue().onResponse(user);
    }

    @Test
    public void submitCreateVoteSuccessAndShowVoteDetail() {
        presenter = new CreateVoteActivityPresenter(voteDataRepository, userDataRepository
                , activityView, optionFragmentView, settingFragmentView);
        presenter.setVoteSettings(voteData);

        optionList.clear();
        for (long i = 0; i < 2; i++) {
            Option option = new Option();
            option.setTitle("OPTION_CODE_" + i);
            option.setId(i);
            option.setCount(0);
            optionList.add(option);
        }
        presenter.setOptionList(optionList);
        presenter.getVoteSettings().setTitle("TITLE_1");
        presenter.getVoteSettings().setAuthorCode("AUTHOR_1");
        presenter.getVoteSettings().setMaxOption(1);
        presenter.getVoteSettings().setMinOption(1);
        presenter.getVoteSettings().setIsUserCanAddOption(false);
        presenter.getVoteSettings().setIsCanPreviewResult(false);
        presenter.getVoteSettings().setIsNeedPassword(false);
        presenter.getVoteSettings().setSecurity(VoteData.SECURITY_PUBLIC);
        presenter.getVoteSettings().setEndTime(System.currentTimeMillis() + 3000 * 86400 * 1000);
        when(settingFragmentView.getFinalVoteSettings(voteData)).thenReturn(voteData);

        presenter.submitCreateVote();
        verify(activityView).showLoadingCircle();
        verify(voteDataRepository).createVote(any(VoteData.class), anyList()
                , any(File.class), getVoteDataCallbackArgumentCaptor.capture());
        getVoteDataCallbackArgumentCaptor.getValue().onVoteDataLoaded(voteData);
        verify(activityView).showHintToast(anyInt());
        verify(activityView).hideLoadingCircle();
        verify(activityView).IntentToVoteDetail(voteData);
    }

    @Test
    public void submitCreateVoteRemoteFailureAndShowErrorToast() {
        presenter = new CreateVoteActivityPresenter(voteDataRepository, userDataRepository
                , activityView, optionFragmentView, settingFragmentView);
        presenter.setVoteSettings(voteData);

        optionList.clear();
        for (long i = 0; i < 2; i++) {
            Option option = new Option();
            option.setTitle("OPTION_CODE_" + i);
            option.setId(i);
            option.setCount(0);
            optionList.add(option);
        }
        presenter.setOptionList(optionList);
        presenter.getVoteSettings().setTitle("TITLE_1");
        presenter.getVoteSettings().setAuthorCode("AUTHOR_1");
        presenter.getVoteSettings().setMaxOption(1);
        presenter.getVoteSettings().setMinOption(1);
        presenter.getVoteSettings().setIsUserCanAddOption(false);
        presenter.getVoteSettings().setIsCanPreviewResult(false);
        presenter.getVoteSettings().setIsNeedPassword(false);
        presenter.getVoteSettings().setSecurity(VoteData.SECURITY_PUBLIC);
        presenter.getVoteSettings().setEndTime(System.currentTimeMillis() + 3000 * 86400 * 1000);
        when(settingFragmentView.getFinalVoteSettings(voteData)).thenReturn(voteData);

        presenter.submitCreateVote();
        verify(activityView).showLoadingCircle();
        verify(voteDataRepository).createVote(any(VoteData.class), anyList()
                , any(File.class), getVoteDataCallbackArgumentCaptor.capture());
        getVoteDataCallbackArgumentCaptor.getValue().onVoteDataNotAvailable();
        verify(activityView).showHintToast(anyInt());
        verify(activityView).hideLoadingCircle();
        verify(activityView, never()).IntentToVoteDetail(voteData);
    }

    @Test
    public void submitCreateVoteLocalFailureAndShowErrorDialog() {
        presenter = new CreateVoteActivityPresenter(voteDataRepository, userDataRepository
                , activityView, optionFragmentView, settingFragmentView);
        presenter.setVoteSettings(voteData);

        for (long i = 0; i < 2; i++) {
            Option option = new Option();
            option.setTitle("OPTION_CODE_" + i);
            option.setId(i);
            option.setCount(0);
            optionList.add(option);
        }
        presenter.setOptionList(optionList);
        //Only no title case
        presenter.getVoteSettings().setTitle("");
        presenter.getVoteSettings().setAuthorCode("AUTHOR_1");
        presenter.getVoteSettings().setMaxOption(1);
        presenter.getVoteSettings().setMinOption(1);
        presenter.getVoteSettings().setIsUserCanAddOption(false);
        presenter.getVoteSettings().setIsCanPreviewResult(false);
        presenter.getVoteSettings().setIsNeedPassword(false);
        presenter.getVoteSettings().setSecurity(VoteData.SECURITY_PUBLIC);
        presenter.getVoteSettings().setEndTime(System.currentTimeMillis() + 3000 * 86400 * 1000);
        when(settingFragmentView.getFinalVoteSettings(voteData)).thenReturn(voteData);

        presenter.submitCreateVote();
        verify(activityView).showLoadingCircle();
        verify(voteDataRepository, never()).createVote(any(VoteData.class), anyList()
                , any(File.class), getVoteDataCallbackArgumentCaptor.capture());
        verify(activityView).hideLoadingCircle();
        verify(activityView).showCreateVoteError(anyMap());
    }

    @Test
    public void addAndReviseAndRemoveOptionUpdateToView() {
        presenter = new CreateVoteActivityPresenter(voteDataRepository, userDataRepository
                , activityView, optionFragmentView, settingFragmentView);
        long optionId = presenter.addNewOption();
        presenter.addNewOption();
        presenter.addNewOption();
        presenter.reviseOption(optionId, "newText");
        String equalText = "";
        for (Option option : presenter.getOptionList()) {
            if (option.getId() == optionId) {
                equalText = option.getTitle();
            }
        }
        Assert.assertEquals(equalText, "newText");
        verify(optionFragmentView, times(3)).refreshOptions();
        int optionNumber = presenter.getOptionList().size();
        presenter.removeOption(optionId);
        verify(optionFragmentView, times(4)).refreshOptions();
        Assert.assertEquals(presenter.getOptionList().size(), optionNumber - 1);
    }

    @Test
    public void updateVoteSecurityAndEndTimeAndTitleAndUpdateToView() {
        presenter = new CreateVoteActivityPresenter(voteDataRepository, userDataRepository
                , activityView, optionFragmentView, settingFragmentView);
        presenter.updateVoteSecurity(VoteData.SECURITY_PRIVATE);
        Assert.assertEquals(presenter.getVoteSettings().getSecurity(), VoteData.SECURITY_PRIVATE);
        presenter.updateVoteEndTime(System.currentTimeMillis()
                - CreateVoteActivityPresenter.Companion.getDEFAULT_END_TIME() * 1000 * 86400);
        verify(activityView).showHintToast(anyInt());
        presenter.updateVoteEndTime(System.currentTimeMillis()
                + (CreateVoteActivityPresenter.Companion.getDEFAULT_END_TIME_MAX() + 10) * 1000 * 86400);
        verify(activityView).showHintToast(anyInt());
        presenter.updateVoteEndTime(System.currentTimeMillis()
                + CreateVoteActivityPresenter.Companion.getDEFAULT_END_TIME_MAX() * 1000 * 86400);
        verify(settingFragmentView).setUpVoteSettings(any(VoteData.class));
        presenter.updateVoteImage(new File("test"));
        Assert.assertNotNull(presenter.getVoteSettings().getImageFile());
        presenter.updateVoteTitle("title");
        Assert.assertEquals(presenter.getVoteSettings().getTitle(),"title");
    }
}
