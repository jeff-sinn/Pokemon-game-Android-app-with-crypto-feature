import sqlite3



# def create_table():
#     conn = sqlite3.connect('pokemon.db')

#     c = conn.cursor()

#     c.execute("""CREATE TABLE Users (
#         u_id INTEGER PRIMARY KEY,
#         u_username text,
#         u_password text
#         )""")

#     # c.execute("""CREATE TABLE Pokemon_cards(
#     #     pc_id INTEGER PRIMARY KEY,
#     #     pc_e_id integer
#     #     )""")

#     c.execute("""CREATE TABLE Encounter(
#         e_id INTEGER PRIMARY KEY,
#         e_u_id integer,
#         e_p_id integer,
#         e_encounter_time text
#     )""")
    
#     c.execute("""CREATE TABLE User_pokemon(
#         up_id INTEGER PRIMARY KEY,
#         up_p_id integer,
#         up_u_id integer,
#         up_pc_id integer,
#         up_is_active integer
#         )""")
#     conn.commit()
#     conn.close()

def insert_data_users(values):
    conn = sqlite3.connect('pokemon.db')

    c = conn.cursor()

    query = f"INSERT INTO Users (u_username, u_password) VALUES (?, ?)"

    c.execute(query, values)

    e_id = c.lastrowid

    conn.commit()
    conn.close()
    return [e_id, values[0], values[1]]

def insert_data_encounter(values):
    conn = sqlite3.connect('pokemon.db')

    c = conn.cursor()

    query = f"INSERT INTO Encounter (e_u_id, e_p_id, e_encounter_time) VALUES (?, ?, ?)"

    c.execute(query, values)
    e_id = c.lastrowid

    conn.commit()
    conn.close()
    return e_id

def insert_data_pc(values):
    conn = sqlite3.connect('pokemon.db')

    c = conn.cursor()

    query = f"INSERT INTO Pokemon_cards (pc_e_id, pc_p_id) VALUES (?, ?)"

    c.execute(query, values)

    conn.commit()
    conn.close()

def insert_data_up(values):
    conn = sqlite3.connect('pokemon.db')

    c = conn.cursor()

    query = f"INSERT INTO User_pokemon (up_p_id, up_u_id, up_pc_id, up_is_active) VALUES (?, ?, ?, ?)"

    c.execute(query, values)

    conn.commit()
    conn.close()

def find_pid_encounter(e_id):
    conn = sqlite3.connect('pokemon.db')

    c = conn.cursor()

    c.execute("SELECT e_p_id FROM Encounter WHERE e_id = ?", (e_id,))

    result = c.fetchone()

    c.close()
    conn.close()

    if result != None:
        return result[0]
    else:
        return "Unfound"

def find_pc_id(e_id):
    conn = sqlite3.connect('pokemon.db')

    c = conn.cursor()

    c.execute("SELECT pc_id FROM Pokemon_cards WHERE pc_e_id = ?", (e_id,))

    result = c.fetchone()

    c.close()
    conn.close()

    if result != None:
        return result[0]
    else:
        return "Unfound"

def find_uid_encounter(e_id):
    conn = sqlite3.connect('pokemon.db')

    c = conn.cursor()

    c.execute("SELECT e_u_id FROM Encounter WHERE e_id = ?", (e_id,))

    result = c.fetchone()

    c.close()
    conn.close()

    if result != None:
        return result[0]
    else:
        return "Unfound"

# def exist_pokemon(p_id, u_id):
#     conn = sqlite3.connect('pokemon.db')

#     c = conn.cursor()

#     query = """
#         SELECT up_is_active
#         FROM Encounter
#         JOIN User_pokemon ON Encounter.e_u_id = User_pokemon.up_u_id
#         WHERE Encounter.e_p_id = {0}
#         AND Encounter.e_u_id = {1};
#         """.format(p_id, u_id)

#     result = c.fetchone()

#     c.close()
#     conn.close()

#     if result != None:
#         return result[0]
#     else:
#         return "Unfound"
def pid_from_upuid(u_id):
    conn = sqlite3.connect('pokemon.db')

    c = conn.cursor()

    query = """
    SELECT up_p_id
    FROM USER_POKEMON
    WHERE up_u_id = ?
    AND up_is_active = 1;
    """

    result = conn.execute(query, (u_id,))
    if result == None:
        return []
        
    up_p_id_list = [row[0] for row in result.fetchall()]

    c.close()
    conn.close()

    if result != None:
        return up_p_id_list


def retrieve_data(table_name):
    # Establish the connection to the database
    conn = sqlite3.connect('pokemon.db')
    c = conn.cursor()

    # Prepare the SQL query
    query = f"SELECT * FROM {table_name}"

    # Execute the query and fetch all rows
    c.execute(query)
    rows = c.fetchall()

    # Close the connection
    conn.close()

    # Return the fetched rows
    return rows

def update_item_amount(u_id, i_id, amount):
    # Connect to the SQLite database
    conn = sqlite3.connect("pokemon.db")
    cursor = conn.cursor()

    # Execute the query
    query = """
        UPDATE Bags
        SET b_item_amount = b_item_amount + ?
        WHERE b_u_id = ? AND b_i_id = ?;
    """
    cursor.execute(query, (amount, u_id, i_id))

    # Commit the changes to the database
    conn.commit()

    # Close the database connection
    conn.close()

def add_to_balance(u_id, amount):
    # Connect to the SQLite database
    conn = sqlite3.connect("pokemon.db")
    cursor = conn.cursor()

    # Execute the query
    query = """
        UPDATE Player_status
        SET ps_u_balance = ps_u_balance + ?
        WHERE ps_u_id = ?;
    """
    cursor.execute(query, (amount, u_id))

    # Commit the changes to the database
    conn.commit()

    # Close the database connection
    conn.close()

def add_to_repel_effect(u_id, amount):
    # Connect to the SQLite database
    conn = sqlite3.connect("pokemon.db")
    cursor = conn.cursor()

    # Execute the query
    query = """
        UPDATE Player_status
        SET ps_repel_effect = ps_repel_effect + ?
        WHERE ps_u_id = ?;
    """
    cursor.execute(query, (amount, u_id))

    # Commit the changes to the database
    conn.commit()

    # Close the database connection
    conn.close()

def get_item_amount(u_id, i_id):
    # Connect to the SQLite database
    conn = sqlite3.connect("pokemon.db")
    cursor = conn.cursor()

    # Execute the query
    query = """
        SELECT b_item_amount
        FROM Bags
        WHERE b_u_id = ? AND b_i_id = ?;
    """
    cursor.execute(query, (u_id, i_id))

    # Fetch the result
    result = cursor.fetchone()

    # Close the database connection
    conn.close()

    # Return the item amount if it exists, otherwise return None
    if result:
        return result[0]
    else:
        return None

def get_item_amounts(b_u_id):
    # Connect to the SQLite database
    conn = sqlite3.connect("pokemon.db")
    cursor = conn.cursor()

    # Execute the query
    query = """
        SELECT b_item_amount
        FROM Bags
        WHERE b_u_id = ? AND b_i_id BETWEEN 0 AND 10
        ORDER BY b_i_id;
    """
    cursor.execute(query, (b_u_id,))

    # Fetch the results
    results = cursor.fetchall()

    # Close the database connection
    conn.close()

    # Create an array of item amounts
    item_amounts = [result[0] for result in results]

    # Return the array of item amounts
    return item_amounts

def encounter_uid(e_id):
    conn = sqlite3.connect('pokemon.db')

    # Create a cursor object to execute SQL queries
    cursor = conn.cursor()

    # Execute the query to find e_u_id for a specific e_id
    cursor.execute("SELECT e_u_id FROM Encounter WHERE e_id = ?", (e_id,))

    # Fetch the result
    result = cursor.fetchone()
    cursor.close()
    conn.close()

    if result:
        return result[0]
    else:
        return None

    # Close the cursor and database connection
    
def check_username_exists(username):
    # Connect to the SQLite database
    conn = sqlite3.connect('pokemon.db')
    cursor = conn.cursor()

    # Execute the SQL query to check if the username exists
    query = "SELECT EXISTS(SELECT 1 FROM Users WHERE u_username = ?)"
    cursor.execute(query, (username,))
    result = cursor.fetchone()[0]

    # Close the database connection
    cursor.close()
    conn.close()

    # Return True if the username exists, False otherwise
    return bool(result)

def check_password(username, password):
    # Connect to the SQLite database
    conn = sqlite3.connect('pokemon.db')
    cursor = conn.cursor()

    # Execute the SQL query to check if the password is correct
    query = "SELECT EXISTS(SELECT 1 FROM Users WHERE u_username = ? AND u_password = ?)"
    cursor.execute(query, (username, password))
    result = cursor.fetchone()[0]

    # Close the database connection
    cursor.close()
    conn.close()

    # Return True if the password is correct, False otherwise
    return bool(result)

def get_user_id(username):
    # Connect to the SQLite database
    conn = sqlite3.connect('pokemon.db')
    cursor = conn.cursor()

    # Execute the SQL query to retrieve the user_id
    query = "SELECT u_id FROM Users WHERE u_username = ?"
    cursor.execute(query, (username,))
    result = cursor.fetchone()

    # Close the database connection
    cursor.close()
    conn.close()

    # Return the user_id if found, or None if not found
    if result:
        return result[0]
    else:
        return None

# def get_user_id(username):
#     # Connect to the SQLite database
#     conn = sqlite3.connect('your_database.db')
#     cursor = conn.cursor()

#     # Execute the SQL query to retrieve the u_id
#     query = "SELECT u_id FROM Users WHERE u_username = ?"
#     cursor.execute(query, (username,))
#     result = cursor.fetchone()

#     # Close the database connection
#     cursor.close()
#     conn.close()

#     # Return the u_id if found, or None if not found
#     if result:
#         return result[0]
#     else:
#         return None

def insert_market_data(seller_uid, pc_id, price, status):
    # Connect to the SQLite database
    conn = sqlite3.connect('pokemon.db')
    cursor = conn.cursor()

    # Execute the SQL query to insert data into the Market table
    query = "INSERT INTO Market (m_u_id_seller, m_pc_id, m_price, m_status) VALUES (?, ?, ?, ?)"
    cursor.execute(query, (seller_uid, pc_id, price, status))

    # Commit the changes to the database
    conn.commit()

    # Close the database connection
    cursor.close()
    conn.close()

def get_market_data():
    # Connect to the SQLite database
    conn = sqlite3.connect('pokemon.db')
    cursor = conn.cursor()

    # Execute the SQL query to retrieve data from the Market table and perform the necessary conversions
    query = "SELECT u.u_id, u.u_username, p.pc_p_id, m.m_pc_id, m.m_price, m.m_status " \
            "FROM Market m " \
            "INNER JOIN Users u ON m.m_u_id_seller = u.u_id " \
            "INNER JOIN Pokemon_cards p ON m.m_pc_id = p.pc_id"
    cursor.execute(query)
    results = cursor.fetchall()

    # Define an array to store the dictionaries
    market_data = []

    # Iterate through the query results
    for row in results:
        # Extract the values from the row
        u_id = row[0]
        username = row[1]
        p_id = row[2]
        pc_id = row[3]
        price = row[4]
        status = row[5]

        # Create a dictionary with the converted values
        data_dict = {
            'u_id': u_id,
            'u_username': username,
            'p_id': p_id,
            'pc_id': pc_id,
            'm_price': price,
            'm_status': status
        }

        # Append the dictionary to the array
        market_data.append(data_dict)

    # Close the database connection
    cursor.close()
    conn.close()

    # Return the array of dictionaries
    return market_data

def delete_user_pokemon(up_u_id, up_pc_id):
    # Connect to the SQLite database
    conn = sqlite3.connect('pokemon.db')
    cursor = conn.cursor()
    cursor.execute("SELECT up_p_id FROM User_pokemon WHERE up_u_id = ? AND up_pc_id = ?", (up_u_id, up_pc_id))
    up_p_id = cursor.fetchone()[0]
    # Delete the row based on up_u_id and up_pc_id
    cursor.execute("DELETE FROM User_pokemon WHERE up_u_id = ? AND up_pc_id = ?", (up_u_id, up_pc_id))
    
    # Get the corresponding up_p_id before deleting the row
    

    # Commit the changes and close the connection
    conn.commit()
    conn.close()

    return up_p_id

def update_market_status(pc_id, m_u_id_seller):
    # Connect to the SQLite database
    conn = sqlite3.connect('pokemon.db')
    cursor = conn.cursor()

    # Update the m_status column for the specific pc_id and m_u_id_seller
    cursor.execute("UPDATE Market SET m_status = 'Sold' WHERE m_pc_id = ? AND m_u_id_seller = ?", (pc_id, m_u_id_seller))
    
    # Commit the changes and close the connection
    conn.commit()
    conn.close()

def get_pc_p_id(pc_id):
    # Connect to the SQLite database
    conn = sqlite3.connect('pokemon.db')
    cursor = conn.cursor()

    # Select the pc_p_id for the specific pc_id
    cursor.execute("SELECT pc_p_id FROM Pokemon_cards WHERE pc_id = ?", (pc_id,))
    pc_p_id = cursor.fetchone()[0]

    # Close the connection
    conn.close()

    return pc_p_id