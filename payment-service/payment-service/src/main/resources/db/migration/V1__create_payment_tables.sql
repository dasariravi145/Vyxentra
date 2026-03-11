-- Payments table
CREATE TABLE payments (
    id VARCHAR(36) PRIMARY KEY,
    payment_number VARCHAR(50) NOT NULL UNIQUE,
    booking_id VARCHAR(36) NOT NULL,
    customer_id VARCHAR(36) NOT NULL,
    provider_id VARCHAR(36),
    amount DECIMAL(10, 2) NOT NULL,
    commission_amount DECIMAL(10, 2),
    provider_amount DECIMAL(10, 2),
    payment_method VARCHAR(50) NOT NULL, -- CARD, UPI, WALLET, NETBANKING
    payment_gateway VARCHAR(50), -- RAZORPAY, STRIPE, PAYU
    gateway_payment_id VARCHAR(255),
    gateway_order_id VARCHAR(255),
    status VARCHAR(50) NOT NULL, -- PENDING, PROCESSING, SUCCESS, FAILED, REFUNDED
    payment_type VARCHAR(50) NOT NULL, -- BOOKING, WALLET_TOPUP, REFUND
    description VARCHAR(500),
    metadata JSONB,
    error_message TEXT,
    error_code VARCHAR(100),
    retry_count INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    INDEX idx_payments_booking (booking_id),
    INDEX idx_payments_customer (customer_id),
    INDEX idx_payments_status (status)
);

-- Refunds table
CREATE TABLE refunds (
    id VARCHAR(36) PRIMARY KEY,
    refund_number VARCHAR(50) NOT NULL UNIQUE,
    payment_id VARCHAR(36) NOT NULL REFERENCES payments(id),
    booking_id VARCHAR(36) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    reason VARCHAR(500) NOT NULL,
    status VARCHAR(50) NOT NULL, -- PENDING, PROCESSING, SUCCESS, FAILED
    gateway_refund_id VARCHAR(255),
    processed_by VARCHAR(36),
    processed_at TIMESTAMP,
    approved_by VARCHAR(36),
    approved_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    INDEX idx_refunds_payment (payment_id)
);

-- Wallets table
CREATE TABLE wallets (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL UNIQUE,
    user_type VARCHAR(50) NOT NULL, -- CUSTOMER, PROVIDER
    balance DECIMAL(10, 2) DEFAULT 0,
    total_credited DECIMAL(10, 2) DEFAULT 0,
    total_debited DECIMAL(10, 2) DEFAULT 0,
    last_transaction_at TIMESTAMP,
    status VARCHAR(50) DEFAULT 'ACTIVE', -- ACTIVE, BLOCKED, CLOSED
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    INDEX idx_wallets_user (user_id)
);

-- Wallet transactions
CREATE TABLE wallet_transactions (
    id VARCHAR(36) PRIMARY KEY,
    wallet_id VARCHAR(36) NOT NULL REFERENCES wallets(id),
    transaction_number VARCHAR(50) NOT NULL UNIQUE,
    type VARCHAR(50) NOT NULL, -- CREDIT, DEBIT
    amount DECIMAL(10, 2) NOT NULL,
    balance_after DECIMAL(10, 2) NOT NULL,
    reference_id VARCHAR(36), -- payment_id, booking_id, refund_id
    reference_type VARCHAR(50), -- PAYMENT, BOOKING, REFUND, TOPUP
    description VARCHAR(500),
    status VARCHAR(50) DEFAULT 'SUCCESS',
    created_at TIMESTAMP NOT NULL,
    INDEX idx_wallet_tx_wallet (wallet_id),
    INDEX idx_wallet_tx_reference (reference_id)
);

-- Provider payouts
CREATE TABLE payouts (
    id VARCHAR(36) PRIMARY KEY,
    payout_number VARCHAR(50) NOT NULL UNIQUE,
    provider_id VARCHAR(36) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    commission_deducted DECIMAL(10, 2),
    net_amount DECIMAL(10, 2) NOT NULL,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    booking_ids JSONB, -- List of booking IDs included
    status VARCHAR(50) NOT NULL, -- PENDING, PROCESSING, SUCCESS, FAILED
    payment_method VARCHAR(50),
    account_details JSONB,
    gateway_payout_id VARCHAR(255),
    processed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    INDEX idx_payouts_provider (provider_id),
    INDEX idx_payouts_status (status)
);

-- Payment methods (saved cards/UPI)
CREATE TABLE payment_methods (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    user_type VARCHAR(50) NOT NULL,
    method_type VARCHAR(50) NOT NULL, -- CARD, UPI, NETBANKING
    gateway_token VARCHAR(255),
    last_four VARCHAR(4),
    card_type VARCHAR(20),
    card_network VARCHAR(20), -- VISA, MASTERCARD, RUPAY
    expiry_month VARCHAR(2),
    expiry_year VARCHAR(4),
    name_on_card VARCHAR(255),
    is_default BOOLEAN DEFAULT false,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    INDEX idx_pm_user (user_id)
);

-- Transaction logs for audit
CREATE TABLE transaction_logs (
    id VARCHAR(36) PRIMARY KEY,
    transaction_id VARCHAR(36),
    transaction_type VARCHAR(50),
    action VARCHAR(100),
    request_payload JSONB,
    response_payload JSONB,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_by VARCHAR(36),
    created_at TIMESTAMP NOT NULL
);