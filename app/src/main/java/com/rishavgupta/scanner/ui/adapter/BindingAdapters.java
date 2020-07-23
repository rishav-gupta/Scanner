package com.rishavgupta.scanner.ui.adapter;

import android.net.Uri;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.databinding.BindingAdapter;
import androidx.exifinterface.media.ExifInterface;


import com.bumptech.glide.Glide;
import com.rishavgupta.scanner.utils.CommonUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class BindingAdapters {

    @BindingAdapter("setImage")
    public static void setImage(ImageView imageView, File file) {
        Uri photoUri = Uri.fromFile(new File(file.getAbsolutePath()));

        Glide.with(imageView.getContext())
                .load(photoUri)
                .into(imageView);

    }

    @BindingAdapter("modifiedDate")
    public static void getTimeStamp(TextView textView, Long millis) {
        String format = "EEE MMM dd HH:mm:ss zzz yyyy";

        textView.setText(new SimpleDateFormat(format, Locale.US).format(millis).concat(""));

    }

    @BindingAdapter("sizeInKb")
    public static void getSize(TextView textView, Long length) {
        textView.setText(String.valueOf(length / 1024).concat(" Kb"));

    }

    @BindingAdapter("latitude")
    public static void getLatitude(TextView textView, String path) {
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            if (exifInterface.getLatLong() != null) {
                String latitude = CommonUtils.doubleDecimalToString(exifInterface.getLatLong()[0]) + ("\u00B0 N");
                textView.setText(latitude);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @BindingAdapter("longitude")
    public static void getLongitude(TextView textView, String path) {
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            if (exifInterface.getLatLong() != null) {
                String latitude = CommonUtils.doubleDecimalToString(exifInterface.getLatLong()[1]) + ("\u00B0 E");
                textView.setText(latitude);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
