-- Notifications table
CREATE TABLE notifications (
    id VARCHAR(36) PRIMARY KEY,
    notification_number VARCHAR(50) NOT NULL UNIQUE,
    user_id VARCHAR(36) NOT NULL,
    user_type VARCHAR(50) NOT NULL, -- CUSTOMER, PROVIDER, EMPLOYEE, ADMIN
    type VARCHAR(50) NOT NULL, -- EMAIL, SMS, PUSH
    channel VARCHAR(50) NOT NULL, -- TRANSACTIONAL, PROMOTIONAL, ALERT
    title VARCHAR(255),
    content TEXT NOT NULL,
    template_name VARCHAR(100),
    template_data JSONB,
    status VARCHAR(50) NOT NULL, -- PENDING, SENT, FAILED, DELIVERED, READ
    priority VARCHAR(20) DEFAULT 'NORMAL', -- HIGH, NORMAL, LOW
    reference_id VARCHAR(36), -- booking_id, payment_id, etc.
    reference_type VARCHAR(50),
    metadata JSONB,
    error_message TEXT,
    retry_count INT DEFAULT 0,
    sent_at TIMESTAMP,
    delivered_at TIMESTAMP,
    read_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    INDEX idx_notifications_user (user_id),
    INDEX idx_notifications_status (status),
    INDEX idx_notifications_reference (reference_id)
);

-- Email logs
CREATE TABLE email_logs (
    id VARCHAR(36) PRIMARY KEY,
    notification_id VARCHAR(36) REFERENCES notifications(id),
    from_email VARCHAR(255) NOT NULL,
    to_email VARCHAR(255) NOT NULL,
    cc_emails TEXT[],
    bcc_emails TEXT[],
    subject VARCHAR(500) NOT NULL,
    html_content TEXT,
    text_content TEXT,
    attachments JSONB,
    provider VARCHAR(50), -- SENDGRID, SMTP
    provider_message_id VARCHAR(255),
    provider_response JSONB,
    status VARCHAR(50),
    error_message TEXT,
    created_at TIMESTAMP NOT NULL,
    INDEX idx_email_logs_notification (notification_id)
);

-- SMS logs
CREATE TABLE sms_logs (
    id VARCHAR(36) PRIMARY KEY,
    notification_id VARCHAR(36) REFERENCES notifications(id),
    from_number VARCHAR(20) NOT NULL,
    to_number VARCHAR(20) NOT NULL,
    message TEXT NOT NULL,
    provider VARCHAR(50), -- TWILIO
    provider_sid VARCHAR(255),
    provider_response JSONB,
    status VARCHAR(50),
    error_message TEXT,
    created_at TIMESTAMP NOT NULL,
    INDEX idx_sms_logs_notification (notification_id)
);

-- Push notification devices
CREATE TABLE push_devices (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    device_token VARCHAR(500) NOT NULL UNIQUE,
    platform VARCHAR(50) NOT NULL, -- ANDROID, IOS, WEB
    device_model VARCHAR(255),
    os_version VARCHAR(50),
    app_version VARCHAR(50),
    is_active BOOLEAN DEFAULT true,
    last_used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    INDEX idx_push_devices_user (user_id),
    INDEX idx_push_devices_token (device_token)
);

-- Push notification logs
CREATE TABLE push_logs (
    id VARCHAR(36) PRIMARY KEY,
    notification_id VARCHAR(36) REFERENCES notifications(id),
    device_id VARCHAR(36) REFERENCES push_devices(id),
    title VARCHAR(255),
    body TEXT,
    data JSONB,
    provider VARCHAR(50), -- FIREBASE, APNS
    provider_message_id VARCHAR(255),
    provider_response JSONB,
    status VARCHAR(50),
    error_message TEXT,
    created_at TIMESTAMP NOT NULL
);

-- Notification templates
CREATE TABLE notification_templates (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    channel VARCHAR(20) NOT NULL, -- EMAIL, SMS, PUSH
    type VARCHAR(50), -- TRANSACTIONAL, PROMOTIONAL
    subject VARCHAR(500), -- for email
    template_content TEXT NOT NULL,
    template_engine VARCHAR(20) DEFAULT 'THYMELEAF',
    variables JSONB, -- List of required variables
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    INDEX idx_templates_name (name)
);

-- User notification preferences
CREATE TABLE notification_preferences (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL UNIQUE,
    email_enabled BOOLEAN DEFAULT true,
    sms_enabled BOOLEAN DEFAULT true,
    push_enabled BOOLEAN DEFAULT true,
    marketing_enabled BOOLEAN DEFAULT false,
    booking_updates BOOLEAN DEFAULT true,
    payment_updates BOOLEAN DEFAULT true,
    emergency_alerts BOOLEAN DEFAULT true,
    promotional_offers BOOLEAN DEFAULT false,
    quiet_hours_start TIME,
    quiet_hours_end TIME,
    quiet_hours_enabled BOOLEAN DEFAULT false,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    INDEX idx_preferences_user (user_id)
);

-- Initial template data
INSERT INTO notification_templates (id, name, channel, subject, template_content, created_at, updated_at) VALUES
(UUID_GENERATE_V4(), 'otp-verification', 'SMS', NULL, 'Your OTP for Vehicle Service Platform is: ${otp}. Valid for 5 minutes.', NOW(), NOW()),
(UUID_GENERATE_V4(), 'booking-confirmation', 'EMAIL', 'Booking Confirmed - Vehicle Service Platform', 'booking-confirmation.html', NOW(), NOW()),
(UUID_GENERATE_V4(), 'payment-success', 'EMAIL', 'Payment Successful - Vehicle Service Platform', 'payment-success.html', NOW(), NOW()),
(UUID_GENERATE_V4(), 'damage-reported', 'EMAIL', 'Damage Report - Action Required', 'damage-reported.html', NOW(), NOW()),
(UUID_GENERATE_V4(), 'emergency-assigned', 'SMS', NULL, 'Emergency service assigned! Provider ${providerName} will arrive in ${etaMinutes} minutes. Track at: ${trackingUrl}', NOW(), NOW()),
(UUID_GENERATE_V4(), 'emergency-assigned', 'PUSH', 'Emergency Assigned', 'Provider ${providerName} is on the way. ETA: ${etaMinutes} minutes', NOW(), NOW());