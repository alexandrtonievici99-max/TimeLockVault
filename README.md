# TimeLock Vault

**A secure, self-locking password vault for Android built with Modern Android Development (MAD), Material 3 Design, and Jetpack Compose.**

## 🔐 Features

- **Secure Password Generation**: Auto-generate complex passwords with customizable options (uppercase, lowercase, numbers, symbols)
- **Manual Password Input**: Paste or type your own password
- **Encrypted Storage**: Uses AndroidX EncryptedSharedPreferences with Android Keystore for bank-grade encryption
- **Self-Locking Timer**: Set lock duration (minutes, hours, days) and app automatically locks until countdown expires
- **Anti-Time-Tampering**: Uses `SystemClock.elapsedRealtime()` to prevent system clock manipulation attacks
- **Boot Resilience**: Detects device reboots and maintains lock state across restarts
- **Hard Lock Gate**: Non-bypassable full-screen overlay when vault is locked; back button is disabled
- **Material 3 Design**: Beautiful dark-themed UI with smooth animations and intuitive controls
- **Jetpack Compose**: Modern declarative UI framework for responsive, maintainable UI code

## 🛠️ Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose + Material 3
- **Architecture**: MVVM + StateFlow
- **Security**: AndroidX Security (EncryptedSharedPreferences), Android Keystore
- **Minimum SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)

## 📦 Project Structure

```
TimeLockVault/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/example/timelockvault/
│   │   │   ├── MainActivity.kt           # Entry point with lock gate routing
│   │   │   ├── TimeLockManager.kt        # Core business logic & encryption
│   │   │   ├── BootReceiver.kt           # Handles device boot events
│   │   │   └── ui/
│   │   │       ├── Theme.kt              # Material 3 dark theme
│   │   │       ├── VaultScreen.kt        # Main password generation & setup screen
│   │   │       └── LockedCountdownScreen.kt  # Lock timer display
│   │   └── res/values/
│   │       ├── strings.xml
│   │       ├── colors.xml
│   │       └── themes.xml
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## 🚀 Getting Started

### Prerequisites
- Android Studio 2024.1+
- JDK 17+
- Android SDK with API 34

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/alexandrtonievici99-max/TimeLockVault.git
   cd TimeLockVault
   ```

2. **Open in Android Studio**
   - File → Open → Select the `TimeLockVault` folder
   - Wait for Gradle sync to complete

3. **Build & Run**
   - Connect an Android device (API 26+) or start an emulator
   - Click "Run" or press `Shift + F10`

## 🔑 How to Use

### 1. **Setup Phase (Unlocked)**
   - App displays the **Generate Password** screen
   - Click "CREATE PASSWORD" to generate a random secure password
   - Customize password length (8-32 chars) and character types using toggles
   - Or paste your own password in the "Password or paste your own" field
   - Click "SAVE AND LOCK" to lock the vault

### 2. **Lock Duration Dialog**
   - A bottom sheet appears with preset durations: 5 min, 30 min, 1 hour, 6 hours, 1 day
   - Select your desired lock duration
   - Read the warning: "Once activated, the app will self-lock. You will NOT be able to access this password until the timer expires."
   - Tap "Confirm & Lock App" to activate the lock
   - **App immediately closes and locks itself**

### 3. **Locked Phase (Hard Lock)**
   - Upon any subsequent app launch while timer is running:
     - Full-screen dark overlay appears
     - Large lock icon in center with circular progress indicator
     - Digital countdown: `DD : HH : MM : SS`
     - Status text: "Vault is locked. Access denied until countdown completes."
   - Back button is **disabled** (no way to bypass)
   - Only "OK" button available (minimizes/closes app)
   - Cannot access generated password until timer expires

### 4. **Unlock Phase (Auto)**
   - When countdown reaches `00:00:00`, vault **automatically unlocks**
   - Normal VaultScreen appears with your saved password ready to copy
   - Lock can be set again for a new cycle

## 🔒 Security Features

### Encryption
- **EncryptedSharedPreferences**: All passwords stored encrypted using Android Keystore
- **AES-256-GCM**: Military-grade encryption for data at rest
- Master Key managed by Android Keystore (hardware-backed on supported devices)

### Anti-Tampering
- **SystemClock.elapsedRealtime()**: Timer immune to system clock changes
- **Boot Token Tracking**: Detects device reboots; maintains lock state across restarts
- **No bypass**: Hard-coded back handler prevents any navigation during lock

### Lock Resilience
- Lock persists across:
  - App force-close
  - Device restart
  - System updates
  - Background kill by OS

## 🧪 Testing

### Unit Tests
Run tests to verify `TimeLockManager` logic:
```bash
./gradlew test
```

Tests validate:
- Password generation with correct length
- Lock duration calculation
- Remaining time accuracy

### Manual Testing (Debug Mode)
In **DEBUG** builds, VaultScreen includes a **"Debug 30s Lock"** button for quick testing:
- Generates a password
- Locks vault for 30 seconds
- Perfect for testing lock UI and timer behavior

## 📝 API Overview

### TimeLockManager

```kotlin
// Initialize (singleton)
val manager = TimeLockManager.getInstance(context)

// Password Operations
manager.savePassword(password: String)
manager.getPassword(): String
manager.clearVault()

// Lock Operations
manager.lockFor(durationMs: Long)
manager.isVaultLocked(): Boolean
manager.getRemainingTimeMs(): Long
manager.unlockVault()

// Password Generation
manager.generatePassword(
    length: Int = 16,
    includeUppercase: Boolean = true,
    includeLowercase: Boolean = true,
    includeNumbers: Boolean = true,
    includeSymbols: Boolean = true
): String

// State Management
manager.state: StateFlow<VaultUiState>
manager.refreshState()

// Debug
manager.debugLockForSeconds(seconds: Long)
```

## 🎨 UI/UX Highlights

- **Dark Material 3 Theme**: Sleek, modern dark color scheme
- **Smooth Animations**: Jetpack Compose built-in transitions
- **Responsive Design**: Adapts to all screen sizes
- **Accessibility**: Proper contrast ratios, readable fonts
- **Live Timer**: Countdown updates every second with smooth refresh
- **Visual Lock Indicator**: Circular progress ring + lock icon during locked state

## 📱 Supported Devices

- **Min SDK**: 26 (Android 8.0 Oreo)
- **Target SDK**: 34 (Android 14)
- **Recommended**: API 30+ for optimal Material 3 rendering

## 🔄 State Flow

```
UNLOCKED STATE
    ↓
[User clicks "SAVE AND LOCK"]
    ↓
[Lock Duration Dialog]
    ↓
[User confirms duration]
    ↓
[Password encrypted & saved]
    ↓
LOCKED STATE (App closes)
    ↓
[On any launch while timer running]
    ↓
[Hard lock overlay shown]
    ↓
[Timer counts down]
    ↓
[Timer reaches 00:00:00]
    ↓
AUTO-UNLOCK
    ↓
UNLOCKED STATE (Password accessible)
```

## 🐛 Known Limitations

- Only one password can be stored at a time (by design)
- Lock timer cannot be cancelled once activated
- Requires Android 8.0+ (earlier versions not supported)

## 🔮 Future Enhancements (Roadmap)

- [ ] Multiple password vault storage
- [ ] Biometric unlock option
- [ ] Custom lock notification
- [ ] Export/import passwords (encrypted)
- [ ] Dark/light theme toggle
- [ ] Customizable password display font
- [ ] Lock history log

## 📄 License

Apache License 2.0 - See LICENSE file for details

## 👨‍💻 Author

**alexandrtonievici99-max** — Secure password vault enthusiast

---

**Questions or issues?** Open a GitHub issue or contact the maintainer.

Stay secure. 🔐
