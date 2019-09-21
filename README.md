# PHBS_BlockChain_2019
blockchian homework
The Name is 沈廷威
##homework1 description

For TxHandler I implement 4 method

1. 

# PHBS_BlockChain_2019
blockchian homework
The Name is 沈廷威

## homework1 description

**warning:** This project is a maven project so the source codes are under the path `src/main/java` and the test cases are under `src/test/java`.

---
### summary
#### For TxHandler I implement 5 method
- 3 of them are required ones, the one **public boolean assetverify(UTXO utxo)** I added is used to verify the UTXOPool's update in the Test, the another **public boolean handleTx(Transaction tx)**  is added for the simplicity of the **public Transaction[] handleTxs(Transaction[] possibleTxs)** . Also a private member **private UTXOPool handler_utxoPool;** is added.


- The first two methods is fulfilled under the guidance of the annotation，and for the third one we verify the invalidness of a transaction in **FCFS** order. If it is validate we will remove the original old UTXO and add a new one. So a double spending will be rejected.

---
### Test case design

#### The function of each desing is to test a certain senario,which can be classified into valid/invalid ones. Before each transaction a method **public void before()** is called with using @Before to initialize the assets for Users A,B,C.     assertThat（） is used to check is expected equals to actual


+ Valid
    + 1. **public void testNormalSingalInSingalOut()**  
    verify if a single input single output transaction is normal
    + 2. **public void testJointPayment()**  
    verify if a joint payment transaction transaction is normal
    + 3. **public void testNormalSingalInMultiOut()**  
    verify if a SingalInMultiOut transaction transaction is normal
    + 4. **testNormalMultiInMultiOut()**  
    verify if a MultiInMultiOut transaction transaction is normal
    + 5. **public void testSubsequentTransaction()**  
    verify if subsequent transactions is normal(if A->B then B use the money A paid him to pay C)

+ Invalid
    + 1. **public void testInvalidSignature()**  
    verify if a transaction with invalid signature will be reject or not
    + 2. **public void testNegativeInput()**  
    verify if a transaction with negative input will be reject or not
    + 3. **public void testDoubleSpending()**  
    verify if a transaction double spending will be reject or not
    + 4. **public void testInvalidPrevious()**  
    verify if a transaction with a invalid previous output will be reject or not（invalid means a tx's hashcode is not in the UTXOPool）
    + 5.  **public void testInputShortage()**  
    verify if a transaction input shortage will be reject or not（input value < output value）
    
    
    
