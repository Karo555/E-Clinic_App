package com.example.e_clinic_app.backend.prescription

import com.example.e_clinic_app.data.model.Prescription

class QrGenerator {

    fun generateQrCode(prescription: Prescription): String {
        // Generate and return a QR code for the prescription
        return "QR Code for prescription ID: ${prescription.id}"
    }
}