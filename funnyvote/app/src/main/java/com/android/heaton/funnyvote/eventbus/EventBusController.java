package com.android.heaton.funnyvote.eventbus;

import com.android.heaton.funnyvote.database.Option;
import com.android.heaton.funnyvote.database.Promotion;
import com.android.heaton.funnyvote.database.User;
import com.android.heaton.funnyvote.database.VoteData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by heaton on 2016/10/2.
 */

public class EventBusController {

    public static class UIControlEvent {
        public static final String SCROLL_TO_TOP = "SCROLL_TO_TOP";
        public static final String SHOW_CIRCLE = "SHOW_CIRCLE";
        public static final String HIDE_CIRCLE = "HIDE_CIRCLE";
        public static final String SEARCH_KEYWORD = "SEARCH_KEYWORD";
        public final String message;
        public final String keyword;

        public UIControlEvent(String message) {
            this.message = message;
            this.keyword = null;
        }
        public UIControlEvent(String message,String keyword) {
            this.message = message;
            this.keyword = keyword;
        }
    }

    public static class NetworkEvent {
        public static final String RELOAD_USER = "RELOAD_USER";
        public final String message;
        public final String tab;

        public NetworkEvent(String message) {
            this.message = message;
            this.tab = null;
        }
        public NetworkEvent(String message,String tab) {
            this.message = message;
            this.tab = tab;
        }
    }

    public final static class OptionChoiceEvent {
        public static final String OPTION_CHOICED = "OPTION_CHOICED";
        public static final String OPTION_EXPAND = "OPTION_EXPAND";
        public final String message;
        public final long Id;
        public final String code;

        public OptionChoiceEvent(Long Id, String message, String code) {
            this.message = message;
            this.Id = Id;
            this.code = code;
        }
    }

    public final static class OptionControlEvent {
        public static final String OPTION_ADD = "OPTION_ADD";
        public static final String OPTION_REMOVE = "OPTION_REMOVE";
        public static final String OPTION_INPUT_TEXT = "OPTION_INPUT_TEXT";
        public static final String OPTION_ADD_CHECK = "OPTION_ADD_CHECK";

        public final String message;
        public final long Id;
        public final String inputText;
        public final String code;

        public OptionControlEvent(long id, String inputText, String message, String code) {
            this.inputText = inputText;
            this.message = message;
            this.Id = id;
            this.code = code;
        }
    }

    public final static class VoteDataControlEvent {
        public static final String VOTE_SYNC_WALL_AND_CONTENT = "VOTE_SYNC_WALL_AND_CONTENT";
        public static final String VOTE_SYNC_WALL_FOR_FAVORITE = "VOTE_SYNC_WALL_FOR_FAVORITE";
        public static final String VOTE_FAVORITE = "VOTE_FAVORITE";
        public static final String VOTE_QUICK_POLL = "VOTE_QUICK_POLL";

        public final String message;
        public final VoteData data;
        public final List<String> optionList;

        public VoteDataControlEvent(VoteData data, String message) {
            this.data = data;
            this.message = message;
            this.optionList = null;
        }

        public VoteDataControlEvent(VoteData data, List<String> optionList, String message) {
            this.data = data;
            this.message = message;
            this.optionList = optionList;
        }

        public VoteDataControlEvent(VoteData data, String optionCode, String message) {
            this.data = data;
            this.message = message;
            optionList = new ArrayList<>();
            optionList.add(optionCode);
        }
    }

    public final static class RemoteServiceEvent {
        public static final String CREATE_VOTE = "create_vote";
        public static final String GET_VOTE = "get_vote";
        public static final String POLL_VOTE = "poll_vote";
        public static final String FAVORITE_VOTE = "favorite_vote";
        public static final String GET_VOTE_LIST_HOT = "get_vote_list_hot";
        public static final String GET_VOTE_LIST_NEW = "get_vote_list_new";
        public static final String GET_VOTE_LIST_FAVORITE = "get_vote_list_favorite";
        public static final String GET_VOTE_LIST_SEARCH = "get_vote_list_search";
        public static final String GET_VOTE_LIST_HISTORY_CREATE = "get_vote_list_history_create";
        public static final String GET_VOTE_LIST_HISTORY_PARTICIPATE = "get_vote_list_history_participate";
        public static final String ADD_NEW_OPTION = "add_new_option";
        public static final String GET_PERSONAL_INFO = "get_user_info";

        public static final String GET_PROMOTION_LIST = "get_promotion_list";

        public String message;
        public boolean success = false;
        public VoteData voteData;
        public List<Option> optionList;
        public List<VoteData> voteDataList;
        public List<Promotion> promotionList;
        public int offset;
        public String errorResponseMessage;
        public User user;


        public RemoteServiceEvent(String message, boolean success, VoteData voteData
                , List<Option> optionList) {
            this.success = success;
            this.message = message;
            this.optionList = optionList;
            this.voteData = voteData;
        }

        public RemoteServiceEvent(String message, boolean success, int offset
                , List<VoteData> voteDataList) {
            this.success = success;
            this.message = message;
            this.offset = offset;
            this.voteDataList = voteDataList;
        }

        public RemoteServiceEvent(String message, boolean success, String errorResponseMessage) {
            this.success = success;
            this.message = message;
            this.errorResponseMessage = errorResponseMessage;
        }

        public RemoteServiceEvent(String message, boolean success, User user) {
            this.success = success;
            this.message = message;
            this.user = user;
        }
        public RemoteServiceEvent(String message, boolean success, List<Promotion> promotionList) {
            this.success = success;
            this.message = message;
            this.promotionList = promotionList;
        }

    }
}
