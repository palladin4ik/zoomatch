from appium.options.android import UiAutomator2Options

def get_android_capabilities(app_path: str = None): # type: ignore
    options = UiAutomator2Options()
    options.platform_name = "Android"
    options.automation_name = "uiautomator2"
    options.device_name = "any"                    # для реального устройства
    options.udid = "35221JEHN10117"                # ← замени на свой UDID из adb devices
    options.no_reset = False
    options.full_reset = False
    options.language = "ru"
    options.locale = "RU"
    options.app_package = "com.example.zoomatch"
    options.app_activity = "com.example.zoomatch.data.startScreen.SplashActivity"
    if app_path:
        options.app = app_path
    return options