import pytest
import allure
import os
import time
# Гарантируем, что Django видит настройки (важно для VS Code discovery)
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'ZooMatch.settings')

from utils.driver_manager import DriverManager
from utils.api_client import ApiClient
from pages.start_page import StartPage


@pytest.fixture(scope="function")
def api_client():
    client = ApiClient()
    yield client
    client.delete_user()

@pytest.fixture(scope="function")
def registered_test_user(api_client):
    timestamp = int(time.time())
    unique_email = f"test_ui_{timestamp}@zoomatch.com"
    
    user_data = {
        "email": unique_email,
        "password": "Password123",
        "name": f"UI Test {timestamp}"
    }
    
    api_client.register(user_data["email"], user_data["password"], user_data["name"])
    api_client.login(user_data["email"], user_data["password"])
    yield user_data   # возвращаем словарь с актуальными данными

@pytest.fixture(scope="function")
def new_test_user():
    timestamp = int(time.time())

    return {
        "email": f"test_ui_reg_{timestamp}@zoomatch.com",
        "password": "Password123",
        "name": f"UI Reg {timestamp}"
    }


@pytest.fixture(scope="function")
def driver():
    drv = DriverManager.get_driver()
    yield drv
    drv.quit()


@pytest.fixture(scope="function")
def logged_in_driver(driver, registered_test_user):
    page = StartPage(driver)
    page.login(registered_test_user["email"], registered_test_user["password"])
    yield driver


@pytest.hookimpl(tryfirst=True, hookwrapper=True)
def pytest_runtest_makereport(item, call):
    outcome = yield
    rep = outcome.get_result()
    if rep.when == "call" and rep.failed:
        driver = item.funcargs.get("driver") or item.funcargs.get("logged_in_driver")
        if driver:
            allure.attach(
                driver.get_screenshot_as_png(),
                name="screenshot",
                attachment_type=allure.attachment_type.PNG
            )