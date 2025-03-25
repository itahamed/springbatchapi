CREATE TABLE customers (
    id INT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(20),
    last_name VARCHAR(20),
    email VARCHAR(100),
    phone_number VARCHAR(20)
);

CREATE TABLE products (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100),
    description VARCHAR(500),
    price DECIMAL(10,2),
    stock INTEGER
);

CREATE TABLE customer_products (
    id INT PRIMARY KEY AUTO_INCREMENT,
    customer_id INT,
    product_id INT
);