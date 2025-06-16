import UIKit
// Import your KMP module, name depends on your KMP setup (e.g., "shared" or "Common KMPModule")
// For now, assume the MainViewControllerKt class from Main.ios.kt is available globally or via a module.
// If your KMP module is named "ElchAppKMPModule", it would be:
// import ElchAppKMPModule

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {

    var window: UIWindow?

    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        window = UIWindow(frame: UIScreen.main.bounds)
        // This assumes MainViewControllerKt.MainViewController() is the way to get the KMP UI.
        // The actual invocation depends on how Kotlin declarations are exposed to Swift.
        // It might be Main_iosKt.MainViewController() if Main.ios.kt is the file name.
        // Or if you have a specific module name in build.gradle.kts like 'shared',
        #if {has KMP module name} // Pseudocode, replace with actual module name
        // window?.rootViewController = SharedModule.MainViewController()
        #else
        // This is a common way KMP projects expose such functions:
        window?.rootViewController = Main_iosKt.MainViewController()
        #endif
        window?.makeKeyAndVisible()

        // Request notification permissions
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge]) { granted, error in
            if let error = error {
                print("Error requesting notification authorization: \(error)")
            }
            if granted {
                print("Notification permission granted.")
            } else {
                print("Notification permission denied.")
            }
        }

        return true
    }
}
