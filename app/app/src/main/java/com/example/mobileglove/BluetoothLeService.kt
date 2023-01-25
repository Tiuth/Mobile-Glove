package com.example.mobileglove

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import java.util.*

private const val TAG = "BluetoothLeService"

const val CHARACTERISTIC_DATA = "mobileglove.CHARACTERISTIC_DATA"
const val CCCD_UUID = "00002902-0000-1000-8000-00805f9b34fb"

class BluetoothLeService : Service() {

    private val binder = LocalBinder()
    private var bluetoothAdapter: BluetoothAdapter? = null
    private lateinit var bluetoothManager: BluetoothManager
    private var bluetoothGatt: BluetoothGatt? = null

    private var connectionState = STATE_DISCONNECTED

    private var bConnectPermissionCheck = Manifest.permission.BLUETOOTH_ADMIN

    //private var initialMcuIncrease = false
    //private var desiredMcu = 256

    private lateinit var mediaBtnManager: MediaBtnManager

    private lateinit var gestureCallbacks: Array<() -> Unit>

    fun initialize(): Boolean {
        mediaBtnManager = MediaBtnManager(this)
        gestureCallbacks = arrayOf( mediaBtnManager::nextBtn,
                                    mediaBtnManager::previousBtn,
                                    mediaBtnManager::playPauseBtn,
                                    mediaBtnManager::volumeUpBtn,
                                    mediaBtnManager::volumeDownBtn)
        if (Build.VERSION.SDK_INT > 30) {
            bConnectPermissionCheck = Manifest.permission.BLUETOOTH_CONNECT
        }
        bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.")
            return false
        }
        return true
    }

    //Suppress since we do check for Permission
    @SuppressLint("MissingPermission")
    fun connect(address: String): Boolean {
        bluetoothAdapter?.let { adapter ->
            try {
                val device = adapter.getRemoteDevice(address)
                // connect to the GATT server on the device
                if (bleConnectPermissionCheck()) {
                    bluetoothGatt = device.connectGatt(this, false, bluetoothGattCallback)
                    return true
                }
                return false
            } catch (exception: IllegalArgumentException) {
                Log.w(TAG, "Device not found with provided address.")
                return false
            }
            // connect to the GATT server on the device
        } ?: run {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return false
        }
    }

    //Suppress since we do check for Permission
    @SuppressLint("MissingPermission")
    private val bluetoothGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // successfully connected to the GATT Server
                connectionState = STATE_CONNECTED
                broadcastUpdate(ACTION_GATT_CONNECTED)
                // Attempts to discover services after successful connection.
                if (bleConnectPermissionCheck()) {
                    //gatt.requestMtu(desiredMcu)
                    //Log.i(TAG, "mtu $desiredMcu requested")
                    bluetoothGatt?.discoverServices()
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // disconnected from the GATT Server
                connectionState = STATE_DISCONNECTED
                broadcastUpdate(ACTION_GATT_DISCONNECTED)
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED)
            } else {
                Log.w(TAG, "onServicesDiscovered received: $status")
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_CHARACTERISTIC_DATA_AVAILABLE, characteristic)
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            //Log.i(TAG, "new value via notify")
            executeGestureCommand(characteristic.getStringValue(0).toInt())
            broadcastUpdate(ACTION_CHARACTERISTIC_DATA_AVAILABLE, characteristic)
        }

        /*override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            Log.i(TAG, "new mtu is $mtu, status: ${status == BluetoothGatt.GATT_SUCCESS}")
            if (!initialMcuIncrease) {
                bluetoothGatt?.discoverServices()
                initialMcuIncrease = true
            }
        }*/
    }

    //Suppress since we do check for Permission
    @SuppressLint("MissingPermission")
    fun readCharacteristic(characteristic: BluetoothGattCharacteristic) {
        bluetoothGatt?.let { gatt ->
            if (bleConnectPermissionCheck()) {
                gatt.readCharacteristic(characteristic)
            }
        } ?: run {
            Log.w(TAG, "BluetoothGatt not initialized")
            return
        }
    }

    fun getAvailableGattServices(): List<BluetoothGattService>? {
        return bluetoothGatt?.services
    }

    //Suppress since we do check for Permission
    @SuppressLint("MissingPermission")
    fun setCharacteristicNotification(characteristic: BluetoothGattCharacteristic, enabled: Boolean ) {
        if (!bleConnectPermissionCheck()) return
        //characteristic.properties contains the properties bitwise
        //BluetoothGattCharacteristic.PROPERTY_NOTIFY is the bitmask for the Notify Property
        if (BluetoothGattCharacteristic.PROPERTY_NOTIFY and characteristic.properties == 0) {
            Log.w(TAG, "Characteristic doesn't support notify")
        }
        bluetoothGatt?.let { gatt ->
            gatt.setCharacteristicNotification(characteristic, enabled)
            if (UUID.fromString(GESTURE_CHARACTERISTIC_UUID) == characteristic.uuid) {
                val descriptor = characteristic.getDescriptor(UUID.fromString(CCCD_UUID))
                if (descriptor == null) {
                    Log.w(TAG, "Descriptor for UUID $CCCD_UUID is null")
                }
                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                gatt.writeDescriptor(descriptor)
            }
        } ?: run {
            Log.w(TAG, "BluetoothGatt not initialized")
        }
    }

    private fun executeGestureCommand(code: Int) {
        if (code < gestureCallbacks.size) {
            gestureCallbacks[code]()
        } else {
            Log.w(TAG, "No Callback set for gesture code $code")
        }
    }

    private fun bleConnectPermissionCheck(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this@BluetoothLeService,
                bConnectPermissionCheck
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        Log.e(TAG, "No Bluetooth Connect Permission")
        return false
    }

    private fun broadcastUpdate(action: String) {
        val intent = Intent(action)
        sendBroadcast(intent)
    }

    private fun broadcastUpdate(action: String, characteristic: BluetoothGattCharacteristic) {
        val intent = Intent(action)

        //val data: ByteArray? = characteristic.value
        val data: String? = characteristic.getStringValue(0)
        if (data?.isNotEmpty() == true) {
            intent.putExtra(CHARACTERISTIC_DATA, "$data")
        }

        sendBroadcast(intent)
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    inner class LocalBinder : Binder() {
        fun getService() : BluetoothLeService {
            return this@BluetoothLeService
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        close()
        return super.onUnbind(intent)
    }

    private fun close() {
        bluetoothGatt?.let { gatt ->
            if (ActivityCompat.checkSelfPermission(
                    this,
                    bConnectPermissionCheck
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e(TAG, "Could not close GATT due to missing Permission")
                return
            }
            gatt.close()
            bluetoothGatt = null
        }
    }

    companion object {
        const val ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED"
        const val ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED"
        const val ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED"
        const val ACTION_CHARACTERISTIC_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_CHARACTERISTIC_DATA_AVAILABLE"

        private const val STATE_DISCONNECTED = 0
        private const val STATE_CONNECTED = 2

    }
}