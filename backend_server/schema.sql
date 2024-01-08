CREATE TABLE Users (
    u_id INTEGER PRIMARY KEY,
    u_username VARCHAR(30),
    u_password VARCHAR(30)
);

CREATE TABLE Pokemon_cards(
    pc_id INTEGER PRIMARY KEY,
    pc_e_id INTEGER,
    pc_p_id INTEGER
);


CREATE TABLE Encounter(
    e_id INTEGER PRIMARY KEY,
    e_u_id integer,
    e_p_id integer,
    e_encounter_time VARCHAR(30)
);

CREATE TABLE User_pokemon(
    up_id INTEGER PRIMARY KEY,
    up_p_id integer,
    up_u_id integer,
    up_pc_id integer,
    up_is_active integer
);

CREATE TABLE Bags(
    b_u_id INTEGER,
    b_i_id INTEGER,
    b_item_amount INTEGER,
    PRIMARY KEY (b_u_id, b_i_id)
);

CREATE TABLE Player_status(
    ps_u_id INTEGER PRIMARY KEY,
    ps_u_balance INTEGER,
    ps_repel_effect INTEGER
);

CREATE TABLE Market(
    m_id INTEGER PRIMARY KEY,
    m_u_id_seller INTEGER,
    m_pc_id, INTEGER,
    m_price INTEGER,
    m_status VARCHAR(30)
);