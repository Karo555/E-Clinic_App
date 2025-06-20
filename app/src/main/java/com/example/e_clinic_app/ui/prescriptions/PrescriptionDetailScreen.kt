package com.example.e_clinic_app.ui.prescriptions


import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.e_clinic_app.data.model.Prescription
import com.example.e_clinic_app.presentation.viewmodel.PrescriptionsViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrescriptionDetailScreen(
    prescription: Prescription,
    navController: NavController,
    prescriptionsViewModel: PrescriptionsViewModel,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
        .withZone(ZoneId.systemDefault())

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Prescription Details") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Medication: ${prescription.medication}", style = MaterialTheme.typography.titleLarge)
            Text("Dosage: ${prescription.dosage}")
            Text("Frequency: ${prescription.frequency}")
            Text("Patient name: ${prescription.patientName}")
            Text("Prescribed by: ${prescription.doctorName}")
            Text("Issued: ${dateFormatter.format(Instant.ofEpochSecond(prescription.dateIssued?.seconds ?: 0))}")
            prescription.notes?.let {
                Text("Notes: $it")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("QR Code:")
            val qrBitmap = generateQrBitmap(prescription.toQrContent())
            qrBitmap?.let {
                Image(bitmap = it.asImageBitmap(), contentDescription = "QR Code", modifier = Modifier.size(200.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                val saved = savePrescriptionAsPdf(context, prescription)
                Toast.makeText(context, if (saved) "Saved as PDF" else "Failed to save", Toast.LENGTH_SHORT).show()
            }) {
                Text("Save as PDF")
            }
        }
    }
}

fun generateQrBitmap(content: String): Bitmap? {
    return try {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512)
        val bmp = Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565)
        for (x in 0 until 512) {
            for (y in 0 until 512) {
                bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        bmp
    } catch (e: Exception) {
        null
    }
}

fun savePrescriptionAsPdf(context: Context, prescription: Prescription): Boolean {
    return try {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(300, 600, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint().apply {
            textSize = 12f
            color = Color.BLACK
        }

        var y = 25f
        fun drawLine(text: String) {
            canvas.drawText(text, 10f, y, paint)
            y += 20f
        }

        drawLine("Medication: ${prescription.medication}")
        drawLine("Dosage: ${prescription.dosage}")
        drawLine("Frequency: ${prescription.frequency}")
        drawLine("Patient ID: ${prescription.patientId}")
        drawLine("Prescribed by: ${prescription.authorId}")
        drawLine("Issued: ${prescription.dateIssued?.seconds}")
        drawLine("Notes: ${prescription.notes ?: "-"}")

        pdfDocument.finishPage(page)

        val file = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "prescription_${prescription.id}.pdf"
        )
        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

fun Prescription.toQrContent(): String {
    return """
        Medication: $medication
        Dosage: $dosage
        Frequency: $frequency
        Patient ID: $patientId
        Author ID: $authorId
        Issued: ${dateIssued?.seconds}
        Notes: ${notes ?: "-"}
    """.trimIndent()
}

