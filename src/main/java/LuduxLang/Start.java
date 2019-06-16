package LuduxLang;

        import IDE.Main;

        import java.util.LinkedList;

public class Start {

    public static StringBuilder currentOutput;

    public static boolean start(String inputCode) {
        currentOutput = new StringBuilder();

        Main.output.append("Здесь отображается проверка лексера, парсера, RPN и стек-машины");

        if (inputCode.isEmpty()) {
            Main.output.setLength(0);
            Main.output.append("Ошибка: пустая программа");
            return false;
        }

        //Отработка лексера
        LinkedList<Token> lexerTokens = Main.lexer.getTokens();
        currentOutput.append("\nТокены из лексера:\n");
        for (Token token : lexerTokens) currentOutput.append(token).append("\n");
        currentOutput.append("\n------Лексер отработал.------\n");
        Main.output.append(currentOutput); currentOutput.setLength(0);

        //Отработка парсера
        Parser parser = new Parser(lexerTokens);
        boolean correct = parser.parse(currentOutput);
        Main.output.append("\nРабота парсера:\n");
        if (!correct) {
            Main.output.append("\nОшибка при парсинге кода\n").append(currentOutput.toString());
            return false;
        }
        currentOutput.append("\n------Парсер отработал успешно.------\n");
        Main.output.append(currentOutput); currentOutput.setLength(0);

        //Отработка RPN
        LinkedList<Token> rpnTokens = new RPN().getOutRPN(lexerTokens);
        Main.output.append("\nТокены из обратной польской записи:\n");
        for (Token token : rpnTokens)
            currentOutput.append(token).append("\n");;
        currentOutput.append("\n------RPN отработал.------\n");
        Main.output.append(currentOutput); currentOutput.setLength(0);

        //Отработка стек-машины
        StackMachine stackMachine = new StackMachine(rpnTokens);
        currentOutput.append("\nСтэк-Машина:\n");
        try {
            correct = stackMachine.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!correct) {
            Main.output.append("\nОшибки при проверке стэк-машины\n").append(currentOutput.toString());
            return false;
        }
        Main.output.append(currentOutput);
        return true;
    }
}
