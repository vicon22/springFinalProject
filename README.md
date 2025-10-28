# Hotel Booking System

Микросервисная система бронирования отелей на базе Spring Boot с использованием Spring Cloud.

## Архитектура

Система состоит из следующих компонентов:

1. **Eureka Server** (порт 8761) - сервер регистрации и обнаружения сервисов
2. **API Gateway** (порт 8080) - шлюз для маршрутизации запросов
3. **Hotel Service** (порт 8081) - управление отелями и номерами
4. **Booking Service** (порт 8082) - управление бронированиями и пользователями

## Функциональность

### Пользователи (USER)
- Регистрация и авторизация (JWT)
- Просмотр доступных отелей и номеров
- Создание бронирований (с автоподбором или выбором конкретного номера)
- Просмотр истории бронирований
- Отмена бронирований

### Администраторы (ADMIN)
- CRUD операции с отелями, номерами и пользователями
- Просмотр статистики загруженности номеров

## Технологии

- Java 21
- Spring Boot 3.5.7
- Spring Cloud 2025.0.0
- Spring Security + JWT
- Spring Data JPA + H2 (in-memory)
- Spring Cloud Gateway
- Spring Cloud Eureka
- MapStruct для маппинга DTO
- Lombok
- Swagger/OpenAPI

## Запуск системы

### 1. Запуск Eureka Server
```bash
cd eureka-server
./gradlew bootRun
```
Eureka Server будет доступен по адресу: http://localhost:8761

### 2. Запуск Hotel Service
```bash
cd hotel-service
./gradlew bootRun
```
Hotel Service будет доступен по адресу: http://localhost:8081

### 3. Запуск Booking Service
```bash
cd booking-service
./gradlew bootRun
```
Booking Service будет доступен по адресу: http://localhost:8082

### 4. Запуск API Gateway
```bash
cd api-gateway
./gradlew bootRun
```
API Gateway будет доступен по адресу: http://localhost:8080

## API Endpoints

### Аутентификация (Booking Service)
- `POST /api/auth/register` - регистрация пользователя
- `POST /api/auth/login` - авторизация пользователя

### Бронирования (через API Gateway)
- `POST /api/bookings` - создание бронирования
- `GET /api/bookings` - получение списка бронирований пользователя
- `GET /api/bookings/{id}` - получение бронирования по ID
- `DELETE /api/bookings/{id}` - отмена бронирования

### Отели (через API Gateway)
- `GET /api/hotels` - получение списка отелей
- `POST /api/hotels` - создание отеля (ADMIN)
- `GET /api/rooms` - получение доступных номеров
- `GET /api/rooms/recommend` - получение рекомендованных номеров

### Управление пользователями (через API Gateway)
- `GET /api/users` - получение списка пользователей (ADMIN)
- `POST /api/users` - создание пользователя (ADMIN)
- `PUT /api/users/{id}` - обновление пользователя (ADMIN)
- `DELETE /api/users/{id}` - удаление пользователя (ADMIN)

## Двухшаговая согласованность

При создании бронирования используется двухшаговая согласованность:

1. **Шаг 1**: Booking Service создает бронирование в статусе `PENDING`
2. **Шаг 2**: Booking Service запрашивает подтверждение доступности у Hotel Service
3. **Шаг 3**: Hotel Service временно блокирует номер
4. **Шаг 4**: При успешном ответе бронирование переводится в `CONFIRMED`
5. **Шаг 5**: При ошибке выполняется компенсация - бронирование переводится в `CANCELLED`

### Обработка ошибок

- **Тайм-ауты**: 10 секунд для подтверждения доступности
- **Повторы**: 3 попытки с экспоненциальной задержкой
- **Компенсация**: Автоматическая отмена при сбоях
- **Идемпотентность**: Использование requestId для предотвращения дубликатов


## Алгоритм планирования занятости

1. Hotel Service ведет счетчик `times_booked` для каждого номера
2. При автоподборе номера система сортирует доступные номера по возрастанию `times_booked`
3. При равенстве `times_booked` сортировка происходит по ID номера
4. Это обеспечивает равномерную загрузку номеров

## Безопасность

- JWT токены с временем жизни 1 час
- Разграничение ролей USER и ADMIN
- Каждый сервис проверяет JWT самостоятельно
- API Gateway выполняет маршрутизацию

## Swagger документация

После запуска сервисов документация доступна по адресам:
- Hotel Service: http://localhost:8082/swagger-ui/index.html
- Booking Service: http://localhost:8081/swagger-ui/index.html

API документация в JSON формате:
- Hotel Service: http://localhost:8082/v3/api-docs
- Booking Service: http://localhost:8081/v3/api-docs

## H2 Console

In-memory базы данных доступны через H2 Console:
- Booking Service: http://localhost:8081/h2-console
- Hotel Service: http://localhost:8082/h2-console

## Пример использования

# API Examples

## 1. Регистрация пользователя

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "password",
    "role": "ADMIN"
  }'
```

## 2. Авторизация пользователя

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "password"
  }'
```

## 3. Создание отеля (ADMIN)

```bash
curl -X POST http://localhost:8080/api/hotels \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "Test Hotel",
    "address": "123 Test Street"
  }'
```

## 4. Получение списка отелей

```bash
curl -X GET http://localhost:8080/api/hotels \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

```

## 5. Создание номера (ADMIN)

```bash
curl -X POST http://localhost:8080/api/rooms \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "hotelId": 1,
    "number": "101",
    "available": true,
    "timesBooked": 0
  }'
```

## 6. Получение доступных номеров

```bash
curl -X GET http://localhost:8080/api/rooms \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

```

## 7. Получение рекомендованных номеров

```bash
curl -X GET http://localhost:8080/api/rooms/recommend \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

```

## 8. Создание бронирования

```bash
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "roomId": 1,
    "startDate": "2025-10-28T14:00:00",
    "endDate": "2025-10-30T12:00:00",
    "autoSelect": false
  }'
```

## 9. Создание бронирования с автоподбором

```bash
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "startDate": "2024-01-01T14:00:00",
    "endDate": "2024-01-03T12:00:00",
    "autoSelect": true
  }'
```

## 10. Получение бронирований пользователя

```bash
curl -X GET http://localhost:8080/api/bookings \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## 11. Получение бронирования по ID

```bash
curl -X GET http://localhost:8080/api/bookings/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## 12. Отмена бронирования

```bash
curl -X DELETE http://localhost:8080/api/bookings/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## 13. Получение всех пользователей (ADMIN)

```bash
curl -X GET http://localhost:8080/api/users \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## 14. Обновление пользователя (ADMIN)

```bash
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "id": 1,
    "username": "updateduser",
    "role": "USER"
  }'
```

## 15. Удаление пользователя (ADMIN)

```bash
curl -X DELETE http://localhost:8080/api/users/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Тестовые данные

Для тестирования можно использовать следующие тестовые данные:

### Пользователи
- **admin** / **password** (роль: ADMIN)
- **user1** / **password** (роль: USER)
- **user2** / **password** (роль: USER)

### Отели
- Grand Hotel (123 Main Street, New York)
- Luxury Resort (456 Ocean Drive, Miami)
- Business Hotel (789 Corporate Plaza, Chicago)

### Номера
- Отель 1: номера 101, 102, 201
- Отель 2: номера 301, 302
- Отель 3: номер 401

## Порядок тестирования

1. Запустите все сервисы
2. Зарегистрируйте пользователя или используйте тестового
3. Получите JWT токен через авторизацию
4. Создайте отель (как ADMIN)
5. Создайте номера (как ADMIN)
6. Создайте бронирование (как USER)
7. Проверьте статус бронирования
8. Отмените бронирование при необходимости
