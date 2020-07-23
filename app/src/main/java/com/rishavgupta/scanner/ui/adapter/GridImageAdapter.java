package com.rishavgupta.scanner.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.rishavgupta.scanner.R;
import com.rishavgupta.scanner.databinding.ItemGridBinding;
import com.rishavgupta.scanner.ui.interfaces.GridItemInterface;

import java.io.File;

public class GridImageAdapter
        extends RecyclerView.Adapter<GridImageAdapter.GroupViewHolder> implements GridItemInterface {
    private File[] gridImageFiles;
    private GridItemInterface mGridItemInterface;

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        ItemGridBinding itemGridBinding =
                DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()),
                        R.layout.item_grid, viewGroup, false);
        itemGridBinding.setClickHandler(this);
        return new GroupViewHolder(itemGridBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder groupViewHolder, int i) {
        File file = gridImageFiles[i];
        groupViewHolder.itemGridBinding.setFile(file);
    }

    @Override
    public int getItemCount() {
        if (gridImageFiles != null) {
            return gridImageFiles.length;
        } else {
            return 0;
        }
    }

    @Override
    public long getItemId(int position) {
       return gridImageFiles[position].getName().hashCode();
    }

    public void setGridItems(File[] gridImageFiles) {
        this.gridImageFiles = gridImageFiles;
        notifyDataSetChanged();
    }

    public void setViewClickListener(GridItemInterface gridItemInterface) {
        mGridItemInterface = gridItemInterface;
    }

    @Override
    public void onGridItemClicked(File file) {
        mGridItemInterface.onGridItemClicked(file);
    }

    class GroupViewHolder extends RecyclerView.ViewHolder {
        private ItemGridBinding itemGridBinding;

        GroupViewHolder(@NonNull ItemGridBinding itemGridBinding) {
            super(itemGridBinding.getRoot());
            this.itemGridBinding = itemGridBinding;
        }
    }


}