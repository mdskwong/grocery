# Grocery Price Tracker (iOS)

This is the native iOS version of the **Grocery Price Tracker** (GPT) app, migrated from the Android codebase.

---

## 📱 Features Implemented

1. **Home Tab (`RecordListView`)**:
   - Live list of price entries using **SwiftData** (equivalent to Android's Room DB + LiveData).
   - Thumbnail previews, product title, category, date, store, and price tags.
   - Interactive actions:
     - **Swipe to Delete** (also cleans up stored photo files from sandbox).
     - **Swipe to Edit** (opens edit sheet with existing values).
     - **Swipe / Button to Copy** (duplicates record into a new entry, matching Android behavior).
     - **Live Search bar** (filters by product name, category, brand, or store).
     - **Floating Action Button (+)** to log a new price record.

2. **Add / Edit Record Form (`AddEditProductView`)**:
   - Native **DatePicker** dialog defaulting to today.
   - **GPS Auto-Fetch** with **CoreLocation** (`LocationManager`).
   - Currency selector (10 common currencies: USD, EUR, JPY, HKD, GBP, AUD, CAD, CHF, CNY, INR).
   - Photo picker using Apple's modern **PhotosUI (`PhotosPicker`)**.
   - Photo storage in the app's sandboxed `Documents/images/` directory with UUIDs.
   - Form validation for required fields (Product Name, Price > 0).

3. **Price Trends Chart (`PriceChartView`)**:
   - Product selector dropdown listing all unique tracked products alphabetically.
   - Summary statistics cards: **Lowest**, **Average**, and **Highest** recorded price.
   - Interactive trend line chart using **Swift Charts** (`LineMark`, `PointMark`, data value callouts).
   - Chronologically sorted by purchase date.

4. **Store Map (`ProductMapView`)**:
   - Native **MapKit** integration (`Map`, `Annotation`).
   - Plots markers for all records with valid GPS coordinates.
   - Custom popup callout card showing:
     - Product thumbnail
     - Product name & category
     - Store name
     - Price tag
   - Auto-centers camera around existing records.

---

## 🛠️ Requirements & How to Run

1. **Environment**: macOS with **Xcode 15.0+** (iOS 17.0+ deployment target).
2. **Open Project**:
   - Double-click `GroceryPriceTracker.xcodeproj` in `/home/teem/Desktop/grocery/ios/`.
3. **Run**:
   - Select an iPhone simulator (e.g., iPhone 15 Pro or iPhone 16) or a connected iOS device.
   - Press **Cmd + R** (or click the Run button).
4. **No External Dependencies Needed**:
   - Uses native Apple frameworks: **SwiftUI**, **SwiftData**, **Swift Charts**, **MapKit**, **PhotosUI**, and **CoreLocation**.
   - No CocoaPods, Gradle, or Google Maps API keys required!

