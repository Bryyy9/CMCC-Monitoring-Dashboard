INSERT INTO services (id, name, url, category, status, last_checked_at, latency_ms, created_at) VALUES
('a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'Payment Gateway', 'https://httpbin.org/status/200', 'Financial', 'UNKNOWN', NULL, NULL, '2026-06-01T00:00:00Z'),
('b2c3d4e5-f6a7-8901-bcde-f12345678901', 'Auth Service', 'https://httpbin.org/status/200', 'Internal', 'UNKNOWN', NULL, NULL, '2026-06-01T00:00:00Z'),
('c3d4e5f6-a7b8-9012-cdef-123456789012', 'CRM API', 'https://httpbin.org/delay/5', 'External', 'UNKNOWN', NULL, NULL, '2026-06-01T00:00:00Z'),
('d4e5f6a7-b8c9-0123-defa-234567890123', 'Notification Service', 'https://httpbin.org/status/500', 'Internal', 'UNKNOWN', NULL, NULL, '2026-06-01T00:00:00Z'),
('e5f6a7b8-c9d0-1234-efab-345678901234', 'Logging Service', 'https://httpbin.org/status/200', 'Infrastructure', 'UNKNOWN', NULL, NULL, '2026-06-01T00:00:00Z');
