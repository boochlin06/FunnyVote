package com.android.heaton.easyvote;

/**
 * Created by heaton on 16/4/1.
 */
public class VoteData {
    public String voteCode = "vote code";
    public String title;
    public String authorName = "Heaton";
    public String authorCode = "heaton_id";
    public String authorIcon ="";
    public String voteImage = "";
    public int localImage;
    public long startTime;
    public long endTime;
    public String option1title = "Option 1 Option 1 Option 1 Option 1Option 1 Option 1Option 1 Option 1";
    public String option1code = "option 1 code";
    public int option1count = 0;

    public String option2title = "Option 2";
    public String option2code = "option 2 code";
    public int option2Count = 0;
    public String optionTopTitle = "Option Champion Option Champion Option Champion Option Champion Option Champion";
    public String optionTopCode = "NONE";
    public int optionTopCount = 2;
    public String optionUserChoiceTitle = "Option User";
    public String optionUserChoiceCode = "2";
    public int optionUserChoiceCount = 0;
    public int minOption = 1;
    public int maxOption = 1;
    public int optionCount = 5;
    public int pollCount;
    public boolean isPolled = false;
    public boolean isFavorite;

    // TODO: OPTION TYPE
    public String pollType;
}
