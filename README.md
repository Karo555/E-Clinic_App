# E-Clinic App

E-Clinic App is a Kotlin-based mobile application designed to streamline clinic management and enhance patient-doctor interactions. The app provides features for patients, doctors, and administrators, offering a seamless experience for managing appointments, medical records, and communication.

## Features

- **Authentication**: Secure login and registration for patients, doctors, and administrators.
- **Dashboards**:
    - Patient Dashboard: View medical records, appointments, and chat with doctors.
    - Doctor Dashboard: Manage appointments, view patient details, and update medical records.
    - Admin Dashboard: Oversee clinic operations and manage users.
- **Navigation**: Intuitive navigation using Jetpack Compose and Navigation components.
- **Onboarding**: Step-by-step medical form completion for first-time users.
- **Chat**: Real-time communication between patients and doctors.
- **Settings**: Manage user preferences and account details.

## Tech Stack

- **Languages**: Kotlin, Java
- **Frameworks**: Jetpack Compose, Android Navigation
- **Build Tool**: Gradle
- **Architecture**: MVVM (Model-View-ViewModel)
- **Backend**: Firebase Firestore for database management
- **GraphQL**: Used for efficient data querying and mutations

## Project Structure

- `app/`: Contains the main application code.
    - `src/main`: Source files for the app.
    - `google-services.json`: Firebase configuration.
    - `build.gradle.kts`: Build configurations.
- `docs/`: Documentation files.
    - `architecture/`: Design decisions and technical details.
- `dataconnect/`: GraphQL-related files.
    - `dataconnect.yaml`: Configuration for data connections.
    - `connector/`: Queries and mutations.
    - `schema/`: GraphQL schema.
- `test/`: Testing-related files.
    - `index.js`: JavaScript-based tests.
    - `package.json`: Dependencies for testing.

## Getting Started

### Prerequisites

- Android Studio Meerkat | 2024.3.1 Patch 2
- JDK 11 or higher
- Gradle 7.0 or higher

### Installation

1. Clone the repository:
    ```bash
    git clone https://github.com/Karo555/E-Clinic_App
    ```
2. Open the project in Android Studio.
3. Sync Gradle and install dependencies.

### Running the App

1. Connect an Android device or emulator.
2. Build and run the app using Android Studio.

## Testing

- Unit tests are located in the `test/` directory.
- Run tests using:
    ```bash
    npm test
    ```