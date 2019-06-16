package LuduxLang;

import LuduxLang.Token;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Stack;

public class RPN {

    // Обратная польская запись в виде токенов
    private LinkedList<Token> outRPN = new LinkedList<>();

    private boolean higherPriority(String op1, String op2) {
        return (op1.equals("*") || op1.equals("/")) && (op2.equals("+") || op2.equals("-"));
    }

    private LinkedList<Token> getExprInBrackets(LinkedList<Token> tokens, String type) {
        LinkedList<Token> body = new LinkedList<>();
        int open = 1;
        int close = 0;
            while (open != close) {
                System.out.println(tokens.toString() + "\n");
                Token token = tokens.poll();
                    if (Objects.requireNonNull(token).getType().equals("OPEN_" + type))
                        open++;
                    else if (token.getType().equals("CLOSE_" + type))
                        close++;
                    body.add(token);
            }

        body.removeLast();
        return body;
    }

    private void whileToRPN(LinkedList<Token> tokens) {
        boolean stop = false;
        int start = outRPN.size();
        int end = 0;
        Token labelStart;
        Token labelEnd = null;

        while (!stop) {
            Token token = tokens.poll();
            String type = Objects.requireNonNull(token).getType();
            switch (type) {
                case "OPEN_BRACKET_KW":
                    LinkedList<Token> condition = getExprInBrackets(tokens, "BRACKET_KW");
                    getRPN(condition);
                    tokens.addFirst(new Token(")", "CLOSE_BRACKET_KW"));
                    break;
                case "CLOSE_BRACKET_KW":
                    labelEnd = new Token("", "LABEL_END");
                    outRPN.add(labelEnd);
                    outRPN.add(new Token("!F", "GOTO_BY_FALSE"));
                    break;
                case "OPEN_BRACE_KW":
                    LinkedList<Token> body = getExprInBrackets(tokens, "BRACE_KW");
                    getRPN(body);
                    tokens.addFirst(new Token("}", "CLOSE_BRACE_KW"));
                    break;
                case "CLOSE_BRACE_KW":
                    labelStart = new Token(String.valueOf(start), "LABEL_START");
                    outRPN.add(labelStart);
                    outRPN.add(new Token("!", "GOTO"));
                    end = outRPN.size();
                    stop = true;
                    break;
            }
        }
        Objects.requireNonNull(labelEnd).setValue(String.valueOf(end));
    }

    private void forToRPN(LinkedList<Token> tokens) {
        boolean stop = false;
        int start = 0;
        int end = 0;

        Token t, labelStart, labelEnd = null;

        LinkedList<Token> initialization = new LinkedList<>();
        LinkedList<Token> stopCondition = new LinkedList<>();
        LinkedList<Token> inc = new LinkedList<>();

        while (!stop) {
            Token token = tokens.poll();
            String type = Objects.requireNonNull(token).getType();
            switch (type) {
                case "OPEN_BRACKET_KW":
                    LinkedList<Token> allCondition = getExprInBrackets(tokens, "BRACKET_KW");

                    while (!(Objects.requireNonNull(t = allCondition.poll())).getType().equals("END_KW"))
                        initialization.addLast(t);

                    while (!(Objects.requireNonNull(t = allCondition.poll())).getType().equals("END_KW"))
                        stopCondition.addLast(t);

                    inc = allCondition;

                    getRPN(initialization);
                    start = outRPN.size();
                    getRPN(stopCondition);

                    tokens.addFirst(new Token(")", "CLOSE_BRACKET_KW"));
                    break;

                case "CLOSE_BRACKET_KW":
                    labelEnd = new Token("", "LABEL_END");
                    outRPN.add(labelEnd);
                    outRPN.add(new Token("!F", "GOTO_BY_FALSE"));
                    break;

                case "OPEN_BRACE_KW":
                    LinkedList<Token> body = getExprInBrackets(tokens, "BRACE_KW");
                    getRPN(body);
                    getRPN(inc);
                    tokens.addFirst(new Token("}", "CLOSE_BRACE_KW"));
                    break;

                case "CLOSE_BRACE_KW":
                    labelStart = new Token(String.valueOf(start), "LABEL_START");
                    outRPN.add(labelStart);
                    outRPN.add(new Token("!", "GOTO"));
                    end = outRPN.size();
                    stop = true;
                    break;
            }
        }
        Objects.requireNonNull(labelEnd).setValue(String.valueOf(end));
    }

    private void ifToRPN(LinkedList<Token> tokens) {
        boolean stop = false;
        int end = 0;
        Token labelEnd = null;
        do {
            Token token = tokens.poll();
            String type = Objects.requireNonNull(token).getType();
            switch (type) {
                case "OPEN_BRACKET_KW":
                    LinkedList<Token> condition = getExprInBrackets(tokens, "BRACKET_KW");
                    getRPN(condition);
                    tokens.addFirst(new Token(")", "CLOSE_BRACKET_KW"));
                    break;
                case "CLOSE_BRACKET_KW":
                    labelEnd = new Token("", "LABEL_END");
                    outRPN.add(labelEnd);
                    outRPN.add(new Token("!F", "GOTO_BY_FALSE"));
                    break;
                case "OPEN_BRACE_KW":
                    LinkedList<Token> body = getExprInBrackets(tokens, "BRACE_KW");
                    getRPN(body);
                    tokens.addFirst(new Token("}", "CLOSE_BRACE_KW"));
                    break;
                case "CLOSE_BRACE_KW":
                    end = outRPN.size();
                    stop = true;
                    break;
            }
        } while (!stop);
        Objects.requireNonNull(labelEnd).setValue(String.valueOf(end));
    }

    private LinkedList<Token> getRPN(LinkedList<Token> tokens) {
        Stack<Token> stack = new Stack<>();
        Token upperInStack;

        while (!tokens.isEmpty()) {
            Token token = tokens.poll();
            String type = token.getType();
            switch (type) {
                case "VAR_KW":
                case "NUM_KW":
                case "LIST_KW":
                    outRPN.add(token);
                    break;
                case "ARITHMETIC_OP_KW":

                    // Выталкивание из стека операций с более высоким приоритетом
                    while (!stack.isEmpty() && (upperInStack = stack.peek()).getType().equals("ARITHMETIC_OP_KW") &&
                            higherPriority(upperInStack.getValue(), token.getValue())) {
                        outRPN.add(stack.pop());
                    }
                    stack.push(token);
                    break;
                case "ASSIGN_OP_KW":
                case "OPEN_BRACKET_KW":
                case "OPERATE_LIST_KW":
                case "WRITE_KW":
                case "IS_KW":
                case "LOGIC_KW":
                case "GET_KW":
                    stack.push(token);
                    break;
                case "CLOSE_BRACKET_KW":

                    // Выталкивание из стека всех операций, пока не встретится скобка '('
                    while (!stack.isEmpty() && !(upperInStack = stack.pop()).getType().equals("OPEN_BRACKET_KW")) {
                        outRPN.add(upperInStack);
                    }
                    break;
                case "END_KW":
                    while (!stack.isEmpty()) {
                        outRPN.add(stack.pop());
                    }
                    break;
                case "WHILE_KW":
                    whileToRPN(tokens);
                    break;
                case "FOR_KW":
                    forToRPN(tokens);
                    break;
                case "IF_KW":
                    ifToRPN(tokens);
                    break;
            }
        }

        // Выталкивание оставшихся токенов из стека в польскую инверсную запись
        while (!stack.isEmpty())
            outRPN.add(stack.pop());
        return outRPN;
    }

    public LinkedList<Token> getOutRPN(LinkedList<Token> tokensFromLexer) {
        return getRPN(
                new LinkedList<>(tokensFromLexer));
    }
}
