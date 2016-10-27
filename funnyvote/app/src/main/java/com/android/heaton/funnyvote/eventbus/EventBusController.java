package com.android.heaton.funnyvote.eventbus;

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

    public static class OptionChoiceEvent {
        public static final String EVENT_CHOICED = "CHOICED";
        public final String message;
        public final int optionPosition;

        public OptionChoiceEvent(int position, String message) {
            this.message = message;
            this.optionPosition = position;
        }
    }

    public static class OptionControlEvent {
        public static final String OPTION_ADD = "OPTION_ADD";
        public static final String OPTION_REMOVE = "OPTION_REMOVE";
        public static final String OPTION_INPUT_TEXT = "OPTION_INPUT_TEXT";
        public static final String OPTION_EXPAND = "OPTION_EXPAND";

        public final String message;
        public final int position;
        public final String inputText;

        public OptionControlEvent(int position, String inputText, String message) {
            this.inputText = inputText;
            this.message = message;
            this.position = position;
        }
    }
}
