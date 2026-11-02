CREATE TABLE members (
    bioguide_id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    party_name TEXT NOT NULL,
    state TEXT NOT NULL,
    chamber TEXT NOT NULL,
    image_url TEXT,
    image_attribution TEXT,
    district INTEGER,
    chamber_start_year INTEGER
);
