-- V5__pdp_behavioral_transit_foundation.sql

-- =====================================================
-- 1️⃣ USER MESSAGES (Raw Chat Storage)
-- =====================================================

CREATE TABLE user_messages (
                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               version BIGINT,
                               tenant_id UUID,

                               user_id UUID NOT NULL,
                               content TEXT NOT NULL,

                               message_date DATE NOT NULL,
                               processed BOOLEAN NOT NULL DEFAULT FALSE,

                               created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                               updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                               created_by UUID,
                               updated_by UUID,
                               enabled BOOLEAN NOT NULL DEFAULT TRUE,

                               CONSTRAINT fk_user_message_user
                                   FOREIGN KEY (user_id)
                                       REFERENCES users(id)
                                       ON DELETE CASCADE
);

CREATE INDEX idx_user_messages_user_date
    ON user_messages(user_id, message_date DESC);

CREATE INDEX idx_user_messages_processed
    ON user_messages(processed);



-- =====================================================
-- 2️⃣ MESSAGE SIGNALS (AI Extraction Snapshot)
-- =====================================================

CREATE TABLE message_signals (
                                 id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                 version BIGINT,
                                 tenant_id UUID,

                                 message_id UUID NOT NULL,
                                 user_id UUID NOT NULL,

                                 signal_version INTEGER NOT NULL,
                                 signals JSONB NOT NULL,

                                 created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                 updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                 created_by UUID,
                                 updated_by UUID,
                                 enabled BOOLEAN NOT NULL DEFAULT TRUE,

                                 CONSTRAINT fk_signal_message
                                     FOREIGN KEY (message_id)
                                         REFERENCES user_messages(id)
                                         ON DELETE CASCADE,

                                 CONSTRAINT fk_signal_user
                                     FOREIGN KEY (user_id)
                                         REFERENCES users(id)
                                         ON DELETE CASCADE
);

CREATE INDEX idx_message_signals_user
    ON message_signals(user_id);

CREATE INDEX idx_message_signals_gin
    ON message_signals USING GIN (signals);



-- =====================================================
-- 3️⃣ DAILY BEHAVIOR METRICS (Transit Map Core)
-- =====================================================

CREATE TABLE daily_behavior_metrics (
                                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                        version BIGINT,
                                        tenant_id UUID,

                                        user_id UUID NOT NULL,
                                        metric_date DATE NOT NULL,

                                        energy_score DOUBLE PRECISION,
                                        motivation_score DOUBLE PRECISION,
                                        friction_count INTEGER,
                                        social_mentions_count INTEGER,
                                        discipline_events_count INTEGER,

                                        raw_summary JSONB,

                                        created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                        updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                        created_by UUID,
                                        updated_by UUID,
                                        enabled BOOLEAN NOT NULL DEFAULT TRUE,

                                        CONSTRAINT fk_daily_user
                                            FOREIGN KEY (user_id)
                                                REFERENCES users(id)
                                                ON DELETE CASCADE,

                                        CONSTRAINT uk_user_date UNIQUE (user_id, metric_date)
);

CREATE INDEX idx_daily_behavior_user_date
    ON daily_behavior_metrics(user_id, metric_date DESC);



-- =====================================================
-- 4️⃣ USER ENTITIES (For Social / Activity Lines)
-- =====================================================

CREATE TABLE user_entities (
                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               version BIGINT,
                               tenant_id UUID,

                               user_id UUID NOT NULL,
                               name VARCHAR(255) NOT NULL,
                               entity_type VARCHAR(50) NOT NULL, -- PERSON, PLACE, ACTIVITY

                               first_seen DATE,
                               last_seen DATE,
                               mention_count INTEGER NOT NULL DEFAULT 1,

                               created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                               updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                               created_by UUID,
                               updated_by UUID,
                               enabled BOOLEAN NOT NULL DEFAULT TRUE,

                               CONSTRAINT fk_entity_user
                                   FOREIGN KEY (user_id)
                                       REFERENCES users(id)
                                       ON DELETE CASCADE
);

CREATE INDEX idx_user_entities_user
    ON user_entities(user_id);

CREATE INDEX idx_user_entities_type
    ON user_entities(entity_type);