-- Employees table
CREATE TABLE employees (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL UNIQUE,
    provider_id VARCHAR(36) NOT NULL,
    employee_code VARCHAR(50) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    alternate_phone VARCHAR(20),
    date_of_birth DATE,
    gender VARCHAR(10),
    address_line1 VARCHAR(255),
    address_line2 VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(50),
    profile_picture VARCHAR(500),
    designation VARCHAR(100) NOT NULL,
    department VARCHAR(100),
    employment_type VARCHAR(50) NOT NULL, -- FULL_TIME, PART_TIME, CONTRACT
    joining_date DATE NOT NULL,
    exit_date DATE,
    status VARCHAR(50) NOT NULL, -- ACTIVE, INACTIVE, ON_LEAVE, TERMINATED
    average_rating DECIMAL(3, 2) DEFAULT 0,
    total_services_completed INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(36),
    updated_by VARCHAR(36)
);

-- Skills table
CREATE TABLE skills (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    category VARCHAR(50), -- MECHANICAL, ELECTRICAL, BODY, etc.
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL
);

-- Employee skills mapping
CREATE TABLE employee_skills (
    employee_id VARCHAR(36) NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    skill_id VARCHAR(36) NOT NULL REFERENCES skills(id) ON DELETE CASCADE,
    proficiency_level INT CHECK (proficiency_level >= 1 AND proficiency_level <= 5),
    years_of_experience INT,
    certified BOOLEAN DEFAULT false,
    certification_details VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    PRIMARY KEY (employee_id, skill_id)
);

-- Employee assignments (to bookings)
CREATE TABLE assignments (
    id VARCHAR(36) PRIMARY KEY,
    employee_id VARCHAR(36) NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    booking_id VARCHAR(36) NOT NULL UNIQUE,
    provider_id VARCHAR(36) NOT NULL,
    assigned_by VARCHAR(36) NOT NULL,
    assigned_at TIMESTAMP NOT NULL,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    status VARCHAR(50) NOT NULL, -- ASSIGNED, STARTED, COMPLETED, CANCELLED
    estimated_duration INT, -- in minutes
    actual_duration INT,
    notes TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Timesheets
CREATE TABLE timesheets (
    id VARCHAR(36) PRIMARY KEY,
    employee_id VARCHAR(36) NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    timesheet_date DATE NOT NULL,
    total_hours DECIMAL(5, 2) DEFAULT 0,
    regular_hours DECIMAL(5, 2) DEFAULT 0,
    overtime_hours DECIMAL(5, 2) DEFAULT 0,
    status VARCHAR(50) NOT NULL, -- DRAFT, SUBMITTED, APPROVED, REJECTED
    submitted_at TIMESTAMP,
    approved_by VARCHAR(36),
    approved_at TIMESTAMP,
    rejection_reason TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    UNIQUE(employee_id, timesheet_date)
);

-- Timesheet entries (time tracking for each assignment)
CREATE TABLE timesheet_entries (
    id VARCHAR(36) PRIMARY KEY,
    timesheet_id VARCHAR(36) NOT NULL REFERENCES timesheets(id) ON DELETE CASCADE,
    assignment_id VARCHAR(36) NOT NULL REFERENCES assignments(id) ON DELETE CASCADE,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    duration_minutes INT NOT NULL,
    is_overtime BOOLEAN DEFAULT false,
    description TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Employee availability
CREATE TABLE employee_availability (
    id VARCHAR(36) PRIMARY KEY,
    employee_id VARCHAR(36) NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    day_of_week INT NOT NULL, -- 0-6 (Monday-Sunday)
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    is_available BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    UNIQUE(employee_id, day_of_week, start_time)
);

-- Indexes
CREATE INDEX idx_employees_provider ON employees(provider_id);
CREATE INDEX idx_employees_status ON employees(status);
CREATE INDEX idx_assignments_employee ON assignments(employee_id);
CREATE INDEX idx_assignments_booking ON assignments(booking_id);
CREATE INDEX idx_assignments_status ON assignments(status);
CREATE INDEX idx_timesheets_employee_date ON timesheets(employee_id, timesheet_date);
CREATE INDEX idx_timesheets_status ON timesheets(status);