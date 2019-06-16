package IDE;

import LuduxLang.Token;
import org.fxmisc.richtext.InlineCssTextArea;
import java.util.LinkedList;

public class TextHighlight {
    public static void computeHighlighting(InlineCssTextArea codeArea, String text) throws Exception {
        //Style
        LinkedList<Token> listToken = Main.lexer.findTokens(text);
        Main.logs.append("-----computeHighlighing-----").append(listToken.size()).append("\n");
        int lastkwend = 0;
        for (Token token: listToken) {
            Main.logs.append("\nType: ").append(token.getType()).append("\n")
                     .append("CSSOptions: ").append(token.getLexeme().getCssOptions()).append("\n")
                     .append("BeginPosition: ").append(token.getBeginPos()).append("\n")
                     .append("EndPosition: ").append(token.getEndPos()).append("\n")
                     .append("LastKWend: ").append(lastkwend).append("\n");
            codeArea.setStyle(lastkwend, token.getBeginPos(), "");
            codeArea.setStyle(token.getBeginPos(), token.getEndPos() + 1, token.getLexeme().getCssOptions() + ";");
            lastkwend = token.getEndPos() + 1;
        }
        if (lastkwend != text.length()-1) {
            codeArea.setStyle(lastkwend, text.length(), "");
        }
    }
}
