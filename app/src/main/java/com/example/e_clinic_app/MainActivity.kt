package com.example.e_clinic_app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.e_clinic_app.ui.theme.EClinic_AppTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EClinic_AppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
        authenticateThenWrite()
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    EClinic_AppTheme {
        Greeting("Android")
    }
}

// Correctly chained Firebase authentication and Firestore write
private fun authenticateThenWrite() {
    val auth = FirebaseAuth.getInstance()
    auth.signInAnonymously()
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("FirebaseTest", "signInAnonymously: success")
                testFirestoreWrite() // Only call Firestore write after successful auth
            } else {
                Log.w("FirebaseTest", "signInAnonymously: failure", task.exception)
            }
        }
}

private fun testFirestoreWrite() {
    val db = FirebaseFirestore.getInstance()
    val docData = hashMapOf("testField" to "Hello Firestore")

    db.collection("testCollection")
        .add(docData)
        .addOnSuccessListener {
            Log.d("FirebaseTest", "Document successfully written!")
        }
        .addOnFailureListener { e ->
            Log.w("FirebaseTest", "Error writing document", e)
        }
}
