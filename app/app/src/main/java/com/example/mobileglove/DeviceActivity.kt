package com.example.mobileglove

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.*
import android.media.AudioManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

private const val TAG = "DeviceActivity"

class DeviceActivity : AppCompatActivity() {
    private var deviceName: String? = ""
    private var deviceAddress: String = ""

    private var bluetoothService : BluetoothLeService? = null

    private var connected: Boolean = false
    private lateinit var statusView: TextView
    private lateinit var gestureView: TextView

    private lateinit var gestureCharacteristic: BluetoothGattCharacteristic

    //val mAudioManager: AudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

    // Code to manage Service lifecycle.
    private val serviceConnection: ServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            bluetoothService = (service as BluetoothLeService.LocalBinder).getService()
            bluetoothService?.let { bluetooth ->
                if (!bluetooth.initialize()) {
                    Log.e("BluetoothLeService", "Unable to initialize Bluetooth")
                    finish()
                }
                bluetooth.connect(deviceAddress)
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            bluetoothService = null
        }
    }

    private val gattUpdateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothLeService.ACTION_GATT_CONNECTED -> {
                    connected = true
                    statusView.text = getString(R.string.connected)
                }
                BluetoothLeService.ACTION_GATT_DISCONNECTED -> {
                    connected = false
                    statusView.text = getString(R.string.disconnected)
                }
                BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED -> {
                    checkGestureServiceAvailability(bluetoothService?.getAvailableGattServices())
                }
                BluetoothLeService.ACTION_CHARACTERISTIC_DATA_AVAILABLE -> {
                    val data = intent.getStringExtra(CHARACTERISTIC_DATA)
                    gestureView.text = data
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device)

        deviceName = intent.getStringExtra(DEVICE_NAME)
        deviceAddress = intent.getStringExtra(DEVICE_ADDRESS) as String

        // bind this Activity to the BLeService
        val gattServiceIntent = Intent(this, BluetoothLeService::class.java)
        bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE)

        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter())

        val deviceNameView = findViewById<TextView>(R.id.deviceName)
        val disconnectBtn = findViewById<Button>(R.id.disconnectBtn)
        gestureView = findViewById(R.id.detectedGesture)
        statusView = findViewById(R.id.statusView)

        deviceNameView.text = deviceName

        disconnectBtn.setOnClickListener {
            finish()
        }
    }

    private fun checkGestureServiceAvailability(gattServices: List<BluetoothGattService>?) {
        Log.w(TAG, "checking Services")
        if (gattServices == null) return
        var uuidFound = false
        gattServices.forEach { gattService ->
            if (gattService.uuid.toString() == GESTURE_SERVICE_UUID) {
                gattService.characteristics.forEach { characteristic ->
                    if (characteristic.uuid.toString() == GESTURE_CHARACTERISTIC_UUID) {
                        uuidFound = true
                        gestureCharacteristic = characteristic
                    }
                }
            }
        }
        if (uuidFound) {
            //bluetoothService?.readCharacteristic(gestureCharacteristic)
            bluetoothService?.setCharacteristicNotification(gestureCharacteristic, true)
        }
        else {
            val toast = Toast.makeText(this, "The Device doesn't provide the needed BLE Service", Toast.LENGTH_LONG)
            toast.show()
        }
    }

    private fun makeGattUpdateIntentFilter(): IntentFilter? {
        return IntentFilter().apply {
            addAction(BluetoothLeService.ACTION_GATT_CONNECTED)
            addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED)
            addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED)
            addAction(BluetoothLeService.ACTION_CHARACTERISTIC_DATA_AVAILABLE)
        }
    }
}