-- liquibase formatted sql

-- changeset yowyob:003-create-notification-settings
-- comment: Table pour stocker les préférences de notification des utilisateurs

CREATE TABLE IF NOT EXISTS notification_settings (
    user_id UUID PRIMARY KEY,
    enable_email BOOLEAN DEFAULT TRUE,
    enable_sms BOOLEAN DEFAULT FALSE,
    enable_push BOOLEAN DEFAULT TRUE,
    enable_whatsapp BOOLEAN DEFAULT FALSE,
    updated_at TIMESTAMP DEFAULT NOW(),
    CONSTRAINT fk_notif_settings_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);


CREATE TABLE IF NOT EXISTS notifications (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    title VARCHAR(255),
    message TEXT,
    type VARCHAR(50), -- INFO, WARNING, SUCCESS
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW(),
    data JSONB, -- Pour stocker les métadonnées (offerId, price, etc.)
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_notif_user_date ON notifications(user_id, created_at DESC);

ALTER TABLE ride_and_go.notifications 
ALTER COLUMN data TYPE TEXT;