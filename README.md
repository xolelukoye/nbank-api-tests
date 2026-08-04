# NBank API Tests

## Test Coverage

### 00_Preconditions
- Admin log in
- Create a new user1
- Create a new user2
- User1 can log in
- User2 can log in
- User1 create account1
- User1 create account2
- User2 create account1

### 01_Deposit
- User1 can deposit to account1 with 5000
- User1 can deposit to account1 with 1
- User1 can't deposit to account1 with -100
- User1 can't deposit to account1 with 0
- User1 can't deposit to account1 with 5001
- User1 can't deposit to user2's account
- User1 can't deposit to a non-existent account
- User1 can't deposit to account without token

### 02_Transfer
- User1 can transfer between their accounts
- User1 can transfer to user2's account
- User can't transfer a negative amount
- User can't make a zero-sum transfer
- User can't transfer over the maximum amount
- User can't transfer an amount greater than the account balance
- User can't transfer from a non-existent account
- User can't transfer to a non-existent account
- User can't transfer without token

### 03_Profile
- Update profile with a valid two-word name
- Invalid one-word name
- Invalid name with only spaces
- Invalid name with numbers
- Invalid name with special characters
- Invalid name with more than two words
- Invalid empty name
- User can't change name without token