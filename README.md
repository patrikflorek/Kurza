![App Logo](logo.png)

# Kurza - Bluetooth Touchpad for PC

Kurza is an Android app that turns your Android phone into a wireless touchpad for any Bluetooth-enabled computer using the standard Bluetooth HID (Human Interface Device) protocol. Works with Windows, macOS, and Linux.

## Links

- **Website:** https://patrikflorek.github.io — overview, screenshots, and quick-start instructions.
- **Latest release (APK):** [GitHub Releases](https://github.com/patrikflorek/Kurza/releases/latest) — download the APK from the newest release.

## Features

- **Wireless Mouse Control**: Use your Android phone's touchscreen as a touchpad to control your computer cursor
- **Left/Right Click Buttons**: Dedicated buttons for mouse clicks
- **Tap to Click**: Tap on the touchpad area for left-click
- **Auto-Reconnect**: Automatically reconnects when your phone wakes from sleep
- **No Software Required on PC**: Uses standard Bluetooth HID protocol - works natively with Windows, macOS, and Linux

## Requirements

- **Android Phone**: Android 9.0 (API 28) or higher with Bluetooth support
- **Computer**: Any Bluetooth-enabled Windows, macOS, or Linux computer

## How to Connect

### First-Time Setup

The phone must be paired as an **HID (input) device**, not as a generic Bluetooth device. Follow these steps carefully:

1. **Remove any existing pairing** between your phone and computer:
   - On Windows: Settings → Bluetooth & devices → Find your phone → Remove device
   - On macOS: System Settings → Bluetooth → Right-click your phone → Remove
   - On Linux: Use your Bluetooth manager to remove/forget the phone
   - On your phone: Bluetooth settings → Forget/Unpair the computer

2. **Open the Kurza app** on your phone
   - Wait for "HID service not ready" message to disappear
   - This indicates the app has registered as a Bluetooth mouse

3. **Make your phone discoverable**:
   - Tap the **"Make Discoverable (for new pairing)"** button in the app
   - Accept the prompt to make your device visible

4. **Pair from your computer**:
   - **Windows**: Settings → Bluetooth & devices → Add device
   - **macOS**: System Settings → Bluetooth → Enable Bluetooth and pair
   - **Linux**: Use your Bluetooth manager to add a new device
   - Select your phone from the list - it should be recognized as an input device (mouse)

5. **Connect in the app**:
   - After pairing, your computer will appear in the device list
   - Tap on it to connect
   - Status should change to "Connected"

### Reconnecting

After the initial setup, reconnecting is simple:

- **Manual**: Open the app and tap **"Reconnect to [Computer Name]"** button
- **Automatic**: The app will automatically attempt to reconnect when you wake your phone from sleep

## Usage

Once connected:

- **Move cursor**: Drag your finger on the touchpad area
- **Left click**: Tap on the touchpad, or press the **L** button
- **Right click**: Double-tap on the touchpad, or press the **R** button
- **Scroll**: Use two fingers and drag up/down on the touchpad
- **Click and drag**: Hold the L button while dragging on the touchpad

## Troubleshooting

### Connection fails or times out

- **Cause**: Phone was paired as a generic Bluetooth device, not as an HID device
- **Solution**: Remove the pairing from both devices and follow the first-time setup again

### Phone shows as "Other device" or generic device

- **Cause**: The HID service wasn't registered when pairing occurred
- **Solution**: Ensure the app is open and "HID service not ready" has disappeared before pairing

### Can't reconnect after phone sleep

- The app should auto-reconnect when you wake your phone
- If it doesn't, tap the "Reconnect" button
- If that fails, the HID service may need to re-initialize - wait a moment and try again

### Other Bluetooth devices

**Yes**, you can use other Bluetooth devices simultaneously:

- Bluetooth headphones
- Other Bluetooth mice/keyboards
- Other Bluetooth peripherals

Your computer can handle multiple Bluetooth connections. Your phone acting as a mouse doesn't interfere with other devices.

## Technical Details

- Uses Android's `BluetoothHidDevice` API (Bluetooth HID Device Profile)
- Phone acts as an HID peripheral (mouse), and the connected computer acts as the HID host
- Standard USB HID mouse report descriptor (3 buttons + wheel + X/Y movement)
- Built with Kotlin and Jetpack Compose

## Project Structure

```
app/src/main/java/io/github/patrikflorek/kurza/
├── MainActivity.kt              # Main activity with lifecycle handling
├── bluetooth
│   ├── BluetoothController.kt   # Manages Bluetooth state and connections
│   └── HidService.kt            # HID device registration and mouse reports
└── ui
    ├── TouchpadScreen.kt        # Touchpad UI and state wiring
    ├── TouchpadScreenPreview.kt # Compose preview setup
    ├── components
    │   ├── ConnectionPanel.kt   # Connection controls and status actions
    │   ├── MouseButtons.kt      # On-screen left/right click buttons
    │   ├── MouseEvent.kt        # Mouse event data class
    │   ├── StatusIndicator.kt   # Connection status indicator pill
    │   ├── TouchpadArea.kt      # Touchpad UI with gesture integration
    │   └── TouchpadGestureHandler.kt  # Testable gesture detection logic
    └── theme
        ├── Color.kt             # Color palette definitions
        ├── Theme.kt             # Compose theme setup
        └── Type.kt              # Typography styles
```

## Permissions

The app requires the following permissions:

- `BLUETOOTH_SCAN` - To discover devices
- `BLUETOOTH_ADVERTISE` - To make phone discoverable
- `BLUETOOTH_CONNECT` - To connect to paired devices

## License

Licensed under the [MIT License](LICENSE).
