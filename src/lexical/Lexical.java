package lexical;

import lexical.assist.Reserved;
import lexical.assist.Signal;
import utils.Pair;

import java.util.ArrayList;

public class Lexical {
    private final ArrayList<String> lines;
    private final ArrayList<Pair<Pair<String, String>, Integer>> items;
    private String state;
    private StringBuilder buffer;

    public Lexical(ArrayList<String> lines) {
        this.lines = lines;
        this.items = new ArrayList<>();
        this.state = "Normal";
        this.buffer = new StringBuilder();
    }

    public void analysis() {
        for (int i = 0; i < lines.size(); i++) {
            boolean comment = false;
            String nowLine = lines.get(i);
            for (int j = 0; j < nowLine.length(); ) {
                if (nowLine.charAt(j) == '\r') {
                    break;
                }
                switch (state) {
                    case "MultiComment":
                        if (nowLine.charAt(j) == '*' && j + 1 < nowLine.length() &&
                                nowLine.charAt(j + 1) == '/') {
                            j += 1;
                            state = "Normal";
                        }
                        break;
                    case "Normal":
                        if (nowLine.charAt(j) == '/' && j + 1 < nowLine.length() &&
                                nowLine.charAt(j + 1) == '/') {
                            comment = true;
                            break;
                        }
                        if (nowLine.charAt(j) == '/' && j + 1 < nowLine.length() &&
                                nowLine.charAt(j + 1) == '*') {
                            state = "MultiComment";
                            j++;
                        } else if (nowLine.charAt(j) == ' '||nowLine.charAt(j) == '\t') {
                            blank();
                        } else if (isNum(nowLine.charAt(j))) {
                            state = "Num";
                            continue;
                        } else if (isAlpha(nowLine.charAt(j)) || nowLine.charAt(j) == '_') {
                            state = "Text";
                            continue;
                        } else if (nowLine.charAt(j) == '\"') {
                            buffer.append(String.valueOf(nowLine.charAt(j)));
                            state = "String";
                        } else {
                            String nowChar = String.valueOf(nowLine.charAt(j));
                            String nextChar =
                                    j + 1 < nowLine.length() ?
                                            String.valueOf(nowLine.charAt(j + 1)) :
                                            "";
                            StringBuilder next2char = new StringBuilder(nowChar).append(nextChar);
                            if (new Signal().is2char(next2char.toString())) {
                                buffer.append(next2char.toString());
                                gen(i);
                                j++;
                            } else if (new Signal().is1char(nowChar)) {
                                buffer.append(nowChar);
                                gen(i);
                            }
                        }
                        break;
                    case "Num":
                        if (isNum(nowLine.charAt(j))) {
                            buffer.append(String.valueOf(nowLine.charAt(j)));
                        } else {
                            gen(i);
                            continue;
                        }
                        break;
                    case "Text":
                        if (isAlpha(nowLine.charAt(j)) || nowLine.charAt(j) == '_' ||
                                isNum(nowLine.charAt(j))) {
                            buffer.append(String.valueOf(nowLine.charAt(j)));
                        } else {
                            gen(i);
                            continue;
                        }
                        break;
                    case "String":
                        if (nowLine.charAt(j) != '\"') {
                            buffer.append(String.valueOf(nowLine.charAt(j)));
                        } else {
                            buffer.append("\"");
                            gen(i);
                        }
                        break;
                    default:
                        break;
                }
                j++;
                if (comment) {
                    break;
                }
            }
            if (buffer.length() != 0) {
                if (!state.equals("MultiComment")) {
                    gen(i);
                }
            }
        }
    }

    public boolean isNum(Character num) {
        return num >= '0' && num <= '9';
    }

    public boolean isAlpha(Character alpha) {
        if (alpha >= 'a' && alpha <= 'z') {
            return true;
        } else {
            return alpha >= 'A' && alpha <= 'Z';
        }
    }

    public void gen(int lineNum) {
        String nowBuffer = buffer.toString();
        if (nowBuffer.isEmpty()) {
            return;
        }
        if (state.equals("Num")) {
            items.add(new Pair<>(new Pair<>("INTCON", nowBuffer), lineNum+1));
        }
        if (state.equals("String")) {
            items.add(new Pair<>(new Pair<>("STRCON", nowBuffer), lineNum+1));
        }
        if (state.equals("Text")) {
            String str = nowBuffer.toLowerCase();
            if (new Reserved().isReserved(nowBuffer)) {
                items.add(new Pair<>(new Pair<>(new Reserved().getType(str), str),
                        lineNum+1));
            } else {
                items.add(new Pair<>(new Pair<>("IDENFR", nowBuffer), lineNum+1));
            }
        }
        if (state.equals("Normal")) {
            items.add(new Pair<>(new Pair<>(new Signal().getType(nowBuffer), nowBuffer), lineNum+1));
        }
        buffer = new StringBuilder("");
        state = "Normal";
    }

    public void blank() {

    }

    public ArrayList<String> toPrint() {
        ArrayList<String> print = new ArrayList<>();
        for (Pair pair : items) {
            String type = (String) ((Pair) pair.getHead()).getHead();
            String example = (String) ((Pair) pair.getHead()).getTail();
            String stringBuilder = type + " " + example + "\n";
            print.add(stringBuilder);
        }
        return print;
    }

    public ArrayList<Pair<Pair<String, String>, Integer>> getItems() {
        return items;
    }
}

