package link.sciber.kurza

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import link.sciber.kurza.bluetooth.BluetoothController
import link.sciber.kurza.ui.TouchpadScreen
import link.sciber.kurza.ui.theme.KurzaTheme

class MainActivity : ComponentActivity() {

    private lateinit var bluetoothController: BluetoothController

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            // Check if all granted or at least essential ones
            val allGranted = permissions.entries.all { it.value }
            if (allGranted) {
                bluetoothController.initialize()
            } else {
                Toast.makeText(this, "Bluetooth permissions are required for this app to function", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        bluetoothController = BluetoothController(this)

        setContent {
            KurzaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TouchpadScreen(bluetoothController, this@MainActivity)
                }
            }
        }
        
        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_SCAN)
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_ADVERTISE)
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        // No location permission needed - we only use paired devices, not scanning for new ones
        // BLUETOOTH_SCAN has neverForLocation flag in manifest

        val missingPermissions = permissionsToRequest.filter {
            checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            requestPermissionLauncher.launch(missingPermissions.toTypedArray())
        } else {
            bluetoothController.initialize()
        }
    }
    
    override fun onResume() {
        super.onResume()
        bluetoothController.onResume()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        bluetoothController.disconnect()
    }
}