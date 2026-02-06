-- liquibase formatted sql

-- changeset yowyob:003-create-notification-settings
-- comment: Table pour stocker les préférences de notification des utilisateurs

CREATE TABLE IF NOT EXISTS fleet.notification_settings (
    user_id UUID PRIMARY KEY,
    enable_email BOOLEAN DEFAULT TRUE,
    enable_sms BOOLEAN DEFAULT FALSE,
    enable_push BOOLEAN DEFAULT TRUE,
    enable_whatsapp BOOLEAN DEFAULT FALSE,
    updated_at TIMESTAMP DEFAULT NOW(),
    CONSTRAINT fk_notif_settings_user FOREIGN KEY (user_id) REFERENCES fleet.users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS fleet.notifications (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    title VARCHAR(255),
    message TEXT,
    type VARCHAR(50), -- INFO, WARNING, SUCCESS
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW(),
    data TEXT, 
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES fleet.users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_notif_user_date ON fleet.notifications(user_id, created_at DESC);