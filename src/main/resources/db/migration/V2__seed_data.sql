-- ============================================================
-- LunaLink - V2: Seed Data
-- ============================================================

-- Users (UUIDs fixos para reprodutibilidade)
-- Senha de todos os usuarios: senha123
INSERT INTO public.users (id, dtype, apartment, email, name, password, role, token_version) VALUES
    ('a1b2c3d4-e5f6-7890-abcd-ef1234567801', 'USER', '101A', 'morador1@email.com', 'Morador1', '$2a$10$BFLGM8yPeS10lsFDP6BUuuibcnSklSSzmzEyCv9JaXEaLPRLNvTKS', 'RESIDENT_ROLE', 0),
    ('a1b2c3d4-e5f6-7890-abcd-ef1234567802', 'USER', '202B', 'adm1@email.com', 'Administrador 1', '$2a$10$BFLGM8yPeS10lsFDP6BUuuibcnSklSSzmzEyCv9JaXEaLPRLNvTKS', 'ADMIN_ROLE', 0),
    ('a1b2c3d4-e5f6-7890-abcd-ef1234567803', 'USER', '303C', 'teste@email.com', 'Teste', '$2a$10$BFLGM8yPeS10lsFDP6BUuuibcnSklSSzmzEyCv9JaXEaLPRLNvTKS', 'RESIDENT_ROLE', 0),
    ('a1b2c3d4-e5f6-7890-abcd-ef1234567804', 'USER', '404D', 'teste2@email.com', 'Teste 2', '$2a$10$BFLGM8yPeS10lsFDP6BUuuibcnSklSSzmzEyCv9JaXEaLPRLNvTKS', 'RESIDENT_ROLE', 0),
    ('a1b2c3d4-e5f6-7890-abcd-ef1234567805', 'USER', '505E', 'zaira@email.com', 'Zaira', '$2a$10$BFLGM8yPeS10lsFDP6BUuuibcnSklSSzmzEyCv9JaXEaLPRLNvTKS', 'RESIDENT_ROLE', 0),
    ('a1b2c3d4-e5f6-7890-abcd-ef1234567806', 'USER', '606F', 'bruno@email.com', 'Bruno', '$2a$10$BFLGM8yPeS10lsFDP6BUuuibcnSklSSzmzEyCv9JaXEaLPRLNvTKS', 'ADMIN_ROLE', 0),
    ('a1b2c3d4-e5f6-7890-abcd-ef1234567807', 'USER', '707G', 'bruno.case@email.com', 'Bruno Case', '$2a$10$BFLGM8yPeS10lsFDP6BUuuibcnSklSSzmzEyCv9JaXEaLPRLNvTKS', 'EMPLOYEE', 0);

-- Spaces
INSERT INTO public.space (id, type) VALUES
    (1, 'SALAO_FESTAS'),
    (2, 'CHURRASQUEIRA'),
    (3, 'ACADEMIA'),
    (4, 'CAMPO_FUTEBOL');

-- Equipment
INSERT INTO public.equipment (id, name, active) VALUES
    (1, 'Televisão Comunitária', true);

-- Reservations (com UUIDs e user_id)
INSERT INTO public.reservation (id, date, user_id, space_id, status) VALUES
    ('b1b2c3d4-e5f6-7890-abcd-ef1234567801', '2025-09-13', 'a1b2c3d4-e5f6-7890-abcd-ef1234567801', 1, 'CONFIRMED'),
    ('b1b2c3d4-e5f6-7890-abcd-ef1234567802', '2024-05-02', 'a1b2c3d4-e5f6-7890-abcd-ef1234567801', 2, 'CONFIRMED'),
    ('b1b2c3d4-e5f6-7890-abcd-ef1234567803', '2024-05-10', 'a1b2c3d4-e5f6-7890-abcd-ef1234567801', 2, 'CONFIRMED'),
    ('b1b2c3d4-e5f6-7890-abcd-ef1234567804', '2024-08-10', 'a1b2c3d4-e5f6-7890-abcd-ef1234567801', 1, 'CONFIRMED'),
    ('b1b2c3d4-e5f6-7890-abcd-ef1234567805', '2024-05-11', 'a1b2c3d4-e5f6-7890-abcd-ef1234567803', 1, 'CONFIRMED'),
    ('b1b2c3d4-e5f6-7890-abcd-ef1234567806', '2025-08-28', 'a1b2c3d4-e5f6-7890-abcd-ef1234567804', 1, 'CONFIRMED'),
    ('b1b2c3d4-e5f6-7890-abcd-ef1234567807', '2025-08-29', 'a1b2c3d4-e5f6-7890-abcd-ef1234567804', 1, 'CONFIRMED'),
    ('b1b2c3d4-e5f6-7890-abcd-ef1234567808', '2025-08-30', 'a1b2c3d4-e5f6-7890-abcd-ef1234567804', 1, 'CONFIRMED'),
    ('b1b2c3d4-e5f6-7890-abcd-ef1234567809', '2025-09-04', 'a1b2c3d4-e5f6-7890-abcd-ef1234567805', 1, 'CONFIRMED'),
    ('b1b2c3d4-e5f6-7890-abcd-ef1234567810', '2025-09-20', 'a1b2c3d4-e5f6-7890-abcd-ef1234567801', 2, 'CONFIRMED'),
    ('b1b2c3d4-e5f6-7890-abcd-ef1234567811', '2026-10-01', 'a1b2c3d4-e5f6-7890-abcd-ef1234567801', 1, 'PENDING'),
    ('b1b2c3d4-e5f6-7890-abcd-ef1234567812', '2026-10-02', 'a1b2c3d4-e5f6-7890-abcd-ef1234567801', 1, 'PENDING'),
    ('b1b2c3d4-e5f6-7890-abcd-ef1234567813', '2026-12-21', 'a1b2c3d4-e5f6-7890-abcd-ef1234567805', 2, 'PENDING'),
    ('b1b2c3d4-e5f6-7890-abcd-ef1234567814', '2026-12-22', 'a1b2c3d4-e5f6-7890-abcd-ef1234567805', 2, 'PENDING');

-- Sequências
SELECT pg_catalog.setval('public.space_id_seq', 4, true);
SELECT pg_catalog.setval('public.equipment_id_seq', 1, true);
