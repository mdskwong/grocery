package com.example.group39.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.icu.util.Currency;
import android.icu.util.ULocale;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.group39.R;
import com.example.group39.databinding.FragmentAddBinding;
import com.example.group39.databinding.FragmentHomeBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AddFragment extends Fragment {

    // TODO: Rename and change types of parameters
    private FragmentAddBinding binding;
    private View homeView;
    private FusedLocationProviderClient fusedLocationClient;
    private ActivityResultLauncher<String[]> locationPermissionLauncher;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMediaLauncher;
    private AppDatabase db;
    private ProductDao productDao;

    private TextView editTitle;

    private EditText editDate, editLocation, editShopName, editCategoryName, editBrandName,
            editProductName, editDescription, editPrice, editRemarks;
    private Spinner editCurrency;
    private ImageView editPhoto;
    private Button btnAdd, btnSave, btnDelete, btnQuery;
    private FloatingActionButton btnFloat;
    private Product currentProduct;
    private List<String> currencyList = new ArrayList<>();
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Set<String> commonCurrencyCodes = new HashSet<>(Arrays.asList("USD", "EUR", "JPY", "HKD", "GBP", "AUD", "CAD", "CHF", "CNY", "INR"));

    public AddFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static AddFragment newInstance(Product product) {
        AddFragment fragment = new AddFragment();
        Bundle args = new Bundle();
        fragment.setCurrentProduct(product);
        fragment.setArguments(args);
        return fragment;
    }

    public void setCurrentProduct(Product product) {
        this.currentProduct = product;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAddBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        homeView = inflater.inflate(R.layout.fragment_home, container, false);
        btnFloat = homeView.findViewById(R.id.floatingAddRecordButton);
        btnFloat.hide();

        return root;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Initialize UI components
        initializeVariables();
    }

    @SuppressLint("MissingPermission")
    private void initializeVariables() {
        // Initialize database and DAO
        db = AppDatabase.getInstance(requireContext());
        productDao = db.productDao();

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        // Find views by ID
        editTitle = binding.editTitle;
        editDate = binding.editDate;
        editLocation = binding.editLocation;
        editShopName = binding.editShopName;
        editCategoryName = binding.editCategoryName;
        editBrandName = binding.editBrandName;
        editProductName = binding.editProductName;
        editDescription = binding.editDescription;
        editPrice = binding.editPrice;
        editCurrency = binding.editCurrency;
        editRemarks = binding.editRemarks;
        editPhoto = binding.editPhoto;
        btnAdd = binding.btnAdd;
        btnSave = binding.btnSave;

        if (currentProduct == null || currentProduct.recordId == null) {
            editTitle.setText(R.string.new_add_product);
            btnAdd.setVisibility(View.VISIBLE);
            btnSave.setVisibility(View.INVISIBLE);
            setupAddButton();
        } else {
            editTitle.setText(R.string.edit_product);
            btnAdd.setVisibility(View.INVISIBLE);
            btnSave.setVisibility(View.VISIBLE);
            setupSaveButton();
        }

        // Initialize other variables if needed
        setDefaultValues();
        setupDatePicker();
        setupLocationPicker();
        setupCurrencyPicker();
        setupPhotoPicker();
        setupCloseButton();
        populateFields();
    }

    private void setDefaultValues() {
        // Date field: set default to current date
        if (currentProduct == null) {
            currentProduct = new Product();

            final Calendar calendar = Calendar.getInstance();
            int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
            int monthOfYear = calendar.get(Calendar.MONTH); // 0-based
            int year = calendar.get(Calendar.YEAR);

            currentProduct.date = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, monthOfYear + 1, year);
            currentProduct.location = "22.28319331569886,114.13633279567568";
            currentProduct.shopName = "Wellcome";
            currentProduct.categoryName = "Drinks";
            currentProduct.brandName = "Coca cola";
            currentProduct.productName = "Coke";
            currentProduct.description = "330ml";
            currentProduct.remarks = "Test 1";
            currentProduct.price = 5.0;
        }
    }

    private void populateFields() {
        if (currentProduct != null) {
            // Date
            editDate.setText(currentProduct.date);
            // Location field
            editLocation.setText(currentProduct.location);
            // Shop Name
            editShopName.setText(currentProduct.shopName);
            // Category Name
            editCategoryName.setText(currentProduct.categoryName);
            // Brand Name
            editBrandName.setText(currentProduct.brandName);
            // Product Name
            editProductName.setText(currentProduct.productName);
            // Description
            editDescription.setText(currentProduct.description);
            // Remarks
            editRemarks.setText(currentProduct.remarks);

            // Price
            editPrice.setText(String.valueOf(currentProduct.price));

            // Currency Spinner
            //Spinner currencySpinner = view.findViewById(R.id.editCurrency);
            // Ensure you have set the adapter to the spinner before setting selection
            //int defaultPosition = 0; // replace with your default position
            //currencySpinner.setSelection(defaultPosition);
            int defaultPosition = currencyList.indexOf(currentProduct.currency);
            if (defaultPosition != -1) {
                editCurrency.setSelection(defaultPosition);
            }

            // Photo ImageView (if you want to set a default image)
            Bitmap bitmap = BitmapFactory.decodeFile(String.valueOf(currentProduct.photoUri));
            if (bitmap != null) {
                editPhoto.setImageBitmap(bitmap);
            } else {
                // Handle the case where the image couldn't be decoded
                // For example, set a placeholder or clear the ImageView
                editPhoto.setImageResource(R.drawable.ic_photo_black_24dp);
            }
        }
    }

    private void setupDatePicker() {
        // Make sure editDate is initialized before setting listener
        if (editDate != null) {
            // Disable manual input if desired
            editDate.setFocusable(false);
            editDate.setClickable(true);

            editDate.setOnClickListener(v -> {
                final Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                // Use getContext() for the dialog
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        requireContext(), // or getContext()
                        (view, yearSelected, monthOfYear, dayOfMonth) -> {
                            String selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, monthOfYear + 1, yearSelected);
                            editDate.setText(selectedDate);
                        }, year, month, day);
                datePickerDialog.show();
            });
        }
    }

    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    private void setupLocationPicker() {
        if (editLocation != null) {
            // Disable manual input if desired
//            editLocation.setFocusable(false);
            editLocation.setClickable(true);

            // Register the permission launcher once
            locationPermissionLauncher = registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    result -> {
                        Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                        Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                    });

            editLocation.setOnClickListener(v -> {
                if (!isLocationEnabled()) {
                    // Show dialog to prompt user to enable location services
                    new AlertDialog.Builder(requireContext())
                            .setTitle(R.string.location_enable_title)
                            .setMessage(R.string.location_enable_msg)
                            .setPositiveButton(R.string.location_enable_open, (dialog, which) -> {
                                // Redirect to location settings
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(intent);
                            })
                            .setNegativeButton(R.string.location_enable_cancel, null)
                            .show();
                    return;
                }

                // Check permissions
                if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // Request permissions
                    locationPermissionLauncher.launch(new String[] {
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    });
                } else {
                    // Permissions already granted
                    fetchLocation();
                }
            });
        }
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            boolean gpsEnabled = false;
            boolean networkEnabled = false;
            try {
                gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return gpsEnabled || networkEnabled;
        }
        return false;
    }

    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    private void fetchLocation() {
        // Get current location
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location == null) {
                        // Fallback to last known location
                        fusedLocationClient.getLastLocation()
                                .addOnSuccessListener(lastLocation -> {
                                    if (lastLocation != null) {
                                        updateLocationText(lastLocation);
                                    } else {
                                        Toast.makeText(requireContext(), R.string.location_fetch_fail_location, Toast.LENGTH_LONG).show();
                                    }
                                });
                    } else {
                        updateLocationText(location);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), R.string.location_fetch_fail_current + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void updateLocationText(Location location) {
        String locationText = location.getLatitude() + "," + location.getLongitude();
        editLocation.setText(locationText);
    }

    private void setupPhotoPicker() {
        pickMediaLauncher = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        try {
                            // Use ContentResolver to open InputStream from Uri
                            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
                            Bitmap bmp = BitmapFactory.decodeStream(inputStream);
                            editPhoto.setImageBitmap(bmp);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            Toast.makeText(requireContext(), R.string.photo_unable_load, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(requireContext(), R.string.photo_no_image, Toast.LENGTH_LONG).show();
                    }
                });

        editPhoto.setOnClickListener(view -> {
            pickMediaLauncher.launch(
                    new PickVisualMediaRequest.Builder()
                            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                            .build()
            );
        });
    }

    private void setupCurrencyPicker() {
        // Get all available currencies
        Set<Currency> currencies = Currency.getAvailableCurrencies();

        // Filter currencies to only include common currencies
        List<Currency> filteredCurrencies = new ArrayList<>();
        for (Currency currency : currencies) {
            if (commonCurrencyCodes.contains(currency.getCurrencyCode())) {
                filteredCurrencies.add(currency);
            }
        }

        for (Currency currency : filteredCurrencies) {
            // You can choose to display the currency code, symbol, or display name
            String displayText = currency.getDisplayName() + " - " + currency.getSymbol();
            currencyList.add(displayText);
        }
        currencyList.sort(String::compareTo);

        // Create ArrayAdapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                currencyList
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Set adapter to spinner
        editCurrency.setAdapter(adapter);

        // Optionally set default selection, e.g., HKD
        String hkdCurrency = "Hong Kong Dollar - HK$";
        int defaultPosition = currencyList.indexOf(hkdCurrency);
        if (defaultPosition != -1) {
            editCurrency.setSelection(defaultPosition);
        }
    }

    private String saveBitmapToInternalStorage(Bitmap bitmap) {
        Context context = requireContext(); // or getContext()

        // Generate a UUID filename
        String uuidFilename = UUID.randomUUID().toString() + ".jpg";

        // Create or get the "images" directory inside internal storage
        File imagesDir = new File(context.getFilesDir(), "images");
        if (!imagesDir.exists()) {
            boolean created = imagesDir.mkdirs();
            if (!created) {
                // Handle failure to create directory
                return null;
            }
        }
        File file = new File(imagesDir, uuidFilename);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            // Save as PNG or JPEG as needed
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            return file.getAbsolutePath();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void setupAddButton() {
        // Make sure editDate is initialized before setting listener
        if (btnAdd != null) {
            btnAdd.setOnClickListener(v -> {
                Product product = getProductInput();
                if (validateInput(product)) {
                    insertProduct(product);
                }
            });
        }
    }

    private void setupSaveButton() {
        // Make sure editDate is initialized before setting listener
        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                Product product = getProductInput();
                if (validateInput(product)) {
                    updateProduct(product);
                }
            });
        }
    }

    private void setupCloseButton() {
    // Inside your activity's onCreate() method
        ImageView btnClose = binding.btnClose;

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle close action here
                // For example, finish the activity or dismiss a dialog
                requireActivity().getSupportFragmentManager().popBackStack();
                // or
                // dialog.dismiss(); // if it's a dialog
            }
        });
    }

    private boolean validateInput(Product product) {
        // Validate product name
        if (product.productName == null || product.productName.isEmpty()) {
            Toast.makeText(getContext(), R.string.validate_product_name, Toast.LENGTH_LONG).show();
            return false;
        }
        // Validate price
        if (product.price <= 0) {
            Toast.makeText(getContext(), R.string.validate_price, Toast.LENGTH_LONG).show();
            return false;
        }
        // Validate date
        if (product.date == null || product.date.trim().isEmpty()) {
            Toast.makeText(getContext(), R.string.validate_date, Toast.LENGTH_LONG).show();
            return false;
        } else {
            // Check if the date is in a valid format, e.g., "yyyy-MM-dd"
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // Adjust pattern if needed
//
//            try {
//                LocalDate.parse(dateString, formatter);
//                return true; // Valid date
//            } catch (DateTimeParseException e) {
//                Toast.makeText(getContext(), R.string.invalid_date_format, Toast.LENGTH_LONG).show();
//                return false; // Invalid date format
//            }
        }

        return true;
    }

    private Product getProductInput() {
        // Collect data from input fields
        Product newProduct = new Product();
        if (currentProduct.recordId != null) {
            newProduct.recordId = currentProduct.recordId;
        }
        newProduct.date = editDate.getText().toString();
        newProduct.location = editLocation.getText().toString();
        newProduct.shopName = editShopName.getText().toString();
        newProduct.categoryName = editCategoryName.getText().toString();
        newProduct.brandName = editBrandName.getText().toString();
        newProduct.productName = editProductName.getText().toString();
        newProduct.description = editDescription.getText().toString();
        newProduct.remarks = editRemarks.getText().toString();

        try {
            newProduct.price = Double.parseDouble(editPrice.getText().toString());
        } catch (NumberFormatException e) {
            newProduct.price = 0.0;
        }

        String selectedValue = editCurrency.getSelectedItem().toString();
        // For example, display it
        newProduct.currency = selectedValue;
//        Toast.makeText(requireContext(), "Selected: " + selectedValue, Toast.LENGTH_LONG).show();

        Drawable drawable = editPhoto.getDrawable();
        if (drawable instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            newProduct.photoUri = saveBitmapToInternalStorage(bitmap);
        }

        return newProduct;
    }

    private void insertProduct(Product product) {
        // Insert in background thread
        executor.execute(() -> {
            long id = productDao.insertRecord(product);
            // Check if insert was successful
            if (id != -1) {
                // Run on UI thread to close or navigate
                handler.post(() -> {
                    Toast.makeText(getContext(), R.string.db_insert_success, Toast.LENGTH_LONG).show();
                    // Close fragment or navigate back
                    requireActivity().getSupportFragmentManager().popBackStack();
                });
            } else {
                handler.post(() -> {
                    Toast.makeText(getContext(), R.string.db_insert_error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void updateProduct(Product product) {
        if (currentProduct != null) {
            executor.execute(() -> {
                // Background work
                int rowsUpdated = productDao.updateRecord(product);
                if (rowsUpdated != 0) {
                    handler.post(() -> {
                        // UI update
                        Toast.makeText(getContext(), R.string.db_save_success, Toast.LENGTH_LONG).show();
                        requireActivity().getSupportFragmentManager().popBackStack();
                    });
                } else {
                    handler.post(() -> {
                        // UI update
                        Toast.makeText(getContext(), R.string.db_save_error, Toast.LENGTH_LONG).show();
                    });
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}