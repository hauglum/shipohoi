CREATE TABLE watched_ship (
    mmsi        VARCHAR(20) PRIMARY KEY,
    ship_name   VARCHAR(100),
    added_at    DATETIMEOFFSET NOT NULL
);
