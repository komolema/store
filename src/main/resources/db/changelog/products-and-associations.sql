-- Insert sample products
INSERT INTO product (id, description) VALUES (1, 'Laptop Computer');
INSERT INTO product (id, description) VALUES (2, 'Wireless Mouse');
INSERT INTO product (id, description) VALUES (3, 'USB-C Cable');
INSERT INTO product (id, description) VALUES (4, 'Monitor 27 inch');
INSERT INTO product (id, description) VALUES (5, 'Mechanical Keyboard');
INSERT INTO product (id, description) VALUES (6, 'Webcam HD');
INSERT INTO product (id, description) VALUES (7, 'USB Hub');
INSERT INTO product (id, description) VALUES (8, 'Desk Lamp');
INSERT INTO product (id, description) VALUES (9, 'Ergonomic Chair');
INSERT INTO product (id, description) VALUES (10, 'Phone Stand');

-- Insert sample order-product associations
-- Order 1 gets products 1, 2, 3 (Laptop + Mouse + Cable)
INSERT INTO order_product (order_id, product_id) VALUES (1, 1);
INSERT INTO order_product (order_id, product_id) VALUES (1, 2);
INSERT INTO order_product (order_id, product_id) VALUES (1, 3);

-- Order 2 gets products 4, 5 (Monitor + Keyboard)
INSERT INTO order_product (order_id, product_id) VALUES (2, 4);
INSERT INTO order_product (order_id, product_id) VALUES (2, 5);

-- Order 3 gets products 1, 6, 7 (Laptop + Webcam + USB Hub)
INSERT INTO order_product (order_id, product_id) VALUES (3, 1);
INSERT INTO order_product (order_id, product_id) VALUES (3, 6);
INSERT INTO order_product (order_id, product_id) VALUES (3, 7);

-- Order 4 gets products 8, 9, 10 (Lamp + Chair + Phone Stand)
INSERT INTO order_product (order_id, product_id) VALUES (4, 8);
INSERT INTO order_product (order_id, product_id) VALUES (4, 9);
INSERT INTO order_product (order_id, product_id) VALUES (4, 10);

-- Order 5 gets products 2, 3, 5 (Mouse + Cable + Keyboard)
INSERT INTO order_product (order_id, product_id) VALUES (5, 2);
INSERT INTO order_product (order_id, product_id) VALUES (5, 3);
INSERT INTO order_product (order_id, product_id) VALUES (5, 5);

