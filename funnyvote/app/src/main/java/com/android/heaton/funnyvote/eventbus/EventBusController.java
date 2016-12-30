package com.android.heaton.funnyvote.eventbus;

import com.android.heaton.funnyvote.database.Option;
import com.android.heaton.funnyvote.database.VoteData;

import java.util.List;

/**
 * Created by heaton on 2016/10/2.
 */

public class EventBusController {

    public static class UIControlEvent {
        public static final String SCROLL_TO_TOP = "SCROLL_TO_TOP";
        public final String message;

        public UIControlEvent(String message) {
            this.message = message;
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
        public final String message;
        public final VoteData data;

        public VoteDataControlEvent(VoteData data, String message) {
            this.data = data;
            this.message = message;
        }
    }

    public final static class RemoteServiceEvent {
        public static final String CREAT_VOTE = "create_vote";
        public static final String GET_VOTE = "get_vote";
        public static final String POLL_VOTE = "poll_vote";
        public static final String GET_VOTE_LIST_HOT = "get_vote_list_hot";
        public static final String GET_VOTE_LIST_NEW = "get_vote_list_new";
        public static final String GET_VOTE_LIST_FAVORITE = "get_vote_list_favorite";
        public static final String GET_VOTE_LIST_HISTORY_CREATE = "get_vote_list_history_create";
        public static final String GET_VOTE_LIST_HISTORY_PARTICIPATE = "get_vote_list_history_participate";

        public String message;
        public boolean success = false;
        public VoteData voteData;
        public List<Option> optionList;
        public List<VoteData> voteDataList;
        public int offset;
        public String errorResponseMessage;


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


    }
}
