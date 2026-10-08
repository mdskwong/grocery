package com.example.group39.ui.home;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.group39.R;
import com.example.group39.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Replace fragment
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_home_container, new RecordListFragment())
//                .addToBackStack(null)
                .commit();

        // Set click listener
        binding.floatingAddRecordButton.setOnClickListener(v -> {
            // Replace fragment
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_home_container, new AddFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        requireActivity().getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment currentFragment = getCurrentFragment();

            if (currentFragment instanceof AddFragment) {
                // Hide button
                binding.floatingAddRecordButton.setVisibility(View.GONE);
            } else {
                // Show button
                binding.floatingAddRecordButton.setVisibility(View.VISIBLE);
            }
        });
    }

    private Fragment getCurrentFragment() {
        return requireActivity().getSupportFragmentManager().findFragmentById(R.id.fragment_home_container);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}