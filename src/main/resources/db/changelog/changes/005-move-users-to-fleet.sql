--liquibase formatted sql

--changeset gabriel:move-users-to-fleet-schema-v3 splitStatements:false
--comment: Migration vers fleet.users avec SQL dynamique pour supporter email_address

-- 1. CRÉATION DE LA TABLE SOUVERAINE
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

-- 2. BLOC PROCÉDURAL ROBUSTE
DO $$ 
BEGIN 
    -- Migration des données via SQL Dynamique pour éviter l'erreur de compilation sur la colonne 'email'
    IF EXISTS (SELECT FROM information_schema.columns 
               WHERE table_schema = 'public' 
               AND table_name = 'users' 
               AND column_name = 'email_address') THEN
        
        EXECUTE 'INSERT INTO fleet.users (id, username, email)
                 SELECT id, COALESCE(name, ''user_'' || id), email_address FROM public.users
                 ON CONFLICT (id) DO NOTHING';
                 
    ELSIF EXISTS (SELECT FROM information_schema.columns 
                  WHERE table_schema = 'public' 
                  AND table_name = 'users' 
                  AND column_name = 'email') THEN
                  
        EXECUTE 'INSERT INTO fleet.users (id, username, email)
                 SELECT id, COALESCE(name, ''user_'' || id), email FROM public.users
                 ON CONFLICT (id) DO NOTHING';
    END IF;

    -- Redirection des contraintes pour fleet_managers
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_schema = 'fleet' AND table_name = 'fleet_managers') THEN
        ALTER TABLE fleet.fleet_managers DROP CONSTRAINT IF EXISTS fk_manager_user_fleet;
        ALTER TABLE fleet.fleet_managers ADD CONSTRAINT fk_manager_user_fleet FOREIGN KEY (user_id) REFERENCES fleet.users(id) ON DELETE CASCADE;
    END IF;

    -- Redirection des contraintes pour drivers
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_schema = 'fleet' AND table_name = 'drivers') THEN
        ALTER TABLE fleet.drivers DROP CONSTRAINT IF EXISTS fk_driver_user_fleet;
        ALTER TABLE fleet.drivers ADD CONSTRAINT fk_driver_user_fleet FOREIGN KEY (user_id) REFERENCES fleet.users(id) ON DELETE CASCADE;
    END IF;
END $$;