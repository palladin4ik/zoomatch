# Запуск проекта

1. Клонируйте репозиторий
```bash
git clone https://github.com/Palladin4ik/ZooMatch.git
```
2. Перейдите в ветку разработки
```bash
git checkout develop
```
3. Создайте и активируйте виртуальное окружение
```bash
python -m venv venv
source venv/Scripts/activate
```
4. Установите зависимости
```bash
pip install -r requirements.txt
```
5. Настройте переменные окружения

Создайте файл `.env` в корне проекта и добавьте необходимые переменные.

**Структура `.env.example`:**
```bash
# DATABASE
POSTGRES_DB=
POSTGRES_USER=
POSTGRES_PASSWORD=
DB_HOST=
DB_PORT=

# Django settings
SECRET_KEY=
DEBUG=True
ALLOWED_HOSTS=
CORS_ALLOWED_ORIGINS=

# REDIS
REDIS_URL=

# CELERY
CELERY_BROKER_URL=
CELERY_RESULT_BACKEND=

# Yandex Maps API
YANDEX_MAPS_API_KEY=
```

6. Выполните миграции
```bash
python manage.py migrate
```

7. Установите фикстуры для базы данных
```bash
python manage.py flush
python manage.py loaddata fixtures/action_categories.json
python manage.py loaddata fixtures/test_data.json
```

8. Запустите docker-контейнер
```bash
docker compose up -d
```

## Документация
API документация доступна по адресам
http://127.0.0.1:8000/api/docs/redoc/ или http://127.0.0.1:8000/api/docs/swagger/
