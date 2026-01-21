--liquibase formatted sql

--changeset gabriel:mock-structure-only context:local

-- 1. Structure PUBLIC (Simulation Auth Service pour les FKs)
CREATE TABLE IF NOT EXISTS public.users (
    id UUID PRIMARY KEY,
    name TEXT,
    email TEXT,
    password TEXT
);

CREATE TABLE IF NOT EXISTS public.roles (
    name VARCHAR(50) PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS public.user_has_roles (
    user_id UUID REFERENCES public.users(id),
    role_name VARCHAR(50) REFERENCES public.roles(name),
    PRIMARY KEY (user_id, role_name)
);

-- 2. Insertion des Rôles (Standards TransEns)
INSERT INTO public.roles (name) VALUES 
('FLEET_ADMIN'), 
('FLEET_MANAGER'), 
('FLEET_DRIVER')
ON CONFLICT DO NOTHING;

-- 3. Insertion des Types de Véhicules (OBLIGATOIRE pour que l'app fonctionne)
INSERT INTO fleet.vehicle_types (id, code, label, description) VALUES 
('11111111-1111-1111-1111-111111111111', 'CAR', 'Voiture', 'Véhicule léger de tourisme'),
('22222222-2222-2222-2222-222222222222', 'TRUCK', 'Camion', 'Poids lourd pour logistique'),
('33333333-3333-3333-3333-333333333333', 'MOTO', 'Moto', 'Deux roues pour livraison rapide'),
('44444444-4444-4444-4444-444444444444', 'VAN', 'Fourgon', 'Transport de matériel')
ON CONFLICT DO NOTHING;

-- FIN : Aucune donnée de flotte ou d'utilisateur n'est insérée.