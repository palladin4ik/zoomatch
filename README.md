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

Создайте файл `.env` в корне проекта и добавьте необходимые переменные
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
```

6. Выполните миграции
```bash
python manage.py migrate
```
7. Запустите сервер разработки
```bash
python manage.py runserver
```

## Документация
API документация доступна по адресам
http://127.0.0.1:8000/api/docs/redoc/ или http://127.0.0.1:8000/api/docs/swagger/
