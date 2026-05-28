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

**Структура `.env`:**
```bash
# DATABSE
DB_NAME=
DB_USER=
DB_PASSWORD=
DB_HOST=
DB_PORT=

# Django SECRET_KEY
SECRET_KEY=
<<<<<<< HEAD
=======

# Yandex Maps API
YANDEX_MAPS_API_KEY=
>>>>>>> 5d46af5 (Update README.md)
```

6. Выполните миграции
```bash
python manage.py migrate
```

7. Установите фикстуры для базы данных
```bash
python manage.py flush
python manage.py loaddata ../fixtures/test_data.json
```
Для изменения паролей тестовых пользователей
```bash
python manage.py shell

from users.models import User

for u in User.objects.all():
    u.set_password("<Любой пароль>")
    u.save()

exit()
```

8. Установите и запустите Redis в Docker
```bash
docker run -d --name redis -p 6379:6379 redis
```

9. Запустите сервер разработки
```bash
daphne -p 8000 ZooMatch.asgi:application
```

## Документация
API документация доступна по адресам
http://127.0.0.1:8000/api/docs/redoc/ или http://127.0.0.1:8000/api/docs/swagger/
