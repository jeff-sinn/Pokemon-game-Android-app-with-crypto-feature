class User:
  def __init__(self, publicKey, privateKey, currency):
    self.publicKey = publicKey
    self.privateKey = privateKey
    self.currency = currency
    self.bank = 0
    self.status = "Null"

class Signature:
  def __init__(self, message, key):
    self.message = message
    self.key = key


class Record:
  def __init__(self, fromAcc, toAcc, item, cost):   #constructor
    self.fromAcc = fromAcc
    self.toAcc = toAcc
    self.item = item
    self.cost = cost
    for user in users:
      if(user.publicKey == fromAcc):
        self.signiture = Signature(str(self.fromAcc + self.toAcc + (self.item * (self.cost * 10))),
                                    user.privateKey)
  
  @property
  def Signiture(self):
    return self.signiture

  def isCorrectSigniture(self, fromAccPrivateKey):
    return fromAccPrivateKey == self.signiture.key

  def __print__(self):
    print("Record: /nFrom:{}  To:{}  Item:{}  Cost:{}/nMessageID(for signiture):{}".format(self.fromAcc, self.toAcc, self.item, self.value, self.signiture.message))



class MarketItem:
  def __init__(self, offer, itemID, itemCost):
    self.offer = offer
    self.itemID = itemID
    self.itemCost = itemCost
    self.reciever = ""
    

class MarketList:
  def __init__(self):
    self.content = []

  def AddItem(self, marketItem : MarketItem):
    self.content.append(marketItem)



class Block:
  def __init__(self, blockID):
    self.records = []
    self.blockID = blockID
    self.validator = None
    self.isVerified = False
    self.prevHash = None

  @property
  def hashValue(self):
    return self._hashValue

  def setValidator(self, validator):
    self.isVerified = True
    self.validator = validator
  
  def AddRecord(self, record):
    print(len(self.records))
    if(len(self.records) < 4):
      self.records.append(record)
    else:
      self.records.append(record)
      print("Block Fulled! Now calculate the hash value")
      self.calculateHash()
  
  def calculateHash(self):
    self._hashValue = 0
    for record in self.records:
        self._hashValue += int(record.item) * int(record.fromAcc) * int(record.toAcc)
    self._hashValue = int(self._hashValue) % 151 + int(self.blockID) % 150
    print("Done calculating hash value: " + str(self._hashValue))

  def setPrevHash(self, prevHash):
    self.prevHash = prevHash
  
  def GuessOrGettingPokemon(self, user):
    if(not self.isVerified):
      method = input("Guess or GetPokemon")
      if(method == "Guess"):
        num = input("Guess a num (1 - 150): ")
        if(int(num) == self.hashValue):
          print("Verify Success!")
          self.setValidator(user)
      elif(method == "GetPokemon"):
        print("You have catched a legendary, Verify Success")
        self.setValidator(user)
  
  def isFull(self):
    if(len(self.records) >= 5):
      return True
    else:
      return False
      



class BlockChain:
  def __init__(self):
    self.chain = []
  
  def AddBlock(self, block : Block):
    if(len(self.chain)>0):
      block.setPrevHash(self.chain[len(self.chain)-1].hashValue)
    self.chain.append(block)



blockChain = BlockChain()
users = []
marketList = MarketList()
# currentBlockID = 0
verifyAwaitBlockID = 0

import random

def PublicToPrivate(public):
  for user in users:
    if user.publicKey == public:
      return user.privateKey
    

import copy
def CheckTransitionValidation(block, newrecord):
  tempUser = copy.deepcopy(users)

  for prevrecord in block.records:
    for user in tempUser:
      if( user.publicKey == prevrecord.fromAcc):
        user.currency -= prevrecord.cost
      if( user.publicKey == prevrecord.toAcc):
        user.currency += prevrecord.cost
  for user in tempUser:
    if( user.publicKey == newrecord.fromAcc):
      user.currency -= newrecord.cost
      if(user.currency < 0):
        return False
  return True

def AddItemToMarket(offerAcc, item, value):
  marketList.AddItem(MarketItem(offerAcc, item, value))

def isPurchaseValid(buyerAcc, marketItem):
  if(CheckTransitionValidation(blockChain.chain[len(blockChain.chain)-1], Record(buyerAcc, 0, marketItem.itemID, marketItem.itemCost))):
    print("Transition Valid")
    pw = input("Enter the buyer's private key: ")
    if(pw == str(PublicToPrivate(buyerAcc))):
      blockChain.chain[len(blockChain.chain)-1].AddRecord(Record(buyerAcc, 0, marketItem.itemID, marketItem.itemCost))
      
    else:
      return False
    return True
  else:
    return False

def AddCoinToBank(offerAcc, marketItem):
  for user in users:
    if user.publicKey == offerAcc:
      user.bank += marketItem.itemCost
      blockChain.chain[len(blockChain.chain)-1].AddRecord(Record(0, offerAcc, marketItem.itemID, marketItem.itemCost))

def DeleteMarketItemFromList(pos):
  marketList.content.pop(pos)

def CheckBlockFull():
  if(len(blockChain.chain[len(blockChain.chain)-1].records) == 5):
    print("\n---New Add a new block since the current block is fulled\n")
    newBlock = Block(len(blockChain.chain))
    blockChain.AddBlock(newBlock)
    PrintBlockChainDetail()

def VerifyBlock():
  for block in blockChain.chain:
    while(not block.isVerified and block.isFull()):
      userNum = input("Which user are you?")
      block.GuessOrGettingPokemon(userNum)
    
      print("\n\n\nYou Have successfully become the validator of this block.\n")
      verifyAwaitBlockID 
  PrintBlockChainDetail()





def PrintUserList():
  print("-----UserList--------------------------------")
  for user in users:
    print("User({})     PublicKey:{}     PrivateKey:{}     Currency:{}     Bank:{}".format(user.status, user.publicKey, user.privateKey, user.currency, user.bank))
  print("-----UserList--------------------------------\n\noffer")

def PrintMarketList():
  print("-----MarketList------------------------------")
  print("if id == 0 -> means its market")
  i = 0
  for marketItem in marketList.content:
    print("{}.Item:{}     Value:{}     Offer by:{}".format(i, marketItem.offer, marketItem.itemID, marketItem.itemCost))
    i += 1
  print("-----MarketList------------------------------\n\n")

def PrintBlockChainDetail():
  print("-----BlockChainDetail------------------------")
  i = 0
  for block in blockChain.chain:
    print("(VerifyState: {}) Block {}: ".format(str(block.isVerified), i))
    for record in block.records:
      print("From: {}     To: {}    Item:{}     Cost:{}".format(record.fromAcc, record.toAcc, record.item, record.cost))
    print("Validator: " + str(block.validator))
    i += 1
  print("-----BlockChainDetail------------------------\n\n")





def main():
  print("BlockChain Simulator\n\n")
  print("---------------Initialisation-------------")
  print("First we create some Users (randomly)")

  while(len(users) <= 5):
    users.append(User(len(users)+1, random.randint(1000,9999), random.randint(1,10)))
  PrintUserList()

  print("Then we create the first block(id = 0)")

  block = Block(len(blockChain.chain))
  blockChain.AddBlock(block)
  PrintBlockChainDetail()
  print("---------------StartSimulation-------------")

  while(True):
    
  
    print("*** User's data will only update AFTER a block is full and is validated")
    PrintUserList()

    #GET INPUT (sender, reciever, itemID, itemCost)
    isValidInput = False
    
    while(not isValidInput):
      nextStep = input("1. Add an item to market\n2. Purchase an item from market\nPlease input 1 or 2: ")
      if(nextStep == "1" or nextStep == "2"):
        isValidInput = True
    isValidInput = False


    #Add Item to Market
    if(nextStep == "1"):
      sender = None
      while(not isValidInput):
        sender = int(input("Enter sender's publicKey: "))
        if(sender > 6 or sender < 1):
          isValidInput = False
        else:
          isValidInput = True
      isValidInput = False
      item = int(input("Enter item ID: "))
      value = int(input("Enter the item's cost: "))
    
      AddItemToMarket(sender, item, value)
      PrintMarketList()


    #Buy Item from Market
    if(nextStep == "2"):
      buyer = None
      while(not isValidInput):
        buyer = int(input("Enter buyer's publicKey: "))
        if(buyer > 6 or buyer < 1):
          isValidInput = False
        else:
          isValidInput = True
      isValidInput = False

      PrintMarketList()
      while(not isValidInput):
        chosenMarketItem = int(input("Please choose an item to buy"))
        if(chosenMarketItem >= len(marketList.content)):
          isValidInput = False
        else:
          isValidInput = True
      isValidInput = False
      if(isPurchaseValid(buyer, marketList.content[chosenMarketItem])):
        CheckBlockFull()
        AddCoinToBank(marketList.content[chosenMarketItem].offer, marketList.content[chosenMarketItem])
        CheckBlockFull()
        DeleteMarketItemFromList(chosenMarketItem)
      else:
        print("Someone's currency < 0 in this record / in this block \nOR\n incorrect private Key")

    PrintBlockChainDetail()

    VerifyBlock()
    

main()