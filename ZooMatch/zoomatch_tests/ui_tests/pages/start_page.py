from appium.webdriver.common.appiumby import AppiumBy
from pages.base_page import BasePage

class StartPage(BasePage):
    TAB_LOGIN = (AppiumBy.ID, "com.example.zoomatch:id/tabLogin")
    TAB_REGISTER = (AppiumBy.ID, "com.example.zoomatch:id/tabRegister")

    EMAIL_FIELD = (AppiumBy.ID, "com.example.zoomatch:id/email")
    PASSWORD_FIELD = (AppiumBy.ID, "com.example.zoomatch:id/password")
    USERNAME_FIELD = (AppiumBy.ID, "com.example.zoomatch:id/username")

    ACTION_BUTTON = (AppiumBy.ID, "com.example.zoomatch:id/login")

    def switch_to_login(self):
        self.click(self.TAB_LOGIN)

    def switch_to_register(self):
        self.click(self.TAB_REGISTER)

    def enter_email(self, email: str):
        self.send_keys(self.EMAIL_FIELD, email)

    def enter_password(self, password: str):
        self.send_keys(self.PASSWORD_FIELD, password)

    def enter_username(self, username: str):
        self.send_keys(self.USERNAME_FIELD, username)

    def click_action_button(self):
        self.click(self.ACTION_BUTTON)

    def login(self, email: str, password: str):
        self.switch_to_login()
        self.enter_email(email)
        self.enter_password(password)
        self.click_action_button()

    def register(self, email: str, password: str, username: str):
        self.switch_to_register()
        self.enter_email(email)
        self.enter_password(password)
        self.enter_username(username)
        self.click_action_button()

    def is_button_enabled(self) -> bool:
        btn = self.find_element(self.ACTION_BUTTON)
        return btn.get_attribute("enabled") == "true"

    def wait_for_home_screen(self, timeout=30):
        self.wait.until(lambda d: "HomeActivity" in d.current_activity)