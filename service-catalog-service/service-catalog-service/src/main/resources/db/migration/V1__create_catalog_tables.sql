-- Service categories
CREATE TABLE service_categories (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    icon VARCHAR(255),
    display_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(36),
    updated_by VARCHAR(36)
);

-- Service definitions
CREATE TABLE service_definitions (
    id VARCHAR(36) PRIMARY KEY,
    category_id VARCHAR(36) NOT NULL REFERENCES service_categories(id),
    service_type VARCHAR(50) NOT NULL UNIQUE, -- From common.enums.ServiceType
    name VARCHAR(255) NOT NULL,
    description TEXT,
    short_description VARCHAR(500),
    icon VARCHAR(255),
    image_url VARCHAR(500),
    estimated_duration_min INT, -- Base duration in minutes
    provider_type VARCHAR(50) NOT NULL, -- SERVICE_CENTER, WASHING_CENTER
    is_active BOOLEAN DEFAULT true,
    is_popular BOOLEAN DEFAULT false,
    is_recommended BOOLEAN DEFAULT false,
    min_price DECIMAL(10, 2),
    max_price DECIMAL(10, 2),
    display_order INT DEFAULT 0,
    tags TEXT, -- Comma separated tags
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(36),
    updated_by VARCHAR(36)
);

-- Service vehicle type mapping
CREATE TABLE service_vehicle_types (
    id VARCHAR(36) PRIMARY KEY,
    service_id VARCHAR(36) NOT NULL REFERENCES service_definitions(id) ON DELETE CASCADE,
    vehicle_type VARCHAR(20) NOT NULL, -- BIKE, CAR
    base_price DECIMAL(10, 2) NOT NULL,
    price_multiplier DECIMAL(5, 2) DEFAULT 1.0,
    estimated_duration_min INT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    UNIQUE(service_id, vehicle_type)
);

-- Service addons
CREATE TABLE service_addons (
    id VARCHAR(36) PRIMARY KEY,
    service_id VARCHAR(36) NOT NULL REFERENCES service_definitions(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    price_type VARCHAR(20) NOT NULL, -- FIXED, PER_VEHICLE, PER_HOUR
    base_price DECIMAL(10, 2),
    is_mandatory BOOLEAN DEFAULT false,
    is_active BOOLEAN DEFAULT true,
    display_order INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Addon vehicle pricing
CREATE TABLE addon_vehicle_pricing (
    id VARCHAR(36) PRIMARY KEY,
    addon_id VARCHAR(36) NOT NULL REFERENCES service_addons(id) ON DELETE CASCADE,
    vehicle_type VARCHAR(20) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    UNIQUE(addon_id, vehicle_type)
);

-- Service pricing rules (for dynamic pricing)
CREATE TABLE service_pricing_rules (
    id VARCHAR(36) PRIMARY KEY,
    service_id VARCHAR(36) NOT NULL REFERENCES service_definitions(id) ON DELETE CASCADE,
    rule_name VARCHAR(100) NOT NULL,
    rule_type VARCHAR(50) NOT NULL, -- TIME_BASED, VEHICLE_TYPE_BASED, DISTANCE_BASED
    condition_expression TEXT,
    price_multiplier DECIMAL(5, 2) DEFAULT 1.0,
    priority INT DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Service FAQs
CREATE TABLE service_faqs (
    id VARCHAR(36) PRIMARY KEY,
    service_id VARCHAR(36) NOT NULL REFERENCES service_definitions(id) ON DELETE CASCADE,
    question VARCHAR(500) NOT NULL,
    answer TEXT NOT NULL,
    display_order INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL
);

-- Indexes
CREATE INDEX idx_services_category ON service_definitions(category_id);
CREATE INDEX idx_services_type ON service_definitions(service_type);
CREATE INDEX idx_services_provider_type ON service_definitions(provider_type);
CREATE INDEX idx_services_active ON service_definitions(is_active);
CREATE INDEX idx_addons_service ON service_addons(service_id);
CREATE INDEX idx_vehicle_types_service ON service_vehicle_types(service_id);