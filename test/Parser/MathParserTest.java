package Parser;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MathParserTest {

    private double epsilon = 1e-6;
    private MathParser parser;

    @Before
    public void initTest() {
        parser = new MathParser();
    }

    @Test
    public void setVariable() throws Exception {
        parser = new MathParser("a + 2");
        parser.setVariable("a", 163.123);
        parser.setVariable("abc", 15.3);
        assertEquals(163.123, parser.getVariable("a"), epsilon);

    }

    @Test
    public void execute() throws Exception {
    }

    @Test
    public void testExecute_01() throws Exception {
        parser = new MathParser("2");
        assertEquals(2, parser.Execute(), epsilon);
    }

    @Test
    public void testExecute_02() throws Exception {
        parser = new MathParser("abc");
        assertEquals(1, parser.Execute(), epsilon);
    }

    @Test
    public void testExecute_03() throws Exception {
        parser = new MathParser("2 + 3");
        assertEquals(5, parser.Execute(), epsilon);
    }

    @Test
    public void testExecute_04() throws Exception {
        parser = new MathParser("2 - 5");
        assertEquals(-3, parser.Execute(), epsilon);
    }

    @Test
    public void testExecute_05() throws Exception {
        parser = new MathParser("-  2");
        assertEquals(-2, parser.Execute(), epsilon);
    }

    @Test
    public void testExecute_06() throws Exception {
        parser = new MathParser("- ( - ( -(2 )))");
        assertEquals(-2, parser.Execute(), epsilon);
    }

    @Test
    public void testExecute_07() throws Exception {
        parser = new MathParser("- (( ( -(2 ))))");
        assertEquals(2, parser.Execute(), epsilon);
    }

    @Test
    public void testExecute_08() throws Exception {
        parser = new MathParser("165.2 + (-15.2)");
        assertEquals(150, parser.Execute(), epsilon);
    }

    @Test
    public void testExecute_09() throws Exception {
        parser = new MathParser("2 + 3 * 4 - 144");
        assertEquals(-130, parser.Execute(), epsilon);
    }

    @Test
    public void testExecute_10() throws Exception {
        parser = new MathParser("- (2 + 3 * 4 - 144)");
        assertEquals(130, parser.Execute(), epsilon);
    }

    @Test
    public void testExecute_11() throws Exception {
        parser = new MathParser("- (2 + 3 * 4 - 144)");
        assertEquals(130, parser.Execute(), epsilon);
    }

    @Test
    public void testExecute_12() throws Exception {
        parser = new MathParser("- (2  -  2)");
        assertEquals(0, parser.Execute(), epsilon);
    }

    @Test
    public void testExecute_13() throws Exception {
        parser = new MathParser("-2+(-2)*2");
        assertEquals(-6, parser.Execute(), epsilon);
    }

    @Test
    public void testExecute_14() throws Exception {
        parser = new MathParser("-(-2-2)*sina-2");
        assertEquals(2, parser.Execute(), epsilon);
    }

    @Test
    public void testExecute_15() throws Exception {
        parser = new MathParser("sin(-2)");
        assertEquals(Math.sin(-2), parser.Execute(), epsilon);
    }

    @Test
    public void testExecute_16() throws Exception {
        parser = new MathParser("sin(pi / 2) * 2 - 3* 10");
        assertEquals(Math.sin(Math.PI / 2)* 2 - 3* 10, parser.Execute(), epsilon);
    }

    @Test
    public void testExecute_17() throws Exception {
        parser = new MathParser("2-(-2)");
        assertEquals(2-(-2), parser.Execute(), epsilon);
    }

    @Test
    public void testExecute_18() throws Exception {
        parser = new MathParser("sin(-(-2-2)*sina)-2");
        parser.setVariable("sina", 15.5);
        assertEquals(Math.sin(-(-2-2)*15.5)-2, parser.Execute(), epsilon);
    }

    @Test
    public void testExecute_19() throws Exception {
        parser = new MathParser("-  2 + 3");
        parser.setVariable("sina", 15.5);
        assertEquals(-2+3, parser.Execute(), epsilon);
    }

    @Test
    public void testExecute_20() throws Exception {
        parser = new MathParser("-  ((2 + 3))");
        parser.setVariable("sina", 15.5);
        assertEquals(-5, parser.Execute(), epsilon);
    }

    @Test
    public void testExecute_21() throws Exception {
        parser = new MathParser("-2 + sin(pi)");
        assertEquals(-2 + Math.sin(Math.PI), parser.Execute(), epsilon);
    }

    @Test
    public void testExecute_22() throws Exception {
        parser = new MathParser("arcsin(sin(pi /2))");
        assertEquals(Math.asin(Math.sin(Math.PI / 2)), parser.Execute(), epsilon);
    }

    @Test
    public void testExecute_23() throws Exception {
        parser = new MathParser("-23.2^3");
        assertEquals(-23.2*(-23.2)*(-23.2), parser.Execute(), epsilon);
    }

    @Test
    public void testExecute_24() throws Exception {
        parser = new MathParser("-23.2^2");
        assertEquals((-23.2)*(-23.2), parser.Execute(), epsilon);
    }

    @Test
    public void testExecute_25() throws Exception {
        parser = new MathParser("3 + 5.4 * 2 / 3 ^(1/2) - 201");
        assertEquals(3 + 5.4 * 2 / Math.pow(3, 1d/2) - 201, parser.Execute(), epsilon);
    }

    @Test
    public void testExecute_26() throws Exception {
        parser = new MathParser("3 + (5.4 * 2 / 3) ^(1/2) - 201");
        assertEquals(3 + Math.pow(5.4 * 2 / 3, 1d/2) - 201, parser.Execute(), epsilon);
    }

    @Test
    public void testExecute_27() throws Exception {
        parser = new MathParser("13 - sin(5) * 24.15 ^(- sin(-13*pi/2) +  3/17) * 10 - 2 / 4  ");
        assertEquals(13 - Math.sin(5) * Math.pow(24.15, (- Math.sin(-13*Math.PI/2) +  3d/17)) * 10 - 2d / 4, parser.Execute(), epsilon);
    }

    @Test
    public void testExecute_101() throws Exception {
        parser = new MathParser("3 + 5.4 * 2 / 3");
        assertEquals(3 + 5.4 * 2 / 3, parser.Execute(), epsilon);
    }

}