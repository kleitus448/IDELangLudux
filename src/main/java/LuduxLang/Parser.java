package LuduxLang;

import IDE.Main;
import LuduxLang.Token;
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
                switch (Objects.requireNonNull(copy.poll()).getType()) {
                    case "ASSIGN_OP_KW": expr_success = expr_assign_method(); break;
                    case "OPERATE_LIST_KW": expr_success = struct_oper_method(); break;
                    default:
                         mustBe("[ASSIGN_OP_KW|OPERATE_LIST_KW]", token);
                        return false;
                } break;
            case "IF_KW": expr_success = if_expr_method(); break;
            case "WHILE_KW": case "FOR_KW": expr_success =  loop_method(); break;
            case "ENABLE_KW": expr_success = structure_enable_method(); break;
            case "WRITE_KW": expr_success =  write_method(); break;
            default:
                mustBe("[IF_KW|WHILE_KW|FOR_KW|ENABLE_KW|WRITE_KW]", token);
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
        return while_keyword() & cond_in_brackets_method() & cycle_body_method();
    }
    private boolean cond_in_brackets_method() {
        return openBracket_keyword() & cond_method() & closeBracket_keyword();
    }
    private boolean cond_method()  {
        return operand_method() & logic_keyword()& operand_method();
    }

    private boolean cycle_body_method()  {
        Token token; boolean stop = false;
        boolean body_success;
        if (!(openBrace_keyword())) return false;
        while (!stop) {
            LinkedList<Token> copy = new LinkedList<>(tokens);
            token = copy.poll();
            switch (Objects.requireNonNull(token).getType()) {
                case "VAR_KW":
                    switch (Objects.requireNonNull(copy.poll()).getType()) {
                        case "ASSIGN_OP_KW": body_success = expr_assign_method(); break;
                        case "OPERATE_LIST_KW": body_success = struct_oper_method(); break;
                        default:
                            mustBe("[ASSIGN_OP_KW|OPERATE_LIST_KW]", token);
                            return false;
                    } break;
                case "WHILE_KW": case "FOR_KW": body_success = loop_method(); break;
                case "IF_KW": body_success = if_expr_method(); break;
                case "ENABLE_KW": body_success = structure_enable_method(); break;
                case "CLOSE_BRACE_KW": body_success = closeBrace_keyword(); stop = true; break;
                case "WRITE_KW": body_success = write_method(); break;
                default:
                    mustBe("[VAR_KW|WHILE_KW|FOR_KW|IF_KW|ENABLE_KW", token);
                    return false;
            }
            if (!body_success) return false;
        }
        return true;
    }

    private boolean for_method() {
        return for_keyword() & for_state_method() & cycle_body_method();
    }

    private boolean for_state_method()  {
        return openBracket_keyword() &
        expr_assign_method() &
        cond_method() &
        end_keyword() &
        i_change_method() &
        closeBracket_keyword();
    }

    private boolean i_change_method()  {
        return var() &
        op_assign_kw() &
        expr_arithmetic_method();
    }

    private boolean if_expr_method()  {
        return if_keyword() &
        cond_in_brackets_method() &
        cycle_body_method();
    }

    private boolean write_method()  {
        return write_keyword() &
        operand_method() &
        end_keyword();
    }

    private boolean structure_enable_method()  {
        return enable_keyword() &
        var() &
        is_keyword() &
        structure_keyword() &
        end_keyword();
    }

    private boolean getOperation()  {
        return var() &
        get_keyword() &
        operand_method();
    }

    private boolean struct_oper_method()  {
        return var() &
        op_struct_keyword() &
        operand_method() &
        end_keyword();
    }

    private boolean expr_assign_method()  {
        return var() &
        op_assign_kw() &
        expr_arithmetic_method() &
        end_keyword();
    }

    private boolean in_brackets_method()  {
        return openBracket_keyword() &
        expr_arithmetic_method() &
        closeBracket_keyword();
    }

    private boolean expr_arithmetic_method()  {
        boolean expr_arithmetic_success;
        if (!operand_method()) return false;
        while (tokens.peek() != null && tokens.peek().getType().equals("ARITHMETIC_OP_KW")) {
            expr_arithmetic_success = op_arithmetic_keyword() & operand_method();
            if (!expr_arithmetic_success) return false;
        }
        return true;
    }

    private boolean operand_method()  {
        LinkedList<Token> t = new LinkedList<>(tokens);
        if (Objects.requireNonNull(t.poll()).getType().equals("OPEN_BRACKET_KW")) {
            return in_brackets_method();
        } else {
            if (Objects.requireNonNull(t.poll()).getType().equals("GET_KW")) {
                return getOperation();
            } else {
                return one_operand_method();
            }
        }
    }

    private boolean one_operand_method()  {
        Token token = tokens.peek();
        switch (Objects.requireNonNull(token).getType()) {
            case "VAR_KW": return var();
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

    private boolean var() {return check("VAR_KW");}

    private boolean op_assign_kw() {return check("ASSIGN_OP_KW");}

    private boolean openBracket_keyword() {return check("OPEN_BRACKET_KW");}

    private boolean closeBracket_keyword() {return check("CLOSE_BRACKET_KW");}

    private boolean op_arithmetic_keyword() {return check("ARITHMETIC_OP_KW");}

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

    private boolean structure_keyword() {return check("STRUCTURE_KW");}

    private boolean op_struct_keyword() {return check("OPERATE_LIST_KW");}
    
    private boolean get_keyword() {return check("GET_KW");}
    
    private boolean end_keyword(){return check("END_KW");}
}
