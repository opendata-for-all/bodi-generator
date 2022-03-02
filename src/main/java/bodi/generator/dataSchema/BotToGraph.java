package bodi.generator.dataSchema;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;


/**
 * Generates a graph description containing the states and transitions between states of a chatbot.
 *
 * This is useful for testing purposes, to easily display the bot structure and see errors, possible improvements, etc.
 */
public final class BotToGraph {

    private BotToGraph() {
    }

    /**
     * A simplified representation of a transition between chatbot states.
     */
    private static class Transition {
        /**
         * Name of the state origin of the {@link Transition}.
         */
        public String origin;

        /**
         * Name of the state destination of the {@link Transition}.
         */
        public String target;

        /**
         * Name of the intent that triggers the {@link Transition}.
         */
        public String whenIntent;
    }

    /**
     * The visitor that goes through the chatbot files and gets the transitions' information.
     */
    private static class TransitionVisitor extends VoidVisitorAdapter<List<Transition>> {
        @Override
        public void visit(final MethodCallExpr md, final List<Transition> arg) {
            super.visit(md, arg);
            if (md.getNameAsString().equals("moveTo")) {
                Transition t = new Transition();
                String target = md.getArgument(0).toString();
                if (target.endsWith("()")) {
                    target = target.split("\\.")[1];
                    target = target.substring(3, target.length() - 2);
                    target = Character.toLowerCase(target.charAt(0)) + target.substring(1);
                }
                t.target = target;
                //finding the intent name if any
                MethodCallExpr left = (MethodCallExpr) md.getChildNodes().get(0);
                if (left.getNameAsString().equals("when") && left.getArgument(0).isMethodCallExpr()) {
                    String whenCondition = ((MethodCallExpr) left.getArgument(0)).getNameAsString();
                    if (whenCondition.equals("intentIs")) {  //For now we only detect the intent if it is a simple intent condition
                        String whenIntent = ((MethodCallExpr) left.getArgument(0)).getArgument(0).toString();
                        if (whenIntent.startsWith("Intents") || whenIntent.startsWith("coreLibraryI18n")) {
                            whenIntent = whenIntent.split("\\.")[1];
                        }
                        t.whenIntent = whenIntent;
                    } else {
                        t.whenIntent = "";
                    }
                } else {
                    t.whenIntent = "";
                }
                Node child = left;
                while (child.getChildNodes().size() > 0) {
                    child = child.getChildNodes().get(0);
                }
                t.origin = child.toString();
                arg.add(t);
            }
        }
    }

    /**
     * Generates the chatbot transition graph definition from a list of files that compose a whole chatbot.
     *
     * @param filePaths the bot files to analyze for the graph generation
     * @return the string containing the graph definition
     * @throws FileNotFoundException the file not found exception
     */
    public static String generateGraph(List<String> filePaths) throws FileNotFoundException {
        VoidVisitor<List<Transition>> transitionVisitor = new TransitionVisitor();
        List<Transition> transitions = new ArrayList<>();
        for (String filePath : filePaths) {
            CompilationUnit cu = StaticJavaParser.parse(new File(filePath));
            transitionVisitor.visit(cu, transitions);
        }
        StringBuilder dotDef = new StringBuilder();
        dotDef.append("digraph transitionGraph {" + "\r\n");
        transitions.forEach(
                n -> dotDef.append("     " + n.origin + "->" + n.target + "[ label=\"" + n.whenIntent + "\" ]" + "\r\n")
        );
        dotDef.append("}");
        return dotDef.toString();
    }
}
