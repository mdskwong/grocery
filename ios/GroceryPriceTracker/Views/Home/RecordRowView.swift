import SwiftUI

struct RecordRowView: View {
    let product: Product
    var onEdit: (() -> Void)?
    var onCopy: (() -> Void)?
    var onDelete: (() -> Void)?

    var body: some View {
        HStack(spacing: 14) {
            // Product Photo or Placeholder
            if let photoName = product.photoFileName,
               let uiImage = ImageStorageService.shared.loadImage(named: photoName) {
                Image(uiImage: uiImage)
                    .resizable()
                    .scaledToFill()
                    .frame(width: 60, height: 60)
                    .clipShape(RoundedRectangle(cornerRadius: 8))
            } else {
                ZStack {
                    RoundedRectangle(cornerRadius: 8)
                        .fill(Color(.systemGray6))
                        .frame(width: 60, height: 60)
                    Image(systemName: "photo")
                        .foregroundColor(.gray)
                        .font(.system(size: 24))
                }
            }

            // Product Details
            VStack(alignment: .leading, spacing: 4) {
                Text(product.productName.isEmpty ? "Unnamed Product" : product.productName)
                    .font(.headline)
                    .foregroundColor(.primary)
                    .lineLimit(1)

                Text(product.categoryName.isEmpty ? "No category" : product.categoryName)
                    .font(.subheadline)
                    .foregroundColor(.secondary)
                    .lineLimit(1)

                HStack(spacing: 6) {
                    Text(product.formattedDate)
                        .font(.caption)
                        .foregroundColor(.secondary)

                    if !product.shopName.isEmpty {
                        Text("•")
                            .font(.caption)
                            .foregroundColor(.secondary)
                        Text(product.shopName)
                            .font(.caption)
                            .foregroundColor(.secondary)
                            .lineLimit(1)
                    }
                }
            }

            Spacer()

            // Price & Currency
            VStack(alignment: .trailing, spacing: 4) {
                Text(String(format: "$%.2f", product.price))
                    .font(.system(.title3, design: .rounded).bold())
                    .foregroundColor(.green)

                // Action Buttons
                HStack(spacing: 12) {
                    Button(action: { onCopy?() }) {
                        Image(systemName: "doc.on.doc")
                            .font(.footnote)
                            .foregroundColor(.blue)
                    }
                    .buttonStyle(.plain)

                    Button(action: { onEdit?() }) {
                        Image(systemName: "pencil")
                            .font(.footnote)
                            .foregroundColor(.orange)
                    }
                    .buttonStyle(.plain)
                }
            }
        }
        .padding(.vertical, 6)
        .contentShape(Rectangle())
    }
}

