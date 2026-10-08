package com.example.group39.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.group39.R;
import com.example.group39.databinding.FragmentRecordlistBinding;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class RecordListFragment extends Fragment {

    private FragmentRecordlistBinding binding;
    private AppDatabase db;
    private ProductDao productDao;
    private RecordAdapter adapter;
    private Executor executor = Executors.newSingleThreadExecutor();

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRecordlistBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize database and DAO
        db = AppDatabase.getInstance(requireContext());
        productDao = db.productDao();

        // Set up RecyclerView
        RecyclerView recyclerView = binding.recordRecyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Create and set your adapter (assuming you have a constructor)
        adapter = new RecordAdapter(); // pass data if needed

        adapter.setOnItemDeleteListener((product, position) -> {
            executor.execute(() -> {
                productDao.deleteRecord(product);
                // Update the UI on the main thread
                requireActivity().runOnUiThread(() -> {
//                    adapter.removeProduct(position);
//                    adapter.notifyItemRemoved(position);
                });
            });
        });

        adapter.setOnItemCopyListener((product, position) -> {
//            executor.execute(() -> {
//                product.recordId = null;
//                productDao.insertRecord(product);
//                // Update the UI on the main thread
//                requireActivity().runOnUiThread(() -> {
//                });
//            });
            // Create a new instance of AddFragment with product data
            product.recordId = null;
            AddFragment addFragment = AddFragment.newInstance(product);
            // Replace current fragment with AddFragment
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_home_container, addFragment)
                    .addToBackStack(null)
                    .commit();
        });

        adapter.setOnItemEditListener((product, position) -> {
            // Create a new instance of AddFragment with product data
            AddFragment addFragment = AddFragment.newInstance(product);
            // Replace current fragment with AddFragment
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_home_container, addFragment)
                    .addToBackStack(null)
                    .commit();
        });

        recyclerView.setAdapter(adapter);

        // Observe LiveData from database
        productDao.getAllRecords().observe(getViewLifecycleOwner(), records -> {
            adapter.setData(records);
            adapter.notifyDataSetChanged();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
