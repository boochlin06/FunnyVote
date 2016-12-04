package com.android.heaton.funnyvote.eventbus;

import com.android.heaton.funnyvote.database.VoteData;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by heaton on 2016/10/2.
 */

public class EventBusController {
    public static class SubmitMessageEvent {
        public static final String EVENT_SUBMIT = "SUBMIT";
        public final String message;

        public SubmitMessageEvent(String message) {
            this.message = message;
        }
    }
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

        public OptionChoiceEvent(Long Id, String message) {
            this.message = message;
            this.Id = Id;
        }
    }

    public final static class OptionControlEvent {
        public static final String OPTION_ADD = "OPTION_ADD";
        public static final String OPTION_REMOVE = "OPTION_REMOVE";
        public static final String OPTION_INPUT_TEXT = "OPTION_INPUT_TEXT";

        public final String message;
        public final long Id;
        public final String inputText;

        public OptionControlEvent(long id, String inputText, String message) {
            this.inputText = inputText;
            this.message = message;
            this.Id = id;
        }
    }
    public final static class VoteDataControlEvent {
        public static final String VOTE_SYNC_WALL_AND_CONTENT = "VOTE_SYNC_WALL_AND_CONTENT";
        public static final String VOTE_SYNC_WALL_FOR_FAVORITE = "VOTE_SYNC_WALL_FOR_FAVORITE";
        public final String message;
        public final VoteData data;

        public VoteDataControlEvent(VoteData data , String message) {
            this.data = data;
            this.message = message;
        }
    }
    public final static class NetworkEvent {
        public static final String INIT_GUEST = "init_guest";
        public static final String INIT_DB = "init_db";

        public Response<ResponseBody> response = null;
        public Call call;
        public String message;
        public boolean success = false;
        public NetworkEvent(String message, boolean success, Call call, Response<ResponseBody> response) {
            this.call=call;
            this.success = success;
            this.response = response;
            this.message = message;
        }

    }
}
