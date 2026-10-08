package com.example.group39.ui.home;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.group39.R;
import com.example.group39.databinding.RecordRowItemBinding;

import java.util.ArrayList;
import java.util.List;


public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.ViewHolder> {

    public interface OnItemDeleteListener {
        void onItemDelete(Product product, int position);
    }

    public interface OnItemEditListener {
        void onItemEdit(Product product, int position);
    }

    public interface OnItemCopyListener {
        void onItemCopy(Product product, int position);
    }

    private List<Product> localDataSet = new ArrayList<>();
    private OnItemDeleteListener deleteListener;
    private OnItemEditListener editListener;
    private OnItemCopyListener copyListener;

    public void setOnItemDeleteListener(OnItemDeleteListener listener) {
        this.deleteListener = listener;
    }

    public void setOnItemEditListener(OnItemEditListener listener) {
        this.editListener = listener;
    }

    public void setOnItemCopyListener(OnItemCopyListener listener) {
        this.copyListener = listener;
    }

    public void setData(List<Product> data) {
        this.localDataSet = data != null ? data : new ArrayList<>();
    }

    public void removeProduct(int position) {
        this.localDataSet.remove(position);
    }

    public void addProduct(Product product) {
        this.localDataSet.add(product);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final RecordRowItemBinding binding;

        public ViewHolder(RecordRowItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public RecordRowItemBinding getBinding() {
            return binding;
        }
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        RecordRowItemBinding binding = RecordRowItemBinding.inflate(
                LayoutInflater.from(viewGroup.getContext()), viewGroup, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        Product product = localDataSet.get(position);
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        RecordRowItemBinding binding = viewHolder.getBinding();

        Bitmap bitmap = BitmapFactory.decodeFile(String.valueOf(product.photoUri));
        if (bitmap != null) {
            binding.viewPhoto.setImageBitmap(bitmap);
        } else {
            // Handle the case where the image couldn't be decoded
            // For example, set a placeholder or clear the ImageView
            binding.viewPhoto.setImageResource(R.drawable.ic_photo_black_24dp);
        }

        binding.viewProductName.setText(String.valueOf(product.productName));
        binding.viewCategoryName.setText(String.valueOf(product.categoryName));
        binding.viewPrice.setText(String.valueOf(product.price));

        // Set click listener for delete button
        binding.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onItemDelete(product, position);
            }
        });

        binding.editArea.setOnClickListener(v -> {
            if (editListener != null) {
                editListener.onItemEdit(product, position);
            }
        });

        binding.btnCopy.setOnClickListener(v -> {
            if (copyListener != null) {
                copyListener.onItemCopy(product, position);
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localDataSet.size();
    }
}
