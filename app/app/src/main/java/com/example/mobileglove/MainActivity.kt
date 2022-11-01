package com.example.mobileglove

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat


const val GESTURE_SERVICE_UUID = "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
const val GESTURE_CHARACTERISTIC_UUID = "beb5483e-36e1-4688-b7f5-ea07361b26a8"

private const val FINE_LOCATION_PERMISSION_REQUEST_CODE = 2
private const val BLUETOOTH_SCAN_PERMISSION_REQUEST_CODE = 3
private const val BLUETOOTH_CONNECT_PERMISSION_REQUEST_CODE = 4

const val DEVICE_NAME = "mobileglove.DEVICE_NAME"
const val DEVICE_ADDRESS = "mobileglove.DEVICE_ADDRESS"

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    private var scanning = false
    private val scanPeriod: Long = 3000
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bleAdapter: BluetoothAdapter
    private lateinit var bleScanner: BluetoothLeScanner

    // Will be changed in on create if SDK Version needs different permissions
    private var bScanPermissionCheck = Manifest.permission.BLUETOOTH
    private var bScanPermissionGet = "android.permission.BLUETOOTH"

    private var bConnectPermissionCheck = Manifest.permission.BLUETOOTH_ADMIN
    private var bConnectPermissionGet = "android.permission.BLUETOOTH_ADMIN"

    private val handler = Handler(Looper.getMainLooper())

    private val deviceNames = ArrayList<String>()
    private val bleDevices = ArrayList<BluetoothDevice>()
    private lateinit var deviceAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.i("SDK Version", Build.VERSION.SDK_INT.toString())
        if (Build.VERSION.SDK_INT > 30) {
            bScanPermissionGet = "android.permission.BLUETOOTH_SCAN"
            bScanPermissionCheck = Manifest.permission.BLUETOOTH_SCAN

            bConnectPermissionGet = "android.permission.BLUETOOTH_CONNECT"
            bConnectPermissionCheck = Manifest.permission.BLUETOOTH_CONNECT
        }
        requestPermissionsPopUp()

        bluetoothManager= getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        // if the Bluetooth Adapter is not enabled it is null
        if (bluetoothManager.adapter != null) {
            bleAdapter = bluetoothManager.adapter
        }
        else {
            promptEnableBluetooth()
        }

        if (bleAdapter.bluetoothLeScanner != null) {
            bleScanner = bleAdapter.bluetoothLeScanner
        }
        else {
            promptEnableBluetooth()
        }

        val scanBtn: Button = findViewById(R.id.scanBtn)
        val deviceList: ListView = findViewById(R.id.deviceList)

        deviceAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, deviceNames)
        //arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        deviceList.adapter = deviceAdapter

        scanBtn.setOnClickListener {
            scanForBleDevices()
            //deviceNames.add("Test1")
            deviceAdapter.notifyDataSetChanged()
        }

        /*class SpinnerActivity : Activity(), AdapterView.OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                val selectedDevice = deviceNames[pos]
                selectedDeviceView.text = selectedDevice
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Another interface callback
            }
        }*/

        //deviceList.onItemSelectedListener = SpinnerActivity()
        deviceList.setOnItemClickListener { parent, view, position, id ->
            val deviceName = deviceAdapter.getItem(position)
            val deviceAddress = bleDevices[position].address
            val intent = Intent(this, DeviceActivity::class.java).apply {
                putExtra(DEVICE_NAME, deviceName)
                putExtra(DEVICE_ADDRESS, deviceAddress)
            }
            startActivity(intent)
        }
    }

    private val leScanCallback: ScanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            // if the device has a name use the name otherwise the address
            val devName = result.device.name ?: result.device.address
            if (!deviceNames.contains(devName)) {
                Log.i("Ble Scan", "Found a device")
                super.onScanResult(callbackType, result)
                bleDevices.add(result.device)
                deviceNames.add(devName)
                deviceAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun scanForBleDevices() {
        // check if Ble scanner is available
        if (bleAdapter.isEnabled) {
            if (!scanning) {
                //Set stop scanning command to be executed scanPeriod ms after scan start
                handler.postDelayed({
                    scanning = false
                    // check for Bluetooth scanning permission
                    if (ActivityCompat.checkSelfPermission(
                            this,
                            bScanPermissionCheck
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        requestBluetoothScanPermission()
                    }
                    else {
                        Log.i("Ble Scan", "Scan Stopped")
                        bleScanner.stopScan(leScanCallback)
                        val toast = Toast.makeText(this, "Scan done", Toast.LENGTH_SHORT)
                        toast.show()
                    }
                }, scanPeriod)

                //check for scan, connect and location permission
                if (ActivityCompat.checkSelfPermission(
                        this,
                        bScanPermissionCheck
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestBluetoothScanPermission()
                }
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestLocationPermission()
                }
                if (ActivityCompat.checkSelfPermission(
                        this,
                        bConnectPermissionCheck
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestBluetoothConnectPermission()
                }
                //clear previously found devices and start ble scan
                else {
                    deviceNames.clear()
                    bleDevices.clear()
                    deviceAdapter.notifyDataSetChanged()
                    scanning = true
                    Log.i("Ble Scan", "Starting Scan")
                    bleScanner.startScan(leScanCallback)
                }
            } else {
                val toast = Toast.makeText(this, "Still scanning ...", Toast.LENGTH_SHORT)
                toast.show()
            }
        }
        else {
            //if the Ble scanner has not been available check again and send a msg to the user if necessary
            if (!bleAdapter.isEnabled) {
                promptEnableBluetooth()
            }
            else {
                bleScanner = bleAdapter.bluetoothLeScanner
                if (bleScanner != null) {
                    val toast = Toast.makeText(this, "No BLE Scanner found", Toast.LENGTH_LONG)
                    toast.show()
                }
            }
        }
    }

    private fun requestPermissionsPopUp() {
        //check which permissions are missing
        val missingPermissions = ArrayList<String>()
        val requestCodes = ArrayList<Int>()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            missingPermissions.add("android.permission.ACCESS_FINE_LOCATION")
            requestCodes.add(FINE_LOCATION_PERMISSION_REQUEST_CODE)
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                bScanPermissionCheck
            ) != PackageManager.PERMISSION_GRANTED) {
            missingPermissions.add(bScanPermissionGet)
            requestCodes.add(BLUETOOTH_SCAN_PERMISSION_REQUEST_CODE)
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                bConnectPermissionCheck
            ) == PackageManager.PERMISSION_GRANTED) {
            missingPermissions.add(bConnectPermissionGet)
            requestCodes.add(BLUETOOTH_CONNECT_PERMISSION_REQUEST_CODE)
        }
        //if all permissions are given return
        if (missingPermissions.size == 0) {
            return
        }

        runOnUiThread {
            AlertDialog.Builder(this)
                .setTitle("Permission needed")
                .setMessage("This App needs to Access your Bluetooth Adapter and Location to scan for BLE devices. ")
                .setPositiveButton("Allow") { _, _ ->
                    requestMissingPermissions(missingPermissions, requestCodes)
                }
                .setNegativeButton("Deny") { _, _ ->
                    val toast = Toast.makeText(this, "Some features of the App might not work as expected", Toast.LENGTH_LONG)
                    toast.show()
                }
                .show()
        }
    }

    private fun requestMissingPermissions(missingPermissions: ArrayList<String>, requestCodes: ArrayList<Int>) {
        for (i in missingPermissions.indices) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(missingPermissions[i]),
                requestCodes[i]
            )
        }
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) {
            return
        }
        runOnUiThread {
            AlertDialog.Builder(this)
                .setTitle("Permission needed")
                .setMessage("In order to scan for devices this app needs to access your location. ")
                .setPositiveButton("Allow") { _, _ ->
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf("android.permission.ACCESS_FINE_LOCATION"),
                        FINE_LOCATION_PERMISSION_REQUEST_CODE
                    )
                }
                .setNegativeButton("Deny") { _, _ ->
                    val toast = Toast.makeText(this, "Can't perform ble scan", Toast.LENGTH_LONG)
                    toast.show()
                }
                .show()
        }
    }

    private fun requestBluetoothScanPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                bScanPermissionCheck
            ) == PackageManager.PERMISSION_GRANTED) {
            return
        }
        runOnUiThread {
            AlertDialog.Builder(this)
                .setTitle("Permission needed")
                .setMessage("In order to scan for devices this app needs to be allowed to perform Bluetooth scans. ")
                .setPositiveButton("Allow") { _, _ ->
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(bScanPermissionGet),
                        BLUETOOTH_SCAN_PERMISSION_REQUEST_CODE
                    )
                }
                .setNegativeButton("Deny") { _, _ ->
                    val toast = Toast.makeText(this, "Can't perform ble scan", Toast.LENGTH_LONG)
                    toast.show()
                }
                .show()
        }
    }

    private fun requestBluetoothConnectPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                bConnectPermissionCheck
            ) == PackageManager.PERMISSION_GRANTED) {
            return
        }
        runOnUiThread {
            AlertDialog.Builder(this)
                .setTitle("Permission needed")
                .setMessage("In order to scan for devices this app needs to be allowed to connect to Bluetooth devices. ")
                .setPositiveButton("Allow") { _, _ ->
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(bConnectPermissionGet),
                        BLUETOOTH_CONNECT_PERMISSION_REQUEST_CODE
                    )
                }
                .setNegativeButton("Deny") { _, _ ->
                    val toast = Toast.makeText(this, "Can't perform ble connect", Toast.LENGTH_LONG)
                    toast.show()
                }
                .show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            FINE_LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.firstOrNull() == PackageManager.PERMISSION_DENIED) {
                    val toast = Toast.makeText(this, "Could not get FINE_LOCATION permission", Toast.LENGTH_LONG)
                    toast.show()
                }
            }
            BLUETOOTH_SCAN_PERMISSION_REQUEST_CODE -> {
                if (grantResults.firstOrNull() == PackageManager.PERMISSION_DENIED) {
                    val toast = Toast.makeText(this, "Could not get BLUETOOTH_SCAN permission", Toast.LENGTH_LONG)
                    toast.show()
                }
            }
            BLUETOOTH_CONNECT_PERMISSION_REQUEST_CODE -> {
                if (grantResults.firstOrNull() == PackageManager.PERMISSION_DENIED) {
                    val toast = Toast.makeText(this, "Could not get BLUETOOTH_CONNECT permission", Toast.LENGTH_LONG)
                    toast.show()
                }
            }
        }
    }

    private fun promptEnableBluetooth() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        if (ActivityCompat.checkSelfPermission(
                this,
                bConnectPermissionCheck
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestBluetoothConnectPermission()
        }
        enableBluetoothLauncher.launch(enableBtIntent)
    }

    private var enableBluetoothLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            bleAdapter = bluetoothManager.adapter
            bleScanner = bleAdapter.bluetoothLeScanner
        }
        else {
            promptEnableBluetooth()
        }
    }
}