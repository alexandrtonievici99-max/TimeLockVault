# 🚀 TimeLock Vault — Complete Setup & Build Guide

## 📥 Step 1: Clone the Repository

```bash
git clone https://github.com/alexandrtonievici99-max/TimeLockVault.git
cd TimeLockVault
```

## 🔧 Step 2: Open in Android Studio

1. **Launch Android Studio**
2. Click **File** → **Open**
3. Navigate to the `TimeLockVault` folder (where you cloned it)
4. Select the folder and click **Open**
5. **Wait for Gradle sync to complete** (you'll see "Gradle sync finished" at the bottom)

⚠️ **First sync may take 2-5 minutes** — this is normal while dependencies download.

## 📱 Step 3: Connect Device or Start Emulator

### Option A: Physical Device
- Connect your Android phone via USB cable
- Enable **Developer Mode** (tap Build Number 7 times in Settings → About Phone)
- Enable **USB Debugging** (Settings → Developer Options → USB Debugging)
- Android Studio will detect your device

### Option B: Android Emulator
1. In Android Studio, go to **Tools** → **Device Manager**
2. Click **Create Device**
3. Select **Pixel 6** or any modern phone
4. Choose API 30+ (recommended)
5. Click **Finish** and wait for emulator to boot (1-2 min)

## 🏗️ Step 4: Build & Run the App

### Method 1: Direct Run (Easiest)
1. Click the **green play button** (▶) in the toolbar at the top
2. Select your device/emulator from the popup
3. Click **OK**
4. **Wait for build to finish** (see "BUILD SUCCESSFUL" in Logcat)
5. App auto-launches on your device

### Method 2: Manual APK Build
1. Click **Build** → **Build Bundle(s)/APK(s)** → **Build APK(s)**
2. Wait for completion (see "BUILD SUCCESSFUL")
3. Folder opens automatically → navigate to `app/build/outputs/apk/debug/`
4. **app-debug.apk** is ready to install or share

## ✅ Step 5: Test the App

### First Launch
- **Generate Password**: Click "CREATE PASSWORD" → random password appears
- **Customize**: Adjust length slider (8-32 chars) and toggle character types
- **Manual Input**: Paste your own password in the text field
- **Copy**: Click copy icon to copy password to clipboard

### Lock Test
1. Click **"SAVE AND LOCK"**
2. Bottom sheet appears with lock duration options
3. Select duration (default: 30 min)
4. Read the warning ⚠️
5. Click **"Confirm & Lock App"**
6. App **immediately closes and locks**

### Locked State
- Reopen the app → **FULL-SCREEN LOCK SCREEN** appears
- Shows countdown timer (DD : HH : MM : SS)
- Lock icon + circular progress bar
- **Back button disabled** (no bypass)
- Tap **OK** to minimize (can't access password)

### Auto-Unlock
- Wait until countdown reaches **00:00:00**
- App **automatically unlocks**
- Normal password screen appears with your saved password
- Copy button works immediately

### Debug Mode (Only in Debug Builds)
- At bottom of VaultScreen: **"🔧 Debug 30s Lock"** button
- Instantly locks vault for 30 seconds (perfect for testing)
- Great for quick demos

## 🐛 Troubleshooting

### Build Fails with "SDK not found"
- Click **File** → **Project Structure**
- Under **SDK Location**, set to your Android SDK path
- Or click **Install missing SDK** if prompted

### App Crashes on Launch
- Check **Logcat** at bottom (red/yellow messages)
- Make sure device API level is ≥ 26 (Android 8.0)
- Try Clean Build: **Build** → **Clean Project** → **Run again**

### Device Not Detected
- Check USB cable (try different cable)
- Disable/enable USB Debugging
- Restart ADB: **Tools** → **Device Manager** → **⚙️** → **Restart ADB**

### Emulator Too Slow
- Close other apps on your computer
- Use Pixel 4/5/6 emulator (optimized)
- Enable **GPU rendering** in emulator settings

## 📊 Project Structure Reference

```
TimeLockVault/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/timelockvault/
│   │   │   ├── MainActivity.kt              ← Entry point
│   │   │   ├── TimeLockManager.kt           ← Core business logic
│   │   │   ├── BootReceiver.kt              ← Boot detection
│   │   │   └── ui/
│   │   │       ├── Theme.kt                 ← Material 3 styling
│   │   │       ├── VaultScreen.kt           ← Password generation UI
│   │   │       └── LockedCountdownScreen.kt ← Lock timer UI
│   │   └── res/
│   │       ├── AndroidManifest.xml
│   │       └── values/
│   │           ├── strings.xml
│   │           ├── colors.xml
│   │           ├── dimens.xml
│   │           └── themes.xml
│   └── build.gradle.kts                     ← Dependencies
├── build.gradle.kts
├── settings.gradle.kts
└── README.md (this file)
```

## 🔐 Security Features Explained

**Encrypted Storage**
- Uses AndroidX EncryptedSharedPreferences
- AES-256-GCM encryption (military-grade)
- Android Keystore manages master key
- Hardware-backed on modern devices

**Lock Mechanism**
- `SystemClock.elapsedRealtime()` — immune to clock tampering
- Boot token tracking — detects device reboots
- Hard lock gate — back button disabled during lock
- Non-bypassable countdown screen

**Lock Persistence**
- Survives app force-close
- Survives device reboot
- Survives battery drain
- Survives system update

## 📝 API Usage (For Developers)

```kotlin
val manager = TimeLockManager.getInstance(context)

// Save password
manager.savePassword("MySecurePassword123!")

// Get password
val pwd = manager.getPassword()

// Generate random password
val genPwd = manager.generatePassword(
    length = 20,
    includeUppercase = true,
    includeLowercase = true,
    includeNumbers = true,
    includeSymbols = true
)

// Lock for 1 hour
manager.lockFor(TimeUnit.HOURS.toMillis(1))

// Check if locked
if (manager.isVaultLocked()) {
    val remainingMs = manager.getRemainingTimeMs()
    // Show countdown...
}

// Unlock (manual)
manager.unlockVault()

// Listen to state changes
manager.state.collect { state ->
    // state.isLocked
    // state.password
    // state.remainingMs
}
```

## 🎨 UI Features

- **Dark Material 3 Theme** with purple/blue accents
- **Smooth animations** on lock countdown
- **Shadow effects** for depth
- **Responsive layout** (portrait locked, scales to all screens)
- **Accessibility**: High contrast, readable fonts
- **Copy-to-clipboard** integration

## 📦 System Requirements

- **Min Android SDK**: 26 (Android 8.0)
- **Target Android SDK**: 34 (Android 14)
- **RAM**: 2GB+ (emulator), 100MB+ (device)
- **Storage**: ~250MB for first build

## 🚀 Release Build (Production APK)

To create an optimized release APK:

```bash
# In Android Studio:
Build → Build Bundle(s)/APK(s) → Build APK(s)

# For release (requires keystore):
Build → Generate Signed Bundle/APK
# Create a keystore or use existing one
# Choose "APK" → Select release variant
```

## 📞 Support

If you encounter issues:
1. Check **Logcat** (bottom panel in Android Studio)
2. Search error message online
3. Restart Android Studio
4. Clean project: **Build** → **Clean Project**
5. Rebuild: **Run**

## ✨ Next Steps

- ✅ Test basic password generation
- ✅ Test lock/unlock cycle
- ✅ Test with different lock durations
- ✅ Test device restart while locked
- ✅ Customize colors in `colors.xml`
- ✅ Add your own app icon

---

**Happy coding!** 🔒
