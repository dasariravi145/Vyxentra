CREATE TABLE IF NOT EXISTS geofences (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    type VARCHAR(50) NOT NULL,
    center_lat DOUBLE PRECISION NOT NULL,
    center_lng DOUBLE PRECISION NOT NULL,
    radius DOUBLE PRECISION NOT NULL,
    city VARCHAR(100),
    pincode VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE
);

CREATE INDEX idx_geofences_type ON geofences(type);
CREATE INDEX idx_geofences_location ON geofences(center_lat, center_lng);