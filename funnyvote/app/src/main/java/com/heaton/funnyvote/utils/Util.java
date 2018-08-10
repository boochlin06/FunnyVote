package com.heaton.funnyvote.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.DisplayMetrics;

import com.heaton.funnyvote.R;
import com.heaton.funnyvote.database.VoteData;
import com.heaton.funnyvote.retrofit.Server;
import com.heaton.funnyvote.ui.ShareDialogActivity;
import com.heaton.funnyvote.ui.personal.PersonalActivity;
import com.heaton.funnyvote.ui.votedetail.VoteDetailContentActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import static android.graphics.Paint.ANTI_ALIAS_FLAG;

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

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static Drawable textAsBitmap(Context context, String text, float textSize, int textColor) {
        Paint paint = new Paint(ANTI_ALIAS_FLAG);
        paint.setTextSize(textSize);
        paint.setColor(textColor);
        paint.setTextAlign(Paint.Align.LEFT);
        float baseline = -paint.ascent(); // ascent() is negative
        int width = (int) (paint.measureText(text) + 0.0f); // round
        int height = (int) (baseline + paint.descent() + 0.0f);
        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(image);
        canvas.drawText(text, 0, baseline, paint);
        return new BitmapDrawable(context.getResources(), image);
    }

    public static String randomUserName(Context context) {
        String[] area = context.getResources().getStringArray(R.array.area);
        String[] name = context.getResources().getStringArray(R.array.name);
        String randomArea = area[(int) (Math.random() * area.length)];
        String randomName = name[(int) (Math.random() * name.length)];
        return randomArea + randomName + Integer.toString((int) (Math.random() * 1000));
    }

    public static String BUNDLE_KEY_VOTE_CODE = "VOTE_ID";

    public static void startActivityToVoteDetail(Context context, String voteCode) {
        Intent intent = new Intent(context, VoteDetailContentActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_KEY_VOTE_CODE, voteCode);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }


    public static void sendShareIntent(Context context, VoteData data) {
        Intent shareDialog = new Intent(context, ShareDialogActivity.class);
        shareDialog.putExtra(ShareDialogActivity.EXTRA_TITLE, data.getTitle());
        shareDialog.putExtra(ShareDialogActivity.EXTRA_IMG_URL, data.getVoteImage());
        shareDialog.putExtra(ShareDialogActivity.EXTRA_VOTE_URL, Server.WEB_URL + data.getVoteCode());
        shareDialog.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(shareDialog);
    }

    public static void sendPersonalDetailIntent(Context context, VoteData data) {
        Intent personalActivity = new Intent(context, PersonalActivity.class);
        personalActivity.putExtra(PersonalActivity.EXTRA_PERSONAL_CODE, data.getAuthorCode());
        personalActivity.putExtra(PersonalActivity.EXTRA_PERSONAL_CODE_TYPE, data.getAuthorCodeType());
        personalActivity.putExtra(PersonalActivity.EXTRA_PERSONAL_NAME, data.getAuthorName());
        personalActivity.putExtra(PersonalActivity.EXTRA_PERSONAL_ICON, data.getAuthorIcon());
        personalActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(personalActivity);
    }

    public static void sendShareAppIntent(Context context) {
        final String appPackageName = context.getApplicationContext().getPackageName();
        String appURL = "https://play.google.com/store/apps/details?id=" + appPackageName;
        Intent shareDialog = new Intent(context, ShareDialogActivity.class);
        shareDialog.putExtra(ShareDialogActivity.EXTRA_VOTE_URL, appURL);
        shareDialog.putExtra(ShareDialogActivity.EXTRA_IS_SHARE_APP, true);
        shareDialog.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(shareDialog);
    }
}
