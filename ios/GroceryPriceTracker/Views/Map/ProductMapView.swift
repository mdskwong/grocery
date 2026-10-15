import SwiftUI
import SwiftData
import MapKit

struct ProductMapView: View {
    @Query private var allProducts: [Product]

    @State private var cameraPosition: MapCameraPosition = .automatic
    @State private var selectedProduct: Product?

    // Only products with valid geographic coordinates
    private var geocodedProducts: [Product] {
        allProducts.filter { $0.coordinate != nil }
    }

    var body: some View {
        NavigationStack {
            ZStack(alignment: .bottom) {
                Map(position: $cameraPosition) {
                    ForEach(geocodedProducts) { product in
                        if let coord = product.coordinate {
                            Annotation(product.productName, coordinate: coord) {
                                Button {
                                    withAnimation(.spring()) {
                                        selectedProduct = product
                                    }
                                } label: {
                                    VStack(spacing: 0) {
                                        ZStack {
                                            Circle()
                                                .fill(Color.accentColor)
                                                .frame(width: 36, height: 36)
                                                .shadow(radius: 3)
                                            Image(systemName: "cart.fill")
                                                .font(.system(size: 16))
                                                .foregroundColor(.white)
                                        }

                                        Image(systemName: "triangle.fill")
                                            .font(.system(size: 10))
                                            .foregroundColor(.accentColor)
                                            .rotationEffect(.degrees(180))
                                            .offset(y: -3)
                                    }
                                }
                            }
                        }
                    }
                }
                .mapControls {
                    MapUserLocationButton()
                    MapCompass()
                    MapScaleView()
                }

                // Custom Info Window Overlay matching Android MarkerAdapter
                if let product = selectedProduct {
                    MapCalloutCard(product: product) {
                        withAnimation {
                            selectedProduct = nil
                        }
                    }
                    .transition(.move(edge: .bottom).combined(with: .opacity))
                    .padding(.horizontal, 16)
                    .padding(.bottom, 24)
                }
            }
            .navigationTitle("Store Map")
            .onAppear(perform: adjustCameraToRecords)
            .onChange(of: geocodedProducts.count) { _, _ in
                adjustCameraToRecords()
            }
        }
    }

    private func adjustCameraToRecords() {
        guard let first = geocodedProducts.first,
              let coord = first.coordinate else {
            // Default center if no items (Hong Kong default from Android source)
            let defaultCoord = CLLocationCoordinate2D(latitude: 22.28319, longitude: 114.13633)
            cameraPosition = .region(MKCoordinateRegion(
                center: defaultCoord,
                span: MKCoordinateSpan(latitudeDelta: 0.05, longitudeDelta: 0.05)
            ))
            return
        }

        cameraPosition = .region(MKCoordinateRegion(
            center: coord,
            span: MKCoordinateSpan(latitudeDelta: 0.05, longitudeDelta: 0.05)
        ))
    }
}

/// Custom Info Window Card matching Android's marker_item.xml
private struct MapCalloutCard: View {
    let product: Product
    var onClose: () -> Void

    var body: some View {
        HStack(spacing: 12) {
            // Thumbnail
            if let photo = product.photoFileName,
               let uiImage = ImageStorageService.shared.loadImage(named: photo) {
                Image(uiImage: uiImage)
                    .resizable()
                    .scaledToFill()
                    .frame(width: 60, height: 60)
                    .clipShape(RoundedRectangle(cornerRadius: 8))
            } else {
                ZStack {
                    RoundedRectangle(cornerRadius: 8)
                        .fill(Color(.systemGray5))
                        .frame(width: 60, height: 60)
                    Image(systemName: "photo")
                        .foregroundColor(.gray)
                }
            }

            // Info
            VStack(alignment: .leading, spacing: 4) {
                Text(product.productName)
                    .font(.headline)
                    .lineLimit(1)

                Text(product.categoryName)
                    .font(.subheadline)
                    .foregroundColor(.secondary)
                    .lineLimit(1)

                if !product.shopName.isEmpty {
                    Text(product.shopName)
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
            }

            Spacer()

            // Price & Close button
            VStack(alignment: .trailing, spacing: 6) {
                Button(action: onClose) {
                    Image(systemName: "xmark.circle.fill")
                        .foregroundColor(.secondary)
                        .font(.title3)
                }
                .buttonStyle(.plain)

                Spacer()

                Text(String(format: "$%.2f", product.price))
                    .font(.system(.title3, design: .rounded).bold())
                    .foregroundColor(.green)
            }
        }
        .padding(12)
        .background(
            RoundedRectangle(cornerRadius: 14)
                .fill(.ultraThinMaterial)
                .shadow(color: .black.opacity(0.15), radius: 8, x: 0, y: 4)
        )
    }
}

