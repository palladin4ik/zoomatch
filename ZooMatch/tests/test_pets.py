import pytest
import logging

logger = logging.getLogger(__name__)


@pytest.mark.django_db
def test_get_pets(load_data, auth_client):
    logger.info("Тест: получение списка питомцев")

    logger.info("Отправка GET запроса на /api/v1/pets/")
    response = auth_client.get('/api/v1/pets/')

    logger.info(f"Статус ответа: {response.status_code}")

    assert response.status_code == 200
    assert isinstance(response.json()['results'], list)


@pytest.mark.django_db
def test_create_pet_valid(load_data, auth_client):
    logger.info("Тест: создание питомца (валидные данные)")

    data = {
        'name': 'Test',
        'animal_type': 1,
        'breed': 1,
        'is_male': True,
        'age': 3,
        'location': 'Тула'
    }

    logger.info(f"Отправляемые данные: {data}")
    response = auth_client.post('/api/v1/pets/', data=data)

    logger.info(f"Статус ответа: {response.status_code}")

    assert response.status_code in (200, 201)
    assert response.json()['name'] == 'Test'


@pytest.mark.django_db
def test_create_pet_invalid(load_data, auth_client):
    logger.info("Тест: создание питомца (невалидные данные)")

    data = {
        'name': '',
        'animal_type': 1,
        'breed': 1,
        'is_male': True,
        'age': 3,
        'location': 'Тула'
    }

    logger.info(f"Отправляемые данные: {data}")
    response = auth_client.post('/api/v1/pets/', data=data)

    logger.info(f"Статус ответа: {response.status_code}")

    assert response.status_code == 400


@pytest.mark.django_db
def test_pet_crud(load_data, auth_client):
    logger.info("Тест: CRUD операции с питомцем")

    data = {
        'name': 'Test',
        'animal_type': 1,
        'breed': 1,
        'is_male': True,
        'age': 3,
        'location': 'Тула'
    }

    logger.info("Создание питомца")
    response = auth_client.post('/api/v1/pets/', data=data)

    assert response.status_code in (200, 201)

    pet_id = response.json()['id']
    logger.info(f"Создан питомец с ID: {pet_id}")

    logger.info("Получение питомца")
    response = auth_client.get(f'/api/v1/pets/{pet_id}/')

    assert response.status_code == 200

    logger.info("Обновление питомца")
    response = auth_client.patch(
        f'/api/v1/pets/{pet_id}/',
        data={'name': 'UpdTest'}
    )

    assert response.status_code in (200, 201)
    assert response.json()['name'] == 'UpdTest'

    logger.info("Удаление питомца")
    response = auth_client.delete(f'/api/v1/pets/{pet_id}/')

    logger.info(f"Статус удаления: {response.status_code}")

    assert response.status_code == 204
