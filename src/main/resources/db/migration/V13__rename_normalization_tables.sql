-- =====================================================
-- V13__rename_normalization_tables.sql
-- Rename extraction-related tables for standardized naming
-- =====================================================

ALTER TABLE tone_states RENAME TO user_tone_states;
ALTER TABLE cognitive_states RENAME TO user_cognitive_states;
ALTER TABLE intent_items RENAME TO user_intents;
ALTER TABLE cognitive_language_items RENAME TO user_cognitive_language;
ALTER TABLE user_preferences RENAME TO user_context;
