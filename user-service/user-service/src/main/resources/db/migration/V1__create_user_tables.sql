-- Users table
CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    phone_number VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    alternate_phone VARCHAR(20),
    role VARCHAR(50) NOT NULL,
    profile_picture VARCHAR(500),

    -- Preferences
    sms_notifications BOOLEAN DEFAULT true,
    email_notifications BOOLEAN DEFAULT true,
    push_notifications BOOLEAN DEFAULT true,
    preferred_language VARCHAR(10) DEFAULT 'en',

    -- Statistics
    total_bookings INT DEFAULT 0,
    total_spent DECIMAL(10, 2) DEFAULT 0,
    average_rating DECIMAL(3, 2) DEFAULT 0,

    -- Status
    is_active BOOLEAN DEFAULT true,
    is_email_verified BOOLEAN DEFAULT false,
    is_phone_verified BOOLEAN DEFAULT true,
    is_profile_complete BOOLEAN DEFAULT false,

    -- Security
    failed_attempts INT DEFAULT 0,
    locked_until TIMESTAMP,

    -- Timestamps
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    last_login_at TIMESTAMP,
    last_login_ip VARCHAR(45),

    INDEX idx_users_phone (phone_number),
    INDEX idx_users_email (email),
    INDEX idx_users_role (role),
    INDEX idx_users_created (created_at)
);

-- Addresses table
CREATE TABLE addresses (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    country VARCHAR(50) NOT NULL,
    address_type VARCHAR(50) NOT NULL, -- HOME, WORK, OTHER
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    landmark VARCHAR(255),
    is_default BOOLEAN DEFAULT false,
    contact_name VARCHAR(255),
    contact_phone VARCHAR(20),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    INDEX idx_addresses_user (user_id),
    INDEX idx_addresses_default (user_id, is_default),
    INDEX idx_addresses_location (latitude, longitude)
);

-- Vehicles table
CREATE TABLE vehicles (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    vehicle_type VARCHAR(20) NOT NULL, -- BIKE, CAR, SUV, etc.
    make VARCHAR(100) NOT NULL,
    model VARCHAR(100) NOT NULL,
    year VARCHAR(4) NOT NULL,
    registration_number VARCHAR(50) NOT NULL UNIQUE,
    color VARCHAR(50),
    is_default BOOLEAN DEFAULT false,
    fuel_type VARCHAR(50), -- PETROL, DIESEL, ELECTRIC, HYBRID
    transmission_type VARCHAR(50), -- MANUAL, AUTOMATIC
    engine_capacity INT, -- in CC
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    INDEX idx_vehicles_user (user_id),
    INDEX idx_vehicles_registration (registration_number),
    INDEX idx_vehicles_default (user_id, is_default)
);

-- User devices for push notifications
CREATE TABLE user_devices (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    device_id VARCHAR(255) NOT NULL,
    device_type VARCHAR(50) NOT NULL, -- ANDROID, IOS, WEB
    fcm_token VARCHAR(500),
    is_active BOOLEAN DEFAULT true,
    last_used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    UNIQUE KEY uk_user_device (user_id, device_id),
    INDEX idx_devices_token (fcm_token)
);

-- User audit logs
CREATE TABLE user_audit_logs (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    action VARCHAR(100) NOT NULL, -- LOGIN, LOGOUT, PROFILE_UPDATE, etc.
    ip_address VARCHAR(45),
    user_agent TEXT,
    details JSONB,
    created_at TIMESTAMP NOT NULL,

    INDEX idx_audit_user (user_id),
    INDEX idx_audit_created (created_at)
);

-- User preferences
CREATE TABLE user_preferences (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    notification_email BOOLEAN DEFAULT true,
    notification_sms BOOLEAN DEFAULT true,
    notification_push BOOLEAN DEFAULT true,
    marketing_emails BOOLEAN DEFAULT false,
    language VARCHAR(10) DEFAULT 'en',
    currency VARCHAR(3) DEFAULT 'INR',
    timezone VARCHAR(50) DEFAULT 'Asia/Kolkata',
    theme VARCHAR(20) DEFAULT 'light',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- User OTP history (for audit)
CREATE TABLE user_otp_history (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    otp_type VARCHAR(50) NOT NULL, -- LOGIN, REGISTRATION, PASSWORD_RESET
    otp VARCHAR(6) NOT NULL,
    is_used BOOLEAN DEFAULT false,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,

    INDEX idx_otp_user (user_id),
    INDEX idx_otp_expires (expires_at)
);

-- User sessions
CREATE TABLE user_sessions (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    session_token VARCHAR(500) NOT NULL UNIQUE,
    device_id VARCHAR(255),
    ip_address VARCHAR(45),
    user_agent TEXT,
    expires_at TIMESTAMP NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL,

    INDEX idx_sessions_user (user_id),
    INDEX idx_sessions_token (session_token),
    INDEX idx_sessions_expires (expires_at)
);

-- User verification tokens
CREATE TABLE verification_tokens (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_type VARCHAR(50) NOT NULL, -- EMAIL_VERIFICATION, PASSWORD_RESET
    token VARCHAR(500) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    is_used BOOLEAN DEFAULT false,
    used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,

    INDEX idx_verification_user (user_id),
    INDEX idx_verification_token (token),
    INDEX idx_verification_expires (expires_at)
);

-- Insert default data if needed
-- Example: Create admin user (password would be hashed in real implementation)
-- INSERT INTO users (id, phone_number, email, full_name, role, created_at, updated_at)
-- VALUES (UUID(), '+919876543210', 'admin@vehicle.com', 'System Admin', 'ADMIN', NOW(), NOW());

-- Add triggers for updated_at timestamps
DELIMITER $$
CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    SET NEW.updated_at = NOW();
$$

CREATE TRIGGER update_addresses_updated_at
    BEFORE UPDATE ON addresses
    FOR EACH ROW
    SET NEW.updated_at = NOW();
$$

CREATE TRIGGER update_vehicles_updated_at
    BEFORE UPDATE ON vehicles
    FOR EACH ROW
    SET NEW.updated_at = NOW();
$$

CREATE TRIGGER update_user_devices_updated_at
    BEFORE UPDATE ON user_devices
    FOR EACH ROW
    SET NEW.updated_at = NOW();
$$

CREATE TRIGGER update_user_preferences_updated_at
    BEFORE UPDATE ON user_preferences
    FOR EACH ROW
    SET NEW.updated_at = NOW();
$$
DELIMITER ;