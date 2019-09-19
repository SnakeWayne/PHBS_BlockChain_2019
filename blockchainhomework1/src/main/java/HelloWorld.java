import java.io.UnsupportedEncodingException;
import java.security.*;
import java.util.ArrayList;
import java.util.List;

public class HelloWorld {
    public class Human{
        public String name;
        public Human(String n){
            name = n;
        }
        public boolean equals(Object other) {
            Human h = (Human)other;
            return h.name.equals(name);
        }
    }
    public void test(){
        List<Human> list = new ArrayList<Human>();
        list.add(new Human("A"));
        list.add(new Human("B"));
        list.remove(new Human("A"));
        for(int i=0;i<list.size();i++){
            System.out.println(list.get(i).name);
        }
    }
public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, UnsupportedEncodingException {
//        HelloWorld h =new HelloWorld();
//        h.test();
    /*
    KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
    KeyPair kp = kpg.generateKeyPair();
    KeyPair kp1 = kpg.generateKeyPair();
    System.out.println(kp.getPrivate());
    System.out.println(kp.getPublic());
    System.out.println(kp1.getPrivate());
    System.out.println(kp1.getPublic());
    String message = "hellow";
    message.getBytes();
    Signature sig = Signature.getInstance("SHA256withRSA");
    sig.initSign(kp1.getPrivate());
    sig.update(message.getBytes());
    byte[] sigd = sig.sign();
    String str = "完成验证";
    str = new String(str.getBytes("gbk"),"utf-8");
    if(Crypto.verifySignature(kp1.getPublic(),message.getBytes(),sigd)){
        System.out.println(str);

    }
     */
    String a = "String";
    System.out.println(a.getBytes());
    System.out.println(a.getBytes());
}
}