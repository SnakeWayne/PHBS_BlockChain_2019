
import java.security.PublicKey;

public class BlockHandler {
    private BlockChain blockChain;

    /** assume blockChain has the genesis block */
    public BlockHandler(BlockChain blockChain) {
        this.blockChain = blockChain;
    }

    /**
     * add {@code block} to the block chain if it is valid.
     * 
     * @return true if the block is valid and has been added, false otherwise
     */
    public boolean processBlock(Block block) {
        if (block == null)
            return false;
        try {
            UTXOPool uPool = blockChain.generateNewUTXOPool(blockChain.getEndBlocksBranch(new ByteArrayWrapper(block.getPrevBlockHash())));
            TxHandler handler = new TxHandler(uPool);
            Transaction[] txs = block.getTransactions().toArray(new Transaction[0]);
            Transaction[] rTxs = handler.handleTxs(txs);
            if(txs.length!=rTxs.length){
                return false;
            }
        } catch (Exception e) {
            return false;
        }

        return blockChain.addBlock(block);
    }

    /** create a new {@code block} over the max height {@code block} */
    public Block createBlock(PublicKey myAddress) {
        Block parent = blockChain.getMaxHeightBlock();
        byte[] parentHash = parent.getHash();
        Block current = new Block(parentHash, myAddress);
        UTXOPool uPool = blockChain.getMaxHeightUTXOPool();
        TransactionPool txPool = blockChain.getTransactionPool();
        TxHandler handler = new TxHandler(uPool);
        Transaction[] txs = txPool.getTransactions().toArray(new Transaction[0]);
        Transaction[] rTxs = handler.handleTxs(txs);
//        System.out.println("rTxs.length is"+rTxs.length);
        for (int i = 0; i < rTxs.length; i++)
            current.addTransaction(rTxs[i]);

        current.finalize();
        if (blockChain.addBlock(current))
            return current;
        else
            return null;
    }

    /** process a {@code Transaction} */
    public void processTx(Transaction tx) {
        blockChain.addTransaction(tx);
    }

    /**
     * mainly used for Testing Purpose
     * @return
     */
    public BlockChain getBlockChain(){
        return blockChain;
    }


}
