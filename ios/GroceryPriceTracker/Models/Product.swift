import Foundation
import SwiftData
import CoreLocation

@Model
final class Product: Identifiable {
    @Attribute(.unique) var id: UUID
    var date: Date
    var location: String
    var latitude: Double?
    var longitude: Double?
    var shopName: String
    var categoryName: String
    var brandName: String
    var productName: String
    var itemDescription: String
    var price: Double
    var currency: String
    var photoFileName: String?
    var remarks: String
    var createdAt: Date

    init(
        id: UUID = UUID(),
        date: Date = Date(),
        location: String = "22.28319331569886,114.13633279567568",
        latitude: Double? = 22.28319331569886,
        longitude: Double? = 114.13633279567568,
        shopName: String = "Wellcome",
        categoryName: String = "Drinks",
        brandName: String = "Coca cola",
        productName: String = "Coke",
        itemDescription: String = "330ml",
        price: Double = 5.0,
        currency: String = "Hong Kong Dollar - HK$",
        photoFileName: String? = nil,
        remarks: String = "Test 1",
        createdAt: Date = Date()
    ) {
        self.id = id
        self.date = date
        self.location = location
        self.latitude = latitude
        self.longitude = longitude
        self.shopName = shopName
        self.categoryName = categoryName
        self.brandName = brandName
        self.productName = productName
        self.itemDescription = itemDescription
        self.price = price
        self.currency = currency
        self.photoFileName = photoFileName
        self.remarks = remarks
        self.createdAt = createdAt
    }

    /// Helper computed property to parse coordinate
    var coordinate: CLLocationCoordinate2D? {
        if let lat = latitude, let lon = longitude {
            return CLLocationCoordinate2D(latitude: lat, longitude: lon)
        }
        let parts = location.split(separator: ",")
        if parts.count == 2,
           let lat = Double(parts[0].trimmingCharacters(in: .whitespaces)),
           let lon = Double(parts[1].trimmingCharacters(in: .whitespaces)) {
            return CLLocationCoordinate2D(latitude: lat, longitude: lon)
        }
        return nil
    }

    /// Formatted date string matching Android dd/MM/yyyy pattern
    var formattedDate: String {
        let formatter = DateFormatter()
        formatter.dateFormat = "dd/MM/yyyy"
        return formatter.string(from: date)
    }

    /// Create a duplicate copy of this product with a new ID
    func duplicate() -> Product {
        return Product(
            id: UUID(),
            date: self.date,
            location: self.location,
            latitude: self.latitude,
            longitude: self.longitude,
            shopName: self.shopName,
            categoryName: self.categoryName,
            brandName: self.brandName,
            productName: self.productName,
            itemDescription: self.itemDescription,
            price: self.price,
            currency: self.currency,
            photoFileName: self.photoFileName,
            remarks: self.remarks,
            createdAt: Date()
        )
    }
}

