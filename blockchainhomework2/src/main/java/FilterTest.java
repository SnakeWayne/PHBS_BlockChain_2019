import java.util.*;
import java.util.stream.Collectors;

public class FilterTest {
    public static void removeSingle(LinkedHashMap<Integer,Integer> hashMap,int i){
        hashMap.remove(i);
    }

    public static void reverseList(List<Integer> list){
        Collections.reverse(list);
    }


    public static void switchList(List<Integer> list1,List<Integer> list2){
        List<Integer> temp = list1;
        list1 = list2;
        list2 = temp;
    }

    public static void main(String args[]){
//        List<Integer> list = new ArrayList<Integer>();
//        list.add(3);
//        list.add(6);
//        System.out.println(list.stream().filter(i->i>5).collect(Collectors.toList()));

//        LinkedHashMap<Integer,Integer> hashMap = new LinkedHashMap<Integer,Integer>();
//        hashMap.put(1,3);
//        hashMap.put(3,4);
//        hashMap.put(2,5);
//        hashMap.remove(3);
//        hashMap.put(3,4);
//        FilterTest.removeSingle(hashMap,1);
//        Iterator iter = hashMap.entrySet().iterator();
//        while (iter.hasNext()) {
//            Map.Entry entry = (Map.Entry) iter.next();
//            Object key = entry.getKey();
//            Object val = entry.getValue();
//            System.out.println((Integer) key);
//            System.out.println((Integer)val);
//        }

        List<Integer> templist = new ArrayList<Integer>();
        templist.add(1);
        templist.add(5);
        templist.add(3);

        List<Integer> templist1 = new ArrayList<Integer>();
        templist1.add(2);
        templist1.add(4);
        templist1.add(6);

        FilterTest.switchList(templist,templist1);

        //Collections.reverse(templist);
        //FilterTest.reverseList(templist);
        for(int i=0;i<templist.size();i++){
            System.out.println(templist.get(i));
        }


    }
}
