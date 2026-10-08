package com.example.group39.ui.map;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.group39.R;
import com.example.group39.databinding.FragmentMapBinding;
import com.example.group39.ui.home.AppDatabase;
import com.example.group39.ui.home.Product;
import com.example.group39.ui.home.ProductDao;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private FragmentMapBinding binding;
    private AppDatabase db;
    private ProductDao productDao;
    private GoogleMap googleMap;
    private MarkerAdapter markerAdapter;

    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMapBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.fragment_map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Initialize your MarkerAdapter
        // Pass context to it
        markerAdapter = new MarkerAdapter(requireContext());

        return root;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize database and DAO
        db = AppDatabase.getInstance(requireContext());
        productDao = db.productDao();

        // Observe LiveData from database
        productDao.getAllRecords().observe(getViewLifecycleOwner(), records -> {
            if (records != null && googleMap != null) {
                addMarkersFromRecords(records);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        // Set your custom info window adapter
        googleMap.setInfoWindowAdapter(markerAdapter);
        // Map is ready, but data might not be loaded yet
        // If data is already available, add markers
        List<Product> records = productDao.getAllRecords().getValue();
        if (records != null && !records.isEmpty()) {
            addMarkersFromRecords(records);
        }
    }

    private void addMarkersFromRecords(List<Product> records) {
        googleMap.clear();

        for (Product record : records) {
            if (record.location == null) continue;
            String[] parts = record.location.split(",");
            if (parts.length != 2) continue;
            try {
                double lat = Double.parseDouble(parts[0].trim());
                double lng = Double.parseDouble(parts[1].trim());
                String title = record.productName;
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(new LatLng(lat, lng))
                        .title(title);
//                        .snippet("Price: "+record.price);
//                        .icon(BitmapDescriptorFactory.fromPath(record.photoUri));
                Marker marker = googleMap.addMarker(markerOptions);
                if (marker != null) {
                    marker.setTag(record);
                }
            } catch (NumberFormatException e) {
                // Log or handle malformed data
            }
        }
        if (!records.isEmpty()) {
            try {
                String[] firstParts = records.get(0).location.split(",");
                double firstLat = Double.parseDouble(firstParts[0].trim());
                double firstLng = Double.parseDouble(firstParts[1].trim());
                LatLng position = new LatLng(firstLat, firstLng);
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 12));
            } catch (Exception e) {
                // Handle possible parsing errors
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}