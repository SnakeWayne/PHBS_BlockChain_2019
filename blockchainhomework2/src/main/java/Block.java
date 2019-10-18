
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Random;

public class Block {

    public static final double COINBASE = 25;

    private byte[] hash;
    private byte[] prevBlockHash;
    private Transaction coinbase;
    private ArrayList<Transaction> txs;

    /** {@code address} is the address to which the coinbase transaction would go */
    public Block(byte[] prevHash, PublicKey address) {
        prevBlockHash = prevHash;
        coinbase = new Transaction(COINBASE, address);
        txs = new ArrayList<Transaction>();
    }

    public Transaction getCoinbase() {
        return coinbase;
    }

    public byte[] getHash() {
        return hash;
    }

    public byte[] getPrevBlockHash() {
        return prevBlockHash;
    }

    public ArrayList<Transaction> getTransactions() {
        return txs;
    }

    public Transaction getTransaction(int index) {
        return txs.get(index);
    }

    public void addTransaction(Transaction tx) {
        txs.add(tx);
    }

    public byte[] getRawBlock() {
        ArrayList<Byte> rawBlock = new ArrayList<Byte>();
        if (prevBlockHash != null)
            for (int i = 0; i < prevBlockHash.length; i++)
                rawBlock.add(prevBlockHash[i]);
        for (int i = 0; i < txs.size(); i++) {
            byte[] rawTx = txs.get(i).getRawTx();
            for (int j = 0; j < rawTx.length; j++) {
                rawBlock.add(rawTx[j]);
            }
        }

        //添加随机性，分叉时需要分清拥有相同交易的不同块。
        Random random = new Random();
        byte[] randomnoise = random.doubles().toString().getBytes();
        for (int i = 0; i < randomnoise.length; i++)
            rawBlock.add(randomnoise[i]);

        byte[] raw = new byte[rawBlock.size()];
        for (int i = 0; i < raw.length; i++)
            raw[i] = rawBlock.get(i);
        return raw;
    }

    public void finalize() {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(getRawBlock());
            hash = md.digest();
        } catch (NoSuchAlgorithmException x) {
            x.printStackTrace(System.err);
        }
    }
}
