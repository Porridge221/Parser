package Parser;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ivano on 09.07.2017.
 */
public class MathParser {

    private static String source;
    private static int pos = 0;
    private static States state = States.Expr;
    private static int priority = 0;
    private static Stack<Node> operations = new Stack<>();
    private static HashMap<String, Double> var;

    private static ArrayList<Node> result = new ArrayList<>();

    private static String var1;
    private static String var2;


    private static class Node {
        Signals signal;
        String token;

        Node(Signals signal_, String token_) {
            signal = signal_;
            token = token_;
        }
    }

    public enum States {
        Expr, Operation, Operand, Success, Error
    };

    public enum Signals {
        Space, Digit, BinOp, UnOp, Func, BktL, BktR, Endl, ID, Unknown
    };


    private String DEFAULT_WHITESPACES = "( )|(\r)|(\t)";
    private String DEFAULT_BINARYOPERATIONS = "(\\+)|(\\*)|(/)";
    //private String DEFAULT_UNARYOPERATIONS[1] = { "-" };
    private String DEFAULT_ENDL = "\n\0";
    private String[] DEFAULT_FUNCTIONS = { "sin", "cos", "tan", "ln", "ctg", "sinh", "cosh", "tanh", "lg", "abs", "sqrt" };


    Signals getSignal(String input) {
        if (Character.toString(input.charAt(pos)).matches(DEFAULT_WHITESPACES))
            return Signals.Space;
        if (Character.isDigit(input.charAt(pos)))
            return Signals.Digit;
        if (Character.toString(input.charAt(pos)).matches(DEFAULT_BINARYOPERATIONS))
            return Signals.BinOp;
        if (input.charAt(pos) == '-')
            return Signals.UnOp;
        if (input.charAt(pos) == '(')
            return Signals.BktL;
        if (input.charAt(pos) == ')')
            return Signals.BktR;
        if (Character.toString(input.charAt(pos)).matches(DEFAULT_ENDL))
            return Signals.Endl;
        if (Character.isAlphabetic(input.charAt(pos))) {
            for (String i : DEFAULT_FUNCTIONS) {
                if (input.regionMatches(pos, i, 0, i.length())
                        && !Character.isAlphabetic(input.charAt(pos + i.length()))
                        && !Character.isDigit(input.charAt(pos + i.length())))
                    return Signals.Func;
            }
            return Signals.ID;
        }

        return Signals.Unknown;
    }

    private static int get_priority(char token) {
        if (token == '+') return 1;
        if (token == '-') return 1;
        if (token == '*') return 2;
        if (token == '/') return 2;
        //if (token == "mod") return 2; // остаток от деления
        //if (token == "**") return 3; // степень
        return 0; // Возвращаем 0 если токен - это не бинарная операция (например ")")
    }

    private static void displace() {
        while (!operations.empty() && operations.peek().signal != Signals.BktL) {
            result.add(operations.pop());
        }

        if (!operations.empty() && operations.peek().signal == Signals.BktL) {
            operations.pop();
        }
    }


    private static void ReadSpaces(States state_) {
        state = state_;
        Next();
    }

    private static void ReadNumber(States state_) {
        state = state_;
        StringBuilder number = new StringBuilder();
        int separatorCount = 0;
        while (!checkEnd() && (Character.isDigit(source.charAt(pos)) || source.charAt(pos) == '.')) {
            if (source.charAt(pos) == '.')
                separatorCount++;
            if (separatorCount > 1)
                throw new RuntimeException("Error in separator Counter when number read");
            number.append(source.charAt(pos));
            Next();
        }
        result.add(new Node(Signals.Digit, number.toString()));

        if (!operations.empty() && operations.peek().signal == Signals.UnOp) {
            result.add(operations.pop());
        }
    }

    private static void ReadBinaryOp(States state_) {
        state = state_;
        char binary = source.charAt(pos);
        Next();

        int newPriority = get_priority(binary);
        if (newPriority <= priority) {
            displace();
        }
        priority = newPriority;
        operations.push(new Node(Signals.BinOp, Character.toString(binary)));
    }

    private static void ReadUnaryOp(States state_) {
        state = state_;
        char unary = source.charAt(pos);
        Next();

        operations.push(new Node(Signals.UnOp, "#"));
    }

    private static void ReadFunction(States state_) {
        state = state_;
        StringBuilder function = new StringBuilder();

        while (!checkEnd() && source.charAt(pos) != ' ' && source.charAt(pos) != '(') {
            function.append(source.charAt(pos));
            Next();
        }

        operations.push(new Node(Signals.Func, function.toString()));
    }

    private static void ReadLeftBkt(States state_) {
        state = state_;
        Next();
        operations.push(new Node(Signals.BktL, "("));
    }

    private static void ReadRightBkt(States state_) {
        state = state_;
        Next();

        displace();
        priority = operations.empty() ? 0 : get_priority(operations.peek().token.charAt(0));
    }

    private static void ReadID(States state_) {
        state = state_;
        StringBuilder id = new StringBuilder();

        while (!checkEnd() && (Character.isAlphabetic(source.charAt(pos)) || Character.isDigit(source.charAt(pos)))) {
            id.append(source.charAt(pos));
            Next();
        }
        if (!operations.empty() && operations.peek().signal == Signals.UnOp) {
            result.add(new Node(Signals.ID, id.toString()));
            result.add(operations.pop());
        }
        else {
            result.add(new Node(Signals.ID, id.toString()));
        }
        if (!var.containsKey(id.toString())) {
            setVariable(id.toString(), 1.0);
            if (var1 == null)
                var1 = id.toString();
            else if (var2 == null)
                var2 = id.toString();
        }

    }

    private static void RP(States state_) { }

    private static void HandleError(States state_) {
        state = state_;
        throw new RuntimeException("Unknown syntax expression");
    }



    public MathParser() {
        source = "";
        pos = 0;
        state = States.Expr;
        priority = 0;
        operations = new Stack<>();

        var = new HashMap<>();
        setVariable("pi", Math.PI);
        setVariable("e", Math.E);

        result = new ArrayList<>();
    }

    public MathParser(String input) {
        this();
        source = input;

        Parse();
    }

    private void Parse() {
        while(!checkEnd()) {
            Signals signal = getSignal(source);
            Cell cell = FSM_table[state.ordinal()][signal.ordinal()];
            cell.worker.work(cell.state);
        }

        while (!operations.empty())
            displace();

        Pattern pattern = Pattern.compile("\\(");
        Matcher matcher = pattern.matcher(source);

        int countBktL = 0;
        while (matcher.find())
            countBktL++;

        pattern = Pattern.compile("\\)");
        matcher = pattern.matcher(source);

        int countBktR = 0;
        while (matcher.find())
            countBktR++;

        if (countBktL != countBktR) {
            result = new ArrayList<>();
            throw new RuntimeException("counter brackets");
        }
    }

    //Space, Digit, BinOp, UnOp, Func, BktL, BktR, Endl, ID, Unknown
    public double Execute() {
        Stack<Double> stack = new Stack<>();
        for (Node i : result) {
            switch (i.signal) {
                case Digit:
                    stack.push(Double.parseDouble(i.token));
                    break;
                case BinOp:
                    Double second = stack.pop();
                    Double first = stack.pop();
                    if (i.token.equals("*"))
                        stack.push(first * second);
                    else if (i.token.equals("/"))
                        stack.push(first / second);
                    else if (i.token.equals("+"))
                        stack.push(first + second);
                    else if (i.token.equals("-"))
                        stack.push(first - second);
                    break;
                case UnOp:
                    stack.push(0 - stack.pop());
                    break;
                case Func:
                    stack.push(processFunction(i.token, stack.pop()));
                    break;
                case ID:
                    stack.push(var.get(i.token));
                    break;
                default:
                    throw new RuntimeException("token " + i.token + " is not defined");
            }
        }

        return stack.pop();
    }

    private double processFunction(String func, Double value) {
        switch (func) {
            case "sin":
                return Math.sin(value);
            case "cos":
                return Math.cos(value);
            case "tan":
                return Math.tan(value);
            case "ln":
                return Math.log(value);
            case "lg":
                return Math.log10(value);
            case "ctg":
                return 1 / Math.tan(value);
            case "sinh":
                return Math.sinh(value);
            case "cosh":
                return Math.cosh(value);
            case "tanh":
                return Math.tanh(value);
            case "abs":
                return Math.abs(value);
            case "sqrt":
                return Math.sqrt(value);
            default:
                throw new RuntimeException("function '" + func + "' is not defined");
        }
    }



    public static void setVariable(String varName, Double varValue) {
        var.put(varName, varValue);
    }

    public void replaceVariable(String varName, Double varValue) {
        var.replace(varName, varValue);
    }

    public Double getVariable(String varName) throws Exception {
        if (!var.containsKey(varName)) {
            throw new Exception("Error:Try get unexists "+ "variable '"+varName+"'" );
        }

        return var.get(varName);
    }


    public ObservableList<MyPair> getVariableList() {
        ObservableList<MyPair> variableList = FXCollections.observableArrayList();
        for (Map.Entry<String, Double> i : var.entrySet()) {
            variableList.add(new MyPair(i.getKey(), i.getValue()));
            System.out.println(i.getKey() + " " + i.getValue());
        }

        return variableList;
    }


    public String getSource() { return source; }

    public String getConvertedString() {
        StringBuilder res = new StringBuilder();
        for (Node i : result) {
            if (i.signal == Signals.ID) {
                res.append(i.token + "(" + String.format("%.4f", var.get(i.token)) + ") ");
                continue;
            }

            res.append(i.token + " ");
        }

        return res.toString();
    }


    public String getVariable() {
        String DEFAULT_CONSTANTS = "(e)|(pi)";

        for (Map.Entry<String, Double> i : var.entrySet()) {
            if (!i.getKey().matches(DEFAULT_CONSTANTS)) {
                return i.getKey();
            }
        }

        return null;
    }

    public String getVar1() {
        return var1;
    }

    public String getVar2() {
        return var2;
    }


    private static char getSymbol() {
        return pos < source.length() ? source.charAt(pos) : (char)0;
    }

    private static boolean checkEnd() {
        return getSymbol() == 0;
    }

    private static void Next() {
        if (!checkEnd())
            pos++;
    }



    @FunctionalInterface
    private interface ParserFactory {
        void work(States state);
    }

    private class Cell {
        States state;
        ParserFactory worker;

        Cell(States state_, ParserFactory parserFactory_){
            state = state_;
            worker = parserFactory_;
        }
    }

    Cell[][] FSM_table = {
            { new Cell(States.Expr, MathParser::ReadSpaces), new Cell(States.Operation, MathParser::ReadNumber), new Cell(States.Error, MathParser::HandleError),
                    new Cell(States.Operand, MathParser::ReadUnaryOp), new Cell(/*Operation*/States.Expr, MathParser::ReadFunction), new Cell(States.Expr, MathParser::ReadLeftBkt),
                    new Cell(States.Operation, MathParser::ReadRightBkt), new Cell(States.Error, MathParser::HandleError), new Cell(States.Operation, MathParser::ReadID) },

            { new Cell(States.Operation, MathParser::ReadSpaces), new Cell(States.Error, MathParser::HandleError), new Cell(States.Operand, MathParser::ReadBinaryOp),
                    new Cell(States.Operand, MathParser::ReadBinaryOp), new Cell(States.Error, MathParser::HandleError), new Cell(States.Error, MathParser::HandleError),
                    new Cell(States.Operation, MathParser::ReadRightBkt), new Cell(States.Success, MathParser::RP), new Cell(States.Error, MathParser::HandleError) },

            { new Cell(States.Operand, MathParser::ReadSpaces), new Cell(States.Operation, MathParser::ReadNumber), new Cell(States.Error, MathParser::HandleError),
                    new Cell(States.Error, MathParser::HandleError), new Cell(/*Operation*/States.Expr, MathParser::ReadFunction), new Cell(States.Expr, MathParser::ReadLeftBkt),
                    new Cell(States.Error, MathParser::HandleError), new Cell(States.Error, MathParser::HandleError), new Cell(States.Operation, MathParser::ReadID ) }
    };

}
