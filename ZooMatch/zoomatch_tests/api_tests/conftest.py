import os
from pathlib import Path
# Гарантируем, что Django видит настройки (важно для VS Code discovery)
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'ZooMatch.settings')

import pytest
from rest_framework.test import APIClient
from django.core.management import call_command


@pytest.fixture
def api_client():
    return APIClient()


@pytest.fixture(scope='session')
def load_data(django_db_setup, django_db_blocker):
    with django_db_blocker.unblock():
        fixtures_path = (
            Path(__file__)
            .parent
            / "fixtures"
            / "test_data.json"
        )
        call_command('loaddata', str(fixtures_path))


@pytest.fixture
def auth_client(api_client, django_user_model):
    user = django_user_model.objects.first()
    api_client.force_authenticate(user=user)

    return api_client
