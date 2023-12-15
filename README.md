# Effective_Mobile_test

1. Скопировать проект к себе
2. В cmd перейти в корневвую директорию проекта
3. В cmd выполнить команду
   docker-compose up
4. После запуска БД будет пустая, для тестирования можно отправить POST запрос на http://localhost:8080/registration
5. Для остальных запросов (кроме /login) нужно передавать заголовок Authorization = Bearer token
6. Документация доступна по адресу http://localhost:8080/swagger-ui/index.html

Пример json 
для /registration или /login 
{
    "email":"user@mail.ru",
    "password":"123"
}

для создания задачи: 
{
    "title":"task 1",
    "description":"desc",
    "priority":"1",
    "executor":{"email":"user@mail.ru"}
}

для добавления комментария:
{
    "message":"comment 1",
    "taskId":"1"
}
