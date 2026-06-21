# ZooMatch Web Client — План разработки

## Обзор

**Стек:** Vue 3 (Composition API) + Vite + Pinia + Vue Router + Axios
**Сервер:** Django 5.2 + DRF, `http://localhost:8000/api/v1/` (локально)
**Дизайн:** Единый стиль с мобильным клиентом (фиолетовая тема, боковое меню)
**WebSocket:** `ws://localhost:8000/ws/chats/{userId}/?token=`

## Текущее состояние проекта

Уже реализовано:
- Логин (`LoginView.vue`, `auth.js` store, `api/index.js`)
- Регистрация (`RegisterView.vue`)
- Профиль (`ProfileView.vue`, `EditProfileView.vue`, `UserProfileView.vue`)
- Питомцы (`PetsView.vue`, `PetEditView.vue`, `PetDetailView.vue`)
- Поиск и мэтчинг (`SearchView.vue`, `RequestsView.vue`)
- Чаты и сообщения (`ChatsView.vue`, `MessagesView.vue`)
- Главная страница (`HomeView.vue`)
- Настройки (`SettingsView.vue`)
- Модерация (`ModerationView.vue`) — с редактированием вида/породы перед одобрением
- Роутинг с auth guard + moderator guard (`router/index.js`)
- Axios instance с JWT interceptor + auto-refresh
- WebSocket (`useWebSocket.js` composable)
- Shared UI-кит (14 компонентов)
- Layout (`AppLayout.vue`, `Sidebar.vue`)
- Pinia stores (9 штук)
- Обработка ошибок (глобальный interceptor, toast)
- Аудит безопасности (15 исправлений)

Не реализовано:
- Тестирование на локальном сервере

---

## ФАЗА 0: Инфраструктура и foundation

### 0.1 Настройка окружения
- [ ] Создать `.env` файл:
  ```
  VITE_API_URL=http://localhost:8000
  VITE_WS_URL=ws://localhost:8000
  VITE_MEDIA_URL=http://localhost:8000
  ```
- [ ] Обновить `vite.config.js`: добавить proxy для API (`/api` → `http://localhost:8000`)
- [ ] Обновить `src/api/index.js`: baseURL из `import.meta.env.VITE_API_URL`
- [ ] Удалить `stores/counter.js` (мёртвый код)

### 0.2 Цвета и стили
- [ ] Создать `src/assets/styles/variables.css` — CSS-переменные (цвета из мобильного клиента):
  - `--purple-primary` (основной фиолетовый)
  - `--green-accent` (зелёный для кнопок навигации)
  - `--bg-main` (светлый фон)
  - `--text-primary`, `--text-secondary`, `--text-muted`
  - `--card-bg`, `--card-shadow`
  - `--border-color`
  - `--radius-sm`, `--radius-md`, `--radius-lg`
- [ ] Создать `src/assets/styles/global.css` — сброс стилей (normalize), базовые typography, утилиты
- [ ] Подключить в `main.js`: `import './assets/styles/variables.css'` и `import './assets/styles/global.css'`

### 0.3 Shared компоненты (UI-кит)
Создать `src/components/ui/`:

| Компонент | Файл | Описание |
|-----------|------|----------|
| `UiButton` | `UiButton.vue` | Кнопки: primary (фиолет), secondary (outlined), danger (красный), ghost. Размеры: sm, md, lg |
| `UiInput` | `UiInput.vue` | Text input с label, placeholder, error state, type (text/email/password/number) |
| `UiSelect` | `UiSelect.vue` | Dropdown select с options, label, disabled state |
| `UiTextarea` | `UiTextarea.vue` | Многострочный ввод |
| `UiToggle` | `UiToggle.vue` | Switch toggle (для is_active и т.д.) |
| `UiAvatar` | `UiAvatar.vue` | Круглое фото с fallback (инициалы), размеры: sm/md/lg |
| `UiCard` | `UiCard.vue` | Карточка-обёртка с тенью и скруглением |
| `UiBadge` | `UiBadge.vue` | Бейдж (статус, количество). Цвета: purple, green, red, gray |
| `UiModal` | `UiModal.vue` | Модальное окно (Teleport) с overlay |
| `UiToast` | `UiToast.vue` | Уведомления (успех/ошибка/инфо) |
| `UiLoader` | `UiLoader.vue` | Спиннер загрузки |
| `UiEmptyState` | `UiEmptyState.vue` | Пустое состояние с иконкой и текстом |
| `UiFileUpload` | `UiFileUpload.vue` | Загрузка файлов (drag & drop + клик) |

**Контрольная точка 0:** Все shared компоненты отрисовываются визуально. Создать временную страницу-песочницу для проверки.

### 0.4 Layout
- [ ] Создать `src/components/layout/AppLayout.vue`:
  - Боковое меню слева (240px, на мобилке — гамбургер)
  - Заголовок страницы справа
  - Контент-area
  - Нижний блок с аватаром пользователя (как на макете)
- [ ] Создать `src/components/layout/Sidebar.vue`:
  - Навигация: Главная, Поиск, Запросы, Чаты, Питомцы (Модерация — только для модераторов)
  - Иконки (SVG или Unicode)
  - Активный пункт подсвечивается зелёным (как на макете)
  - Сворачиваемое на мобилке
- [ ] Обернуть роутинг через `AppLayout` (route meta: `layout: 'app'`)

### 0.5 Роутинг
Обновить `src/router/index.js`:

| Путь | Name | Компонент | Auth | Описание |
|------|------|-----------|------|----------|
| `/login` | login | LoginView | No | Вход |
| `/register` | register | RegisterView | No | Регистрация |
| `/` | home | HomeView | Yes | Главная (дашборд) |
| `/search` | search | SearchView | Yes | Поиск/рекомендации |
| `/search/:petId` | match | SearchView | Yes | Свайпинг карточек |
| `/chats` | chats | ChatsView | Yes | Список чатов |
| `/chats/:interlocutorId` | messages | MessagesView | Yes | Чат с пользователем |
| `/pets` | pets | PetsView | Yes | Мои питомцы |
| `/pets/new` | pet-create | PetEditView | Yes | Создание питомца |
| `/pets/:id/edit` | pet-edit | PetEditView | Yes | Редактирование |
| `/pets/:id` | pet-detail | PetDetailView | Yes | Карточка питомца |
| `/profile` | profile | ProfileView | Yes | Мой профиль |
| `/profile/:id` | user-profile | UserProfileView | Yes | Профиль другого |
| `/settings` | settings | SettingsView | Yes | Настройки |
| `/requests` | requests | RequestsView | Yes | Запросы на мэтч |
| `/moderation` | moderation | ModerationView | Yes (admin) | Модерация (requiresModerator) |

### 0.6 Pinia stores — структура
Создать `src/stores/`:

| Store | Файл | Описание |
|-------|------|----------|
| `auth` | `auth.js` | Токены, текущий пользователь, логин/регистрация/выход |
| `user` | `user.js` | Профиль текущего пользователя, обновление |
| `pets` | `pets.js` | Список питомцев, CRUD |
| `animalTypes` | `animalTypes.js` | Типы животных, породы |
| `search` | `search.js` | Рекомендации, фильтры |
| `matches` | `matches.js` | Мэтчи, запросы |
| `chats` | `chats.js` | Список чатов |
| `messages` | `messages.js` | Сообщения, WebSocket |
| `moderation` | `moderation.js` | Заявки на модерацию |

**Контрольная точка 1:** Проект запускается, layout отображается, навигация работает, shared компоненты доступны. Все страницы-заглушки отрисованы.

---

## ФАЗА 1: Авторизация

### 1.1 Регистрация
- [ ] Создать `src/views/RegisterView.vue`
  - Поля: Имя, Фамилия, Email, Пароль, Подтверждение пароля
  - Валидация: email формат, пароль >= 6 символов, совпадение паролей
  - Кнопка "Зарегистрироваться"
  - Ссылка "Уже есть аккаунт? Войти"
- [ ] Добавить в `auth.js` store: `registerAction(firstname, lastname, email, password)`
- [ ] API: `POST /api/v1/register/` → автоматический вход после регистрации

### 1.2 Обновление auth.js store
- [ ] Добавить `refreshTokenAction()` — автоматическое обновление токена
- [ ] Добавить `fetchUserAction()` — загрузка профиля `GET /api/v1/me/`
- [ ] Добавить interceptor в axios для auto-refresh при 401
- [ ] Добавить action `logout()` — очистка tokens + редирект

### 1.3 Страница входа (обновление LoginView)
- [ ] Обновить `LoginView.vue`: ссылка на регистрацию, согласование стилей с макетом

**Контрольная точка 2:** Регистрация → автоматический вход → редирект на главную. Выход → редирект на логин. Токен обновляется автоматически.

---

## ФАЗА 2: Профиль

### 2.1 Просмотр профиля
- [ ] Создать `src/views/ProfileView.vue`
  - Аватар (с кнопкой изменения)
  - Имя, фамилия
  - Описание/био
  - Количество питомцев
  - Кнопка "Редактировать"
  - Кнопка "Настройки"
- [ ] Создать `src/stores/user.js`: `fetchProfile()`, `updateProfile()`, `uploadAvatar()`
- [ ] API: `GET /api/v1/me/`, `PATCH /api/v1/me/`, `PATCH /api/v1/me/avatar/`

### 2.2 Редактирование профиля
- [ ] Создать `src/views/EditProfileView.vue`
  - Поля: Имя, Фамилия, Email, Телефон, Описание
  - Кнопка "Сохранить"
- [ ] API: `PATCH /api/v1/me/`

### 2.3 Профиль другого пользователя
- [ ] Создать `src/views/UserProfileView.vue`
  - Информация о пользователе
  - Список его питомцев
- [ ] API: `GET /api/v1/users/{id}/`

**Контрольная точка 3:** Профиль загружается, редактируется, аватар загружается. Профиль другого пользователя отображается.

---

## ФАЗА 3: Питомцы

### 3.1 Список питомцев
- [ ] Создать `src/views/PetsView.vue`
  - Сетка карточек питомцев (как на макете: аватар, имя, порода, пол, статус)
  - Кнопка "Добавить питомца"
  - Поиск по имени
  - Фильтр по типу животного
- [ ] Создать `src/stores/pets.js`: `fetchMyPets()`, `deletePet()`
- [ ] Создать `src/components/pets/PetCard.vue` — карточка питомца
- [ ] API: `GET /api/v1/pets/me/`, `DELETE /api/v1/pets/{id}/`

### 3.2 Создание/редактирование питомца
- [ ] Создать `src/views/PetEditView.vue`
  - Форма как на макете: фото, основные данные, характеристики, описание, доп. опции
  - Поля: Кличка, Вид животного (select), Порода (select, фильтруется по виду), Дата рождения, Окрас, Пол (переключатель), Возраст, Готов к вязке (toggle), Описание
  - Загрузка аватара (multipart)
  - Загрузка родословной (PDF)
  - Карта для выбора местоположения (Leaflet/OpenStreetMap)
  - Кнопки "Отмена" / "Сохранить"
- [ ] Создать `src/stores/animalTypes.js`: `fetchTypes()`, `fetchBreeds(typeId)`
- [ ] API: `POST /api/v1/pets/`, `PATCH /api/v1/pets/{id}/`, `PATCH /api/v1/pets/{id}/avatar/`, `PATCH /api/v1/pets/{id}/documents/`
- [ ] API: `GET /api/v1/animal-type/`, `GET /api/v1/breed/`

### 3.3 Карточка питомца (детальный просмотр)
- [ ] Создать `src/views/PetDetailView.vue`
  - Большое фото питомца
  - Имя, возраст, порода, пол
  - Местоположение (карта)
  - Описание
  - Информация о владельце
  - Кнопки "Написать владельцу", "Связаться"
- [ ] API: `GET /api/v1/pets/{id}/`

### 3.4 Интеграция карты
- [ ] Установить `leaflet` + `@vue-leaflet/vue-leaflet`
- [ ] Создать `src/components/ui/UiMap.vue` — обёртка над Leaflet
- [ ] Использовать в `PetEditView` (выбор координат) и `PetDetailView` (просмотр)

**Контрольная точка 4:** CRUD питомцев работает: список загружается, создание с валидацией, редактирование, удаление с подтверждением, аватар загружается, карта отображается.

---

## ФАЗА 4: Поиск и мэтчинг

### 4.1 Страница поиска
- [ ] Создать `src/views/SearchView.vue`
  - Выбор питомца из списка (для которого ищем пару)
  - Карточки-рекомендации (свайп или список)
  - Кнопки: лайк (❤️), пропуск, информация
  - Фильтры (модальное окно): радиус, родословная, возраст
- [ ] Создать `src/stores/search.js`: `fetchRecommendations()`, `applyFilters()`
- [ ] API: `GET /api/v1/recommend/recommend/`

### 4.2 Карточки рекомендаций
- [ ] Создать `src/components/search/RecommendationCard.vue`
  - Фото, имя, возраст, порода, расстояние
  - Кнопки взаимодействия
- [ ] Создать `src/components/search/FilterModal.vue`
  - Радиус: 50/150/300 км
  - Только с родословной (toggle)
  - Мин/макс возраст
  - Макс месяцев с момента вязки

### 4.3 Создание мэтча (лайк)
- [ ] API: `POST /api/v1/matches/` (pet_from, pet_to)
- [ ] Обработка ответа: если мэтч взаимный — показать уведомление

### 4.4 Запросы на мэтч
- [ ] Создать `src/views/RequestsView.vue` (или подстраницу)
  - Список входящих запросов
  - Кнопки "Принять" / "Отклонить"
- [ ] Создать `src/stores/matches.js`: `fetchMatches()`, `acceptMatch()`, `rejectMatch()`
- [ ] API: `GET /api/v1/matches/`, `PATCH /api/v1/matches/{id}/`

**Контрольная точка 5:** Рекомендации загружаются, фильтры работают, лайк создаёт мэтч, запросы принимаются/отклоняются.

---

## ФАЗА 5: Чаты и сообщения

### 5.1 Список чатов
- [ ] Создать `src/views/ChatsView.vue`
  - Список чатов с аватаром, именем, последним сообщением, временем, unread badge
- [ ] Создать `src/stores/chats.js`: `fetchChats()`
- [ ] Создать `src/components/chats/ChatListItem.vue`
- [ ] API: `GET /api/v1/chats/`

### 5.2 Экран сообщений
- [ ] Создать `src/views/MessagesView.vue`
  - Заголовок: аватар + имя собеседника + кнопка "назад"
  - Список сообщений (входящие/исходящие пузыри)
  - Поле ввода + кнопка отправки + кнопка прикрепления файла
  - Режим редактирования сообщения (indicate bar сверху)
- [ ] Создать `src/components/chats/MessageBubble.vue`
  - Входящее: белый фон, фиолетовый текст
  - Исходящее: фиолетовый фон, белый текст
  - Изображения: Glide-style загрузка
  - Файлы: имя файла, иконка, кликабельность
  - Время, статус прочтения
- [ ] Создать `src/components/chats/MessageInput.vue`
  - Text input + кнопки отправки и прикрепления
  - Режим редактирования (indicate bar)

### 5.3 Загрузка истории сообщений
- [ ] API: `GET /api/v1/messages/?receiver={userId}`
- [ ] Пагинация (cursor-based): подгрузка при скролле вверх
- [ ] Автопрокрутка вниз при новом сообщении

### 5.4 Отправка сообщений
- [ ] API: `POST /api/v1/messages/` (text, receiver_id)
- [ ] API: `PATCH /api/v1/messages/{id}/` (edit)
- [ ] API: `DELETE /api/v1/messages/{id}/` (delete)
- [ ] Optimistic update: сообщение появляется сразу, статус "отправляется"

### 5.5 WebSocket
- [ ] Создать `src/composables/useWebSocket.js`:
  - Подключение `ws://localhost:8000/ws/chats/{userId}/?token=`
  - Обработка входящих событий: `message`, `read`, `delivered`
  - Отправка: `message`, `media_message`, `read`, `delivered`
  - Автопереподключение при разрыве
- [ ] Интеграция в `MessagesView`: реалтайм-получение сообщений
- [ ] Интеграция в `ChatsView`: обновление unread badge в реальном времени

### 5.6 Загрузка файлов в чат
- [ ] API: `POST /api/v1/messages/{id}/media/` (multipart)
- [ ] Поддержка: изображения (inline preview), документы (ссылка на скачивание)
- [ ] Прогресс-индикатор загрузки (круговой, VK-стиль)

**Контрольная точка 6:** Список чатов загружается, сообщения отправляются/получаются в реальном времени, редактирование/удаление работает, файлы загружаются и отображаются.

---

## ФАЗА 6: Главная страница (дашборд)

### 6.1 Дашборд
- [ ] Создать `src/views/HomeView.vue`
  - Приветствие с именем
  - Статистика: количество мэтчов, чатов, питомцев
  - Активные питомцы (карточки)
  - Кнопки быстрого действия: "Добавить питомца", "Начать поиск"
- [ ] API: `GET /api/v1/matches/`, `GET /api/v1/chats/`, `GET /api/v1/pets/me/`

**Контрольная точка 7:** Главная страница отображает статистику и питомцев.

---

## ФАЗА 7: Настройки

### 7.1 Страница настроек
- [ ] Создать `src/views/SettingsView.vue`
  - Редактировать профиль → EditProfileView
  - Изменить пароль
  - Удалить аккаунт (с подтверждением)
  - Выход из аккаунта
- [ ] API: `PATCH /api/v1/me/change-password/`, `DELETE /api/v1/me/`

**Контрольная точка 8:** Настройки работают: смена пароля, удаление аккаунта, выход.

---

## ФАЗА 8: Финализация

### 8.1 Обработка ошибок
- [ ] Глобальный обработчик ошибок Axios (interceptor response)
- [ ] Toast-уведомления при ошибках
- [ ] Страница 404
- [ ] Защита от двойной отправки форм

### 8.2 Адаптивность
- [ ] Мобильная версия (sidebar → гамбургер, адаптивные сетки)
- [ ] Проверка на разных ширинах экрана

### 8.3 Тестирование на локальном сервере
- [ ] Все CRUD-операции работают
- [ ] Авторизация (вход, регистрация, обновление токена)
- [ ] Загрузка файлов (аватары, документы, сообщения)
- [ ] WebSocket чат
- [ ] Мэтчинг (рекомендации, лайки, принятие/отклонение)

### 8.4 Подключение к продакшену
- [ ] Создать `.env.production`:
  ```
  VITE_API_URL=https://zoomatch.ru
  VITE_WS_URL=wss://zoomatch.ru
  VITE_MEDIA_URL=https://zoomatch.ru
  ```
- [ ] Сборка: `npm run build`
- [ ] Деплой на сервер

---

## ФАЗА 9: Расширенная модерация

### 9.1 Новый PATCH эндпоинт на сервере
- [ ] `moderation/serializers.py` — добавить `ModerationRequestUpdateSerializer` (поля `animal_type`, `breed` доступны для записи)
- [ ] `moderation/views.py` — добавить `@action(detail=True, methods=['patch'])` с именем `edit`:
  - `@extend_schema` с описанием, request body, responses (по паттерну проекта)
  - Принимает `{ animal_type?: string, breed?: string }`
  - Возвращает обновлённый `ModerationRequestSerializer`
  - URL: `PATCH /api/v1/moderation/{id}/edit/`
  - Permission: `IsAdminUser`

### 9.2 Фронтенд: API + Store
- [ ] `api/index.js` — добавить `editModerationRequest(id, data)`
- [ ] `stores/moderation.js` — добавить `editRequest(id, data)` action

### 9.3 ModerationView.vue — полная переработка
- [ ] Карточки заявок с данными питомца (аватар, кличка, возраст, пол, описание, владелец)
- [ ] Редактируемые UiInput для "Вид животного" и "Порода" (pre-filled из заявки)
- [ ] Локальное отслеживание изменений (`editedRequests`)
- [ ] Кнопка "Одобрить": если были правки → PATCH /edit/ → POST /approve/, иначе просто POST /approve/
- [ ] Кнопка "Отклонить": POST /reject/
- [ ] Пустое состояние: UiEmptyState "Нет заявок на модерацию"

**Контрольная точка 9:** Модератор видит заявки, редактирует вид/породу, одобряет/отклоняет. В БД появляются новые AnimalType/Breed.

---

## Порядок выполнения (рекомендуемый)

```
Фаза 0 → Фаза 1 → Фаза 2 → Фаза 3 → Фаза 6 → Фаза 4 → Фаза 5 → Фаза 7 → Фаза 8 → Фаза 9
```

Причина: Сначала инфраструктура, потом авторизация (без неё ничего не работает), затем профиль и питомцы (основные данные), потом главная (нужны данные), затем поиск и чаты (зависят от питомцев), настройки, финализация.

---

## Аудит безопасности (Июнь 2026)

### Найденные проблемы и исправления

| # | Проблема | Критичность | Файл | Исправление |
|---|----------|-------------|------|-------------|
| 1 | `/moderation` доступна всем авторизованным пользователям | 🔴 КРИТИЧЕСКАЯ | `router/index.js` | Добавлен `meta: { requiresModerator: true }` + guard проверяет `authStore.isModerator` |
| 2 | Ссылка "Модерация" видна в sidebar всем пользователям | 🔴 КРИТИЧЕСКАЯ | `Sidebar.vue` | `navItems` теперь computed, ссылка добавляется только если `authStore.isModerator` |
| 3 | `authStore` не очищается при logout — `isModerator` остаётся | 🔴 КРИТИЧЕСКАЯ | `auth.js` | `logout()` сбрасывает `isModerator: false` |
| 4 | ChatsView подключает WebSocket с `userId=null` (до fetchProfile) | 🔴 КРИТИЧЕСКАЯ | `ChatsView.vue` | `fetchProfile()` вызывается до `connect()` |
| 5 | Дублирующий Axios interceptor в App.vue | 🟡 ВЫСОКАЯ | `App.vue` | Удалён дублирующий interceptor (оставлен в `api/index.js`) |
| 6 | Нет валидации размера файлов на клиенте | 🟡 ВЫСОКАЯ | `UiFileUpload.vue`, `MessagesView.vue`, `ProfileView.vue` | Добавлены лимиты: файлы 10МБ, аватар 5МБ, медиа 50МБ |
| 7 | Login: нет проверки пустых полей | 🟡 ВЫСОКАЯ | `LoginView.vue` | Добавлена валидация пустого email/password |
| 8 | Register: нет проверки пустых имён | 🟡 ВЫСОКАЯ | `RegisterView.vue` | Добавлена валидация пустого firstname/lastname |
| 9 | `alert()` вместо toast в SettingsView | 🟠 СРЕДНЯЯ | `SettingsView.vue` | Заменено на `inject('toast')` |
| 10 | 6 мест с silently swallowed errors | 🟠 СРЕДНЯЯ | MessagesView, ProfileView, SearchView, UserProfileView, ModerationView, RequestsView | Добавлены `alert()` с описанием ошибки |
| 11 | Нет `.trim()` перед отправкой данных | 🟠 СРЕДНЯЯ | Register, EditProfile, PetEdit | Добавлен `.trim()` перед API-вызовами |
| 12 | Logout не очищает userStore | 🟡 ВЫСОКАЯ | `SettingsView.vue` | Добавлен `userStore.clear()` перед `authStore.logout()` |
| 13 | Server error response может быть строкой, а не массивом | 🟠 СРЕДНЯЯ | EditProfile, PetEdit | Добавлена проверка `Array.isArray()` |
| 14 | PetEdit не показывает detail ошибки от сервера | 🟠 СРЕДНЯЯ | `PetEditView.vue` | Добавлен вывод `data?.detail` |
| 15 | `isModerator` не синхронизируется с authStore | 🔴 КРИТИЧЕСКАЯ | `user.js` | `fetchProfile()` вызывает `authStore.setModerator()` |

### Что НЕ нуждается в исправлении (уже безопасно):
- **XSS**: Vue 3 `{{ }}` автоматически экранирует. Нет `v-html`, нет `innerHTML`
- **SQL injection**: Все запросы через DRF → параметризованные запросы
- **Double-submit**: У всех форм `loading` ref + `:loading` на кнопках
- **Token refresh**: Очередь на 401, `isRefreshing` флаг, автоматический retry
- **Route guard**: `requiresAuth` на всех защищённых маршрутах
- **File types**: `accept` атрибуты на file input (image/*, .pdf)

### Изменения в store/auth.js:
- Добавлено поле `isModerator: false`
- Добавлен getter `isModerator`
- Добавлен action `setModerator(value)`
- `logout()` сбрасывает `isModerator`

### Изменения в store/user.js:
- `fetchProfile()` вызывает `authStore.setModerator(data.is_moderator)` для синхронизации

---

## Ключевые зависимости API

```
Авторизация → Всё остальное
Питомцы → Поиск (нужен pet_id для рекомендаций)
Питомцы → Мэтчинг (нужны pet_from/pet_to)
Мэтчинг → Чаты (нужен ACCEPTED match)
Чаты → WebSocket (нужен match + токен)
```

---

## Файловая структура (целевая)

```
src/
├── api/
│   └── index.js                 (Axios instance + все API-функции)
├── assets/
│   └── styles/
│       ├── variables.css
│       └── global.css
├── components/
│   ├── layout/
│   │   ├── AppLayout.vue
│   │   └── Sidebar.vue
│   ├── ui/
│   │   ├── UiButton.vue
│   │   ├── UiInput.vue
│   │   ├── UiSelect.vue
│   │   ├── UiTextarea.vue
│   │   ├── UiToggle.vue
│   │   ├── UiAvatar.vue
│   │   ├── UiCard.vue
│   │   ├── UiBadge.vue
│   │   ├── UiModal.vue
│   │   ├── UiToast.vue
│   │   ├── UiLoader.vue
│   │   ├── UiEmptyState.vue
│   │   ├── UiFileUpload.vue
│   │   └── UiMap.vue
│   ├── pets/
│   │   └── PetCard.vue
│   ├── search/
│   │   ├── RecommendationCard.vue
│   │   └── FilterModal.vue
│   └── chats/
│       ├── ChatListItem.vue
│       ├── MessageBubble.vue
│       └── MessageInput.vue
├── composables/
│   ├── useWebSocket.js
│   └── useGeolocation.js
├── router/
│   └── index.js
├── stores/
│   ├── auth.js
│   ├── user.js
│   ├── pets.js
│   ├── animalTypes.js
│   ├── search.js
│   ├── matches.js
│   ├── chats.js
│   ├── messages.js
│   └── moderation.js
├── views/
│   ├── LoginView.vue
│   ├── RegisterView.vue
│   ├── HomeView.vue
│   ├── SearchView.vue
│   ├── ChatsView.vue
│   ├── MessagesView.vue
│   ├── PetsView.vue
│   ├── PetEditView.vue
│   ├── PetDetailView.vue
│   ├── ProfileView.vue
│   ├── EditProfileView.vue
│   ├── UserProfileView.vue
│   ├── RequestsView.vue
│   ├── SettingsView.vue
│   ├── ModerationView.vue
│   └── NotFoundView.vue
├── App.vue
└── main.js
```
