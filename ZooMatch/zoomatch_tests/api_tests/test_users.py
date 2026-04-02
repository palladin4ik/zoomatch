import pytest
import logging


logger = logging.getLogger(__name__)


@pytest.mark.django_db
def test_get_users(api_client, load_data, auth_client):
    logger.info('Тест: получение списка пользователей')
    logger.info('Отправка GET запроса на /api/v1/users/')

    response = api_client.get('/api/v1/users/')

    logger.info(f'Статус ответа: {response.status_code}')

    assert response.status_code == 200
    assert isinstance(response.json()['results'], list)


@pytest.mark.django_db
def test_create_user_invalid(api_client):
    logger.info("Тест: создание пользователя с невалидными данными")
    logger.info("Отправка POST запроса с пустыми данными")

    response = api_client.post('/api/v1/register/', data={})

    logger.info(f"Статус ответа: {response.status_code}")

    assert response.status_code == 400


@pytest.mark.django_db
def test_create_user_valid(api_client):
    logger.info("Тест: создание пользователя с валидными данными")

    data = {
        'name': 'Test Name',
        'email': 'test@mail.ru',
        'password': 'testpass',
        'location': 'Тула'
    }
    logger.info(f"Отправляемые данные: {data}")
    response = api_client.post('/api/v1/register/', data=data)

    logger.info(f"Статус ответа: {response.status_code}")

    assert response.status_code in (201, 200)
    assert response.json()['email'] == 'test@mail.ru'
