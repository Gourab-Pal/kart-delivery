CREATE TABLE deliveries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    -- One delivery record per order.
    -- No foreign key because kart-order owns the order data.
    order_id UUID NOT NULL UNIQUE,
    status VARCHAR(30) NOT NULL DEFAULT 'CREATED'
        CHECK (status IN (
            'CREATED',
            'READY_FOR_PICKUP',
            'PICKED_UP',
            'IN_TRANSIT',
            'OUT_FOR_DELIVERY',
            'DELIVERED',
            'DELIVERY_FAILED',
            'CANCELLED'
        )),
    tracking_number VARCHAR(100) UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    picked_up_at TIMESTAMPTZ,
    delivered_at TIMESTAMPTZ,
    cancelled_at TIMESTAMPTZ
);