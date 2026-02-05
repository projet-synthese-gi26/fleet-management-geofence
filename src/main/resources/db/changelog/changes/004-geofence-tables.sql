--liquibase formatted sql

--changeset hassana:upgrade-geofence-zones-v2 splitStatements:true
-- On aligne la table locale sur les besoins de l'entité Java
ALTER TABLE fleet.geofence_zones 
ADD COLUMN IF NOT EXISTS zone_type VARCHAR(20) DEFAULT 'CIRCLE',
ADD COLUMN IF NOT EXISTS center_latitude NUMERIC,
ADD COLUMN IF NOT EXISTS center_longitude NUMERIC,
ADD COLUMN IF NOT EXISTS is_temporal_enabled BOOLEAN DEFAULT false,
ADD COLUMN IF NOT EXISTS start_time TIME,
ADD COLUMN IF NOT EXISTS end_time TIME,
ADD COLUMN IF NOT EXISTS is_conditional_enabled BOOLEAN DEFAULT false,
ADD COLUMN IF NOT EXISTS max_speed NUMERIC,
ADD COLUMN IF NOT EXISTS max_dwell_time INTEGER,
ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT true;

-- Correction : Le champ 'type' de la migration 002 peut être supprimé ou ignoré au profit de 'zone_type'