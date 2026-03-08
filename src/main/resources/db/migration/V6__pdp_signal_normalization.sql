-- V6__pdp_signal_normalization.sql

-- =====================================================
-- 1️⃣ UPDATE ENTITY TYPES
-- =====================================================

ALTER TABLE user_entities
DROP CONSTRAINT IF EXISTS user_entities_entity_type_check;

ALTER TABLE user_entities
    ADD CONSTRAINT user_entities_entity_type_check
        CHECK (entity_type IN (
                               'PERSON',
                               'LOCATION',
                               'PROJECT',
                               'TOOL',
                               'ACTIVITY',
                               'TOPIC'
            ));



-- =====================================================
-- 2️⃣ USER ACTIVITIES
-- extracted from facts.activities
-- =====================================================

CREATE TABLE user_activities (

                                 id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                 version BIGINT,
                                 tenant_id UUID,

                                 user_id UUID NOT NULL,
                                 message_id UUID,

                                 activity_name TEXT NOT NULL,
                                 activity_date DATE,

                                 created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                 updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                 created_by UUID,
                                 updated_by UUID,
                                 enabled BOOLEAN NOT NULL DEFAULT TRUE,

                                 CONSTRAINT fk_activity_user
                                     FOREIGN KEY (user_id)
                                         REFERENCES users(id)
                                         ON DELETE CASCADE,

                                 CONSTRAINT fk_activity_message
                                     FOREIGN KEY (message_id)
                                         REFERENCES user_messages(id)
                                         ON DELETE SET NULL
);

CREATE INDEX idx_user_activities_user
    ON user_activities(user_id);

CREATE INDEX idx_user_activities_date
    ON user_activities(activity_date);



-- =====================================================
-- 3️⃣ USER TOPICS
-- extracted from topics.topic_tags
-- =====================================================

CREATE TABLE user_topics (

                             id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                             version BIGINT,
                             tenant_id UUID,

                             user_id UUID NOT NULL,
                             message_id UUID,

                             topic TEXT,
                             domain TEXT,

                             created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                             updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                             created_by UUID,
                             updated_by UUID,
                             enabled BOOLEAN NOT NULL DEFAULT TRUE,

                             CONSTRAINT fk_topic_user
                                 FOREIGN KEY (user_id)
                                     REFERENCES users(id)
                                     ON DELETE CASCADE,

                             CONSTRAINT fk_topic_message
                                 FOREIGN KEY (message_id)
                                     REFERENCES user_messages(id)
                                     ON DELETE SET NULL
);

CREATE INDEX idx_user_topics_user
    ON user_topics(user_id);



-- =====================================================
-- 4️⃣ INTENT ITEMS
-- extracted from intent.goals/plans/decisions/etc
-- =====================================================

CREATE TABLE intent_items (

                              id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                              version BIGINT,
                              tenant_id UUID,

                              user_id UUID NOT NULL,
                              message_id UUID,

                              intent_type VARCHAR(50) NOT NULL,
                              description TEXT NOT NULL,
                              temporal_scope VARCHAR(50),

                              created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                              updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                              created_by UUID,
                              updated_by UUID,
                              enabled BOOLEAN NOT NULL DEFAULT TRUE,

                              CONSTRAINT fk_intent_user
                                  FOREIGN KEY (user_id)
                                      REFERENCES users(id)
                                      ON DELETE CASCADE,

                              CONSTRAINT fk_intent_message
                                  FOREIGN KEY (message_id)
                                      REFERENCES user_messages(id)
                                      ON DELETE SET NULL,

                              CONSTRAINT chk_intent_type CHECK (
                                  intent_type IN (
                                                  'GOAL',
                                                  'PLAN',
                                                  'COMMITMENT',
                                                  'DECISION',
                                                  'OBLIGATION'
                                      )
                                  )
);

CREATE INDEX idx_intent_items_user
    ON intent_items(user_id);

CREATE INDEX idx_intent_items_type
    ON intent_items(intent_type);



-- =====================================================
-- 5️⃣ USER PREFERENCES
-- extracted from context.likes/dislikes/constraints
-- =====================================================

CREATE TABLE user_preferences (

                                  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                  version BIGINT,
                                  tenant_id UUID,

                                  user_id UUID NOT NULL,
                                  message_id UUID,

                                  preference_type VARCHAR(50) NOT NULL,
                                  value TEXT NOT NULL,

                                  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                  created_by UUID,
                                  updated_by UUID,
                                  enabled BOOLEAN NOT NULL DEFAULT TRUE,

                                  CONSTRAINT fk_pref_user
                                      FOREIGN KEY (user_id)
                                          REFERENCES users(id)
                                          ON DELETE CASCADE,

                                  CONSTRAINT fk_pref_message
                                      FOREIGN KEY (message_id)
                                          REFERENCES user_messages(id)
                                          ON DELETE SET NULL,

                                  CONSTRAINT chk_pref_type CHECK (
                                      preference_type IN (
                                                          'LIKE',
                                                          'DISLIKE',
                                                          'CONSTRAINT',
                                                          'AVOIDANCE'
                                          )
                                      )
);

CREATE INDEX idx_user_preferences_user
    ON user_preferences(user_id);



-- =====================================================
-- 6️⃣ COGNITIVE STATES
-- extracted from cognitive.*
-- =====================================================

CREATE TABLE cognitive_states (

                                  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                  version BIGINT,
                                  tenant_id UUID,

                                  user_id UUID NOT NULL,
                                  message_id UUID,

                                  clarity_level VARCHAR(50),
                                  decision_state VARCHAR(50),
                                  hesitation_detected BOOLEAN,

                                  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                  created_by UUID,
                                  updated_by UUID,
                                  enabled BOOLEAN NOT NULL DEFAULT TRUE,

                                  CONSTRAINT fk_cognitive_user
                                      FOREIGN KEY (user_id)
                                          REFERENCES users(id)
                                          ON DELETE CASCADE,

                                  CONSTRAINT fk_cognitive_message
                                      FOREIGN KEY (message_id)
                                          REFERENCES user_messages(id)
                                          ON DELETE SET NULL
);

CREATE INDEX idx_cognitive_states_user
    ON cognitive_states(user_id);