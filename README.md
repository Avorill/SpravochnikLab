# Лабораторная работа №2 
## Справочники в PostGRES
Файл создания таблиц для быза данных лежит в файйлу init.sql.
## Как запустить программу
1. Создать PostgreSQL базу с названием labs, зайти в нее и запустить в ней скрипт init.sql
2. запустить файл Spravochnik-1.0-SNAPSHOT.jar через консоль командой java -jar Spravochnik-1.0-SNAPSHOT.jar из папке в которой хранится этот файл.

## Как работает программа
При запуске попросят ввести логин и пароль вашей базы данных с созданными данными по скрипту

![Image alt](https://raw.githubusercontent.com/P0ZiT1V/SpravochnikLab/master/screen/login.png)
![Image alt](https://raw.githubusercontent.com/P0ZiT1V/SpravochnikLab/master/screen/pass.png)

Затем открывается поле для выбора первой таблицы, которая будет открыты

![Image alt](https://raw.githubusercontent.com/P0ZiT1V/SpravochnikLab/master/screen/choose.png)

После выбора мы переходим к нужной нам таблице, так выглядят таблицы после их открытия  :
![Image alt](https://raw.githubusercontent.com/P0ZiT1V/SpravochnikLab/master/screen/cities.png)
![Image alt](https://raw.githubusercontent.com/P0ZiT1V/SpravochnikLab/master/screen/department.png)
![Image alt](https://raw.githubusercontent.com/P0ZiT1V/SpravochnikLab/master/screen/employees.png)

Пройдемся по заданиям:
###  Выбирать справочник из списка

Есть кнопка октрыть таблицу после нажатия на которую выскакивает окно в котором находится выпадающий список для выбора таблицы
![Image alt](https://raw.githubusercontent.com/P0ZiT1V/SpravochnikLab/master/screen/chooseAnotherTable.png)
###   Отображать выбранный справочник в виде таблички. Обеспечить сортировку по колонкам
Для сотировки надо нажать на заголовок таблицы. Вот как это выглядит
![Image alt](https://raw.githubusercontent.com/P0ZiT1V/SpravochnikLab/master/screen/sort1.png)
![Image alt](https://raw.githubusercontent.com/P0ZiT1V/SpravochnikLab/master/screen/sort2.png)
![Image alt](https://raw.githubusercontent.com/P0ZiT1V/SpravochnikLab/master/screen/sort3.png)
![Image alt](https://raw.githubusercontent.com/P0ZiT1V/SpravochnikLab/master/screen/sort4.png)

###   Добавлять значения справочника
Для добавления нового значения справочника необходимо нажать кнопку добавить, тогда появляется окно для заполнения новой информации.
Для заполнения дат предусмотрен календарик. Для заполнения столбца, зависящего от другой таблицы предусмотрен выпадающий список.
![Image alt](https://raw.githubusercontent.com/P0ZiT1V/SpravochnikLab/master/screen/add.png)
![Image alt](https://raw.githubusercontent.com/P0ZiT1V/SpravochnikLab/master/screen/choosefromanothertable.png)
![Image alt](https://raw.githubusercontent.com/P0ZiT1V/SpravochnikLab/master/screen/calendar.png)


После заполнения строка появляется в таблице. Поле для ввода меняет неправильную дату на  правильную, так что ввести несуществующую дату нельзя


###   Удалять значения справочника
При нажатии на кнопку "удалить" выделенная строка удаляется.
![Image alt](https://raw.githubusercontent.com/P0ZiT1V/SpravochnikLab/master/screen/delete1.png)
![Image alt](https://raw.githubusercontent.com/P0ZiT1V/SpravochnikLab/master/screen/afterDelete.png)


###   Редактировать значения справочника
Для редактирования надо выделить строку, нажать на кнопку изменить (изменение посредством ввода чего-то нового в поле не работает)
Кнопка " отменить выделение" существует на случай, если выделил то, что не хотел.
![Image alt](https://raw.githubusercontent.com/P0ZiT1V/SpravochnikLab/master/screen/update.png)






