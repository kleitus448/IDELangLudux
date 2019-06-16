package LuduxLang;

public class Token {
    private String value;
    private String type;
    private Lexeme lexeme;
    private int beginPos;
    private int endPos;
    private int lineCounter;

    public int getLineCounter() {
        return lineCounter;
    }

    public void setLineCounter(int lineCounter) {
        this.lineCounter = lineCounter;
    }

    public Token(String value, String type) {
        this.value = value;
        this.type = type;
    }

    public Token(String value, Lexeme lexeme) {
        this.value = value;
        this.lexeme = lexeme;
        this.type = lexeme.type;
    }


    @Override
    public String toString() {
        return (this.value + "  |  " + this.type);
    }

    public String getValue() {
        return value;
    }

    public String getType(){
        return type;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getBeginPos() {
        return beginPos;
    }

    public void setBeginPos(int beginPos) {
        this.beginPos = beginPos;
    }

    public int getEndPos() {
        return endPos;
    }

    public void setEndPos(int endPos) {
        this.endPos = endPos;
    }

    public Lexeme getLexeme() {
        return lexeme;
    }
}

