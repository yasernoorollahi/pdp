-- =====================================================
-- V14__add_user_tools_and_projects.sql
-- Add user_tools and user_projects tables
-- =====================================================

-- =====================================================
-- 1️⃣ USER TOOLS
-- extracted from facts.tools
-- =====================================================

CREATE TABLE user_tools (

                             id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                             version BIGINT,
                             tenant_id UUID,

                             user_id UUID NOT NULL,
                             message_id UUID,

                             tool_name TEXT NOT NULL,
                             normalized_name TEXT,
                             source TEXT DEFAULT 'llm',

                             signal_id UUID,
                             source_hash TEXT,
                             extraction_model VARCHAR(100),
                             pipeline_version VARCHAR(50),

                             created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                             updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                             created_by UUID,
                             updated_by UUID,
                             enabled BOOLEAN NOT NULL DEFAULT TRUE,

                             CONSTRAINT fk_user_tools_user
                                 FOREIGN KEY (user_id)
                                     REFERENCES users(id)
                                     ON DELETE CASCADE,

                             CONSTRAINT fk_user_tools_message
                                 FOREIGN KEY (message_id)
                                     REFERENCES user_messages(id)
                                     ON DELETE SET NULL,

                             CONSTRAINT fk_user_tools_signal
                                 FOREIGN KEY (signal_id)
                                     REFERENCES message_signals(id)
                                     ON DELETE SET NULL
);

CREATE INDEX idx_user_tools_user
    ON user_tools(user_id);

CREATE INDEX idx_user_tools_signal
    ON user_tools(signal_id);

CREATE UNIQUE INDEX ux_user_tools_source_hash
    ON user_tools(source_hash);



-- =====================================================
-- 2️⃣ USER PROJECTS
-- extracted from facts.projects
-- =====================================================

CREATE TABLE user_projects (

                                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                version BIGINT,
                                tenant_id UUID,

                                user_id UUID NOT NULL,
                                message_id UUID,

                                project_name TEXT NOT NULL,
                                normalized_name TEXT,
                                source TEXT DEFAULT 'llm',

                                signal_id UUID,
                                source_hash TEXT,
                                extraction_model VARCHAR(100),
                                pipeline_version VARCHAR(50),

                                created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                created_by UUID,
                                updated_by UUID,
                                enabled BOOLEAN NOT NULL DEFAULT TRUE,

                                CONSTRAINT fk_user_projects_user
                                    FOREIGN KEY (user_id)
                                        REFERENCES users(id)
                                        ON DELETE CASCADE,

                                CONSTRAINT fk_user_projects_message
                                    FOREIGN KEY (message_id)
                                        REFERENCES user_messages(id)
                                        ON DELETE SET NULL,

                                CONSTRAINT fk_user_projects_signal
                                    FOREIGN KEY (signal_id)
                                        REFERENCES message_signals(id)
                                        ON DELETE SET NULL
);

CREATE INDEX idx_user_projects_user
    ON user_projects(user_id);

CREATE INDEX idx_user_projects_signal
    ON user_projects(signal_id);

CREATE UNIQUE INDEX ux_user_projects_source_hash
    ON user_projects(source_hash);
