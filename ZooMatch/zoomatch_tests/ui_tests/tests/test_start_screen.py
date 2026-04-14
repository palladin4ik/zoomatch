import allure
from pages.start_page import StartPage

@allure.feature("Стартовая страница (Вход / Регистрация)")
class TestStartScreen:

    @allure.title("Успешный вход")
    def test_successful_login(self, driver, registered_test_user):   # ← используем новую фикстуру
        page = StartPage(driver)
        page.login(registered_test_user["email"], registered_test_user["password"])
        page.wait_for_home_screen()
        assert "HomeActivity" in driver.current_activity

    @allure.title("Успешная регистрация + проверка через API")
    def test_successful_register(self, driver, api_client, new_test_user):
        page = StartPage(driver)
        page.register(
            new_test_user["email"],
            new_test_user["password"],
            new_test_user["name"]
        )
        page.wait_for_home_screen()
        assert "HomeActivity" in driver.current_activity

        # Авторизуем API клиент после UI-регистрации
        api_client.login(new_test_user["email"], new_test_user["password"])

        profile = api_client.get_profile()
        assert profile["email"] == new_test_user["email"]

    @allure.title("Валидация полей (кнопка отключена)")
    def test_invalid_data(self, driver):
        page = StartPage(driver)
        page.enter_email("invalid-email")
        page.enter_password("123")
        assert not page.is_button_enabled()