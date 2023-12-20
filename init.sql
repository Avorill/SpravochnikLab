create table cities
(
    city_id   serial
        primary key,
    city_name varchar(255)
);
create table departments
(
    department_id   serial
        primary key,
    department_name varchar(255),
    city_id         integer
        references cities
            on delete set null
);
create table employees
(
    employee_id   serial
        primary key,
    last_name     varchar(255),
    first_name    varchar(255),
    birth_date    date,
    department_id integer
        references departments
            on delete set null,
    position      varchar(255),
    salary        numeric(10, 2)
);
create table sinonim
(
    id_foreign        varchar,
    id_column_foreign varchar,
    table_name        varchar
);
INSERT INTO cities (city_id, city_name) VALUES (51, 'Barcelona');
INSERT INTO cities (city_id, city_name) VALUES (5, 'Moskow');
INSERT INTO cities (city_id, city_name) VALUES (53, 'Amsterdam');
INSERT INTO cities (city_id, city_name) VALUES (6, 'London2');
INSERT INTO cities (city_id, city_name) VALUES (49, 'New York');
INSERT INTO cities (city_id, city_name) VALUES (47, 'Warshaw');
INSERT INTO cities (city_id, city_name) VALUES (46, 'Wroztlaw');



INSERT INTO sinonim (id_foreign, id_column_foreign, table_name) VALUES ('city_id', 'city_name', 'cities');
INSERT INTO sinonim (id_foreign, id_column_foreign, table_name) VALUES ('employee_id', 'last_name', 'employees');
INSERT INTO sinonim (id_foreign, id_column_foreign, table_name) VALUES ('department_id', 'department_name', 'departments');


INSERT INTO departments (department_id, department_name, city_id) VALUES (4, 'Go', 5);
INSERT INTO departments (department_id, department_name, city_id) VALUES (2, 'IT Department', 5);
INSERT INTO departments (department_id, department_name, city_id) VALUES (1, 'HR Department', 5);
INSERT INTO departments (department_id, department_name, city_id) VALUES (8, 'Art', 6);
INSERT INTO departments (department_id, department_name, city_id) VALUES (15, 'New', 49);
INSERT INTO departments (department_id, department_name, city_id) VALUES (9, 'Economist', 49);
INSERT INTO departments (department_id, department_name, city_id) VALUES (20, 'dqd', 51);

INSERT INTO employees (employee_id, last_name, first_name, birth_date, department_id, position, salary) VALUES (1, 'Smith', 'John', '1990-01-15', 1, 'Manager', 50000.00);
INSERT INTO employees (employee_id, last_name, first_name, birth_date, department_id, position, salary) VALUES (2, 'Johnson', 'Alice', '1985-05-20', 2, 'Developer', 60000.00);
INSERT INTO employees (employee_id, last_name, first_name, birth_date, department_id, position, salary) VALUES (9, 'Mike', 'Jordan', '1900-01-01', 2, 'IT', 400.00);
INSERT INTO employees (employee_id, last_name, first_name, birth_date, department_id, position, salary) VALUES (17, 'Angela', 'Merkel', '2023-01-02', 2, 'Consultant', 50.00);
INSERT INTO employees (employee_id, last_name, first_name, birth_date, department_id, position, salary) VALUES (22, 'Donatello', 'Anton', '2023-12-03', 4, 'Art-Director', 100000.00);
INSERT INTO employees (employee_id, last_name, first_name, birth_date, department_id, position, salary) VALUES (14, 'John', 'Do', '2023-08-06', 4, 'Manager', 4.00);

