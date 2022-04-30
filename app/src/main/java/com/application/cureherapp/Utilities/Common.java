package com.application.cureherapp.Utilities;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.application.cureherapp.R;

public class Common {
    public static boolean isConnectedToInternet(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (null != networkInfo &&
                (networkInfo.getType() == ConnectivityManager.TYPE_WIFI || networkInfo.getType() == ConnectivityManager.TYPE_MOBILE)) {
            return true;
        } else {
            return false;
        }
    }

    public static void openNoInternetBottomSheet(Context context) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottomsheet_no_internet);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.BottomSheetAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        dialog.setCancelable(false);

        ConstraintLayout retryBtn = dialog.findViewById(R.id.retry_btn);

        dialog.show();

        retryBtn.setOnClickListener(view -> {
            if (isConnectedToInternet(context)) {
                dialog.dismiss();
            } else {
                Toast.makeText(context, "Still No Connection!", Toast.LENGTH_SHORT).show();
                return;
            }
        });
    }

    public static String convertTimeSlotToString(int slot) {
        switch (slot) {
            case 0:
                return "09:00 AM";
            case 1:
                return "09:30 AM";
            case 2:
                return "10:00 AM";
            case 3:
                return "10:30 AM";
            case 4:
                return "11:00 AM";
            case 5:
                return "11:30 AM";
            case 6:
                return "12:00 PM";
            case 7:
                return "12:30 PM";
            case 8:
                return "01:00 PM";
            case 9:
                return "01:30 PM";
            case 10:
                return "02:00 PM";
            case 11:
                return "02:30 PM";
            case 12:
                return "03:00 PM";
            case 13:
                return "03:30 PM";
            case 14:
                return "04:00 PM";
            case 15:
                return "04:30 PM";
            case 16:
                return "05:00 PM";
            case 17:
                return "05:30 PM";
            case 18:
                return "06:00 PM";
            case 19:
                return "06:30 PM";
            default:
                return "CLOSED";
        }
    }
}
