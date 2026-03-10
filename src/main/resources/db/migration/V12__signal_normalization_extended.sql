-- =====================================================
-- V12__signal_normalization_extended.sql
-- Extend normalization tables for tone, context, and cognitive language
-- =====================================================

-- =====================================================
-- 1️⃣ TONE STATES
-- extracted from tone.*
-- =====================================================

CREATE TABLE tone_states (

                             id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                             version BIGINT,
                             tenant_id UUID,

                             user_id UUID NOT NULL,
                             message_id UUID,

                             sentiment VARCHAR(50),
                             mood TEXT,
                             motivation_level VARCHAR(50),
                             effort_perception VARCHAR(50),
                             friction_detected BOOLEAN,

                             signal_id UUID,
                             source_hash TEXT,
                             extraction_model VARCHAR(100),
                             pipeline_version VARCHAR(50),

                             created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                             updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                             created_by UUID,
                             updated_by UUID,
                             enabled BOOLEAN NOT NULL DEFAULT TRUE,

                             CONSTRAINT fk_tone_user
                                 FOREIGN KEY (user_id)
                                     REFERENCES users(id)
                                     ON DELETE CASCADE,

                             CONSTRAINT fk_tone_message
                                 FOREIGN KEY (message_id)
                                     REFERENCES user_messages(id)
                                     ON DELETE SET NULL,

                             CONSTRAINT fk_tone_signal
                                 FOREIGN KEY (signal_id)
                                     REFERENCES message_signals(id)
                                     ON DELETE SET NULL
);

CREATE INDEX idx_tone_states_user
    ON tone_states(user_id);

CREATE INDEX idx_tone_states_signal
    ON tone_states(signal_id);

CREATE UNIQUE INDEX ux_tone_states_source_hash
    ON tone_states(source_hash);



-- =====================================================
-- 2️⃣ USER CONTEXT FLAGS
-- extracted from context.collaboration_detected
-- =====================================================

CREATE TABLE user_context_flags (

                                    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                    version BIGINT,
                                    tenant_id UUID,

                                    user_id UUID NOT NULL,
                                    message_id UUID,

                                    collaboration_detected BOOLEAN,

                                    signal_id UUID,
                                    source_hash TEXT,
                                    extraction_model VARCHAR(100),
                                    pipeline_version VARCHAR(50),

                                    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                    created_by UUID,
                                    updated_by UUID,
                                    enabled BOOLEAN NOT NULL DEFAULT TRUE,

                                    CONSTRAINT fk_context_user
                                        FOREIGN KEY (user_id)
                                            REFERENCES users(id)
                                            ON DELETE CASCADE,

                                    CONSTRAINT fk_context_message
                                        FOREIGN KEY (message_id)
                                            REFERENCES user_messages(id)
                                            ON DELETE SET NULL,

                                    CONSTRAINT fk_context_signal
                                        FOREIGN KEY (signal_id)
                                            REFERENCES message_signals(id)
                                            ON DELETE SET NULL
);

CREATE INDEX idx_user_context_flags_user
    ON user_context_flags(user_id);

CREATE INDEX idx_user_context_flags_signal
    ON user_context_flags(signal_id);

CREATE UNIQUE INDEX ux_user_context_flags_source_hash
    ON user_context_flags(source_hash);



-- =====================================================
-- 3️⃣ COGNITIVE LANGUAGE ITEMS
-- extracted from cognitive.uncertainty_language/confidence_language
-- =====================================================

CREATE TABLE cognitive_language_items (

                                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                          version BIGINT,
                                          tenant_id UUID,

                                          user_id UUID NOT NULL,
                                          message_id UUID,

                                          language_type VARCHAR(50) NOT NULL,
                                          value TEXT NOT NULL,

                                          signal_id UUID,
                                          source_hash TEXT,
                                          extraction_model VARCHAR(100),
                                          pipeline_version VARCHAR(50),

                                          created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                          updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                          created_by UUID,
                                          updated_by UUID,
                                          enabled BOOLEAN NOT NULL DEFAULT TRUE,

                                          CONSTRAINT fk_cognitive_language_user
                                              FOREIGN KEY (user_id)
                                                  REFERENCES users(id)
                                                  ON DELETE CASCADE,

                                          CONSTRAINT fk_cognitive_language_message
                                              FOREIGN KEY (message_id)
                                                  REFERENCES user_messages(id)
                                                  ON DELETE SET NULL,

                                          CONSTRAINT fk_cognitive_language_signal
                                              FOREIGN KEY (signal_id)
                                                  REFERENCES message_signals(id)
                                                  ON DELETE SET NULL,

                                          CONSTRAINT chk_cognitive_language_type CHECK (
                                              language_type IN (
                                                              'UNCERTAINTY',
                                                              'CONFIDENCE'
                                                  )
                                              )
);

CREATE INDEX idx_cognitive_language_items_user
    ON cognitive_language_items(user_id);

CREATE INDEX idx_cognitive_language_items_type
    ON cognitive_language_items(language_type);

CREATE INDEX idx_cognitive_language_items_signal
    ON cognitive_language_items(signal_id);

CREATE UNIQUE INDEX ux_cognitive_language_items_source_hash
    ON cognitive_language_items(source_hash);
