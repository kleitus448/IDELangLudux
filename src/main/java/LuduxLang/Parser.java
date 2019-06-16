package LuduxLang;

import java.util.LinkedList;
import java.util.Objects;

public class Parser {
    private LinkedList<Token> tokens;
    private LinkedList<Token> expr = new LinkedList<>();

    public Parser(LinkedList<Token> tokensFromLexer){
        this.tokens = new LinkedList<>(tokensFromLexer);
    }

    public boolean parse(StringBuilder output) {
        while (!tokens.isEmpty()) if (!expr_method()) return false;
        output.append("\nПолное высказывание:\n");
        int str = expr.getFirst().getLineCounter();
        output.append(str).append(": ");
        for (Token token: expr) {
            if (str != token.getLineCounter()) {
                str = token.getLineCounter();
                output.append("\n").append(str).append(": ");;
            }
            output.append("[").append(token.toString()).append("]; ");
        }
        output.append("\n");
        return true;
    }

    private void mustBe(String variants, Token token) {
        Start.currentOutput.append("Строка ").append(token.getLineCounter()).append(": Должен быть ").append(variants);
    }

    //---------------------Проверка нетерминалов----------------------
    private Boolean expr_method() {
        LinkedList<Token> copy = new LinkedList<>(tokens);
        Token token = copy.poll();
        boolean expr_success;

        switch (Objects.requireNonNull(token).getType()) {
            case "VAR_KW":
                expr_success = var_expr_method(Objects.requireNonNull(copy.poll()));
                break;
            case "IF_KW":
                expr_success = if_method();
                break;
            case "WHILE_KW":
            case "FOR_KW":
                expr_success = loop_method();
                break;
            case "ENABLE_KW":
                expr_success = list_enable_method();
                break;
            case "WRITE_KW":
                expr_success = write_method();
                break;
            default:
                mustBe("[VAR_KW|IF_KW|WHILE_KW|FOR_KW|ENABLE_KW|WRITE_KW]", token);
                return false;
        }
        return expr_success;
    }

    //---------------------------Описывание циклов---------------------------
    private boolean loop_method() {
        Token token = tokens.peek();
        switch (Objects.requireNonNull(token).getType()) {
            case "WHILE_KW": return while_method();
            case "FOR_KW": return for_method();
            default:
                mustBe("[WHILE_KW | FOR_KW]", token);
                return false;
        }
    }

    private boolean while_method() {
        return while_keyword() && condition_in_brackets_method() && condition_body_method();
    }
    private boolean condition_in_brackets_method() {
        return openBracket_keyword() && condition_method() && closeBracket_keyword();
    }
    private boolean condition_method()  {
        return operand_method() && logic_keyword() && operand_method();
    }

    private boolean var_expr_method(Token token) {
        switch (token.getType()) {
            case "ASSIGN_OP_KW": return expr_assign_method();
            case "OPERATE_LIST_KW": return list_oper_method();
            default:
                mustBe("[ASSIGN_OP_KW|OPERATE_LIST_KW]", token);
                return false;
        }

    }

    private boolean condition_body_method()  {
        Token token; boolean stop = false;
        boolean body_success;
        if (!(openBrace_keyword())) return false;
        while (!stop) {
            LinkedList<Token> copy = new LinkedList<>(tokens);
            token = copy.poll();
            switch (Objects.requireNonNull(token).getType()) {
                case "VAR_KW":
                    body_success = var_expr_method(Objects.requireNonNull(copy.poll()));
                    break;
                case "WHILE_KW":
                case "FOR_KW":
                    body_success = loop_method();
                    break;
                case "IF_KW":
                    body_success = if_method();
                    break;
                case "ENABLE_KW":
                    body_success = list_enable_method();
                    break;
                case "CLOSE_BRACE_KW":
                    body_success = closeBrace_keyword();
                    stop = true;
                    break;
                case "WRITE_KW":
                    body_success = write_method();
                    break;
                default:
                    mustBe("[VAR_KW|WHILE_KW|IF_KW|ENABLE_KW|CLOSE_BRACE_KW|WRITE_KW]", token);
                    return false;
            }
            if (!body_success) return false;
        }
        return true;
    }

    private boolean for_method() {
        return for_keyword() && for_state_method() && condition_body_method();
    }

    private boolean for_state_method()  {
        return openBracket_keyword() &&
        expr_assign_method() &&
        condition_method() &&
        end_keyword() &&
        expr_assign_method() &&
        closeBracket_keyword();
    }

    private boolean if_method()  {
        return if_keyword() &&
        condition_in_brackets_method() &&
        condition_body_method();
    }

    private boolean write_method()  {
        return write_keyword() &&
        operand_method() &&
        end_keyword();
    }

    private boolean list_enable_method()  {
        return enable_keyword() &&
        var_keyword() &&
        is_keyword() &&
        list_keyword() &&
        end_keyword();
    }

    private boolean operation_method()  {
        return var_keyword() &&
        get_keyword() &&
        operand_method();
    }

    private boolean list_oper_method()  {
        return var_keyword() &&
        operate_list_keyword() &&
        operand_method() &&
        end_keyword();
    }

    private boolean expr_assign_method()  {
        return var_keyword() &&
        assign_op_keyword() &&
        expr_arithmetic_method() &&
        end_keyword();
    }

    private boolean expr_in_brackets_method()  {
        return openBracket_keyword() &&
        expr_arithmetic_method() &&
        closeBracket_keyword();
    }

    private boolean expr_arithmetic_method()  {
        boolean expr_arithmetic_success;
        if (!operand_method()) return false;
        while (tokens.peek() != null && tokens.peek().getType().equals("ARITHMETIC_OP_KW")) {
            expr_arithmetic_success = arithmetic_op_keyword() && operand_method();
            if (!expr_arithmetic_success) return false;
        }
        return true;
    }

    private boolean operand_method()  {
        LinkedList<Token> t = new LinkedList<>(tokens);
        if (Objects.requireNonNull(t.poll()).getType().equals("OPEN_BRACKET_KW")) {
            return expr_in_brackets_method();
        } else {
            if (Objects.requireNonNull(t.poll()).getType().equals("GET_KW")) {
                return operation_method();
            } else {
                return one_operand_method();
            }
        }
    }

    private boolean one_operand_method()  {
        Token token = tokens.peek();
        switch (Objects.requireNonNull(token).getType()) {
            case "VAR_KW": return var_keyword();
            case "NUM_KW": return num_keyword();
            default:
                mustBe("[VAR_KW|NUM_KW]", token);
                tokens.remove();
                return false;
        }
    }

    //---------------------Проверки имеющихся терминалов---------------------

    //Метод проверки
    private boolean check(String type){
        Token currentToken = tokens.poll();
        expr.add(currentToken);
        if (!Objects.requireNonNull(currentToken).getType().equals(type)) {
            mustBe(type, currentToken);
            return false;
        }
        return true;
    }

    //Передача в метод вариантов терминалов

    private boolean var_keyword() {return check("VAR_KW");}

    private boolean assign_op_keyword() {return check("ASSIGN_OP_KW");}

    private boolean openBracket_keyword() {return check("OPEN_BRACKET_KW");}

    private boolean closeBracket_keyword() {return check("CLOSE_BRACKET_KW");}

    private boolean arithmetic_op_keyword() {return check("ARITHMETIC_OP_KW");}

    private boolean num_keyword() {return check("NUM_KW");}

    private boolean logic_keyword() {return check("LOGIC_KW");}

    private boolean openBrace_keyword() {return check("OPEN_BRACE_KW");}

    private boolean closeBrace_keyword() {return check("CLOSE_BRACE_KW"); }

    private boolean while_keyword() {return check("WHILE_KW");}

    private boolean for_keyword() {return check("FOR_KW");}

    private boolean if_keyword() {return check("IF_KW");}

    private boolean enable_keyword()  {return check("ENABLE_KW");}

    private boolean is_keyword() {return check("IS_KW");}

    private boolean write_keyword() {return check("WRITE_KW");}

    private boolean list_keyword() {return check("LIST_KW");}

    private boolean operate_list_keyword() {return check("OPERATE_LIST_KW");}
    
    private boolean get_keyword() {return check("GET_KW");}
    
    private boolean end_keyword(){return check("END_KW");}
}
