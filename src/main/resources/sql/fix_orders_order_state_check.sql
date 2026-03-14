-- One-time migration for PostgreSQL.
-- Updates the check constraint of orders.order_state to include new PayPal states.

DO $$
DECLARE
    constraint_name text;
BEGIN
    SELECT c.conname
    INTO constraint_name
    FROM pg_constraint c
    JOIN pg_class t ON t.oid = c.conrelid
    JOIN pg_namespace n ON n.oid = t.relnamespace
    WHERE c.contype = 'c'
      AND t.relname = 'orders'
      AND n.nspname = 'public'
      AND pg_get_constraintdef(c.oid) ILIKE '%order_state%'
    LIMIT 1;

    IF constraint_name IS NOT NULL THEN
        EXECUTE format('ALTER TABLE public.orders DROP CONSTRAINT %I', constraint_name);
    END IF;
END $$;

ALTER TABLE public.orders
ADD CONSTRAINT orders_order_state_check
CHECK (order_state IN (
    'CREATED',
    'PENDING_PAYMENT',
    'PAID',
    'PAYMENT_FAILED',
    'CANCELLED',
    'CONFIRMED'
));
