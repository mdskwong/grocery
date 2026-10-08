package com.example.group39.ui.chart;

import android.graphics.DashPathEffect;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Currency;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.group39.R;
import com.example.group39.databinding.FragmentChartBinding;
import com.example.group39.ui.home.AddFragment;
import com.example.group39.ui.home.AppDatabase;
import com.example.group39.ui.home.Product;
import com.example.group39.ui.home.ProductDao;
import com.example.group39.ui.home.RecordAdapter;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ChartFragment extends Fragment {

    private FragmentChartBinding binding;
    private AppDatabase db;
    private ProductDao productDao;
    private Spinner chartItem;
    private LineChart chartGraph;
    private TextView chartText;
    private Executor executor = Executors.newSingleThreadExecutor();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentChartBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        chartItem = binding.chartItem;
        chartGraph = binding.chartGraph;
//        chartText = binding.chartText;

        return root;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize database and DAO
        db = AppDatabase.getInstance(requireContext());
        productDao = db.productDao();

        // Create and set your adapter (assuming you have a constructor)
        setupItemPicker();

        // Add OnItemSelectedListener to chartItem Spinner
        chartItem.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = (String) parent.getItemAtPosition(position);
                if (selectedItem != null && !selectedItem.isEmpty()) {
                    setupItemChart(selectedItem);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Optional: handle case when nothing is selected
            }
        });
    }

    private void setupItemPicker() {
        // Create an empty list for spinner items
        List<String> itemList = new ArrayList<>();

        // Initialize the adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                itemList
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        chartItem.setAdapter(adapter);

        // Observe LiveData from the database
        productDao.getUniqueRecords().observe(getViewLifecycleOwner(), records -> {
            itemList.clear();
            if (records != null) {
                itemList.addAll(records);
                // Optional: sort the list alphabetically
                itemList.sort(String::compareTo);
            }
            adapter.notifyDataSetChanged();
        });
    }

    private void setupItemChart(String itemName) {
        // Observe LiveData from the database
        productDao.getRecordsByName(itemName).observe(getViewLifecycleOwner(), itemList -> {
            List<Entry> entries = new ArrayList<Entry>();
            List<String> xLabels = new ArrayList<String>();
            double maxPrice = 0.0;
            int num = 0;

            itemList.sort(Comparator.comparing(p -> {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                try {
                    return sdf.parse(p.date);
                } catch (ParseException e) {
                    return new Date(0);
                }
            }));

            for (Product data : itemList) {
                // turn your data into Entry objects
                if (data.price > maxPrice) {
                    maxPrice = data.price;
                }
                entries.add(new Entry((float) num, (float) data.price));
                xLabels.add(data.date);
                num++;
            }

            LineDataSet dataSet = new LineDataSet(entries, itemName); // add entries to dataset
            dataSet.setDrawCircles(true);
            dataSet.enableDashedLine(10f, 0f, 0f);
            dataSet.enableDashedHighlightLine(10f, 0f, 0f);
            dataSet.setColor(ContextCompat.getColor(requireContext(), R.color.chart_line));
            dataSet.setCircleColor(ContextCompat.getColor(requireContext(), R.color.chart_circle));
            dataSet.setLineWidth(2f);
            dataSet.setCircleRadius(5f);
            dataSet.setDrawCircleHole(true);
            dataSet.setValueTextSize(10f);
//            dataSet.setDrawFilled(true);
            dataSet.setFormLineWidth(5f);
            dataSet.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            dataSet.setFormSize(5.f);
            dataSet.setDrawValues(true);

            LineData lineData = new LineData(dataSet);
            chartGraph.setData(lineData);

            ValueFormatter formatter = new ValueFormatter() {
                @Override
                public String getAxisLabel(float value, AxisBase axis) {
                    int index = (int) value;
                    if (index >= 0 && index < xLabels.size()) {
                        return xLabels.get(index);
                    } else {
                        return "";
                    }
                }
            };

            XAxis xAxis = chartGraph.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.enableGridDashedLine(10f, 10f, 0f);
            xAxis.setAxisMaximum(xLabels.size());
            xAxis.setAxisMinimum(0f);
            xAxis.setLabelCount(6, false);
//            xAxis.setGranularityEnabled(true);
            xAxis.setGranularity(1f);
            xAxis.setLabelRotationAngle(90f);
            xAxis.removeAllLimitLines();
            xAxis.setValueFormatter(formatter);
            xAxis.setCenterAxisLabels(false);
            xAxis.setDrawLimitLinesBehindData(true);

            YAxis leftAxis = chartGraph.getAxisLeft();
            leftAxis.removeAllLimitLines();
            leftAxis.setAxisMaximum((float) maxPrice + 10f);
            leftAxis.setAxisMinimum(0f);
            leftAxis.enableGridDashedLine(10f, 10f, 0f);
            leftAxis.setDrawZeroLine(false);
            leftAxis.setDrawLimitLinesBehindData(false);

            Legend legend = chartGraph.getLegend();
            legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
            legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
            legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
            legend.setTextSize(16f);
            legend.setDrawInside(false);

            chartGraph.getAxisRight().setEnabled(false);
            chartGraph.setExtraBottomOffset(50f);
            chartGraph.getDescription().setEnabled(false);

            // Enable dragging and scaling
            chartGraph.setDragEnabled(true);
            chartGraph.setScaleXEnabled(true);

            // Set initial visible range
            int visibleRange = 6; // or any number of your choice
            chartGraph.setVisibleXRangeMaximum(visibleRange);

            // Move to the end to show latest data points
            if (xLabels.size() > visibleRange) {
                chartGraph.moveViewToX(xLabels.size() - 1);
            }

            chartGraph.invalidate(); // refresh
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
