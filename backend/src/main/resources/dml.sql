USE customer_db;

INSERT INTO country (name, code) VALUES
('Sri Lanka', 'LK'),
('India',     'IN'),
('Australia', 'AU'),
('United Kingdom', 'GB'),
('United States',  'US'),
('Canada',    'CA'),
('Germany',   'DE'),
('France',    'FR'),
('Japan',     'JP'),
('Singapore', 'SG')
ON DUPLICATE KEY UPDATE name=VALUES(name);

INSERT INTO city (name, country_id) VALUES
('Colombo',      (SELECT id FROM country WHERE code='LK')),
('Kandy',        (SELECT id FROM country WHERE code='LK')),
('Galle',        (SELECT id FROM country WHERE code='LK')),
('Negombo',      (SELECT id FROM country WHERE code='LK')),
('Jaffna',       (SELECT id FROM country WHERE code='LK')),
('Matara',       (SELECT id FROM country WHERE code='LK')),
('Kurunegala',   (SELECT id FROM country WHERE code='LK')),
('Anuradhapura', (SELECT id FROM country WHERE code='LK')),
('Mumbai',    (SELECT id FROM country WHERE code='IN')),
('Delhi',     (SELECT id FROM country WHERE code='IN')),
('Bangalore', (SELECT id FROM country WHERE code='IN')),
('Chennai',   (SELECT id FROM country WHERE code='IN')),
('Sydney',    (SELECT id FROM country WHERE code='AU')),
('Melbourne', (SELECT id FROM country WHERE code='AU')),
('Brisbane',  (SELECT id FROM country WHERE code='AU')),
('London',     (SELECT id FROM country WHERE code='GB')),
('Manchester', (SELECT id FROM country WHERE code='GB')),
('Birmingham', (SELECT id FROM country WHERE code='GB')),
('New York',   (SELECT id FROM country WHERE code='US')),
('Los Angeles',(SELECT id FROM country WHERE code='US')),
('Chicago',    (SELECT id FROM country WHERE code='US'));

INSERT INTO customer (name, date_of_birth, nic_number) VALUES
('Tharu Perera',      '1998-05-15', '199812345678'),
('Nimal Silva',       '1985-08-22', '850823456789'),
('Kamani Fernando',   '1992-03-10', '922234567890'),
('Ruwan Bandara',     '1975-11-30', '751130987654'),
('Dilini Jayawardena','2000-01-20', '200012098765');

INSERT INTO customer_mobile (customer_id, mobile)
SELECT id, '0771234567' FROM customer WHERE nic_number='199812345678';
INSERT INTO customer_mobile (customer_id, mobile)
SELECT id, '0712345678' FROM customer WHERE nic_number='199812345678';
INSERT INTO customer_mobile (customer_id, mobile)
SELECT id, '0769876543' FROM customer WHERE nic_number='850823456789';

INSERT INTO customer_address (customer_id, address_line1, address_line2, city_id, country_id)
SELECT c.id, '123 Main Street', 'Apartment 4B',
       (SELECT id FROM city WHERE name='Colombo' LIMIT 1),
       (SELECT id FROM country WHERE code='LK')
FROM customer c WHERE c.nic_number='199812345678';

INSERT INTO customer_address (customer_id, address_line1, address_line2, city_id, country_id)
SELECT c.id, '45 Temple Road', NULL,
       (SELECT id FROM city WHERE name='Kandy' LIMIT 1),
       (SELECT id FROM country WHERE code='LK')
FROM customer c WHERE c.nic_number='850823456789';

INSERT INTO customer_family (customer_id, family_member_id)
SELECT c1.id, c2.id FROM customer c1, customer c2
WHERE c1.nic_number='199812345678' AND c2.nic_number='850823456789';

INSERT INTO customer_family (customer_id, family_member_id)
SELECT c1.id, c2.id FROM customer c1, customer c2
WHERE c1.nic_number='850823456789' AND c2.nic_number='199812345678';