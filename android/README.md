# OffDuty Call Filter 📞🛡️

An Android call filtering application that intelligently manages incoming calls based on custom schedules and whitelists. Perfect for managing call availability without missing important calls.

## ✨ Features

- **Smart Call Screening**: Automatically filters incoming calls based on your preferences
- **Whitelist Management**: Always allow calls from specific contacts
- **Schedule-Based Filtering**: Define time windows when you want to accept calls
- **Call Log**: Track all blocked calls with timestamps
- **Easy Toggle**: Enable/disable call filtering with a single tap
- **Material Design UI**: Modern, intuitive interface with bottom navigation

## 🛠️ Technical Details

- **Language**: Java
- **Min SDK**: Android 10 (API 29)
- **Target SDK**: Android 14 (API 34)
- **Architecture**: Fragment-based navigation with ViewBinding
- **Data Storage**: JSON-based configuration using Gson
- **Key Technologies**:
  - Android CallScreeningService API
  - Material Design Components
  - ViewBinding
  - SharedPreferences for data persistence

## 📋 Requirements

- Android 10 (API 29) or higher
- Permission to read contacts
- Phone screening service permission

## 🔧 Permissions

The app requires the following permissions:
- `READ_CONTACTS` - To access and whitelist specific contacts

## 🚀 Getting Started

1. Clone this repository
2. Open the project in Android Studio
3. Build and run on an Android device or emulator (API 29+)
4. Grant the necessary permissions when prompted
5. Configure your whitelist and schedule preferences

## 📱 App Structure

The app consists of four main sections:

1. **Home**: Main dashboard with enable/disable toggle
2. **Whitelist**: Manage contacts that can always reach you
3. **Schedule**: Define time windows for accepting calls
4. **Log**: View history of blocked calls

## 🔒 Privacy

- All data is stored locally on your device
- No data is sent to external servers
- No analytics or tracking

## 📄 License

This project is available for personal and educational use.

## 🤝 Contributing

Contributions, issues, and feature requests are welcome!

## 👨‍💻 Author

Built with ❤️ for managing call interruptions effectively







