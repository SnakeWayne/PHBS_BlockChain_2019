# PHBS_BlockChain_2019
blockchian homework
The Name is 沈廷威


## homework1 description

**warning:** This project is a maven project so the source codes are under the path `src/main/java` and the test cases are under `src/test/java`.

---
### Summary
#### For TxHandler I implement 5 method
- 3 of them are required ones, the one **public boolean assetverify(UTXO utxo)** I added is used to verify the UTXOPool's update in the Test, the another **public boolean handleTx(Transaction tx)**  is added for the simplicity of the **public Transaction[] handleTxs(Transaction[] possibleTxs)** . Also a private member **private UTXOPool handler_utxoPool;** is added.


- The first two methods is fulfilled under the guidance of the annotation，and for the third one we verify the invalidness of a transaction in **FCFS** order. If it is validate we will remove the original old UTXO and add a new one. So a double spending will be rejected.

---
### Test case design

**The function of each desing is to test a certain senario,which can be classified into valid/invalid ones. Before each transaction a method **public void before()** is called with using @Before to initialize the assets for Users A,B,C.     assertThat（） is used to check is expected equals to actual**


+ Valid
    + 1.**public void testNormalSingalInSingalOut()**  
    verify if a single input single output transaction is normal
    + 2.**public void testJointPayment()**  
    verify if a joint payment transaction transaction is normal
    + 3.**public void testNormalSingalInMultiOut()**  
    verify if a SingalInMultiOut transaction transaction is normal
    + 4.**testNormalMultiInMultiOut()**  
    verify if a MultiInMultiOut transaction transaction is normal
    + 5.**public void testSubsequentTransaction()**  
    verify if subsequent transactions is normal(if A->B then B use the money A paid him to pay C)

+ Invalid
    + 1.**public void testInvalidSignature()**  
    verify if a transaction with invalid signature will be reject or not
    + 2.**public void testNegativeInput()**  
    verify if a transaction with negative input will be reject or not
    + 3.**public void testDoubleSpending()**  
    verify if a transaction double spending will be reject or not
    + 4.**public void testInvalidPrevious()**  
    verify if a transaction with a invalid previous output will be reject or not（invalid means a tx's hashcode is not in the UTXOPool）
    + 5.**public void testInputShortage()**  
    verify if a transaction input shortage will be reject or not（input value < output value）
    


---
---
--- 

## homework2 description

**warning:** This project is a maven project so the source codes are under the path `src/main/java` and the test cases are under `src/test/java`.

---


### Summary
#### Modification on some of the code provided
During the programming，I made some necessary adjustment to the given codes, among some of which contain pretty serious bugs.

- Adding some random noise while generate the hash of block or the Txs in **Block.java and Transaction.java**   
In `Block.java` adding something like:
```java {.line-numbers}
Random random = new Random();
byte[] randomnoise = random.doubles().toString().getBytes();
for (int i = 0; i < randomnoise.length; i++)
    rawBlock.add(randomnoise[i]);
```
This is necessary because when a miner mined two blocks,the ***hash of the coinbase Tx*** remains the same,so the ***UTXOPool*** will only record one ***UTXO***,this random is used to separate the UTXOs from each other.

- Adding a ***public BlockChain getBlockChain()*** method in **BlockHandler.java** for Test purpose

- Change the **HashMap** to **LinkedHashMap** in **TransactionPool** for the purpose that Transaction's order is in the same order as we adding into the chain.

- Adding some tx verification in ***public boolean processBlock(Block block)*** because inside **BlockChain.java** there is no tx verification which need to be done in **BlockHandler.java**.Not like ***public Block createBlock(PublicKey myAddress)*** where the valid txs will be reserved,the block will be rejected as a whole.



#### Class Design
##### Data member
- ***private LinkedHashMap<ByteArrayWrapper,Block> blocks***
To save all the blocks on the chain,whether the main branch or the side branch,easily to iterate back to get a branch from a certain block

- ***private LinkedHashMap<ByteArrayWrapper,Integer> endBlocksHeight***
To save the height of a branch,the key is the ByteArayWrapper of the end block's hash

-  ***private LinkedHashMap<ByteArrayWrapper,UTXOPool> utxopoolForEachBranch***
To save the utxopool of a certain branch,the key is the ByteArayWrapper of the end block's hash


- ***private TransactionPool transactionPool***
To save the transactions for creating the next block,receive the transactions from `public void processTx(Transaction tx) {
        blockChain.addTransaction(tx);
    }`


##### UTXOPool Generation
For each branch,there is a corresponding UTXOPool.The Key is the hash of the last block of a branch.If the new block is appended on a existing branch,we can renew the **utxopoolForEachBranch**'s certain pair.
Otherwise,we may need to get the new branch and generate the correspongding UTXOPool and put it into the **utxopoolForEachBranch**

##### Age indicator
Since the **Block** or **Transation** don't have a certain data member to represent age of the block or the tx,we represent the age with the order they appear in the **LinkedHashMap**, which is the reason we use it instead of HashMap.

##### How to reorganize to meet the max volume limit
The Limitation to block is 40，so when it reach the upperbond we do the following thins
1. select the longest(if same then oldest) branch to be the main branch to be
reserved,discard the side branches(no matter if the are still be able to be appended)
2. if the main branch is still too long(length>10),then we travel the main branch from beginning,discarding the transactions which their outputs has been used up,the remaining transactions a being added to the new root block.  
**Attention:**
The irrational part is that adding new transactons will compromise the hash of the root block，so we choose not to call the ***finalize()*** function to pretend the hash remains the same.Otherwise the root node will be unreachable or the blocks hash will all be changed,we have to compromise to realize the function.Authoring the root node to have a privilege that hash is inconsistent with the meta data seem like quite a solution.
---
### Test case design

<font size="4" >In this section the Test Cases can be devided into two parts,single branch just using </font>***public Block createBlock(PublicKey myAddress)*** <font size="4">,or the multibranches using </font> ***public void processTx(Transaction tx)*** .


+ single branch
    + 1.***public void testGenesisBlockInitialization()***
    verify if the blockchain can be initialized with a genesis block correcttly
    + 2.***public void testCommonTransaction()***  
    verify if a normal transaction is handled normally
    + 3.***public void testInvalidTransactionInOneBlock()***  
    verify if transactions which start with a valid transaction and then subsequent invalid ones(double spending) in one block will be handled normally
    + 4.***testInvalidTransactionInMultiBlock()***  
    verify if transactions which start with a valid transaction and then subsequent invalid ones(double spending) in multi blocks will be handled normally
    + 5.***public void testBlockChainCapacity()***  
    verify if adding more than 50 block will the block be reorganized returning the correct length and the unspent transactions being reserved.
    + 6.***public void testBlockChainDumpTxDuringReorganization()***  
    verify if adding more than 50 block will the block be reorganized returning the correct length and the spent transactions being discarded.

+ multibranches
    + 1.***public void testBlockChainSimpleBranch()***  
    verify if a valid branch action will run normally
    + 2.***public void testBlockChainInvalidPreviousBranch()***  
    verify if a branch action adding the block with invalid previous hash will be reject or not
    + 3.***public void testBlockChainInsertPositionTooSmallBranch()***  
    verify if a branch action wanting to add the block which height is too small(< Maxheight-CUT_OFF_AGE) will be reject or not
    + 4.***public void testBlockChainReorganizeWithBranch()***  
    verify if the side branches will be discarded or not while Reorganization
    + 5.***public void testBlockChainInvalidTxBranch()***  
    verify if using the ProcessBlock methon will reject a block which contains invalid txs as a whole

    
    
