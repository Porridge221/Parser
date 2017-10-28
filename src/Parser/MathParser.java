package Parser;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

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

    private String source;
    private int pos = 0;
    private States state = States.Expr;
    private int priority = 0;
    private int parenthesesCount = 0;
    private Stack<Node> operations = new Stack<>();
    private HashMap<String, Double> var;

    private ArrayList<Node> result = new ArrayList<>();

    private String var1;
    private String var2;


    private class Node {
        Signals signal;
        String token;

        Node(Signals signal, String token) {
            this.signal = signal;
            this.token = token;
        }
    }

    public enum States {
        Expr, Operation, Operand, Success, Error
    };

    public enum Signals {
        Space, Digit, BinOp, UnOp, Func, BktL, BktR, Endl, ID, Unknown
    };


    private String DEFAULT_WHITESPACES = "( )|(\r)|(\t)";
    private String DEFAULT_BINARYOPERATIONS = "(\\+)|(\\*)|(/)|(\\^)";
    //private String DEFAULT_UNARYOPERATIONS[1] = { "-" };
    private String DEFAULT_ENDL = "\n\0";
    private String[] DEFAULT_FUNCTIONS = { "sin", "cos", "arcsin", "arccos", "tan", "ln", "ctg", "sinh", "cosh", "tanh", "lg", "abs", "sqrt" };


    private Signals getSignal() {
        if (Character.toString(source.charAt(pos)).matches(DEFAULT_WHITESPACES))
            return Signals.Space;
        if (Character.isDigit(source.charAt(pos)))
            return Signals.Digit;
        if (Character.toString(source.charAt(pos)).matches(DEFAULT_BINARYOPERATIONS))
            return Signals.BinOp;
        if (source.charAt(pos) == '-')
            return Signals.UnOp;
        if (source.charAt(pos) == '(')
            return Signals.BktL;
        if (source.charAt(pos) == ')')
            return Signals.BktR;
        if (Character.toString(source.charAt(pos)).matches(DEFAULT_ENDL))
            return Signals.Endl;
        if (Character.isAlphabetic(source.charAt(pos))) {
            for (String i : DEFAULT_FUNCTIONS) {
                if (source.regionMatches(pos, i, 0, i.length())
                        && !Character.isAlphabetic(source.charAt(pos + i.length()))
                        && !Character.isDigit(source.charAt(pos + i.length())))
                    return Signals.Func;
            }
            return Signals.ID;
        }

        return Signals.Unknown;
    }

    private int get_priority(char token) {
        if (token == '+') return 1;
        if (token == '-') return 1;
        if (token == '*') return 2;
        if (token == '/') return 2;
        if (token == '^') return 3;
        //if (token == "mod") return 2; // остаток от деления
        //if (token == "**") return 3; // степень
        return 0; // Возвращаем 0 если токен - это не бинарная операция (например ")")
    }

    private void displace() {
        while (!operations.empty() && operations.peek().signal != Signals.BktL) {
            result.add(operations.pop());
        }
    }


    private void ReadSpaces(States state) {
        this.state = state;
        Next();
    }

    private void ReadNumber(States state) {
        this.state = state;
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

    private void ReadBinaryOp(States state) {
        this.state = state;
        char binary = source.charAt(pos);
        Next();

        int newPriority = get_priority(binary);
        if (newPriority < priority) {
            displace();
        }
        priority = newPriority;
        operations.push(new Node(Signals.BinOp, Character.toString(binary)));
    }

    private void ReadUnaryOp(States state) {
        this.state = state;
        char unary = source.charAt(pos);
        Next();

        operations.push(new Node(Signals.UnOp, "#"));
    }

    private void ReadFunction(States state) {
        this.state = state;
        StringBuilder function = new StringBuilder();

        while (!checkEnd() && source.charAt(pos) != ' ' && source.charAt(pos) != '(') {
            function.append(source.charAt(pos));
            Next();
        }

        operations.push(new Node(Signals.Func, function.toString()));
    }

    private void ReadLeftBkt(States state) {
        this.state = state;
        Next();
        parenthesesCount++;
        priority = 0;
        operations.push(new Node(Signals.BktL, "("));
    }

    private void ReadRightBkt(States state) {
        this.state = state;
        Next();
        parenthesesCount--;

        displace();
        if (!operations.empty() && operations.peek().signal == Signals.BktL) {
            operations.pop();
            while (!operations.empty() && (operations.peek().signal == Signals.UnOp || operations.peek().signal == Signals.Func || operations.peek().token.equals("^")))
                result.add(operations.pop());
        } else {
            throw new RuntimeException("mismatched parentheses");
        }

        priority = operations.empty() ? 0 : get_priority(operations.peek().token.charAt(0));
    }

    private void ReadID(States state) {
        this.state = state;
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

    private void RP(States state_) { }

    private void HandleError(States state_) {
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
            Signals signal = getSignal();
            Cell cell = FSM_table[state.ordinal()][signal.ordinal()];
            cell.worker.work(cell.state);
        }
        displace();

        if (parenthesesCount != 0) {
            result = new ArrayList<>();
            throw new RuntimeException("mismatched parentheses");
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
                    else if (i.token.equals("^"))
                        stack.push(Math.pow(first, second));
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

        return stack.empty() ? 0 : stack.pop();
    }

    private double processFunction(String func, Double value) {
        switch (func) {
            case "sin":
                return Math.sin(value);
            case "cos":
                return Math.cos(value);
            case "arcsin":
                return Math.asin(value);
            case "arccos":
                return Math.acos(value);
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



    public void setVariable(String varName, Double varValue) {
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


    private char getSymbol() {
        return pos < source.length() ? source.charAt(pos) : (char)0;
    }

    private boolean checkEnd() {
        return getSymbol() == 0;
    }

    private void Next() {
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
            { new Cell(States.Expr, this::ReadSpaces), new Cell(States.Operation, this::ReadNumber), new Cell(States.Error, this::HandleError),
                    new Cell(States.Operand, this::ReadUnaryOp), new Cell(/*Operation*/States.Expr, this::ReadFunction), new Cell(States.Expr, this::ReadLeftBkt),
                    new Cell(States.Operation, this::ReadRightBkt), new Cell(States.Error, this::HandleError), new Cell(States.Operation, this::ReadID) },

            { new Cell(States.Operation, this::ReadSpaces), new Cell(States.Error, this::HandleError), new Cell(States.Operand, this::ReadBinaryOp),
                    new Cell(States.Operand, this::ReadBinaryOp), new Cell(States.Error, this::HandleError), new Cell(States.Error, this::HandleError),
                    new Cell(States.Operation, this::ReadRightBkt), new Cell(States.Success, this::RP), new Cell(States.Error, this::HandleError) },

            { new Cell(States.Operand, this::ReadSpaces), new Cell(States.Operation, this::ReadNumber), new Cell(States.Error, this::HandleError),
                    new Cell(States.Error, this::HandleError), new Cell(/*Operation*/States.Expr, this::ReadFunction), new Cell(States.Expr, this::ReadLeftBkt),
                    new Cell(States.Error, this::HandleError), new Cell(States.Error, this::HandleError), new Cell(States.Operation, this::ReadID ) }
    };

}
