CREATE TABLE ship_position (
    id          BIGINT IDENTITY(1,1) PRIMARY KEY,
    mmsi        VARCHAR(20),
    ship_name   VARCHAR(100),
    latitude    FLOAT NOT NULL,
    longitude   FLOAT NOT NULL,
    speed       FLOAT,
    course      FLOAT,
    recorded_at DATETIMEOFFSET NOT NULL
);
