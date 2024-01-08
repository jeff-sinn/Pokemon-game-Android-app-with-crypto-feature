from flask import Flask, request
import random, time, sqlite3
from sqlite_helper import *
from collections import Counter
app = Flask(__name__)

#temp variables
player_inv = [0, 15, 10, 5, 0, 1, 0, 0, 0]
#player_inv: [balance, pokeball, greatball, ultraball, permierball, masterball, repel(+shortly percentage of high rarity, amulet coin(+price per encounter), shiny charm(+permanent percentage of high rarity), repel_effect_left]

#dictionary
item_price = {1:200, 2:500, 3:1500, 5:100000, 6:1500, 7:1000000, 8:2500000}

#lists
common = [10, 13, 16, 19, 21, 23, 27, 29, 32]
uncommon = [1, 4, 7, 11, 14, 17, 20, 22, 24, 25, 28, 30, 33]
rare = [2, 5, 8, 12, 15, 18, 26, 31, 34]
epic = [3, 6, 9]
legendary = [144, 145, 146, 150, 151]
shiny = [152]
pokedex = [common, uncommon, rare, epic, legendary, shiny]
nameList = ["Missing No.", "Bulbasaur", "Ivysaur", "Venusaur", "Charmander", "Charmeleon", "Charizard", "Squirtle", "Wartortle", "Blastoise", "Caterpie", "Metapod", "Butterfree", "Weedle", "Kakuna", "Beedrill", "Pidgey", "Pidgeotto", "Pidgeot", "Rattata", "Raticate", "Spearow", "Fearow", "Ekans", "Arbok", "Pikachu", "Raichu", "Sandshrew", "Sandslash", "Nidoran (female)", "Nidorina", "Nidoqueen", "Nidoran (male)", "Nidorino", "Nidoking", "Clefairy", "Clefable", "Vulpix", "Ninetales", "Jigglypuff", "Wigglytuff", "Zubat", "Golbat", "Oddish", "Gloom", "Vileplume", "Paras", "Parasect", "Venonat", "Venomoth", "Diglett", "Dugtrio", "Meowth", "Persian", "Psyduck", "Golduck", "Mankey", "Primeape", "Growlithe", "Arcanine", "Poliwag", "Poliwhirl", "Poliwrath", "Abra", "Kadabra", "Alakazam", "Machop", "Machoke", "Machamp", "Bellsprout", "Weepinbell", "Victreebel", "Tentacool", "Tentacruel", "Geodude", "Graveler", "Golem", "Ponyta", "Rapidash", "Slowpoke", "Slowbro", "Magnemite", "Magneton", "Farfetch'd", "Doduo", "Dodrio", "Seel", "Dewgong", "Grimer", "Muk", "Shellder", "Cloyster", "Gastly", "Haunter", "Gengar", "Onix", "Drowzee", "Hypno", "Krabby", "Kingler", "Voltorb", "Electrode", "Exeggcute", "Exeggutor", "Cubone", "Marowak", "Hitmonlee", "Hitmonchan", "Lickitung", "Koffing", "Weezing", "Rhyhorn", "Rhydon", "Chansey", "Tangela", "Kangaskhan", "Horsea", "Seadra", "Goldeen", "Seaking", "Staryu", "Starmie", "Mr. Mime", "Scyther", "Jynx", "Electabuzz", "Magmar", "Pinsir", "Tauros", "Magikarp", "Gyarados", "Lapras", "Ditto", "Eevee", "Vaporeon", "Jolteon", "Flareon", "Porygon", "Omanyte", "Omastar", "Kabuto", "Kabutops", "Aerodactyl", "Snorlax", "Articuno", "Zapdos", "Moltres", "Dratini", "Dragonair", "Dragonite", "Mewtwo", "Mew", "Shiny Ponyta"]
rarity = ["common", "uncommon", "rare", "epic", "legendary", "shiny"]
reverse_rarity = ["Shiny", "Legendary", "Epic", "Rare", "Uncommon", "Common"]

def check_rarity(pid):
    if pid in common:
        return "Common"
    elif pid in uncommon:
        return "Uncommon"
    elif pid in rare:
        return "Rare"
    elif pid in epic:
        return "Epic"
    elif pid in legendary:
        return "Legendary"
    elif pid in shiny:
        return "Shiny"
    else:
        return "Unknown"

#Encounter
@app.route('/encounter/<int:user_id>')
def encounter_pokemon(user_id):
    result = []
    #if(repel)
    roll_add = 0
    if get_item_amount(user_id, 9) > 0:
        update_item_amount(user_id, 9, -1)
        roll_add += 100
    #else
    roll=random.randint(0,1001) + roll_add
    # if get_item_amount()
    if(roll<400): rarity = 0 
    elif(roll<700): rarity = 1
    elif(roll<970): rarity = 2 
    elif(roll<990): rarity = 3
    elif(roll<999): rarity = 4
    else: rarity = 5

    roll = random.randint(0 , len(pokedex[rarity])-1)
    spriteId = pokedex[rarity][roll]
    #write to database: encounter id
    timee = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime(time.time())) 
    value = (user_id, spriteId, timee)
    e_id = insert_data_encounter(value)
    # data = retrieve_data('Encounter')
    return str(e_id)+" "+str(rarity)+" "+str(spriteId)

    #get from database
    # for i in range(0, 6) : result.append(str(player_inv[i]))
    # result.extend([str(rarity), str(spriteId), nameList[spriteId]])
        
@app.route('/capture/<int:e_id>/<ball>')
def catch_pokemon(e_id, ball):
    result = []
    pid = find_pid_encounter(e_id)
    uid = encounter_uid(e_id)
    # print(pid)
    count = 0
    for category in pokedex:
        if pid in category:
            rarity = count
            break
        count += 1

    if(rarity==0): catch_rate = 70 
    elif(rarity==1): catch_rate = 60 
    elif(rarity==2): catch_rate = 37 
    elif(rarity==3): catch_rate = 20 
    elif(rarity==4): catch_rate = 5 
    else: catch_rate = 0

    if(rarity==0): coin_earned = random.randint(125, 325) 
    elif(rarity==1): coin_earned = random.randint(250, 525) 
    elif(rarity==2): coin_earned = random.randint(450, 875) 
    elif(rarity==3): coin_earned = random.randint(1200, 2700) 
    else: coin_earned = random.randint(10000, 25000)
    # coin_earned *= (1+0.1*player_inv[7])
    coin_earned *= (1+0.1* float(get_item_amount(uid, 7)))
    coin_earned = int(coin_earned)
    #write to database

    if(ball=="Pokeball") and get_item_amount(uid, 1) > 0: ballRate = 10; update_item_amount(uid, 1, -1)
    elif(ball=="Greatball") and get_item_amount(uid, 2) > 0: ballRate = 25; update_item_amount(uid, 2, -1)
    elif(ball=="Ultraball") and get_item_amount(uid, 3) > 0: ballRate = 35; update_item_amount(uid, 3, -1)
    elif(ball=="Premerball") and get_item_amount(uid, 4) > 0: ballRate = 50; update_item_amount(uid, 4, -1)
    elif(ball=="Masterball") and get_item_amount(uid, 5) > 0: ballRate = 100; update_item_amount(uid, 5, -1)
    else: ballRate = 0
    
    if((catch_rate+ballRate)>100): catch_rate = 100 
    else: catch_rate = catch_rate+ballRate
    print(catch_rate)
    
    roll = random.randint(0, 100)
    if(catch_rate<roll): coin_earned = 0 #catch fail
    else: 
        update_item_amount(uid, 0, coin_earned)
        value = (e_id, pid)
        insert_data_pc(value)
        pc_id = find_pc_id(e_id)
        u_id = find_uid_encounter(e_id)
        if pc_id != "Unfound" and u_id != "Unfound":
            value = (pid, u_id, pc_id, True)
            insert_data_up(value)
            data = retrieve_data('User_pokemon')
            print(data)
            return [coin_earned, roll, catch_rate]
            #return data
    return [coin_earned, roll, catch_rate]
    #return "fail"
    #else if roll <= catch rate >> catch success

    #write to database: coin + pokemon card
    # player_inv[0]+=coin_earned
    # for i in range(0, 6) : result.append(str(player_inv[i]))
    # result.extend([str(coin_earned), str(roll), str(catch_rate)])
    # return " ".join(result)

#Check box/History
@app.route('/check_box/<int:sort_type>/<int:u_id>')
def check_box(sort_type, u_id):
    result = []
    pid_arr = pid_from_upuid(u_id)
    if pid_arr == []:
        return []
    if sort_type == 1:
        #sort by rarity
        sort_arr = sorted(pid_arr)

        for i, category in enumerate(pokedex):
            for pid in sort_arr:
                if pid in category:
                    inner = []
                    inner.append(pid)
                    result.append(pid)
                    

        return result #sorted pokemon box
    elif sort_type == 2:
        # Count the frequency of each element
        counter = Counter(pid_arr)

        # Sort the elements by decreasing frequency, then by the element itself
        sorted_arr = sorted(pid_arr, key=lambda x: (-counter[x], x))
        #sort by amount
        return sorted_arr
        #sorted pokemon box
    elif sort_type == 3:
        #sort by pokedex
        return sorted(pid_arr)
        return #sorted pokemon box
    else:#can cancel?
        #sort by date

        return #sorted pokemon box

@app.route('/check_pokebox/<int:u_id>')
def check_pokebox(u_id): 
    # Count the frequency of each element
    pid_arr = pid_from_upuid(u_id)
    if pid_arr == []:
        return []
    else:
        freq_dict = Counter(pid_arr)

        # Step 2: Sort unique elements by frequency in descending order
        unique_elements = sorted(set(pid_arr), key=lambda x: freq_dict[x], reverse=True)

        # Step 3: Sort the array using the custom sorting function
        sorted_arr = sorted(pid_arr, key=lambda x: (freq_dict[x], -x), reverse=True)

        temp = list(dict.fromkeys(sorted_arr))

        result = [[element, freq_dict[element]] for element in temp]
        

        for i, arr in enumerate(result):
            rarity = check_rarity(arr[0])
            result[i].append(rarity)
            #result[i][0] = nameList[result[i][0]]
        final_arr = []
        
        for rarityy in reverse_rarity:
            for arra in result:
                if arra[2] == rarityy:
                    final_arr.append(arra)

        return final_arr

@app.route('/add_bank_change_market/<int:offeruser_Id>/<int:pc_id>/<int:cost>/<int:getuser_id>')
def add_bank_change(offeruser_Id, pc_id, cost, getuser_id):
    if get_item_amount(getuser_id, 0) < cost:
        return "Not enough money"
    update_item_amount(offeruser_Id, 10, cost)
    update_item_amount(getuser_id, 0, -cost)
    # remove_pid = delete_user_pokemon(offeruser_Id, pc_id)
    value = (get_pc_p_id(pc_id), getuser_id, pc_id, True)
    insert_data_up(value)
    update_market_status(pc_id, offeruser_Id)
    return "Success"
    

    


@app.route('/check_history/<int:u_id>/<int:times_called>')
def check_history(u_id, times_called):
    #50 latest records to player
    #e.g. first time call return nearest 1-50
    #second time return 51-100 etc.

    conn = sqlite3.connect('pokemon.db')
    c = conn.cursor()

    query = """
    SELECT e_p_id, e_encounter_time
    FROM Encounter
    WHERE e_u_id = ?
    ORDER BY e_encounter_time DESC;
    """
    c.execute(query, (u_id,))
    
    result = c.fetchall()

    if 50 * times_called + 1 > len(result):
        c.close()
        conn.close()
        return []
    else:
        if (times_called+1) * 50 <= len(result):
            start_index = 50 * times_called
            last_index = (times_called+1) * 50 + 1
            selected_arr = result[start_index:last_index]
        else:
            start_index = 50 * times_called
            last_index = len(result)
            selected_arr = result[start_index:last_index]
        return_arr = []
        for row in selected_arr:    
            return_arr.append(row)
        c.close()
        conn.close()
        return return_arr


@app.route('/personal_rarespawn/<int:u_id>/<int:times_called>')
def personal_rarespawn(u_id, times_called):
    conn = sqlite3.connect('pokemon.db')
    c = conn.cursor()

    query = """
    SELECT e_p_id, e_encounter_time
    FROM Encounter
    WHERE e_u_id = ?
    ORDER BY e_encounter_time DESC;
    """
    c.execute(query, (u_id,))
    
    result = c.fetchall()
    fil_pidarr = []
    for row in result:
        if row[0] in legendary or row[0] in shiny:
            fil_pidarr.append(row)

    if 50 * times_called + 1 > len(fil_pidarr):
        c.close()
        conn.close()
        return []
    else:
        if (times_called+1) * 50 <= len(fil_pidarr):
            start_index = 50 * times_called
            last_index = (times_called+1) * 50 + 1
            selected_arr = fil_pidarr[start_index:last_index]
        else:
            start_index = 50 * times_called
            last_index = len(fil_pidarr)
            selected_arr = fil_pidarr[start_index:last_index]
        return_arr = []
        for row in selected_arr:
            return_arr.append(row)
        c.close()
        conn.close()
        return return_arr

    return #50 latest legendary/shiny records by player to player

@app.route('/global_rarespawn/<int:times_called>')
def global_rarespawn(times_called):
    #50 latest legendary/shiny global records to player

    conn = sqlite3.connect('pokemon.db')
    c = conn.cursor()

    query = """
    SELECT e_p_id, e_encounter_time
    FROM Encounter
    ORDER BY e_encounter_time DESC;
    """
    c.execute(query)
    
    result = c.fetchall()

    fil_pidarr = []
    for row in result:
        if row[0] in legendary or row[0] in shiny:
            fil_pidarr.append(row)

    if 50 * times_called + 1 > len(fil_pidarr):
        c.close()
        conn.close()
        return []
    else:
        if (times_called+1) * 50 <= len(fil_pidarr):
            start_index = 50 * times_called
            last_index = (times_called+1) * 50 + 1
            selected_arr = fil_pidarr[start_index:last_index]
        else:
            start_index = 50 * times_called
            last_index = len(fil_pidarr)
            selected_arr = fil_pidarr[start_index:last_index]
        return_arr = []
        for row in selected_arr:    
            return_arr.append(row)
        return return_arr

#Release pokemon
@app.route('/release/<int:u_id>/<int:p_id>/<int:amount>')
def release(p_id, amount): #release certain amount of pokemon of the same dex id
    #find pokemon by dex id
    #find rarity
    count = 0
    for category in pokedex:
        if p_id in category:
            rarity = count
            break
        count += 1
    #delete
    # rarity = 0 #temp
    if(rarity==0): player_inv[0] += 150*amount
    elif(rarity==1): player_inv[0] += 300*amount
    elif(rarity==2): player_inv[0] += 500*amount
    elif(rarity==3): player_inv[0] += 1000*amount
    elif(rarity==4): player_inv[0] += 10000*amount
    else: player_inv[0] += 25000*amount
    return " ".join([str(int) for int in player_inv])
    #return player bal and updated player pokemon box


@app.route('/release_card/<int:pc_id>')
def release_card(pc_id): #release a single pokemon card
    #find pokemon by card id
    #find rarity
    #delete
    rarity = 0 #temp
    if(rarity==0): player_inv[0] += 150
    elif(rarity==1): player_inv[0] += 300
    elif(rarity==2): player_inv[0] += 500
    elif(rarity==3): player_inv[0] += 1000
    elif(rarity==4): player_inv[0] += 10000
    else: player_inv[0] += 25000
    return " ".join([str(int) for int in player_inv])
    #return player bal and updated player pokemon box

@app.route('/release_duplicate')#can cancel?
def release_duplicate():
    #release all pokemon that are duplicated, except legendary and shiny and favourited pokemon

    return " ".join([str(int) for int in player_inv])
    #return player bal and updated player pokemon box


#Inventory
@app.route('/check_bag/<int:u_id>')
def check_bag(u_id):
    return get_item_amounts(u_id)
    # return " ".join([str(int) for int in player_inv])


@app.route('/use_repel/<int:u_id>/<int:amount>')
def use_repel(u_id, amount):
    #repel status += 30*amount
    if get_item_amount(u_id, 6) >= amount:

        update_item_amount(u_id, 9, 30*amount)
        # player_inv[6]-=amount
        update_item_amount(u_id, 6, -amount)
        return get_item_amounts(u_id)
    else:
        return "Not enough repel items"
    return " ".join([str(int) for int in player_inv])


#Shop
@app.route('/shop_buy/<int:u_id>/<int:i_id>/<int:amount>')
def shop_buy(u_id, i_id, amount):
    # get from database
    if get_item_amount(u_id, 0)>=item_price[i_id]*amount: 
        #write to database
        # player_inv[0]-=item_price[i_id]*amount
        update_item_amount(u_id, 0, -item_price[i_id]*amount)
        # player_inv[i_id]+=amount
        update_item_amount(u_id, i_id, amount)

        #get from database
        return get_item_amounts(u_id)
    else:
        return "Not enough money"
    return " ".join([str(int) for int in player_inv])

#Login
@app.route('/login/<username>/<password>')
def login(username, password):
    if not check_username_exists(username):
        return "Username not exist!"
    if not check_password(username, password):
        return "Password incorrect"
    else:
        return [get_user_id(username), username, password] #return user_id, username if success

#Register
@app.route('/register/<username>/<password>')
def register(username, password):
    value = (username, password)
    if check_username_exists(username):
        return "Username already exist!"
    resultt = insert_data_users(value)
    # data = retrieve_data('Users')
    conn = sqlite3.connect('pokemon.db')
    c = conn.cursor()
    query = """
    SELECT u_id
    FROM Users
    WHERE u_username = ?;
    """
    c.execute(query, (username,))

    # Fetch the result
    result = c.fetchone()
    u_id = result[0]

    # query = """
    # INSERT INTO Player_status (ps_u_id, ps_u_balance, ps_repel_effect)
    # VALUES (?, 0, 0);
    # """
    # c.execute(query, (u_id,))
    # conn.commit()

    query = """
    INSERT INTO Bags (b_u_id, b_i_id, b_item_amount)
    VALUES (?, ?, 0);
    """ 
    for b_i_id in range(11):
        c.execute(query, (u_id, b_i_id))
    conn.commit()

    c.close()
    conn.close()
    update_item_amount(u_id, 1, 15)
    update_item_amount(u_id, 2, 5)
    update_item_amount(u_id, 3, 1)
    return resultt

@app.route('/market_list/<int:offerUserID>/<int:pc_id>/<int:pokemon_cost>/<status>')
def market_list(offerUserID, pc_id, pokemon_cost, status):
    insert_market_data(offerUserID, pc_id, pokemon_cost, status)
    pid = delete_user_pokemon(offerUserID, pc_id)
    return "Added market record successfully"

#market
#communitcate with both blockchain and database

@app.route('/get_market_list')
def get_market_list():
    result = get_market_data()
    if result:
        return result
    else:
        "No market data"

@app.route('/get_user_pokemon/<int:u_id>')
def get_user_poke(u_id):
    conn = sqlite3.connect('pokemon.db')
    cursor = conn.cursor()

    cursor.execute("SELECT up_p_id, up_pc_id FROM User_pokemon WHERE up_u_id = ?", (u_id,))
    result = cursor.fetchall()

    conn.close()
    for i, row in enumerate(result):
        result[i] = list(result[i])
        result[i].append(nameList[result[i][0]])
    return result

@app.route('/get_bank/<int:UserID>')
def get_bank(UserID):
    return str(get_item_amount(UserID, 10))

@app.route("/bank_to_currency/<int:UserID>")
def bank_to_currency(UserID):
    value = get_item_amount(UserID, 10)
    if value > 0:
        update_item_amount(UserID, 10, -value)
        update_item_amount(UserID, 0, value)
        return "Transferred to currency!"
    else:
        return "No money in bank"

@app.route('/get_currency/<int:UserID>')
def get_currency(UserID):
    return str(get_item_amount(UserID, 0))

if __name__ == '__main__':
    app.run(host='0.0.0.0')
    
