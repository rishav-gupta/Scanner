package com.rishavgupta.scanner.ui.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.rishavgupta.scanner.R;
import com.rishavgupta.scanner.databinding.ActivityReviewScanImageBinding;
import com.rishavgupta.scanner.utils.AppConstants;
import com.rishavgupta.scanner.utils.CommonUtils;

import java.io.File;
import java.util.Objects;

public class ReviewScanImageActivity extends AppCompatActivity {
    private ActivityReviewScanImageBinding activityReviewScanImageBinding;
    private Button btnRetake;
    private Button btnSave;
    private EditText etFileName;
    private ImageView imgBack;
    private File photoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityReviewScanImageBinding = DataBindingUtil.setContentView(this, R.layout.activity_review_scan_image);
        photoFile = (File) Objects.requireNonNull(getIntent().getExtras()).get(AppConstants.FILE);
        if (photoFile != null)
            activityReviewScanImageBinding.setFile(photoFile);
        init();
        setListener();
    }

    private void init() {
        btnRetake = activityReviewScanImageBinding.btnRetake;
        btnSave = activityReviewScanImageBinding.btnSave;
        etFileName = activityReviewScanImageBinding.etFileName;
        imgBack = activityReviewScanImageBinding.imgBack;
    }

    private void setListener() {
        btnRetake.setOnClickListener(v -> {
            if (photoFile.exists()) {
                if (photoFile.delete())
                    finish();
            }
        });

        btnSave.setOnClickListener(v -> {
            String newFileName = etFileName.getText().toString();
            if (!newFileName.toLowerCase().endsWith(".jpg")) {
                newFileName = newFileName + ".jpg";
            }
            if (photoFile.exists()) {
                //noinspection ResultOfMethodCallIgnored
                photoFile.renameTo(new File(CommonUtils.getOutputDirectory(ReviewScanImageActivity.this),
                        newFileName));
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });

        imgBack.setOnClickListener(v -> {
            if (photoFile.exists()) {
                if (photoFile.delete())
                    finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (photoFile.exists()) {
            if (photoFile.delete())
                finish();
        }
    }
}
