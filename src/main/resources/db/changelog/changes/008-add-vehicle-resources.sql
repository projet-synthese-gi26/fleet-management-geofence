--liquibase formatted sql
--changeset gabriel:add-vehicle-resource-tables

CREATE TABLE IF NOT EXISTS fleet.fuel_types (
    id UUID PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    label VARCHAR(100) NOT NULL,
    description TEXT
);

CREATE TABLE IF NOT EXISTS fleet.manufacturers (
    id UUID PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    label VARCHAR(100) NOT NULL,
    description TEXT
);

-- Index pour les recherches rapides lors des lookups
CREATE INDEX idx_fuel_code ON fleet.fuel_types(code);
CREATE INDEX idx_mfr_code ON fleet.manufacturers(code);