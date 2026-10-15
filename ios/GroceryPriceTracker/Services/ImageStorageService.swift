import Foundation
import UIKit

/// Handles saving, retrieving, and deleting product images in local sandbox storage
final class ImageStorageService {
    static let shared = ImageStorageService()
    private let imagesDirectoryName = "images"

    private init() {
        createImagesDirectoryIfNeeded()
    }

    private var imagesDirectoryURL: URL? {
        guard let documentsDirectory = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first else {
            return nil
        }
        return documentsDirectory.appendingPathComponent(imagesDirectoryName, isDirectory: true)
    }

    private func createImagesDirectoryIfNeeded() {
        guard let dirURL = imagesDirectoryURL else { return }
        if !FileManager.default.fileExists(atPath: dirURL.path) {
            try? FileManager.default.createDirectory(at: dirURL, withIntermediateDirectories: true, attributes: nil)
        }
    }

    /// Saves a UIImage as JPEG and returns the relative file name
    func saveImage(_ image: UIImage) -> String? {
        guard let dirURL = imagesDirectoryURL,
              let data = image.jpegData(compressionQuality: 0.85) else {
            return nil
        }

        let fileName = "\(UUID().uuidString).jpg"
        let fileURL = dirURL.appendingPathComponent(fileName)

        do {
            try data.write(to: fileURL)
            return fileName
        } catch {
            print("Error saving image: \(error.localizedDescription)")
            return nil
        }
    }

    /// Loads a UIImage by its file name
    func loadImage(named fileName: String?) -> UIImage? {
        guard let fileName = fileName, !fileName.isEmpty,
              let dirURL = imagesDirectoryURL else {
            return nil
        }

        let fileURL = dirURL.appendingPathComponent(fileName)
        if FileManager.default.fileExists(atPath: fileURL.path) {
            return UIImage(contentsOfFile: fileURL.path)
        }
        // Fallback: check if the string itself was an absolute path
        if FileManager.default.fileExists(atPath: fileName) {
            return UIImage(contentsOfFile: fileName)
        }
        return nil
    }

    /// Removes an image file from storage
    func deleteImage(named fileName: String?) {
        guard let fileName = fileName, !fileName.isEmpty,
              let dirURL = imagesDirectoryURL else { return }
        let fileURL = dirURL.appendingPathComponent(fileName)
        try? FileManager.default.removeItem(at: fileURL)
    }
}

