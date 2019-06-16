package LuduxLang;
import IDE.Main;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

public class Lexer {
    private LinkedList<Lexeme> lexemes = new LinkedList<>();
    private LinkedList<Token> tokens = new LinkedList<>();

    public LinkedList<Token> getTokens() {
        return tokens;
    }

    public void printLexemes() {
        Main.logs.append("\n");
        for (Lexeme lexeme: lexemes) Main.logs.append(lexeme.toString()).append("\n");
        Main.logs.append("\n");
    }

    public Lexer() {
        try {
            System.out.println(System.getProperty("user.dir"));
            JSONObject json_file = (JSONObject) new JSONParser().parse(new FileReader(
                    System.getProperty("user.dir")+"/terminals.json"));
            JSONArray json_array = (JSONArray) json_file.get("KEYWORDS");
            for (Object json_object : json_array) {
                JSONObject keyword = (JSONObject) json_object;
                JSONObject cssOptions = (JSONObject) keyword.get("css");
                StringBuilder css_string = new StringBuilder();
                Main.logs.append(keyword.get("kw").toString());
                Main.logs.append(cssOptions.keySet()).append("\n");
                for (Object cssKey : cssOptions.keySet())
                    css_string.append((String) cssKey).append(": ").append((String) cssOptions.get(cssKey)).append(";");
                lexemes.add(new Lexeme(keyword.get("kw").toString(),
                                       keyword.get("reg_expr").toString(),
                                       css_string.toString()));
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    public LinkedList<Token> findTokens(String text) {
        LinkedList<Token> tokens_list = new LinkedList<>();
        int line_counter = 1;

        // Аккумулятор, последний найденный токен, флаг поиска
        String accum = ""; Token lastFoundToken = null; boolean founded;

        // Позиция символа (текущая + 1ая позиция терминала)
        int position = 0; int b_position = 0;

        while (position < text.length()) {

            //Добавляем символ к аккумулятору
            accum = accum + text.charAt(position);
            founded = false;

            //Проверяем аккумулятор на соответствие какой-либо лексеме
            for (Lexeme lexeme : lexemes)
                if (lexeme.pattern.matcher(accum).matches()) {
                    lastFoundToken = new Token(accum, lexeme);
                    founded = true;
                    break;
                }

            if (founded) position++;

            if (!founded || position == text.length()) {
                if (lastFoundToken != null) {
                    lastFoundToken.setBeginPos(b_position);
                    lastFoundToken.setEndPos(position-1);
                    lastFoundToken.setLineCounter(line_counter);
                    tokens_list.add(lastFoundToken);
                    lastFoundToken = null;
                    b_position = position;
                }
                else {
                    position++;
                    if (!(accum.matches("[ \n\t]")))
                        Start.currentOutput.append("Лексер не нашёл соотвествия для выражения ").append(accum);
                    else if (accum.matches("\n")) line_counter++;
                }

                accum = "";
            }
        }
        tokens = tokens_list;
        return tokens_list;
    }
}
