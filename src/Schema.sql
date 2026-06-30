CREATE DATABASE IF NOT EXISTS student_db;

USE student_db;

DROP TABLE IF EXISTS students;

CREATE TABLE students (
    id INT PRIMARY KEY,
    name VARCHAR(100),
    age INT,
    email VARCHAR(100),
    course VARCHAR(100),
    department VARCHAR(100)
);