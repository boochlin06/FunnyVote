package com.android.heaton.funnyvote;

import android.content.Context;
import android.util.DisplayMetrics;

import com.android.heaton.funnyvote.retrofit.Server;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by heaton on 2016/10/12.
 */

public class Util {
    /**
     * Return date in specified format.
     *
     * @param milliSeconds Date in milliseconds
     * @param dateFormat   Date format
     * @return String representing date in specified format
     */
    public static String getDate(long milliSeconds, String dateFormat) {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    /**
     * Covert dp to px
     *
     * @param dp
     * @param context
     * @return pixel
     */
    public static float convertDpToPixel(float dp, Context context) {
        float px = dp * getDensity(context);
        return px;
    }

    /**
     * Covert px to dp
     *
     * @param px
     * @param context
     * @return dp
     */
    public static float convertPixelToDp(float px, Context context) {
        float dp = px / getDensity(context);
        return dp;
    }

    /**
     * 120dpi = 0.75
     * 160dpi = 1 (default)
     * 240dpi = 1.5
     *
     * @param context
     * @return
     */
    public static float getDensity(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.density;
    }

    /**
     * Get vote link by vote code
     *
     * @param voteCode
     */
    public static String getVoteLink(String voteCode) {
        return Server.BASE_URL.substring(0,Server.BASE_URL.length()-1) + ":5487/link/" + voteCode;
    }
}
