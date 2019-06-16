package LuduxLang;

import java.util.regex.Pattern;

public class Lexeme {

    //Внутренние параметры
    public final String type;
    final Pattern pattern;

    //Параметры отображения в IDE
    private String cssOptions;
    public String getCssOptions() {
        return cssOptions;
    }

    Lexeme(String type, String reg_value, String cssOptions) {
        this.cssOptions = cssOptions;
        this.type = type;
        this.pattern = Pattern.compile(reg_value);

    }

    @Override
    public String toString() {return ("Тип: " + type + "  |  " + "Паттерн: " + pattern);}
}
