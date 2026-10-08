package com.example.group39.ui.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.group39.R;
import com.example.group39.ui.home.Product;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class MarkerAdapter implements GoogleMap.InfoWindowAdapter {
    private final View mWindow;
    private final Context mContext;

    public MarkerAdapter(Context context) {
        this.mContext = context;
        LayoutInflater inflater = LayoutInflater.from(context);
        mWindow = inflater.inflate(R.layout.marker_item, null);
    }

    private void populateInfoWindow(Marker marker) {
        // Find views by IDs
        ImageView mapPhoto = mWindow.findViewById(R.id.mapPhoto);
        TextView mapProductName = mWindow.findViewById(R.id.mapProductName);
        TextView mapCategoryName = mWindow.findViewById(R.id.mapCategoryName);
        TextView mapPrice = mWindow.findViewById(R.id.mapPrice);

        // Set data to views
        // Example: set image, name, category, and price based on marker's data
        // You might need to store custom data in the marker's tag or snippet

        // For demonstration, setting static data:
        // TODO: Replace with actual data retrieval logic

        // Retrieve custom data
        Product data = null;
        Object tag = marker.getTag();
        if (tag != null && tag instanceof Product) {
            data = (Product) tag;
        }

        // Load image from path
        if (data != null && data.photoUri != null && !data.photoUri.isEmpty()) {
            Bitmap bitmap = BitmapFactory.decodeFile(data.photoUri);
            if (bitmap != null) {
                mapPhoto.setImageBitmap(bitmap);
            } else {
                mapPhoto.setImageResource(R.drawable.ic_photo_black_24dp);
            }
        } else {
            mapPhoto.setImageResource(R.drawable.ic_photo_black_24dp);
        }

        // Set product name
        String title = marker.getTitle();
        if (title != null && !title.isEmpty()) {
            mapProductName.setText(title);
        } else if (data != null && data.productName != null && !data.productName.isEmpty()) {
            mapProductName.setText(data.productName);
        } else {
            mapProductName.setText("Product");
        }

        // Set category and price from data
        if (data != null) {
            mapCategoryName.setText(data.categoryName);
            mapPrice.setText(String.valueOf(data.price));
        } else {
            // fallback static data
            mapCategoryName.setText("Category");
            mapPrice.setText("Price");
        }
    }

    @Override
    public View getInfoWindow(Marker marker) {
        populateInfoWindow(marker);
        return mWindow;
    }

    @Override
    public View getInfoContents(Marker marker) {
        // Returning null uses getInfoWindow() instead
        populateInfoWindow(marker);
        return mWindow;
    }
}
