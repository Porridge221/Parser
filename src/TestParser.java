import Parser.MathParser;

/**
 * Created by ivano on 11.07.2017.
 */
public class TestParser {

    public static void main(String[] args) {
        //Parser.MathParser parser = new Parser.MathParser("-2+(-2)*2");
        //Parser.MathParser parser = new Parser.MathParser("-(-2-2)*sina-2");
        //Parser.MathParser parser = new Parser.MathParser("sin(-2)");
        //Parser.MathParser parser = new Parser.MathParser("sin(pi / 2) * 2 - 3* 10");
        //Parser.MathParser parser = new Parser.MathParser("2-(-2)");
//        MathParser parser = new MathParser("sin(pi / 2)");
        MathParser parser = new MathParser("- (2 + 3 * 4 - 144) * 2");


        System.out.println(parser.getConvertedString());
        System.out.println(parser.Execute());
    }

}
