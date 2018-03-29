CREATE SEQUENCE polygons_id_seq;
CREATE TABLE polygons (
    id              integer PRIMARY KEY DEFAULT nextval('polygons_id_seq'),
    coordinates     double precision [][],
    shape_length    double precision,
    shape_area      double precision
);

CREATE SEQUENCE all_places_id_seq;
CREATE TABLE all_places (
    id              integer PRIMARY KEY DEFAULT nextval('all_places_id_seq'),
    lat             double precision,
    long            double precision,
    primary_name    text,
    lang            char(3),
    population      integer,
    feature_code    integer,
    osm_id          integer,
    country         char(2),
    polygon         integer REFERENCES polygons (id)
);

CREATE SEQUENCE alt_names_id_seq;
CREATE TABLE alt_names (
    place_id        integer REFERENCES all_places (id),
    lang            char(3),
    name            text,
    PRIMARY KEY     (place_id, lang, name)
);