# Yandex Disk API Autotests

Пример проекта автотестов для REST API Яндекс.Диска, выполненный в рамках тестового задания на позицию стажёра  
**«Инженер по автоматизации тестирования (Финтех)»**.

---

## 🔧 Стек
- Java 11
- JUnit 4
- RestAssured
- Maven
- Allure
- Lombok

---

## 📚 Тестируемый сервис
- API Яндекс.Диска: https://yandex.ru/dev/disk/rest/
- Base URL: `https://cloud-api.yandex.net`

---

## 🧪 Покрытие тестами
Тесты проверяют работу основных методов API:

- **PUT /resources** — создание папки
- **GET /resources** — получение метаданных
- **POST /resources/copy** — копирование папки
- **DELETE /resources** — удаление ресурса
- Негативные сценарии (401, 404)

Для асинхронных операций (202 Accepted) реализовано ожидание завершения по `href`.

---

## ▶️ Запуск тестов

### 1. Задать OAuth-токен через терминал
### 2. Запустить тесты
```bash
export YANDEX_OAUTH=your_oauth_token 
mvn clean test