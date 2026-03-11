-- Admin actions log
CREATE TABLE admin_actions (
    id VARCHAR(36) PRIMARY KEY,
    admin_id VARCHAR(36) NOT NULL,
    action_type VARCHAR(100) NOT NULL, -- APPROVE_PROVIDER, SUSPEND_USER, UPDATE_CONFIG, etc.
    target_type VARCHAR(50), -- PROVIDER, USER, BOOKING, PAYMENT, CONFIG
    target_id VARCHAR(36),
    before_state JSONB,
    after_state JSONB,
    reason TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP NOT NULL,
    INDEX idx_admin_actions_admin (admin_id),
    INDEX idx_admin_actions_target (target_type, target_id),
    INDEX idx_admin_actions_created (created_at)
);

-- Audit logs (system-wide)
CREATE TABLE audit_logs (
    id VARCHAR(36) PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    service_name VARCHAR(100),
    user_id VARCHAR(36),
    user_type VARCHAR(50),
    resource_type VARCHAR(50),
    resource_id VARCHAR(36),
    action VARCHAR(100),
    request_payload JSONB,
    response_payload JSONB,
    http_method VARCHAR(10),
    http_path VARCHAR(500),
    http_status INT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    duration_ms INT,
    success BOOLEAN,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL,
    INDEX idx_audit_logs_event (event_type),
    INDEX idx_audit_logs_user (user_id),
    INDEX idx_audit_logs_resource (resource_type, resource_id),
    INDEX idx_audit_logs_created (created_at)
);

-- System configuration
CREATE TABLE system_config (
    id VARCHAR(36) PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value TEXT,
    config_type VARCHAR(50), -- STRING, INTEGER, BOOLEAN, JSON
    description VARCHAR(500),
    is_encrypted BOOLEAN DEFAULT false,
    updated_by VARCHAR(36),
    updated_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL
);

-- Commission configuration
CREATE TABLE commission_config (
    id VARCHAR(36) PRIMARY KEY,
    provider_type VARCHAR(50) NOT NULL UNIQUE, -- SERVICE_CENTER, WASHING_CENTER
    commission_percentage DECIMAL(5, 2) NOT NULL,
    min_commission DECIMAL(10, 2),
    max_commission DECIMAL(10, 2),
    effective_from DATE NOT NULL,
    effective_to DATE,
    is_active BOOLEAN DEFAULT true,
    updated_by VARCHAR(36),
    updated_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL
);

-- Service configuration (for dynamic service catalog)
CREATE TABLE service_config (
    id VARCHAR(36) PRIMARY KEY,
    service_type VARCHAR(50) NOT NULL UNIQUE,
    base_price DECIMAL(10, 2),
    price_multiplier DECIMAL(5, 2) DEFAULT 1.0,
    is_active BOOLEAN DEFAULT true,
    requires_approval BOOLEAN DEFAULT false,
    max_daily_bookings INT,
    min_advance_hours INT,
    updated_by VARCHAR(36),
    updated_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL
);

-- Dashboard cache table
CREATE TABLE dashboard_cache (
    id VARCHAR(36) PRIMARY KEY,
    cache_key VARCHAR(255) NOT NULL UNIQUE,
    cache_data JSONB NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    INDEX idx_dashboard_cache_key (cache_key),
    INDEX idx_dashboard_cache_expires (expires_at)
);

-- Insert default configurations
INSERT INTO system_config (id, config_key, config_value, config_type, description, created_at, updated_at) VALUES
(UUID_GENERATE_V4(), 'booking.expiry.minutes', '30', 'INTEGER', 'Booking expiry time in minutes', NOW(), NOW()),
(UUID_GENERATE_V4(), 'damage.approval.hours', '24', 'INTEGER', 'Damage report approval timeout in hours', NOW(), NOW()),
(UUID_GENERATE_V4(), 'emergency.multiplier.repair', '1.5', 'DECIMAL', 'Emergency repair price multiplier', NOW(), NOW()),
(UUID_GENERATE_V4(), 'emergency.multiplier.petrol', '1.2', 'DECIMAL', 'Petrol emergency price multiplier', NOW(), NOW()),
(UUID_GENERATE_V4(), 'provider.settlement.days', '3', 'INTEGER', 'Provider payout settlement days', NOW(), NOW()),
(UUID_GENERATE_V4(), 'wallet.max.balance', '50000', 'DECIMAL', 'Maximum wallet balance', NOW(), NOW()),
(UUID_GENERATE_V4(), 'wallet.min.topup', '100', 'DECIMAL', 'Minimum wallet topup amount', NOW(), NOW());

INSERT INTO commission_config (id, provider_type, commission_percentage, effective_from, created_at, updated_at) VALUES
(UUID_GENERATE_V4(), 'SERVICE_CENTER', 15.00, CURRENT_DATE, NOW(), NOW()),
(UUID_GENERATE_V4(), 'WASHING_CENTER', 10.00, CURRENT_DATE, NOW(), NOW());