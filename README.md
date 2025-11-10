# Запуск проекта

1. Клонируйте репозиторий
```bash
git clone https://github.com/Palladin4ik/ZooMatch.git
```
2. Создайте и активируйте виртуальное окружение
```bash
python -m venv venv
source venv/Scripts/activate
```
3. Установите зависимости
```bash
pip install -r requirements.txt
```
4. Настройте переменные окружения

Создайте файл `.env` в корне проекта и добавьте необходимые переменные
5. Выполните миграции
```bash
python manage.py migrate
```
6. Запустите сервер разработки
```bash
python manage.py runserver
```

## Документация
API документация доступна по адресам
http://127.0.0.1:8000/api/docs/redoc/ или http://127.0.0.1:8000/api/docs/swagger/