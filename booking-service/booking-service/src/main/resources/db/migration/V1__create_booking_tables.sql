-- Bookings table
CREATE TABLE bookings (
    id VARCHAR(36) PRIMARY KEY,
    booking_number VARCHAR(50) NOT NULL UNIQUE,
    customer_id VARCHAR(36) NOT NULL,
    provider_id VARCHAR(36) NOT NULL,
    employee_id VARCHAR(36),
    vehicle_type VARCHAR(20) NOT NULL, -- BIKE, CAR
    vehicle_details JSONB, -- Store vehicle make, model, registration
    service_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL, -- PENDING_PAYMENT, PENDING_CONFIRMATION, CONFIRMED, ASSIGNED, IN_PROGRESS, COMPLETED, CANCELLED, EXPIRED, DAMAGE_REPORTED, DAMAGE_APPROVED, DAMAGE_REJECTED, WAITING_FOR_APPROVAL
    scheduled_time TIMESTAMP NOT NULL,
    actual_start_time TIMESTAMP,
    actual_end_time TIMESTAMP,
    location_lat DECIMAL(10, 8),
    location_lng DECIMAL(11, 8),
    location_address TEXT,
    total_amount DECIMAL(10, 2),
    approved_amount DECIMAL(10, 2),
    paid_amount DECIMAL(10, 2),
    commission_amount DECIMAL(10, 2),
    provider_amount DECIMAL(10, 2),
    is_emergency BOOLEAN DEFAULT false,
    emergency_type VARCHAR(50), -- REPAIR_EMERGENCY, PETROL_EMERGENCY
    upfront_payment_required BOOLEAN DEFAULT false,
    upfront_paid BOOLEAN DEFAULT false,
    payment_status VARCHAR(50), -- PENDING, PROCESSING, COMPLETED, FAILED
    cancellation_reason TEXT,
    customer_notes TEXT,
    provider_notes TEXT,
    rating INTEGER CHECK (rating >= 1 AND rating <= 5),
    review TEXT,
    snapshot_id VARCHAR(36),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(36),
    updated_by VARCHAR(36)
);

-- Booking services (items)
CREATE TABLE booking_services (
    id VARCHAR(36) PRIMARY KEY,
    booking_id VARCHAR(36) NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    service_id VARCHAR(36) NOT NULL,
    service_name VARCHAR(255) NOT NULL,
    service_type VARCHAR(50) NOT NULL,
    quantity INTEGER DEFAULT 1,
    unit_price DECIMAL(10, 2) NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    is_approved BOOLEAN DEFAULT false,
    approved_at TIMESTAMP,
    rejection_reason TEXT,
    created_at TIMESTAMP NOT NULL
);

-- Booking addons
CREATE TABLE booking_addons (
    id VARCHAR(36) PRIMARY KEY,
    booking_service_id VARCHAR(36) NOT NULL REFERENCES booking_services(id) ON DELETE CASCADE,
    addon_id VARCHAR(36),
    addon_name VARCHAR(255) NOT NULL,
    quantity INTEGER DEFAULT 1,
    unit_price DECIMAL(10, 2) NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    is_approved BOOLEAN DEFAULT false,
    created_at TIMESTAMP NOT NULL
);

-- Damage reports (for service centers)
CREATE TABLE damage_reports (
    id VARCHAR(36) PRIMARY KEY,
    booking_id VARCHAR(36) NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    reported_by VARCHAR(36) NOT NULL, -- employee_id
    reported_at TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL, -- REPORTED, APPROVED, REJECTED, PARTIALLY_APPROVED
    total_amount DECIMAL(10, 2),
    approved_amount DECIMAL(10, 2),
    notes TEXT,
    approved_by VARCHAR(36),
    approved_at TIMESTAMP,
    rejection_reason TEXT,
    images TEXT[], -- Array of image URLs
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Damage items
CREATE TABLE damage_items (
    id VARCHAR(36) PRIMARY KEY,
    damage_report_id VARCHAR(36) NOT NULL REFERENCES damage_reports(id) ON DELETE CASCADE,
    item_name VARCHAR(255) NOT NULL,
    description TEXT,
    estimated_cost DECIMAL(10, 2) NOT NULL,
    approved_cost DECIMAL(10, 2),
    is_approved BOOLEAN DEFAULT false,
    approved_at TIMESTAMP,
    rejection_reason TEXT,
    images TEXT[],
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Booking timeline (audit trail)
CREATE TABLE booking_timeline (
    id VARCHAR(36) PRIMARY KEY,
    booking_id VARCHAR(36) NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    status VARCHAR(50) NOT NULL,
    notes TEXT,
    changed_by VARCHAR(36),
    changed_at TIMESTAMP NOT NULL
);

-- Booking snapshots (immutable records)
CREATE TABLE booking_snapshots (
    id VARCHAR(36) PRIMARY KEY,
    booking_id VARCHAR(36) NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    snapshot_data JSONB NOT NULL, -- Complete booking data at point in time
    snapshot_type VARCHAR(50) NOT NULL, -- INITIAL, PRE_SERVICE, POST_SERVICE, DAMAGE
    created_by VARCHAR(36) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

-- Indexes
CREATE INDEX idx_bookings_customer ON bookings(customer_id);
CREATE INDEX idx_bookings_provider ON bookings(provider_id);
CREATE INDEX idx_bookings_status ON bookings(status);
CREATE INDEX idx_bookings_scheduled ON bookings(scheduled_time);
CREATE INDEX idx_bookings_emergency ON bookings(is_emergency) WHERE is_emergency = true;
CREATE INDEX idx_damage_reports_booking ON damage_reports(booking_id);
CREATE INDEX idx_damage_reports_status ON damage_reports(status);
CREATE INDEX idx_booking_timeline_booking ON booking_timeline(booking_id);