import java.util.ArrayList;
import java.security.*;
import java.util.Iterator;

public class TxHandler {

    private UTXOPool handler_utxoPool;
    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        handler_utxoPool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        ArrayList<UTXO> index_used = new ArrayList<UTXO>();
        double sum_of_inputs = 0;
        double sum_of_outputs = 0;
        for(int i = 0;i<tx.getInputs().size();i++){

            //verify if output claimed by tx are in the current UTXO pool

            UTXO u = new UTXO(tx.getInput(i).prevTxHash, tx.getInput(i).outputIndex);
            if(!handler_utxoPool.contains(u)){
                System.out.println("current transaction"+tx.getHash()+"'s"+i+"input is not available");
                return false;
            }

            //verify signature on each input of tx are valid

            Transaction.Output previous_output = handler_utxoPool.getTxOutput(u);
            if(!Crypto.verifySignature(previous_output.address,tx.getRawDataToSign(i),tx.getInput(i).signature)){
                System.out.println("sig invalid");
                return false;
            }

            //verify UTXO is claimed multiple times by tx

            if(index_used.contains(u)){
                System.out.println("multiple use of same UTXO");
                return false;
            }
            else{
                index_used.add(u);
            }

            sum_of_inputs += previous_output.value;

        }

        //verify the outputs are all non_negative

        for(int i = 0;i<tx.getOutputs().size();i++){
           if(tx.getOutput(i).value<0){
               System.out.println("output value is negative");
               return false;
           }
           sum_of_outputs +=tx.getOutput(i).value;
        }

        //verify if input value can cover output values
        if(sum_of_inputs<sum_of_outputs){
            System.out.println("input value can not cover output values");
            return false;
        }

        System.out.println("transaction has been verified");
        return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
        ArrayList<Transaction> valid_transactions = new ArrayList<Transaction>();
        for(int i=0;i<possibleTxs.length;i++){
            if(handleTx(possibleTxs[i])){
                valid_transactions.add(possibleTxs[i]);
            }
        }
        Transaction[] valid_transactions_array = new Transaction[valid_transactions.size()];
        for(int i=0;i<valid_transactions.size();i++){
            valid_transactions_array[i]=valid_transactions.get(i);
        }
        return valid_transactions_array;
    }

    /**
     *handle a single transaction and update the UTXO pool,return true if transaction is successfully handled
     */
    public boolean handleTx(Transaction tx){
        if(!isValidTx(tx)){
            return false;
        }

        //get rid of all the used TXO
        for(int i=0;i<tx.getInputs().size();i++){
            handler_utxoPool.removeUTXO(new UTXO(tx.getInput(i).prevTxHash, tx.getInput(i).outputIndex));
        }

        //adding the new UTXOs to the pool

        for(int i=0;i<tx.getOutputs().size();i++){
            handler_utxoPool.addUTXO(new UTXO(tx.getHash(),i),tx.getOutput(i));
        }


        return true;
    }
    public boolean assetverify(UTXO utxo){
        return handler_utxoPool.contains(utxo);
    }

}
