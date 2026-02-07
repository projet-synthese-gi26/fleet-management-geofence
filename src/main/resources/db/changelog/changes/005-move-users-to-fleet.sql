--liquibase formatted sql

--changeset gabriel:move-users-to-fleet-schema-v4 splitStatements:false
--comment: Migration robuste vers fleet.users avec création forcée du schéma et import conditionnel

-- 1. GARANTIR L'EXISTENCE DU SCHÉMA (Correction du crash Prod)
CREATE SCHEMA IF NOT EXISTS fleet;

-- 2. CRÉATION DE LA TABLE SOUVERAINE
CREATE TABLE IF NOT EXISTS fleet.users (
    id UUID PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    photo_url VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    last_login_at TIMESTAMP,
    deleted_at TIMESTAMP
);


-- 3. BLOC PROCÉDURAL POUR LA MIGRATION CONDITIONNELLE
DO $$ 
BEGIN 
    -- A. Vérifier si la table source public.users existe vraiment
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'users') THEN
        
        RAISE NOTICE 'Table public.users détectée, tentative de migration des données...';

        -- Gestion dynamique du nom de la colonne email (email vs email_address)
        IF EXISTS (SELECT FROM information_schema.columns 
                   WHERE table_schema = 'public' AND table_name = 'users' AND column_name = 'email_address') THEN
            
            EXECUTE 'INSERT INTO fleet.users (id, username, email)
                     SELECT id, COALESCE(name, ''user_'' || id), email_address FROM public.users
                     ON CONFLICT (id) DO NOTHING';
                     
        ELSIF EXISTS (SELECT FROM information_schema.columns 
                      WHERE table_schema = 'public' AND table_name = 'users' AND column_name = 'email') THEN
                      
            EXECUTE 'INSERT INTO fleet.users (id, username, email)
                     SELECT id, COALESCE(name, ''user_'' || id), email FROM public.users
                     ON CONFLICT (id) DO NOTHING';
        END IF;
    ELSE
        RAISE NOTICE 'Table public.users absente, création de fleet.users à vide (Normal pour une fresh install).';
    END IF;

    -- B. Redirection des contraintes pour fleet_managers (si la table existe)
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_schema = 'fleet' AND table_name = 'fleet_managers') THEN
        -- On tente de supprimer l'ancienne contrainte si elle existe pour éviter les doublons
        BEGIN
            ALTER TABLE fleet.fleet_managers DROP CONSTRAINT IF EXISTS fk_manager_user_fleet;
        EXCEPTION WHEN OTHERS THEN 
            NULL; 
        END;
        ALTER TABLE fleet.fleet_managers ADD CONSTRAINT fk_manager_user_fleet FOREIGN KEY (user_id) REFERENCES fleet.users(id) ON DELETE CASCADE;
    END IF;

    -- C. Redirection des contraintes pour drivers (si la table existe)
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_schema = 'fleet' AND table_name = 'drivers') THEN
        BEGIN
            ALTER TABLE fleet.drivers DROP CONSTRAINT IF EXISTS fk_driver_user_fleet;
        EXCEPTION WHEN OTHERS THEN 
            NULL; 
        END;
        ALTER TABLE fleet.drivers ADD CONSTRAINT fk_driver_user_fleet FOREIGN KEY (user_id) REFERENCES fleet.users(id) ON DELETE CASCADE;
    END IF;

END $$;