# Решение к домашнему заданию «1.1. HTTP и современный Web»

Код был отрефакторен - выделен класс Server с методами и реализована обработка подключений с помощью ThreadPool на 64 потока.

Это реализация новой функциональности в ветке feature/query. 
Сервер стал расширяемым благодаря добавлению обработчиков на определённые шаблоны путей, а также получаем параметры из Query String.