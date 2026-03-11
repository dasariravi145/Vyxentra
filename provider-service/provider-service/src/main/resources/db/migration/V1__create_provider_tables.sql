-- Providers table
CREATE TABLE providers (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL UNIQUE,
    business_name VARCHAR(255) NOT NULL,
    business_type VARCHAR(50) NOT NULL, -- SERVICE_CENTER, WASHING_CENTER
    gst_number VARCHAR(50) NOT NULL UNIQUE,
    pan_number VARCHAR(50),
    registration_number VARCHAR(100),
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    country VARCHAR(50) NOT NULL,
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    alternate_phone VARCHAR(20),
    email VARCHAR(255) NOT NULL,
    website VARCHAR(255),
    description TEXT,
    status VARCHAR(50) NOT NULL, -- PENDING_APPROVAL, ACTIVE, SUSPENDED, INACTIVE, REJECTED
    supports_bike BOOLEAN NOT NULL DEFAULT false,
    supports_car BOOLEAN NOT NULL DEFAULT false,
    opening_time TIME,
    closing_time TIME,
    working_days VARCHAR(100), -- MON,TUE,WED,THU,FRI,SAT,SUN
    average_rating DECIMAL(3, 2) DEFAULT 0,
    total_reviews INT DEFAULT 0,
    total_bookings INT DEFAULT 0,
    completion_rate DECIMAL(5, 2) DEFAULT 0,
    approved_by VARCHAR(36),
    approved_at TIMESTAMP,
    suspended_reason TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(36),
    updated_by VARCHAR(36),
    CONSTRAINT check_supports_vehicle CHECK (supports_bike OR supports_car)
);

-- Service offerings table
CREATE TABLE service_offerings (
    id VARCHAR(36) PRIMARY KEY,
    provider_id VARCHAR(36) NOT NULL REFERENCES providers(id) ON DELETE CASCADE,
    service_type VARCHAR(50) NOT NULL, -- From common.enums.ServiceType
    name VARCHAR(255) NOT NULL,
    description TEXT,
    estimated_duration INT, -- in minutes
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    UNIQUE(provider_id, service_type)
);

-- Service pricing table (separate pricing per vehicle type)
CREATE TABLE service_pricing (
    id VARCHAR(36) PRIMARY KEY,
    service_offering_id VARCHAR(36) NOT NULL REFERENCES service_offerings(id) ON DELETE CASCADE,
    vehicle_type VARCHAR(20) NOT NULL, -- BIKE, CAR
    base_price DECIMAL(10, 2) NOT NULL,
    discounted_price DECIMAL(10, 2),
    currency VARCHAR(3) DEFAULT 'INR',
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    UNIQUE(service_offering_id, vehicle_type)
);

-- Provider availability (time slots)
CREATE TABLE availability (
    id VARCHAR(36) PRIMARY KEY,
    provider_id VARCHAR(36) NOT NULL REFERENCES providers(id) ON DELETE CASCADE,
    day_of_week INT NOT NULL, -- 0-6 (Monday-Sunday)
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    is_available BOOLEAN DEFAULT true,
    max_bookings_per_slot INT DEFAULT 1,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    UNIQUE(provider_id, day_of_week, start_time)
);

-- Provider documents
CREATE TABLE documents (
    id VARCHAR(36) PRIMARY KEY,
    provider_id VARCHAR(36) NOT NULL REFERENCES providers(id) ON DELETE CASCADE,
    document_type VARCHAR(50) NOT NULL, -- GST_CERTIFICATE, PAN_CARD, etc.
    document_url VARCHAR(500) NOT NULL,
    document_number VARCHAR(100),
    verified BOOLEAN DEFAULT false,
    verified_by VARCHAR(36),
    verified_at TIMESTAMP,
    expiry_date DATE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Provider reviews
CREATE TABLE reviews (
    id VARCHAR(36) PRIMARY KEY,
    provider_id VARCHAR(36) NOT NULL REFERENCES providers(id) ON DELETE CASCADE,
    user_id VARCHAR(36) NOT NULL,
    booking_id VARCHAR(36) NOT NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP NOT NULL,
    UNIQUE(booking_id)
);

-- Indexes
CREATE INDEX idx_providers_status ON providers(status);
CREATE INDEX idx_providers_location ON providers(latitude, longitude);
CREATE INDEX idx_providers_business_type ON providers(business_type);
CREATE INDEX idx_service_offerings_provider ON service_offerings(provider_id);
CREATE INDEX idx_availability_provider ON availability(provider_id);
CREATE INDEX idx_reviews_provider ON reviews(provider_id);
CREATE INDEX idx_reviews_rating ON reviews(rating);