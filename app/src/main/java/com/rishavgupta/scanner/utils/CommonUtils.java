package com.rishavgupta.scanner.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.core.app.ActivityCompat;

import com.rishavgupta.scanner.R;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Objects;

public class CommonUtils {

    public static boolean isOnline(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connMgr != null) {
            networkInfo = connMgr.getActiveNetworkInfo();
        }
        return (networkInfo != null && networkInfo.isConnected());
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean allPermissionsGranted(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }

    public static boolean createFolder(Context context) {
        File folder = new File(Objects.requireNonNull(context.getExternalFilesDir(null)).getAbsolutePath()
                + File.separator + context.getResources().getString(R.string.app_name));
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdirs();
        }
        return success;
    }

    public static File[] getAllImages(Context context) {
        File folder = new File(Objects.requireNonNull(context.getExternalFilesDir(null)).getAbsolutePath()
                + File.separator + context.getResources().getString(R.string.app_name));
        return folder.listFiles();
    }

    public static File getOutputDirectory(Context context) {
        File folder = new File(Objects.requireNonNull(context.getExternalFilesDir(null)).getAbsolutePath()
                + File.separator + context.getResources().getString(R.string.app_name));
        if (!folder.exists()) {
            //noinspection ResultOfMethodCallIgnored
            folder.mkdirs();
        }
        return folder;
    }

    public static String doubleDecimalToString(Double val) {
        DecimalFormat df = new DecimalFormat("0.00");
        return df.format(val);
    }

    public static Boolean locationEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }
        return false;
    }

}
