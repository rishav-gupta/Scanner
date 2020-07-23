package com.rishavgupta.scanner.ui.view;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.rishavgupta.scanner.R;
import com.rishavgupta.scanner.databinding.ActivityImageListBinding;
import com.rishavgupta.scanner.ui.adapter.GridImageAdapter;
import com.rishavgupta.scanner.ui.interfaces.GridItemInterface;
import com.rishavgupta.scanner.utils.AppConstants;
import com.rishavgupta.scanner.utils.CommonUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.Arrays;

public class ImageListActivity extends AppCompatActivity implements GridItemInterface {

    private static final int EXTERNAL_PERMISSION_CODE = 100;
    private static final int CAMERA_PERMISSION_CODE = 200;
    private static final int SCAN_IMAGE_REQUEST = 700;
    private ActivityImageListBinding activityImageListBinding;
    private Toolbar toolbar;
    private RecyclerView recyclerImages;
    private GridImageAdapter gridImageAdapter;
    private LinearLayout layoutSort;
    private View placeholder;
    private RadioGroup radioGroup;
    private TextView tvClearSort;
    private FloatingActionButton fabScanImage;
    private String[] externalStoragePermissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    private String[] cameraPermissions = {
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityImageListBinding = DataBindingUtil.setContentView(this, R.layout.activity_image_list);
        init();
        setToolbar();
        setListeners();
        setScrollAction();
        setRadioGroupListener();
        getPermissionsFromUser();
    }

    private void init() {
        toolbar = activityImageListBinding.toolbar;
        fabScanImage = activityImageListBinding.fab;
        recyclerImages = activityImageListBinding.content.recyclerImages;
        placeholder = activityImageListBinding.content.placeholder;
        radioGroup = activityImageListBinding.radioGroup;
        layoutSort = activityImageListBinding.layoutSort;
        tvClearSort = activityImageListBinding.tvClearSort;
    }


    private void setToolbar() {
        toolbar.setTitle(getResources().getString(R.string.app_name));
        setSupportActionBar(toolbar);
    }

    private void setListeners() {

        fabScanImage.setOnClickListener(view -> {
            if(CommonUtils.isOnline(this)) {
                if (CommonUtils.hasPermissions(ImageListActivity.this, cameraPermissions)) {
                    proceedToScan();
                } else {
                    ActivityCompat.requestPermissions(ImageListActivity.this, cameraPermissions, CAMERA_PERMISSION_CODE);
                }
            }else{
                Toast.makeText(getApplicationContext(),"No Internet connection",Toast.LENGTH_SHORT).show();
            }
        });

        tvClearSort.setOnClickListener(v -> {
            radioGroup.clearCheck();
            tvClearSort.setVisibility(View.INVISIBLE);
            gridImageAdapter.setGridItems(CommonUtils.getAllImages(this));
        });
    }

    private void setScrollAction() {
        recyclerImages.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE)
                    fabScanImage.setVisibility(View.VISIBLE);
                else
                    fabScanImage.setVisibility(View.GONE);
            }
        });
    }

    private void setRadioGroupListener() {
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton radioButton = group.findViewById(checkedId);
            if (radioButton != null) {
                tvClearSort.setVisibility(View.VISIBLE);
                File[] tempList = CommonUtils.getAllImages(this);
                switch (checkedId) {
                    case R.id.radioName:
                        Arrays.sort(tempList, (o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
                        gridImageAdapter.setGridItems(tempList);
                        break;
                    case R.id.radioSize:
                        Arrays.sort(tempList, (o1, o2) -> Long.compare(o1.length(), o2.length()));
                        gridImageAdapter.setGridItems(tempList);
                        break;
                }
            }

        });
    }

    private void getPermissionsFromUser() {
        if (CommonUtils.hasPermissions(this, externalStoragePermissions)) {
            getAllImages();
        } else {
            ActivityCompat.requestPermissions(this, externalStoragePermissions, EXTERNAL_PERMISSION_CODE);
        }
    }

    private void getAllImages() {
        if (CommonUtils.createFolder(this)) {
            File[] allImages = CommonUtils.getAllImages(this);

            if (allImages.length > 0) {
                setData(allImages);
                layoutSort.setVisibility(View.VISIBLE);
                placeholder.setVisibility(View.GONE);
            } else {
                layoutSort.setVisibility(View.GONE);
                placeholder.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setData(File[] allImages) {
        recyclerImages.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerImages.setHasFixedSize(true);
        gridImageAdapter = new GridImageAdapter();
        gridImageAdapter.setHasStableIds(true);
        gridImageAdapter.setViewClickListener(this);
        recyclerImages.setAdapter(gridImageAdapter);
        gridImageAdapter.setGridItems(allImages);
    }

    private void proceedToScan() {
        Intent scanIntent = new Intent(ImageListActivity.this, ScanImageActivity.class);
        startActivityForResult(scanIntent, SCAN_IMAGE_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == EXTERNAL_PERMISSION_CODE) {
            if (grantResults.length == externalStoragePermissions.length && CommonUtils.allPermissionsGranted(grantResults)) {
                getAllImages();
            } else {
                showPermissionActionSnackbar();
            }
        } else if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length == cameraPermissions.length && CommonUtils.allPermissionsGranted(grantResults)) {
                proceedToScan();
            } else {
                showPermissionActionSnackbar();
            }
        }
    }

    private void showPermissionActionSnackbar() {
        Snackbar.make(toolbar, R.string.permission_request,
                Snackbar.LENGTH_INDEFINITE).setAction(R.string.settings, view -> {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        }).show();
    }


    @Override
    public void onGridItemClicked(File file) {
        Intent viewIntent = new Intent(ImageListActivity.this, ViewImageActivity.class);
        viewIntent.putExtra(AppConstants.FILE, file);
        startActivity(viewIntent);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SCAN_IMAGE_REQUEST) {
            if (resultCode == Activity.RESULT_CANCELED) {
                radioGroup.clearCheck();
                getAllImages();
            }
        }
    }
}
