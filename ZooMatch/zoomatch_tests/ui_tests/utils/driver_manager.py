from appium import webdriver
from capabilities.android_capabilities import get_android_capabilities

class DriverManager:
    @staticmethod
    def get_driver(app_path=None):
        options = get_android_capabilities(app_path) # type: ignore
        driver = webdriver.Remote("http://localhost:4723", options=options)
        driver.implicitly_wait(10)
        return driver