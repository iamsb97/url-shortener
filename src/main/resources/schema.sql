CREATE TABLE IF NOT EXISTS urltab (
    short_url VARCHAR(7) PRIMARY KEY,
    long_url TEXT UNIQUE 
);