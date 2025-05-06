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

## Project Structure

- `app/src/main/java/com/example/e_clinic_app/ui`: Contains UI components for different screens.
- `app/src/main/java/com/example/e_clinic_app/backend`: Backend logic and ViewModels.
- `app/src/main/java/com/example/e_clinic_app/ui/navigation`: Navigation graph configuration.

## Getting Started

### Prerequisites

- Android Studio Meerkat | 2024.3.1 Patch 2
- JDK 11 or higher
- Gradle 7.0 or higher

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/timGalk/E-Clinic_App.git