

import org.junit.Test; 
import org.junit.Before; 
import org.junit.After;
import java.security.*;
import java.util.*;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/** 
* BlockChain Tester. 
* 
* @author <Authors name> 
* @since <pre>10月 16, 2019</pre> 
* @version 1.0 
*/ 
public class BlockChainTest {
    BlockHandler test_handler;
    List<KeyPair> keyPairList;
    int countOfUsers=5;
    Block genesis;

    /**
     * 默认keyPairList的第一项 aka [0]，作为挖矿节点，提供PublicKey给Block用于初始化
     * @throws Exception
     */
    @Before
public void before() throws Exception {
    KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
    keyPairList = new ArrayList<KeyPair>();
    for(int i=0;i<countOfUsers;i++){
        keyPairList.add(kpg.generateKeyPair());
    }
    byte[] genesisPreviousHash  = new String("genesisPreviousHash").getBytes();
    genesis = new Block(genesisPreviousHash,keyPairList.get(0).getPublic());
    genesis.finalize();
    test_handler = new BlockHandler(new BlockChain(genesis));
} 

@After
public void after() throws Exception { 
}

//the following test are for adding on the same branch，just check if txs can be handled correctly Under block chain and reorganization is functional

    @Test
public void testGenesisBlockInitialization(){
    assertThat(test_handler.getBlockChain().getMaxHeightUTXOPool().contains(new UTXO(genesis.getCoinbase().getHash(),0)),is(true));
}

@Test
public void testCommonTransaction() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
    Transaction tx = new Transaction();
    tx.addInput(genesis.getCoinbase().getHash(),0);
    for(int i=1;i<countOfUsers;i++){
        tx.addOutput(1,keyPairList.get(i).getPublic());
    }
    Signature sig = Signature.getInstance("SHA256withRSA");
    sig.initSign(keyPairList.get(0).getPrivate());
    sig.update(tx.getRawDataToSign(0));
    byte[] sigd = sig.sign();
    tx.addSignature(sigd,0);
    tx.finalize();
    test_handler.processTx(tx);
    test_handler.createBlock(keyPairList.get(0).getPublic());

    for(int i = 1;i<countOfUsers;i++) {
        assertThat(test_handler.getBlockChain().getMaxHeightUTXOPool().contains(new UTXO(tx.getHash(), i-1)), is(true));
    }
    assertThat(test_handler.getBlockChain().getMaxHeight(), is(2));
}

    /**
     * since last time we have test all kinds of invalid cases,this time we just test double spending as a example
     */
    @Test
public void testInvalidTransactionInOneBlock() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        List<Transaction> transactionList = new ArrayList<Transaction>();
        for(int i=1;i<countOfUsers;i++){
            Transaction tx = new Transaction();
            tx.addInput(genesis.getCoinbase().getHash(),0);
            tx.addOutput(1,keyPairList.get(i).getPublic());
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initSign(keyPairList.get(0).getPrivate());
            sig.update(tx.getRawDataToSign(0));
            byte[] sigd = sig.sign();
            tx.addSignature(sigd,0);
            tx.finalize();
            transactionList.add(tx);
            test_handler.processTx(tx);

        }
        test_handler.createBlock(keyPairList.get(0).getPublic());
        assertThat(test_handler.getBlockChain().getMaxHeightUTXOPool().contains(new UTXO(transactionList.get(0).getHash(), 0)), is(true));
        for(int i = 1;i<countOfUsers-1;i++) {
            assertThat(test_handler.getBlockChain().getMaxHeightUTXOPool().contains(new UTXO(transactionList.get(i).getHash(), 0)), is(false));
        }
        assertThat(test_handler.getBlockChain().getMaxHeight(), is(2));
}
    /**
     * since last time we have test all kinds of invalid cases,this time we just test double spending as a example
     */
    @Test
    public void testInvalidTransactionInMultiBlock() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        List<Transaction> transactionList = new ArrayList<Transaction>();
        for(int i=1;i<countOfUsers;i++){
            Transaction tx = new Transaction();
            tx.addInput(genesis.getCoinbase().getHash(),0);
            tx.addOutput(1,keyPairList.get(i).getPublic());
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initSign(keyPairList.get(0).getPrivate());
            sig.update(tx.getRawDataToSign(0));
            byte[] sigd = sig.sign();
            tx.addSignature(sigd,0);
            tx.finalize();
            transactionList.add(tx);
            test_handler.processTx(tx);
            test_handler.createBlock(keyPairList.get(0).getPublic());

        }

        assertThat(test_handler.getBlockChain().getMaxHeightUTXOPool().contains(new UTXO(transactionList.get(0).getHash(), 0)), is(true));
        for(int i = 1;i<countOfUsers-1;i++) {
            assertThat(test_handler.getBlockChain().getMaxHeightUTXOPool().contains(new UTXO(transactionList.get(i).getHash(), 0)), is(false));
        }
        assertThat(test_handler.getBlockChain().getMaxHeight(), is(5));
    }


    /**
     * all the transaction are coinbase tx, just want to verify if block will be reorganize correctly or not
     */
    @Test
    public void testBlockChainCapacity(){
        int overwhelmNum = 50;
        for(int i=0;i<overwhelmNum;i++) {
            test_handler.createBlock(keyPairList.get(0).getPublic());
        }
        assertThat(test_handler.getBlockChain().getTotalCount(), is(overwhelmNum+1-BlockChain.MAX_BLOCK_NUM+BlockChain.CUT_OFF_AGE));
        assertThat(test_handler.getBlockChain().getMaxHeightUTXOPool().getAllUTXO().size(), is(overwhelmNum+1));
        ArrayList<UTXO> utxos=test_handler.getBlockChain().getMaxHeightUTXOPool().getAllUTXO();
        for(int i=0;i<overwhelmNum-BlockChain.MAX_BLOCK_NUM+1;i++){
            assertThat(test_handler.getBlockChain().getMaxHeightUTXOPool().getTxOutput(utxos.get(i)).address,is(keyPairList.get(0).getPublic()));
        }
    }

    @Test
    public void testBlockChainDumpTxDuringReorganization()throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Transaction unreservedTx = new Transaction();
        unreservedTx.addInput(genesis.getCoinbase().getHash(),0);
        unreservedTx.addOutput(25,keyPairList.get(1).getPublic());
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(keyPairList.get(0).getPrivate());
        sig.update(unreservedTx.getRawDataToSign(0));
        byte[] sigd = sig.sign();
        unreservedTx.addSignature(sigd,0);
        unreservedTx.finalize();
        test_handler.processTx(unreservedTx);
        test_handler.createBlock(keyPairList.get(0).getPublic());

        Transaction reservedTx = new Transaction();
        reservedTx.addInput(unreservedTx.getHash(),0);
        reservedTx.addOutput(25,keyPairList.get(2).getPublic());
        Signature sig1 = Signature.getInstance("SHA256withRSA");
        sig1.initSign(keyPairList.get(1).getPrivate());
        sig1.update(reservedTx.getRawDataToSign(0));
        byte[] sigd1 = sig1.sign();
        reservedTx.addSignature(sigd1,0);
        reservedTx.finalize();
        test_handler.processTx(reservedTx);

        int overwhelmNum = 50;
        for(int i=0;i<overwhelmNum;i++) {
            test_handler.createBlock(keyPairList.get(0).getPublic());
        }


        assertThat(test_handler.getBlockChain().getTotalCount(), is(overwhelmNum+1+1-BlockChain.MAX_BLOCK_NUM+BlockChain.CUT_OFF_AGE));
        assertThat(test_handler.getBlockChain().getMaxHeightUTXOPool().getAllUTXO().size(), is(overwhelmNum+2));

        final LinkedHashMap<ByteArrayWrapper,Block> blocks =  test_handler.getBlockChain().getBlocks();

        Iterator iter = blocks.entrySet().iterator();

        Map.Entry entry = (Map.Entry) iter.next();
        Object key = entry.getKey();
        Object val = entry.getValue();
        assertThat(((Block)val).getTransactions(),hasItem(reservedTx));
        assertThat(((Block)val).getTransactions().contains(unreservedTx),is(false));

        }

// the following test is to check the branching function

    @Test
    public void testBlockChainSimpleBranch(){
        Block oldOne = new Block(genesis.getHash(), keyPairList.get(0).getPublic());
        oldOne.finalize();
        test_handler.processBlock(oldOne);
        Block newOne = new Block(genesis.getHash(), keyPairList.get(0).getPublic());
        newOne.finalize();
        test_handler.processBlock(newOne);

        assertThat(test_handler.getBlockChain().getMaxHeight(),is(2));
        assertThat(test_handler.getBlockChain().getEndBlocksHeight().size(),is(2));
        assertThat(test_handler.getBlockChain().getMaxHeightBlock(),is(oldOne));

    }

    @Test
    public void testBlockChainInvalidPreviousBranch(){
        Block oldOne = new Block(genesis.getHash(), keyPairList.get(0).getPublic());
        oldOne.finalize();
        test_handler.processBlock(oldOne);
        Block newOne = new Block(new String("Invalid").getBytes(), keyPairList.get(0).getPublic());
        newOne.finalize();
        test_handler.processBlock(newOne);

        assertThat(test_handler.getBlockChain().getMaxHeight(),is(2));
        assertThat(test_handler.getBlockChain().getEndBlocksHeight().size(),is(1));
        assertThat(test_handler.getBlockChain().getBlocks().containsValue(newOne),is(false));

    }

    @Test
    public void testBlockChainInsertPositionTooSmallBranch(){

        int mainBranchLenth =20;
        for(int i =0;i<mainBranchLenth;i++){
            test_handler.createBlock(keyPairList.get(0).getPublic());
        }

        Block branchBlock = new Block(genesis.getHash(), keyPairList.get(0).getPublic());
        branchBlock.finalize();

        assertThat(test_handler.processBlock(branchBlock),is(false));
        assertThat(test_handler.getBlockChain().getMaxHeight(),is(21));
        assertThat(test_handler.getBlockChain().getEndBlocksHeight().size(),is(1));
        assertThat(test_handler.getBlockChain().getBlocks().containsValue(branchBlock),is(false));

    }

    @Test
    public void testBlockChainReorganizeWithBranch()throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {


        test_handler.createBlock(keyPairList.get(0).getPublic());

        Transaction unreservedTx = new Transaction();
        unreservedTx.addInput(genesis.getCoinbase().getHash(),0);
        unreservedTx.addOutput(25,keyPairList.get(1).getPublic());
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(keyPairList.get(0).getPrivate());
        sig.update(unreservedTx.getRawDataToSign(0));
        byte[] sigd = sig.sign();
        unreservedTx.addSignature(sigd,0);
        unreservedTx.finalize();

        Block unreservedBlock = new Block(genesis.getHash(), keyPairList.get(0).getPublic());
        unreservedBlock.addTransaction(unreservedTx);
        unreservedBlock.finalize();
        test_handler.processBlock(unreservedBlock);

        assertThat(test_handler.getBlockChain().getBlocks().containsValue(unreservedBlock),is(true));

        int overwhelmNum = 50;
        for(int i=0;i<overwhelmNum;i++) {
            test_handler.createBlock(keyPairList.get(0).getPublic());
        }



        assertThat(test_handler.getBlockChain().getMaxHeight(),is(overwhelmNum+1+1+1-BlockChain.MAX_BLOCK_NUM+BlockChain.CUT_OFF_AGE));
        assertThat(test_handler.getBlockChain().getEndBlocksHeight().size(),is(1));
        assertThat(test_handler.getBlockChain().getBlocks().containsValue(unreservedBlock),is(false));

    }


}




