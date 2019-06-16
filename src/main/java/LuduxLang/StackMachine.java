package LuduxLang;

import IDE.Main;
import javafx.util.Pair;

import java.util.LinkedList;
import java.util.Objects;
import java.util.Stack;

public class StackMachine {
    private VariableTable table;
    private LinkedList<Token> tokens;
    private Stack<Token> stack = new Stack<>();

    public StackMachine(LinkedList<Token> tokens){
        this.tokens = tokens;
        this.table = new VariableTable();
    }

    private Pair<Integer, Integer> getOpTokens(String operation) {
        Token tokenOp2 = stack.pop(); Token tokenOp1 = stack.pop();
        int numOp1 = 0; int numOp2 = 0;
        try {
            if (tokenOp1.getType().equals("VAR_KW")) {
                if (table.getVariableType(tokenOp1.getValue()).equals("newList"))
                    Start.currentOutput.append("Ошибка: несовместимость типов (").append(operation).append(")");
            }
            if (tokenOp2.getType().equals("VAR_KW")) {
                if (table.getVariableType(tokenOp2.getValue()).equals("newList"))
                    Start.currentOutput.append("Ошибка: несовместимость типов (").append(operation).append(")");
            }
            numOp1 = tokenOp1.getType().equals("VAR_KW") ? (int) Objects.requireNonNull(table.getVariableValue(tokenOp1.getValue())) :
                    Integer.valueOf(tokenOp1.getValue());
            numOp2 = tokenOp2.getType().equals("VAR_KW") ? (int) Objects.requireNonNull(table.getVariableValue(tokenOp2.getValue())) :
                    Integer.valueOf(tokenOp2.getValue());

        } catch (Exception e) {
            Start.currentOutput.append(e.getMessage());
        }
        return new Pair<>(numOp1, numOp2);
    }

    private Token arithmetic_result(String operation) {
        Pair<Integer, Integer> tokensPair = getOpTokens(operation);
        System.out.println("numOp1 = " + tokensPair.getKey() + ", numOp2 = " + tokensPair.getValue());
        int result = 0;
        switch (operation) {
            case "+": result = tokensPair.getKey() + tokensPair.getValue(); break;
            case "-": result = tokensPair.getKey() - tokensPair.getValue(); break;
            case "*": result = tokensPair.getKey() * tokensPair.getValue(); break;
            case "/": result = tokensPair.getKey() / tokensPair.getValue(); break;
        }
        return new Token(String.valueOf(result), "NUM_KW");
    }

    private Token condition_result(String operation) {
        Pair<Integer, Integer> tokensPair = getOpTokens(operation);
        String result = "";
        switch (operation) {
            case ">": result = String.valueOf(tokensPair.getKey() > tokensPair.getValue()); break;
            case "<": result = String.valueOf(tokensPair.getKey() < tokensPair.getValue()); break;
            case "==": result = String.valueOf(tokensPair.getKey().equals(tokensPair.getValue())); break;
            case ">=": result = String.valueOf(tokensPair.getKey() >= tokensPair.getValue()); break;
            case "<=": result = String.valueOf(tokensPair.getKey() <= tokensPair.getValue()); break;
            case "!=": result = String.valueOf(!tokensPair.getKey().equals(tokensPair.getValue())); break;
        }
        return new Token(result, "");
    }

    private void assign() throws Exception {
        Integer value = Integer.valueOf(stack.pop().getValue());
        table.addVariable(stack.pop().getValue(), "number", value);
    }

    public Boolean run() {
        try {
            Token currentToken;
            for (int i = 0; i < tokens.size(); i++) {
                currentToken = tokens.get(i);
                switch (currentToken.getType()) {
                    case "VAR_KW":
                    case "NUM_KW":
                    case "LABEL_START":
                    case "LABEL_END":
                    case "STRUCTURE_KW":
                        stack.push(currentToken);
                        break;
                    case "ARITHMETIC_OP_KW":
                        stack.push(arithmetic_result(currentToken.getValue()));
                        break;
                    case "LOGIC_KW":
                        stack.push(condition_result(currentToken.getValue()));
                        break;
                    case "ASSIGN_OP_KW":
                        assign();
                        break;
                    case "GOTO":
                        Token it = stack.pop();
                        i = it.getType().equals("LABEL_START") || it.getType().equals("LABEL_END") ?
                                Integer.valueOf(it.getValue()) - 1 : -1;
                        break;
                    case "GOTO_BY_FALSE":
                        it = stack.pop();
                        System.out.println(stack.toString());
                        if (stack.pop().getValue().equals("false")) {
                            i = Integer.valueOf(it.getValue()) - 1;
                        }
                        break;
                    case "WRITE_KW":
                        Token token = stack.pop();
                        String arg = token.getType().equals("VAR_KW") ?
                                Objects.requireNonNull(table.getVariableValue(token.getValue())).toString() : token.getValue();
                        Start.currentOutput.append(arg.equals("@var") ? table.toString() : arg).append("\n");
                        break;

                    case "IS_KW":
                        String type = stack.pop().getValue();
                        String name = stack.pop().getValue();
                        if (type.equals("newList")) table.addVariable(name, type, new LuduxLinkedList());
                        break;

                    case "OPERATE_LIST_KW":
                        Token tokenOperand = stack.pop();
                        Object operand;
                        if ("NUM_KW".equals(tokenOperand.getType())) {
                            operand = Integer.valueOf(tokenOperand.getValue());
                        } else {
                            operand = Objects.requireNonNull(table.getVariableValue(tokenOperand.getValue()));
                        }
                        String structureName = stack.pop().getValue();
                        LuduxLinkedList oper_list = (LuduxLinkedList) table.getVariableValue(structureName);
                        switch (currentToken.getValue()) {
                            case "append":
                            case "++":
                                oper_list.add(operand);
                                break;
                            case "has":
                            case "<<":
                                Start.currentOutput.append(oper_list.contains(operand) ? "true" : "false").append("\n");
                                break;
                            case "delete":
                            case "--":
                                try {
                                    oper_list.remove(Integer.valueOf(operand.toString()));
                                } catch (IndexOutOfBoundsException e) {
                                    Start.currentOutput.append("Элемента с индексом ").append(operand)
                                            .append(" не существует в списке ")
                                            .append(structureName).append("\n");
                                    return false;
                                }
                                break;
                        }

                        break;
                    case "GET_KW":
                        int index = Integer.parseInt(stack.pop().getValue());
                        LuduxLinkedList list = (LuduxLinkedList) table.getVariableValue(stack.pop().getValue());
                        try {
                            stack.push(new Token(String.valueOf(list.get(index)), "NUM_KW"));
                        } catch (IndexOutOfBoundsException e) {
                            Start.currentOutput.append("Индекс ").append(index).append(" отсутствует в списке").append("\n");
                            return false;
                        }
                        break;
                }
            }
        } catch (Exception e) {
            Start.currentOutput.append("\nОшибка: ").append(e.getMessage()).append("\n");
            return false;
        }
        return true;
    }
}
