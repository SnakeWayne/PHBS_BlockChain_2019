// Block Chain should maintain only limited block nodes to satisfy the functions
// You should not have all the blocks added to the block chain in memory 
// as it would cause a memory overflow.


import java.util.*;

public class BlockChain {
    public static final int CUT_OFF_AGE = 10;
    public static final int MAX_BLOCK_NUM = 40;

    private LinkedHashMap<ByteArrayWrapper,Block> blocks;
    private LinkedHashMap<ByteArrayWrapper,Integer> endBlocksHeight;
    private LinkedHashMap<ByteArrayWrapper,UTXOPool> utxopoolForEachBranch;
    private TransactionPool transactionPool;

    private int maxHeight;
    private int totalCount;

    public int getMaxHeight() {
        return maxHeight;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public LinkedHashMap<ByteArrayWrapper, Block> getBlocks() {
        return blocks;
    }

    public LinkedHashMap<ByteArrayWrapper, Integer> getEndBlocksHeight() {
        return endBlocksHeight;
    }

    /**
     * create an empty block chain with just a genesis block. Assume {@code genesisBlock} is a valid
     * block
     */
    public BlockChain(Block genesisBlock) {
        blocks = new LinkedHashMap<ByteArrayWrapper,Block>();
        endBlocksHeight = new LinkedHashMap<ByteArrayWrapper,Integer>();
        utxopoolForEachBranch = new LinkedHashMap<ByteArrayWrapper,UTXOPool>();
        transactionPool = new TransactionPool();
        maxHeight = 0;
        totalCount=0;
        blocks.put(new ByteArrayWrapper(genesisBlock.getHash()), genesisBlock);
        updateEndBlocksHeight(new ByteArrayWrapper(genesisBlock.getHash()));
        updateUTXOPoolForSingleBranch(new ByteArrayWrapper(genesisBlock.getHash()));
        updateMaxHeight();
        updateTransactionPool();
        updatetotalCount();

    }

    /** Get the maximum height block */
    public Block getMaxHeightBlock() {
        int current_max_height = 0;
        ByteArrayWrapper hash = null;
        Iterator iter = endBlocksHeight.entrySet().iterator();
        while (iter.hasNext()) {
        Map.Entry entry = (Map.Entry) iter.next();
        Object key = entry.getKey();
        Object val = entry.getValue();
        if((Integer)val>current_max_height){
            hash = (ByteArrayWrapper)key;
            current_max_height = (Integer)val;
        }
        }
        return blocks.get(hash);
    }

    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {
        int current_max_height = 0;
        ByteArrayWrapper hash = null;
        Iterator iter = endBlocksHeight.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Object key = entry.getKey();
            Object val = entry.getValue();
            if((Integer)val>current_max_height){
                hash = (ByteArrayWrapper)key;
            }
        }
        return utxopoolForEachBranch.get(hash);
    }

    /** Get the transaction pool to mine a new block */
    public TransactionPool getTransactionPool() {
        return transactionPool;
    }

    private LinkedHashMap<ByteArrayWrapper,Block> getEndBlocksBranch(ByteArrayWrapper hash){
//        if(totalCount==0){
//            return new LinkedHashMap<ByteArrayWrapper,Block>();
//        }
        List<Block> tempList = new ArrayList<Block>();
        LinkedHashMap<ByteArrayWrapper,Block> result = new LinkedHashMap<ByteArrayWrapper,Block>();
        while(true){
            Block tempBlock = (Block)(this.blocks.get(hash));
            tempList.add(tempBlock);
            //有可能删除区块时，把创世块都删掉了，所以不光要previousHash 存在，还要验证他是否还在链上
            if(tempBlock.getPrevBlockHash()!=null&&this.blocks.containsKey(new ByteArrayWrapper(tempBlock.getPrevBlockHash()))){
                hash = new ByteArrayWrapper(tempBlock.getPrevBlockHash());
            }
            else{
                break;
            }

        }
        Collections.reverse(tempList);
        for(int i=0;i<tempList.size();i++){
            result.put(new ByteArrayWrapper(tempList.get(i).getHash()),tempList.get(i));
        }
        return result;
    }

    private void addNewUTXOPool(ByteArrayWrapper hash){
        if(endBlocksHeight.containsKey(hash)&&!utxopoolForEachBranch.containsKey(hash)){
            LinkedHashMap<ByteArrayWrapper,Block> currentBranch = getEndBlocksBranch(hash);
            utxopoolForEachBranch.put(hash,generateNewUTXOPool(currentBranch));
        }

    }

    private UTXOPool generateNewUTXOPool(LinkedHashMap<ByteArrayWrapper,Block> blockChainBranch){
        Iterator iter = blockChainBranch.entrySet().iterator();
        UTXOPool result = new UTXOPool();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Object key = entry.getKey();
            Object val = entry.getValue();
            Block current_block = (Block)val;
            Transaction coinBaseTx = current_block.getCoinbase();
            //由于之前在处理Transaction的时候按照一定顺序，因此在当前也必须按照当时的顺序来
            handleTxForUTXOPool(coinBaseTx,result);

            ArrayList<Transaction> current_transactions=current_block.getTransactions();
            for(int i=0;i<current_transactions.size();i++){
                Transaction tx = current_transactions.get(i);
                handleTxForUTXOPool(tx,result);
            }

        }
        return result;
    }

    private void updateEndBlocksHeight(ByteArrayWrapper hash){
        ByteArrayWrapper lastBlockHash = new ByteArrayWrapper(blocks.get(hash).getPrevBlockHash());
        if(endBlocksHeight.get(lastBlockHash)!=null){
            int oldHeight = endBlocksHeight.get(lastBlockHash);
            endBlocksHeight.remove(lastBlockHash);
            endBlocksHeight.put(hash,oldHeight+1);
        }
        else{
            Integer newHeight = getEndBlocksBranch(hash).size();
            endBlocksHeight.put(hash,newHeight);

        }
    }

    private void updateUTXOPoolForSingleBranch(ByteArrayWrapper hash){
        ByteArrayWrapper lastBlockHash = new ByteArrayWrapper(this.blocks.get(hash).getPrevBlockHash());

        if(utxopoolForEachBranch.get(lastBlockHash)!=null){
            UTXOPool oldPool = utxopoolForEachBranch.get(lastBlockHash);
            handleTxForUTXOPool(this.blocks.get(hash).getCoinbase(),oldPool);
            ArrayList<Transaction> current_transactions=this.blocks.get(hash).getTransactions();
            for(int i=0;i<current_transactions.size();i++){
                Transaction tx = current_transactions.get(i);
                handleTxForUTXOPool(tx,oldPool);
            }

            utxopoolForEachBranch.remove(lastBlockHash);
            utxopoolForEachBranch.put(hash,oldPool);
        }
        else{
            addNewUTXOPool(hash);
        }
    }

    private void updateTransactionPool(){
        transactionPool = new TransactionPool();
    }

    private void updateMaxHeight(){
        int current_max_height = 0;
        ByteArrayWrapper hash = null;
        Iterator iter = endBlocksHeight.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Object key = entry.getKey();
            Object val = entry.getValue();
            if((Integer)val>current_max_height){
                hash = (ByteArrayWrapper)key;
                current_max_height = (Integer)val;
            }
        }
        maxHeight = current_max_height;
    }

    private void updatetotalCount(){
        totalCount = totalCount+1;
    }

    private void reorganizeBlockChainIfNecessary(){
        if(totalCount>MAX_BLOCK_NUM){
            ByteArrayWrapper longestBlockHash = new ByteArrayWrapper(getMaxHeightBlock().getHash());
            blocks = getEndBlocksBranch(longestBlockHash);
            halfTheSingleBranch(blocks);
            endBlocksHeight = new LinkedHashMap<ByteArrayWrapper,Integer>();
            utxopoolForEachBranch = new LinkedHashMap<ByteArrayWrapper,UTXOPool>();
            updateEndBlocksHeight(longestBlockHash);
            updateUTXOPoolForSingleBranch(longestBlockHash);
            updateMaxHeight();
            totalCount = blocks.size();



        }
    }

    //基本原则是将本来的链缩短为10
    private void halfTheSingleBranch(LinkedHashMap<ByteArrayWrapper,Block> singleBranch){
        int counter =1;
        TransactionPool transactionWaitingRoom = new TransactionPool();
        UTXOPool utxoPoolForOldBlocks = new UTXOPool();
        int branchLength = singleBranch.size();
        Iterator iter = singleBranch.entrySet().iterator();
        while (iter.hasNext()&&counter<=branchLength-CUT_OFF_AGE) {
            Map.Entry entry = (Map.Entry) iter.next();
            Object key = entry.getKey();
            Object val = entry.getValue();
            Block oldBlock = (Block)val;


            if(counter!=branchLength-CUT_OFF_AGE) {
                ArrayList<Transaction> transactions=oldBlock.getTransactions();
                for(int i=0;i<transactions.size();i++){
                    handleTxForUTXOPool(transactions.get(i),utxoPoolForOldBlocks);
                    transactionWaitingRoom.addTransaction(transactions.get(i));
                }
                handleTxForUTXOPool(oldBlock.getCoinbase(),utxoPoolForOldBlocks);
                transactionWaitingRoom.addTransaction(oldBlock.getCoinbase());
                iter.remove();
            }
            else{
                ArrayList<UTXO> utxosList = utxoPoolForOldBlocks.getAllUTXO();
                for(int i=0;i<utxosList.size();i++){
                    if(transactionWaitingRoom.getTransaction(utxosList.get(i).getTxHash())!=null){
                        oldBlock.addTransaction(transactionWaitingRoom.getTransaction(utxosList.get(i).getTxHash()));
                    }
                }
                break;
            }
            counter++;


        }
    }

    private void handleTxForUTXOPool(Transaction tx,UTXOPool utxoPool){
        for(int i=0;i<tx.getInputs().size();i++){
            if(utxoPool.contains(new UTXO(tx.getInput(i).prevTxHash, tx.getInput(i).outputIndex))) {
                utxoPool.removeUTXO(new UTXO(tx.getInput(i).prevTxHash, tx.getInput(i).outputIndex));
            }
        }

        //adding the new UTXOs to the pool

        for(int i=0;i<tx.getOutputs().size();i++){
            utxoPool.addUTXO(new UTXO(tx.getHash(),i),tx.getOutput(i));
        }
    }


    /**
     * Add {@code block} to the block chain if it is valid. For validity, all transactions should be
     * valid and block should be at {@code height > (maxHeight - CUT_OFF_AGE)}.
     * 
     * <p>
     * For example, you can try creating a new block over the genesis block (block height 2) if the
     * block chain height is {@code <=
     * CUT_OFF_AGE + 1}. As soon as {@code height > CUT_OFF_AGE + 1}, you cannot create a new block
     * at height 2.
     * 
     * @return true if block is successfully added
     */
    public boolean addBlock(Block block) {

        //交易的合法性因为依靠外部的handler进行检验，交易不再验证了，仅仅验证是否能在当前长度加上即可

        //如果没有previous,没法上链，但是创世块没有此约束
        if(blocks.containsKey(new ByteArrayWrapper(block.getPrevBlockHash()))){
            int appendHeight=0;
            if(endBlocksHeight.get(new ByteArrayWrapper(block.getPrevBlockHash()))!=null){
                appendHeight = endBlocksHeight.get(new ByteArrayWrapper(block.getPrevBlockHash()))+1;
            }
            else{
                appendHeight = getEndBlocksBranch(new ByteArrayWrapper(block.getPrevBlockHash())).size()+1;
            }

            if(appendHeight>maxHeight-CUT_OFF_AGE) {
                blocks.put(new ByteArrayWrapper(block.getHash()), block);
                updateEndBlocksHeight(new ByteArrayWrapper(block.getHash()));
                updateUTXOPoolForSingleBranch(new ByteArrayWrapper(block.getHash()));
                updateMaxHeight();
                updateTransactionPool();
                updatetotalCount();
                reorganizeBlockChainIfNecessary();
                return true;
            }

        }
        return false;
    }

    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        transactionPool.addTransaction(tx);
    }
}