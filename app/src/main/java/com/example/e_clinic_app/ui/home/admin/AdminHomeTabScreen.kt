import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.e_clinic_app.ui.bottomNavBar.BottomNavigationBar
import com.example.e_clinic_app.ui.navigation.Routes

/**
 * A composable function that represents the Admin Home Tab screen in the e-clinic application.
 *
 * This screen serves as the main dashboard for administrators, providing quick access to key
 * functionalities such as managing doctors, managing patients, viewing reports, and handling
 * system alerts. It also displays recent reports for quick review.
 *
 * The screen includes a top app bar, a bottom navigation bar, and a grid-based navigation layout
 * for admin actions. Recent reports are displayed in a horizontally scrollable list.
 *
 * @param navController The `NavController` used for navigation to other screens.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeTabScreen(navController: NavController) {
    val recentReports = listOf(
        "Report 1 - User Issue",
        "Report 2 - System Alert",
        "Report 3 - Doctor Verification"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Admin Dashboard", style = MaterialTheme.typography.headlineSmall)
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
            )
        },
        bottomBar = {
            BottomNavigationBar(navController)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                // Greeting + Avatar
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Hi, Admin!",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = CircleShape
                            )
                    )
                }
            }

            item {
                AdminNavigationGrid(navController)
            }

            item {
                Text(
                    text = "📄 Recent Reports",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            item {
                LazyRow (horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(recentReports) { report ->
                        ElevatedCard(
                            modifier = Modifier
                                .width(220.dp)
                                .clickable {
                                    navController.navigate("reportDetail/$report")
                                }
                        ) {
                            Text(
                                text = report,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * A composable function that represents the Admin Home Tab screen in the e-clinic application.
 *
 * This screen serves as the main dashboard for administrators, providing quick access to key
 * functionalities such as managing doctors, managing patients, viewing reports, and handling
 * system alerts. It also displays recent reports for quick review.
 *
 * The screen includes a top app bar, a bottom navigation bar, and a grid-based navigation layout
 * for admin actions. Recent reports are displayed in a horizontally scrollable list.
 *
 * @param navController The `NavController` used for navigation to other screens.
 */
@Composable
fun AdminNavigationGrid(navController: NavController) {
    val navigationItems = listOf(
        "Manage Doctors" to "${Routes.MANAGE_DOCTORS}",
        "Manage Patients" to "${Routes.MANAGE_PATIENTS}",
        "Reports" to "reports",
        "System Alerts" to "systemAlerts"
    )

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        navigationItems.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                rowItems.forEach { (label, route) ->
                    ElevatedCard(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clickable { navController.navigate(route) }
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.titleSmall,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                if (rowItems.size < 2) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

