-- Emergency requests table
CREATE TABLE emergency_requests (
    id VARCHAR(36) PRIMARY KEY,
    request_number VARCHAR(50) NOT NULL UNIQUE,
    customer_id VARCHAR(36) NOT NULL,
    customer_name VARCHAR(255),
    customer_phone VARCHAR(20),
    emergency_type VARCHAR(50) NOT NULL, -- REPAIR_EMERGENCY, PETROL_EMERGENCY
    vehicle_type VARCHAR(20) NOT NULL, -- BIKE, CAR
    vehicle_details JSONB,
    location_lat DECIMAL(10, 8) NOT NULL,
    location_lng DECIMAL(11, 8) NOT NULL,
    location_address TEXT,
    status VARCHAR(50) NOT NULL, -- SEARCHING, ASSIGNED, EXPIRED, CANCELLED, COMPLETED
    search_radius_km INT DEFAULT 5,
    current_radius_km INT DEFAULT 5,
    max_radius_km INT DEFAULT 50,
    expiry_time TIMESTAMP NOT NULL,

    -- Repair specific
    service_type VARCHAR(50),
    issue_description TEXT,

    -- Petrol specific
    fuel_type VARCHAR(20),
    quantity_liters INT,
    fuel_cost_per_liter DECIMAL(10, 2),
    total_fuel_cost DECIMAL(10, 2),

    -- Amounts
    base_amount DECIMAL(10, 2),
    multiplier DECIMAL(5, 2) DEFAULT 1.0,
    total_amount DECIMAL(10, 2),

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Emergency assignments (which provider accepted)
CREATE TABLE emergency_assignments (
    id VARCHAR(36) PRIMARY KEY,
    request_id VARCHAR(36) NOT NULL REFERENCES emergency_requests(id) ON DELETE CASCADE,
    provider_id VARCHAR(36) NOT NULL,
    provider_name VARCHAR(255),
    provider_phone VARCHAR(20),
    provider_lat DECIMAL(10, 8),
    provider_lng DECIMAL(11, 8),
    distance_km DECIMAL(10, 2),
    eta_minutes INT,
    accepted_at TIMESTAMP NOT NULL,
    booking_id VARCHAR(36),
    status VARCHAR(50) NOT NULL, -- ACCEPTED, ARRIVED, COMPLETED, CANCELLED
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Provider notifications tracking
CREATE TABLE provider_notifications (
    id VARCHAR(36) PRIMARY KEY,
    request_id VARCHAR(36) NOT NULL REFERENCES emergency_requests(id) ON DELETE CASCADE,
    provider_id VARCHAR(36) NOT NULL,
    notified_at TIMESTAMP NOT NULL,
    response_status VARCHAR(20), -- ACCEPTED, REJECTED, TIMEOUT
    responded_at TIMESTAMP,
    UNIQUE(request_id, provider_id)
);

-- Indexes
CREATE INDEX idx_emergency_requests_status ON emergency_requests(status);
CREATE INDEX idx_emergency_requests_customer ON emergency_requests(customer_id);
CREATE INDEX idx_emergency_requests_expiry ON emergency_requests(expiry_time) WHERE status = 'SEARCHING';
CREATE INDEX idx_emergency_assignments_request ON emergency_assignments(request_id);
CREATE INDEX idx_emergency_assignments_provider ON emergency_assignments(provider_id);