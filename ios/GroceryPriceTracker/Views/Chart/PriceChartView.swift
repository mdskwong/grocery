import SwiftUI
import SwiftData
import Charts

struct PriceChartView: View {
    @Query(sort: \Product.date, order: .forward) private var allProducts: [Product]

    @State private var selectedProductName: String = ""

    // Unique product names sorted alphabetically
    private var uniqueProductNames: [String] {
        let names = Set(allProducts.map { $0.productName }.filter { !$0.isEmpty })
        return names.sorted()
    }

    // Records for the currently selected product name, sorted chronologically
    private var selectedProductRecords: [Product] {
        allProducts
            .filter { $0.productName == selectedProductName }
            .sorted(by: { $0.date < $1.date })
    }

    private var minPrice: Double {
        selectedProductRecords.map { $0.price }.min() ?? 0
    }

    private var maxPrice: Double {
        selectedProductRecords.map { $0.price }.max() ?? 0
    }

    private var averagePrice: Double {
        guard !selectedProductRecords.isEmpty else { return 0 }
        let total = selectedProductRecords.reduce(0) { $0 + $1.price }
        return total / Double(selectedProductRecords.count)
    }

    var body: some View {
        NavigationStack {
            VStack(spacing: 20) {
                if uniqueProductNames.isEmpty {
                    VStack(spacing: 12) {
                        Image(systemName: "chart.line.uptrend.xyaxis")
                            .font(.system(size: 60))
                            .foregroundColor(.gray.opacity(0.6))
                        Text("No Price Data")
                            .font(.title3.bold())
                            .foregroundColor(.secondary)
                        Text("Add products on the Home tab to see price trends over time.")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                            .multilineTextAlignment(.center)
                            .padding(.horizontal, 32)
                    }
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else {
                    // Product Selection Picker
                    VStack(alignment: .leading, spacing: 6) {
                        Text("Select Product")
                            .font(.caption)
                            .foregroundColor(.secondary)
                            .padding(.horizontal)

                        Picker("Select Product", selection: $selectedProductName) {
                            ForEach(uniqueProductNames, id: \.self) { name in
                                Text(name).tag(name)
                            }
                        }
                        .pickerStyle(.menu)
                        .padding(.horizontal)
                        .padding(.vertical, 8)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .background(Color(.systemGray6))
                        .cornerRadius(10)
                        .padding(.horizontal)
                    }
                    .padding(.top, 8)

                    // Summary Stats Card
                    if !selectedProductRecords.isEmpty {
                        HStack(spacing: 16) {
                            StatCard(title: "Lowest", value: String(format: "$%.2f", minPrice), color: .green)
                            StatCard(title: "Average", value: String(format: "$%.2f", averagePrice), color: .blue)
                            StatCard(title: "Highest", value: String(format: "$%.2f", maxPrice), color: .orange)
                        }
                        .padding(.horizontal)
                    }

                    // Line Chart
                    if selectedProductRecords.isEmpty {
                        Text("No price history for \(selectedProductName)")
                            .foregroundColor(.secondary)
                            .frame(maxWidth: .infinity, maxHeight: .infinity)
                    } else {
                        Chart {
                            ForEach(selectedProductRecords) { record in
                                LineMark(
                                    x: .value("Date", record.date, unit: .day),
                                    y: .value("Price", record.price)
                                )
                                .interpolationMethod(.catmullRom)
                                .foregroundStyle(Color.accentColor)
                                .lineStyle(StrokeStyle(lineWidth: 2.5))

                                PointMark(
                                    x: .value("Date", record.date, unit: .day),
                                    y: .value("Price", record.price)
                                )
                                .foregroundStyle(Color.accentColor)
                                .symbolSize(45)
                                .annotation(position: .top) {
                                    Text(String(format: "$%.2f", record.price))
                                        .font(.caption2.bold())
                                        .foregroundColor(.secondary)
                                }
                            }
                        }
                        .chartYScale(domain: (max(0, minPrice - 2)...(maxPrice + 3)))
                        .chartXAxis {
                            AxisMarks(values: .automatic) { value in
                                AxisGridLine()
                                AxisTick()
                                AxisValueLabel(format: .dateTime.month(.abbreviated).day())
                            }
                        }
                        .chartYAxis {
                            AxisMarks(position: .leading) { value in
                                AxisGridLine()
                                AxisTick()
                                if let doubleVal = value.as(Double.self) {
                                    AxisValueLabel(String(format: "$%.1f", doubleVal))
                                }
                            }
                        }
                        .frame(height: 280)
                        .padding(.horizontal)
                        .padding(.vertical, 8)
                    }

                    Spacer()
                }
            }
            .navigationTitle("Price Trends")
            .onAppear {
                if selectedProductName.isEmpty, let first = uniqueProductNames.first {
                    selectedProductName = first
                }
            }
            .onChange(of: uniqueProductNames) { _, newNames in
                if !newNames.contains(selectedProductName), let first = newNames.first {
                    selectedProductName = first
                }
            }
        }
    }
}

private struct StatCard: View {
    let title: String
    let value: String
    let color: Color

    var body: some View {
        VStack(spacing: 4) {
            Text(title)
                .font(.caption2)
                .foregroundColor(.secondary)
            Text(value)
                .font(.system(.subheadline, design: .rounded).bold())
                .foregroundColor(color)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 10)
        .background(Color(.systemGray6))
        .cornerRadius(8)
    }
}

