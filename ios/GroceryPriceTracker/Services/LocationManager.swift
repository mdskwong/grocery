import Foundation
import CoreLocation
import Combine

/// Manages CoreLocation authorization and GPS coordinate fetching
final class LocationManager: NSObject, ObservableObject, CLLocationManagerDelegate {
    static let shared = LocationManager()

    private let manager = CLLocationManager()
    @Published var lastLocation: CLLocation?
    @Published var authorizationStatus: CLAuthorizationStatus = .notDetermined
    @Published var isFetching: Bool = false
    @Published var errorMessage: String?

    private var locationContinuation: CheckedContinuation<CLLocationCoordinate2D, Error>?

    override private init() {
        super.init()
        manager.delegate = self
        manager.desiredAccuracy = kCLLocationAccuracyBest
        authorizationStatus = manager.authorizationStatus
    }

    func requestPermission() {
        manager.requestWhenInUseAuthorization()
    }

    /// Asynchronously requests current location coordinates
    @MainActor
    func fetchCurrentLocation() async throws -> CLLocationCoordinate2D {
        isFetching = true
        errorMessage = nil

        if authorizationStatus == .notDetermined {
            manager.requestWhenInUseAuthorization()
        }

        return try await withCheckedThrowingContinuation { continuation in
            self.locationContinuation = continuation
            self.manager.requestLocation()
        }
    }

    // MARK: - CLLocationManagerDelegate

    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        DispatchQueue.main.async {
            self.authorizationStatus = manager.authorizationStatus
        }
    }

    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        DispatchQueue.main.async {
            self.isFetching = false
            if let location = locations.last {
                self.lastLocation = location
                self.locationContinuation?.resume(returning: location.coordinate)
                self.locationContinuation = nil
            }
        }
    }

    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        DispatchQueue.main.async {
            self.isFetching = false
            self.errorMessage = error.localizedDescription
            self.locationContinuation?.resume(throwing: error)
            self.locationContinuation = nil
        }
    }
}

