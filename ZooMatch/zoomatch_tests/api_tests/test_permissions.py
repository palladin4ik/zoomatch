import pytest
import logging

logger = logging.getLogger(__name__)


@pytest.mark.django_db
def test_unauthorized_access(api_client):
    logger.info("Тест: доступ без авторизации")

    logger.info("Попытка GET /api/v1/pets/ без токена")
    response = api_client.get('/api/v1/pets/')

    logger.warning(f"Получен статус: {response.status_code}")

    assert response.status_code in (401, 403)


@pytest.mark.django_db
def test_user_without_access(load_data, auth_client):
    logger.info("Тест: удаление чужого питомца")

    logger.info("Попытка DELETE /api/v1/pets/5/")
    response = auth_client.delete('/api/v1/pets/5/')

    logger.warning(f"Получен статус: {response.status_code}")

    assert response.status_code == 404
