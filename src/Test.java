import javafx.util.Pair;

import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Created by ivano on 09.07.2017.
 */
public class Test {

    static Integer b = new Integer(10);
    public static PersonFactory personFactory = Person::RB;

    int[][] bb = { {1, 1}, {2, 3} };

  //  static Pair<Integer, PersonFactory<Person>>[][] tt = (Pair<Integer, PersonFactory<Person>>[][]) new Object[3][10];


    public static void test() {
    //    tt[0][0] = new Pair<>(b, Person::RS);
//        a[0] = Person::RB;
      //  System.out.println(tt[0][0].getKey());
    }

    static String DEFAULT_WHITESPACES = " \r\t";

    public static void main(String[] args) {
        String text = "-12.34";
        double result = Double.parseDouble(text);

        System.out.println(text.charAt(5));
        System.out.println(0-result);


        PersonFactory personFactory = Person::RB;
        personFactory.work(10);
        //System.out.println(person);

        Person person = new Person();
        PersonFactory[] a = new PersonFactory[10];
        a[0] = Person::RS;
        a[0].work(11);

        System.out.println(" 1".matches("( )|(\r)|(\t)|(\\*)|(\\+)"));
    }

}

class Person {

    PersonFactory personFactory = Person::RB;


    public static void RS(int gg) {
        System.out.println(gg + "  RS");
    }

    public static void RB(int gg) {
        System.out.println(gg + "  RB");
    }
}

@FunctionalInterface
interface PersonFactory {
    void work(int gg);
}