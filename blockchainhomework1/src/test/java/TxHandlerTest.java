
import org.junit.Test; 
import org.junit.Before; 
import org.junit.After;

import java.security.*;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/** 
* TxHandler Tester. 
* 
* @author <Authors name> 
* @since <pre>9ÔÂ 18, 2019</pre> 
* @version 1.0 
*/ 
public class TxHandlerTest {

    TxHandler test_handler;
    KeyPair kpA;
    KeyPair kpB;
    KeyPair kpC;
    byte[] origin_tx_hashA;
    byte[] origin_tx_hashB;
    byte[] origin_tx_hashC;
@Before
public void before() throws Exception {
    KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
     kpA = kpg.generateKeyPair();
     kpB = kpg.generateKeyPair();
     kpC = kpg.generateKeyPair();
    Transaction.Output outA = new Transaction().new Output(100,kpA.getPublic());
    Transaction.Output outB = new Transaction().new Output(100,kpB.getPublic());
    Transaction.Output outC = new Transaction().new Output(100,kpC.getPublic());
    origin_tx_hashA  = new String("origin_tx_hashA").getBytes();
    origin_tx_hashB  = new String("origin_tx_hashB").getBytes();
    origin_tx_hashC  = new String("origin_tx_hashC").getBytes();
    UTXOPool pool = new UTXOPool();
    pool.addUTXO(new UTXO(origin_tx_hashA,0),outA);
    pool.addUTXO(new UTXO(origin_tx_hashB,0),outB);
    pool.addUTXO(new UTXO(origin_tx_hashC,0),outC);
    test_handler = new TxHandler(pool);
    //generate all initial inputs

} 

@After
public void after() throws Exception { 
}


    /**
     * verify if a single transaction is normal
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws SignatureException
     */
    @Test
public void testNormalSingalInSingalOut() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
Transaction trans_A_to_B = new Transaction();
    trans_A_to_B.addInput(origin_tx_hashA,0);
    trans_A_to_B.addOutput(100,kpB.getPublic());
    Signature sig = Signature.getInstance("SHA256withRSA");
    sig.initSign(kpA.getPrivate());
    sig.update(trans_A_to_B.getRawDataToSign(0));
    byte[] sigd = sig.sign();
    trans_A_to_B.addSignature(sigd,0);
    trans_A_to_B.finalize();
    Transaction[] trans = {trans_A_to_B};
    test_handler.handleTxs(trans);
    assertThat(test_handler.assetverify(new UTXO(origin_tx_hashA,0)),is(false));
    assertThat(test_handler.assetverify(new UTXO(origin_tx_hashB,0)),is(true));
    assertThat(test_handler.assetverify(new UTXO(origin_tx_hashC,0)),is(true));
    assertThat(test_handler.assetverify(new UTXO(trans_A_to_B.getHash(),0)),is(true));


}

    /**
     * verify if a joint payment transaction transaction is normal
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws SignatureException
     */
    @Test
    public void testJointPayment() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Transaction trans_AC_to_B = new Transaction();
        trans_AC_to_B.addInput(origin_tx_hashA,0);
        trans_AC_to_B.addInput(origin_tx_hashC,0);
        trans_AC_to_B.addOutput(200,kpB.getPublic());
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(kpA.getPrivate());
        sig.update(trans_AC_to_B.getRawDataToSign(0));
        byte[] sig1 = sig.sign();
        trans_AC_to_B.addSignature(sig1,0);
        sig.initSign(kpC.getPrivate());
        sig.update(trans_AC_to_B.getRawDataToSign(1));
        byte[] sig2 = sig.sign();
        trans_AC_to_B.addSignature(sig2,1);
        trans_AC_to_B.finalize();

        Transaction[] trans = {trans_AC_to_B};
        test_handler.handleTxs(trans);
        assertThat(test_handler.assetverify(new UTXO(origin_tx_hashA,0)),is(false));
        assertThat(test_handler.assetverify(new UTXO(origin_tx_hashB,0)),is(true));
        assertThat(test_handler.assetverify(new UTXO(origin_tx_hashC,0)),is(false));
        assertThat(test_handler.assetverify(new UTXO(trans_AC_to_B.getHash(),0)),is(true));


    }

    /**
     * verify if a SingalInMultiOut transaction transaction is normal
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws SignatureException
     */
    @Test
    public void testNormalSingalInMultiOut() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Transaction trans_A_to_BC = new Transaction();
        trans_A_to_BC.addInput(origin_tx_hashA,0);
        trans_A_to_BC.addOutput(50,kpB.getPublic());
        trans_A_to_BC.addOutput(50,kpC.getPublic());
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(kpA.getPrivate());
        sig.update(trans_A_to_BC.getRawDataToSign(0));
        byte[] sigd = sig.sign();
        trans_A_to_BC.addSignature(sigd,0);
        trans_A_to_BC.finalize();
        Transaction[] trans = {trans_A_to_BC};
        test_handler.handleTxs(trans);
        assertThat(test_handler.assetverify(new UTXO(origin_tx_hashA,0)),is(false));
        assertThat(test_handler.assetverify(new UTXO(origin_tx_hashB,0)),is(true));
        assertThat(test_handler.assetverify(new UTXO(origin_tx_hashC,0)),is(true));
        assertThat(test_handler.assetverify(new UTXO(trans_A_to_BC.getHash(),0)),is(true));


    }

    @Test
    public void testNormalMultiInMultiOut() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Transaction trans_AB_to_BC = new Transaction();
        trans_AB_to_BC.addInput(origin_tx_hashA,0);
        trans_AB_to_BC.addInput(origin_tx_hashB,0);
        trans_AB_to_BC.addOutput(100,kpB.getPublic());
        trans_AB_to_BC.addOutput(100,kpC.getPublic());
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(kpA.getPrivate());
        sig.update(trans_AB_to_BC.getRawDataToSign(0));
        byte[] sigd = sig.sign();
        trans_AB_to_BC.addSignature(sigd,0);
        sig.initSign(kpB.getPrivate());
        sig.update(trans_AB_to_BC.getRawDataToSign(1));
        sigd = sig.sign();
        trans_AB_to_BC.addSignature(sigd,1);
        trans_AB_to_BC.finalize();
        Transaction[] trans = {trans_AB_to_BC};
        test_handler.handleTxs(trans);
        assertThat(test_handler.assetverify(new UTXO(origin_tx_hashA,0)),is(false));
        assertThat(test_handler.assetverify(new UTXO(origin_tx_hashB,0)),is(false));
        assertThat(test_handler.assetverify(new UTXO(origin_tx_hashC,0)),is(true));
        assertThat(test_handler.assetverify(new UTXO(trans_AB_to_BC.getHash(),0)),is(true));


    }


    /**
     * verify if subsequent transactions is normal
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws SignatureException
     */
    @Test
    public void testSubsequentTransaction() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Transaction trans_A_to_B = new Transaction();
        trans_A_to_B.addInput(origin_tx_hashA,0);
        trans_A_to_B.addOutput(100,kpB.getPublic());
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(kpA.getPrivate());
        sig.update(trans_A_to_B.getRawDataToSign(0));
        byte[] sig1 = sig.sign();
        trans_A_to_B.addSignature(sig1,0);
        trans_A_to_B.finalize();
        Transaction trans_B_to_C = new Transaction();
        trans_B_to_C.addInput(trans_A_to_B.getHash(),0);
        trans_B_to_C.addOutput(100,kpB.getPublic());
        sig.initSign(kpB.getPrivate());
        sig.update(trans_B_to_C.getRawDataToSign(0));
        byte[] sig2 = sig.sign();
        trans_B_to_C.addSignature(sig2,0);
        trans_B_to_C.finalize();
        Transaction[] trans = {trans_A_to_B,trans_B_to_C};
        test_handler.handleTxs(trans);
        assertThat(test_handler.assetverify(new UTXO(origin_tx_hashA,0)),is(false));
        assertThat(test_handler.assetverify(new UTXO(origin_tx_hashB,0)),is(true));
        assertThat(test_handler.assetverify(new UTXO(origin_tx_hashC,0)),is(true));
        assertThat(test_handler.assetverify(new UTXO(trans_A_to_B.getHash(),0)),is(false));
        assertThat(test_handler.assetverify(new UTXO(trans_B_to_C.getHash(),0)),is(true));


    }


    //the following are invalid transactions

    /**
     * verify if a transaction with invalid signature will be reject or not
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws SignatureException
     */
    @Test
    public void testInvalidSignature() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Transaction trans_A_to_B = new Transaction();
        trans_A_to_B.addInput(origin_tx_hashA,0);
        trans_A_to_B.addOutput(100,kpB.getPublic());
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(kpB.getPrivate());
        sig.update(trans_A_to_B.getRawDataToSign(0));
        byte[] sigd = sig.sign();
        trans_A_to_B.addSignature(sigd,0);
        trans_A_to_B.finalize();
        Transaction[] trans = {trans_A_to_B};
        test_handler.handleTxs(trans);
        assertThat(test_handler.assetverify(new UTXO(origin_tx_hashA,0)),is(true));
        assertThat(test_handler.assetverify(new UTXO(origin_tx_hashB,0)),is(true));
        assertThat(test_handler.assetverify(new UTXO(origin_tx_hashC,0)),is(true));
        assertThat(test_handler.assetverify(new UTXO(trans_A_to_B.getHash(),0)),is(false));


    }

    /**
     * verify if a transaction with negative input will be reject or not
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws SignatureException
     */
    @Test
    public void testNegativeInput() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Transaction trans_A_to_B = new Transaction();
        trans_A_to_B.addInput(origin_tx_hashA,0);
        trans_A_to_B.addOutput(-1,kpB.getPublic());
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(kpA.getPrivate());
        sig.update(trans_A_to_B.getRawDataToSign(0));
        byte[] sigd = sig.sign();
        trans_A_to_B.addSignature(sigd,0);
        trans_A_to_B.finalize();
        Transaction[] trans = {trans_A_to_B};
        test_handler.handleTxs(trans);
        assertThat(test_handler.assetverify(new UTXO(origin_tx_hashA,0)),is(true));
        assertThat(test_handler.assetverify(new UTXO(origin_tx_hashB,0)),is(true));
        assertThat(test_handler.assetverify(new UTXO(origin_tx_hashC,0)),is(true));
        assertThat(test_handler.assetverify(new UTXO(trans_A_to_B.getHash(),0)),is(false));


    }


    /**
     * verify if a transaction double spending will be reject or not
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws SignatureException
     */
    @Test
    public void testDoubleSpending() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Transaction trans_A_to_B = new Transaction();
        trans_A_to_B.addInput(origin_tx_hashA,0);
        trans_A_to_B.addOutput(100,kpB.getPublic());
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(kpA.getPrivate());
        sig.update(trans_A_to_B.getRawDataToSign(0));
        byte[] sig1 = sig.sign();
        trans_A_to_B.addSignature(sig1,0);
        trans_A_to_B.finalize();

        Transaction trans_A_to_C = new Transaction();
        trans_A_to_C.addInput(origin_tx_hashA,0);
        trans_A_to_C.addOutput(100,kpC.getPublic());
        sig.initSign(kpA.getPrivate());
        sig.update(trans_A_to_C.getRawDataToSign(0));
        byte[] sig2 = sig.sign();
        trans_A_to_C.addSignature(sig2,0);
        trans_A_to_C.finalize();

        Transaction[] trans = {trans_A_to_B,trans_A_to_C};
        Transaction[] expected = {trans_A_to_B};
        assertThat(test_handler.handleTxs(trans),is(expected));
        assertThat(test_handler.assetverify(new UTXO(origin_tx_hashA,0)),is(false));
        assertThat(test_handler.assetverify(new UTXO(origin_tx_hashB,0)),is(true));
        assertThat(test_handler.assetverify(new UTXO(origin_tx_hashC,0)),is(true));
        assertThat(test_handler.assetverify(new UTXO(trans_A_to_B.getHash(),0)),is(true));
        assertThat(test_handler.assetverify(new UTXO(trans_A_to_C.getHash(),0)),is(false));


    }

    /**
     * verify if a transaction with a invalid previous output will be reject or not
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws SignatureException
     */
    @Test
    public void testInvalidPrevious() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Transaction trans_A_to_B = new Transaction();
        trans_A_to_B.addInput(new String("random_previous").getBytes(),0);
        trans_A_to_B.addOutput(100,kpB.getPublic());
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(kpA.getPrivate());
        sig.update(trans_A_to_B.getRawDataToSign(0));
        byte[] sigd = sig.sign();
        trans_A_to_B.addSignature(sigd,0);
        trans_A_to_B.finalize();
        Transaction[] trans = {trans_A_to_B};
        test_handler.handleTxs(trans);
        assertThat(test_handler.assetverify(new UTXO(origin_tx_hashA,0)),is(true));
        assertThat(test_handler.assetverify(new UTXO(origin_tx_hashB,0)),is(true));
        assertThat(test_handler.assetverify(new UTXO(origin_tx_hashC,0)),is(true));
        assertThat(test_handler.assetverify(new UTXO(trans_A_to_B.getHash(),0)),is(false));


    }

    /**
     * verify if a transaction input shortage will be reject or not
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws SignatureException
     */
    @Test
    public void testInputShortage() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Transaction trans_AC_to_B = new Transaction();
        trans_AC_to_B.addInput(origin_tx_hashA,0);
        trans_AC_to_B.addInput(origin_tx_hashC,0);
        trans_AC_to_B.addOutput(201,kpB.getPublic());
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(kpA.getPrivate());
        sig.update(trans_AC_to_B.getRawDataToSign(0));
        byte[] sig1 = sig.sign();
        trans_AC_to_B.addSignature(sig1,0);
        sig.initSign(kpC.getPrivate());
        sig.update(trans_AC_to_B.getRawDataToSign(1));
        byte[] sig2 = sig.sign();
        trans_AC_to_B.addSignature(sig2,1);
        trans_AC_to_B.finalize();

        Transaction[] trans = {trans_AC_to_B};
        test_handler.handleTxs(trans);
        assertThat(test_handler.assetverify(new UTXO(origin_tx_hashA,0)),is(true));
        assertThat(test_handler.assetverify(new UTXO(origin_tx_hashB,0)),is(true));
        assertThat(test_handler.assetverify(new UTXO(origin_tx_hashC,0)),is(true));
        assertThat(test_handler.assetverify(new UTXO(trans_AC_to_B.getHash(),0)),is(false));

    }
}
