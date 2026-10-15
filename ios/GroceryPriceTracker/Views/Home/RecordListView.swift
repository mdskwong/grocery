import SwiftUI
import SwiftData

struct RecordListView: View {
    @Environment(\.modelContext) private var modelContext
    @Query(sort: \Product.date, order: .reverse) private var products: [Product]

    @State private var searchText = ""
    @State private var showingAddSheet = false
    @State private var selectedProductForEdit: Product?

    private var filteredProducts: [Product] {
        if searchText.isEmpty {
            return products
        }
        return products.filter {
            $0.productName.localizedCaseInsensitiveContains(searchText) ||
            $0.categoryName.localizedCaseInsensitiveContains(searchText) ||
            $0.shopName.localizedCaseInsensitiveContains(searchText) ||
            $0.brandName.localizedCaseInsensitiveContains(searchText)
        }
    }

    var body: some View {
        NavigationStack {
            ZStack(alignment: .bottomTrailing) {
                Group {
                    if products.isEmpty {
                        VStack(spacing: 16) {
                            Image(systemName: "cart.badge.plus")
                                .font(.system(size: 60))
                                .foregroundColor(.gray.opacity(0.6))
                            Text("No Records Yet")
                                .font(.title3.bold())
                                .foregroundColor(.secondary)
                            Text("Tap the + button to track your first grocery price.")
                                .font(.subheadline)
                                .foregroundColor(.secondary)
                                .multilineTextAlignment(.center)
                                .padding(.horizontal, 32)
                        }
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                    } else {
                        List {
                            ForEach(filteredProducts) { product in
                                RecordRowView(
                                    product: product,
                                    onEdit: {
                                        selectedProductForEdit = product
                                    },
                                    onCopy: {
                                        duplicate(product)
                                    },
                                    onDelete: {
                                        delete(product)
                                    }
                                )
                                .swipeActions(edge: .trailing, allowsFullSwipe: true) {
                                    Button(role: .destructive) {
                                        delete(product)
                                    } label: {
                                        Label("Delete", systemImage: "trash")
                                    }

                                    Button {
                                        selectedProductForEdit = product
                                    } label: {
                                        Label("Edit", systemImage: "pencil")
                                    }
                                    .tint(.orange)
                                }
                                .swipeActions(edge: .leading) {
                                    Button {
                                        duplicate(product)
                                    } label: {
                                        Label("Copy", systemImage: "doc.on.doc")
                                    }
                                    .tint(.blue)
                                }
                            }
                        }
                        .listStyle(.insetGrouped)
                        .searchable(text: $searchText, prompt: "Search products, shops, brands...")
                    }
                }

                // Floating Action Button matching Android
                Button(action: { showingAddSheet = true }) {
                    Image(systemName: "plus")
                        .font(.title2.bold())
                        .foregroundColor(.white)
                        .frame(width: 56, height: 56)
                        .background(Color.accentColor)
                        .clipShape(Circle())
                        .shadow(color: .black.opacity(0.25), radius: 4, x: 0, y: 3)
                }
                .padding(.trailing, 20)
                .padding(.bottom, 20)
            }
            .navigationTitle("Grocery Records")
            .sheet(isPresented: $showingAddSheet) {
                AddEditProductView(productToEdit: nil)
            }
            .sheet(item: $selectedProductForEdit) { product in
                AddEditProductView(productToEdit: product)
            }
        }
    }

    private func delete(_ product: Product) {
        if let photo = product.photoFileName {
            ImageStorageService.shared.deleteImage(named: photo)
        }
        modelContext.delete(product)
    }

    private func duplicate(_ product: Product) {
        let copy = product.duplicate()
        // Open the copy directly in the edit sheet so user can adjust date/price
        selectedProductForEdit = copy
    }
}

