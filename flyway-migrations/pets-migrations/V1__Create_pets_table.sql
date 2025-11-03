CREATE TABLE IF NOT EXISTS cats (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    date_of_birth DATE NOT NULL,
    breed VARCHAR(255) NOT NULL,
    color VARCHAR(255),
    owner_id UUID NOT NULL
);

CREATE TABLE cat_friends (
    first_cat_id UUID REFERENCES cats(id),
    second_cat_id UUID REFERENCES cats(id),
    PRIMARY KEY (first_cat_id, second_cat_id)
);
