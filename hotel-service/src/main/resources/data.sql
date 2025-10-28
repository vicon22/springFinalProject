-- Тестовые отели и номера для hotel-service
INSERT INTO hotels (id, name, address) VALUES (1, 'Grand Hotel', '123 Main Street, New York');
INSERT INTO hotels (id, name, address) VALUES (2, 'Luxury Resort', '456 Ocean Drive, Miami');
INSERT INTO hotels (id, name, address) VALUES (3, 'Business Hotel', '789 Corporate Plaza, Chicago');

INSERT INTO rooms (id, hotel_id, number, available, times_booked) VALUES (1, 1, '101', true, 0);
INSERT INTO rooms (id, hotel_id, number, available, times_booked) VALUES (2, 1, '102', true, 0);
INSERT INTO rooms (id, hotel_id, number, available, times_booked) VALUES (3, 1, '201', true, 0);
INSERT INTO rooms (id, hotel_id, number, available, times_booked) VALUES (4, 2, '301', true, 0);
INSERT INTO rooms (id, hotel_id, number, available, times_booked) VALUES (5, 2, '302', true, 0);
INSERT INTO rooms (id, hotel_id, number, available, times_booked) VALUES (6, 3, '401', true, 0);
