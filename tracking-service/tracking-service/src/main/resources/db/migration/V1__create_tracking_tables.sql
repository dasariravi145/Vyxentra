-- Location updates history
CREATE TABLE location_updates (
    id VARCHAR(36) PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL, -- PROVIDER, EMPLOYEE, CUSTOMER
    entity_id VARCHAR(36) NOT NULL,
    booking_id VARCHAR(36),
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    speed DECIMAL(10, 2), -- km/h
    heading DECIMAL(5, 2), -- degrees
    accuracy DECIMAL(10, 2), -- meters
    altitude DECIMAL(10, 2), -- meters
    source VARCHAR(50), -- GPS, NETWORK, MANUAL
    created_at TIMESTAMP NOT NULL,
    INDEX idx_location_entity (entity_type, entity_id),
    INDEX idx_location_booking (booking_id),
    INDEX idx_location_created (created_at)
);

-- Tracking sessions
CREATE TABLE tracking_sessions (
    id VARCHAR(36) PRIMARY KEY,
    booking_id VARCHAR(36) NOT NULL UNIQUE,
    customer_id VARCHAR(36) NOT NULL,
    provider_id VARCHAR(36) NOT NULL,
    employee_id VARCHAR(36),
    status VARCHAR(50) NOT NULL, -- ACTIVE, PAUSED, COMPLETED, EXPIRED
    start_location_lat DECIMAL(10, 8),
    start_location_lng DECIMAL(11, 8),
    current_location_lat DECIMAL(10, 8),
    current_location_lng DECIMAL(11, 8),
    destination_lat DECIMAL(10, 8) NOT NULL,
    destination_lng DECIMAL(11, 8) NOT NULL,
    destination_address TEXT,
    started_at TIMESTAMP NOT NULL,
    last_update_at TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    total_distance_km DECIMAL(10, 2) DEFAULT 0,
    current_eta_minutes INT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- ETA history
CREATE TABLE eta_history (
    id VARCHAR(36) PRIMARY KEY,
    tracking_session_id VARCHAR(36) NOT NULL REFERENCES tracking_sessions(id) ON DELETE CASCADE,
    eta_minutes INT NOT NULL,
    distance_km DECIMAL(10, 2),
    reason VARCHAR(255), -- TRAFFIC, ROUTE_CHANGE, etc.
    calculated_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    INDEX idx_eta_session (tracking_session_id)
);

-- Path points (for route replay)
CREATE TABLE path_points (
    id VARCHAR(36) PRIMARY KEY,
    tracking_session_id VARCHAR(36) NOT NULL REFERENCES tracking_sessions(id) ON DELETE CASCADE,
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    sequence INT NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    INDEX idx_path_session (tracking_session_id)
);

-- WebSocket connections
CREATE TABLE websocket_connections (
    id VARCHAR(36) PRIMARY KEY,
    session_id VARCHAR(100) NOT NULL UNIQUE,
    user_id VARCHAR(36) NOT NULL,
    user_type VARCHAR(50) NOT NULL, -- CUSTOMER, PROVIDER, EMPLOYEE
    tracking_session_id VARCHAR(36),
    connected_at TIMESTAMP NOT NULL,
    last_heartbeat_at TIMESTAMP,
    disconnected_at TIMESTAMP,
    INDEX idx_ws_user (user_id)
);