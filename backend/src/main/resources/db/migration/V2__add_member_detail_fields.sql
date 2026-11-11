ALTER TABLE members
    ADD COLUMN official_website_url TEXT,
    ADD COLUMN current_member BOOLEAN,
    ADD COLUMN source_update_date TIMESTAMP WITH TIME ZONE;