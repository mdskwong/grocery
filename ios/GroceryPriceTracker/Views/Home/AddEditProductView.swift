import SwiftUI
import SwiftData
import PhotosUI

struct AddEditProductView: View {
    @Environment(\.modelContext) private var modelContext
    @Environment(\.dismiss) private var dismiss

    var productToEdit: Product?

    // Form Fields
    @State private var date: Date = Date()
    @State private var location: String = "22.28319331569886,114.13633279567568"
    @State private var latitude: Double? = 22.28319331569886
    @State private var longitude: Double? = 114.13633279567568
    @State private var shopName: String = "Wellcome"
    @State private var categoryName: String = "Drinks"
    @State private var brandName: String = "Coca cola"
    @State private var productName: String = "Coke"
    @State private var itemDescription: String = "330ml"
    @State private var priceString: String = "5.0"
    @State private var currency: String = "Hong Kong Dollar - HK$"
    @State private var remarks: String = "Test 1"

    // Photo selection
    @State private var selectedPhotoItem: PhotosPickerItem?
    @State private var photoImage: UIImage?
    @State private var photoFileName: String?

    // Location state
    @StateObject private var locationManager = LocationManager.shared
    @State private var isFetchingLocation = false

    // Alerts
    @State private var alertMessage = ""
    @State private var showAlert = false

    // Currencies list matching Android app
    private let currencies = [
        "Australian Dollar - A$",
        "British Pound - £",
        "Canadian Dollar - CA$",
        "Chinese Yuan - CN¥",
        "Euro - €",
        "Hong Kong Dollar - HK$",
        "Indian Rupee - ₹",
        "Japanese Yen - ¥",
        "Swiss Franc - CHF",
        "US Dollar - $"
    ]

    private var isEditMode: Bool {
        productToEdit != nil
    }

    var body: some View {
        NavigationStack {
            Form {
                // Section: Basic Info
                Section(header: Text("Product Details")) {
                    TextField("Product Name *", text: $productName)
                        .autocorrectionDisabled()

                    HStack {
                        TextField("Price *", text: $priceString)
                            .keyboardType(.decimalPad)

                        Picker("Currency", selection: $currency) {
                            ForEach(currencies, id: \.self) { curr in
                                Text(curr).tag(curr)
                            }
                        }
                        .labelsHidden()
                    }

                    TextField("Brand Name", text: $brandName)
                    TextField("Category Name", text: $categoryName)
                    TextField("Shop Name", text: $shopName)
                    TextField("Description & Specs", text: $itemDescription)
                }

                // Section: Date & Location
                Section(header: Text("Date & Location")) {
                    DatePicker("Date *", selection: $date, displayedComponents: .date)

                    VStack(alignment: .leading, spacing: 8) {
                        HStack {
                            TextField("Location (lat,lng)", text: $location)
                                .font(.footnote)
                                .foregroundColor(.secondary)

                            Button(action: fetchLocation) {
                                if isFetchingLocation {
                                    ProgressView()
                                        .scaleEffect(0.8)
                                } else {
                                    Label("GPS", systemImage: "location.fill")
                                        .font(.caption)
                                }
                            }
                            .buttonStyle(.bordered)
                        }

                        if let error = locationManager.errorMessage {
                            Text(error)
                                .font(.caption2)
                                .foregroundColor(.red)
                        }
                    }
                }

                // Section: Photo
                Section(header: Text("Photo")) {
                    HStack {
                        if let photoImage = photoImage {
                            Image(uiImage: photoImage)
                                .resizable()
                                .scaledToFill()
                                .frame(width: 80, height: 80)
                                .clipShape(RoundedRectangle(cornerRadius: 8))
                        } else {
                            ZStack {
                                RoundedRectangle(cornerRadius: 8)
                                    .fill(Color(.systemGray6))
                                    .frame(width: 80, height: 80)
                                Image(systemName: "camera")
                                    .font(.title2)
                                    .foregroundColor(.gray)
                            }
                        }

                        VStack(alignment: .leading, spacing: 8) {
                            PhotosPicker(
                                selection: $selectedPhotoItem,
                                matching: .images,
                                photoLibrary: .shared()
                            ) {
                                Label(photoImage == nil ? "Select Photo" : "Change Photo", systemImage: "photo")
                                    .font(.subheadline)
                            }

                            if photoImage != nil {
                                Button(role: .destructive, action: removePhoto) {
                                    Label("Remove", systemImage: "trash")
                                        .font(.caption)
                                }
                            }
                        }
                        .padding(.leading, 8)
                    }
                    .padding(.vertical, 4)
                }

                // Section: Remarks
                Section(header: Text("Remarks")) {
                    TextField("Remarks", text: $remarks, axis: .vertical)
                        .lineLimit(3...6)
                }
            }
            .navigationTitle(isEditMode ? "Edit Product" : "Add Product")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") {
                        dismiss()
                    }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button(isEditMode ? "Save" : "Add") {
                        saveProduct()
                    }
                    .bold()
                }
            }
            .alert("Validation Error", isPresented: $showAlert) {
                Button("OK", role: .cancel) {}
            } message: {
                Text(alertMessage)
            }
            .onChange(of: selectedPhotoItem) { _, newItem in
                handlePhotoSelection(newItem)
            }
            .onAppear(perform: populateInitialValues)
        }
    }

    private func populateInitialValues() {
        if let p = productToEdit {
            date = p.date
            location = p.location
            latitude = p.latitude
            longitude = p.longitude
            shopName = p.shopName
            categoryName = p.categoryName
            brandName = p.brandName
            productName = p.productName
            itemDescription = p.itemDescription
            priceString = String(p.price)
            currency = p.currency
            remarks = p.remarks
            photoFileName = p.photoFileName

            if let photoName = p.photoFileName {
                photoImage = ImageStorageService.shared.loadImage(named: photoName)
            }
        }
    }

    private func handlePhotoSelection(_ item: PhotosPickerItem?) {
        guard let item = item else { return }
        Task {
            if let data = try? await item.loadTransferable(type: Data.self),
               let uiImage = UIImage(data: data) {
                await MainActor.run {
                    self.photoImage = uiImage
                }
            }
        }
    }

    private func removePhoto() {
        photoImage = nil
        selectedPhotoItem = nil
        photoFileName = nil
    }

    private func fetchLocation() {
        isFetchingLocation = true
        Task {
            do {
                let coord = try await locationManager.fetchCurrentLocation()
                await MainActor.run {
                    latitude = coord.latitude
                    longitude = coord.longitude
                    location = "\(coord.latitude),\(coord.longitude)"
                    isFetchingLocation = false
                }
            } catch {
                await MainActor.run {
                    isFetchingLocation = false
                }
            }
        }
    }

    private func validate() -> Double? {
        let trimmedName = productName.trimmingCharacters(in: .whitespacesAndNewlines)
        if trimmedName.isEmpty {
            alertMessage = "Product name is required."
            showAlert = true
            return nil
        }

        guard let price = Double(priceString), price > 0 else {
            alertMessage = "Price must be greater than zero."
            showAlert = true
            return nil
        }

        return price
    }

    private func saveProduct() {
        guard let validPrice = validate() else { return }

        // Save image to sandbox if modified
        var savedPhotoName = photoFileName
        if let image = photoImage {
            if savedPhotoName == nil {
                savedPhotoName = ImageStorageService.shared.saveImage(image)
            }
        } else {
            if let oldName = photoFileName {
                ImageStorageService.shared.deleteImage(named: oldName)
                savedPhotoName = nil
            }
        }

        // Parse coordinates from text field if manually edited
        var finalLat = latitude
        var finalLon = longitude
        let parts = location.split(separator: ",")
        if parts.count == 2,
           let lat = Double(parts[0].trimmingCharacters(in: .whitespaces)),
           let lon = Double(parts[1].trimmingCharacters(in: .whitespaces)) {
            finalLat = lat
            finalLon = lon
        }

        if let existingProduct = productToEdit {
            // Update existing
            existingProduct.productName = productName
            existingProduct.price = validPrice
            existingProduct.currency = currency
            existingProduct.date = date
            existingProduct.location = location
            existingProduct.latitude = finalLat
            existingProduct.longitude = finalLon
            existingProduct.shopName = shopName
            existingProduct.categoryName = categoryName
            existingProduct.brandName = brandName
            existingProduct.itemDescription = itemDescription
            existingProduct.remarks = remarks
            existingProduct.photoFileName = savedPhotoName
        } else {
            // Create new
            let newProduct = Product(
                id: UUID(),
                date: date,
                location: location,
                latitude: finalLat,
                longitude: finalLon,
                shopName: shopName,
                categoryName: categoryName,
                brandName: brandName,
                productName: productName,
                itemDescription: itemDescription,
                price: validPrice,
                currency: currency,
                photoFileName: savedPhotoName,
                remarks: remarks,
                createdAt: Date()
            )
            modelContext.insert(newProduct)
        }

        dismiss()
    }
}

