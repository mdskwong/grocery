# How to Build and Run GPT
This guide provides instructions on how to compile and execute GPT defined in the build.gradle.kts file.

## Prerequisites

- Android Studio (Arctic Fox or later recommended)
- Java Development Kit (JDK) 11
- Android SDK (API level 35 recommended)
- Gradle (bundled with Android Studio)

## Setting Up the Project

1. Download the project source code.

2. Configure API Keys for Google Maps:

   - Create a `secrets.properties` file in the root directory of your project (same level as `local.properties`).
   - Add your Google Maps API key to `secrets.properties`:

     ```
     MAPS_API_KEY=YOUR_API_KEY
     ```

   - Replace `YOUR_API_KEY` with your actual Google Maps API key.

   - Ensure the `secrets.properties` file is not ignored in `.gitignore` if you want to keep it private.

## Building the Application

### Using Android Studio

- Open the project in Android Studio.
- Sync the project with Gradle files:

  - Click on \"Sync Project with Gradle Files\" button or go to **File > Sync Project with Gradle Files**.
- Build the project:

  - Select **Build > Make Project** or click the Build button.


## Running the Application

### On an Emulator

- Create or start an Android Emulator with API level 24 or higher.
- Run the application:

  - In Android Studio, click the Run button.
  - Select the emulator as the deployment target.

### On a Physical Device

- Enable Developer Options and USB Debugging on your device.
- Connect your device via USB.
- Run the app from Android Studio.


## Additional Notes

- The project uses Java 11 for source and target compatibility.
- The app integrates Google Maps and Location Services, requiring a valid API key.
- Ensure your device or emulator has internet access to load map resources.


## Troubleshooting

- Ensure API key is correctly set in `secrets.properties`.
- Check that the API key has necessary permissions enabled in Google Cloud Console.
- Verify device/emulator configurations and SDK versions match the project's `compileSdk` and `minSdk`.