/*
 * Created on 05.06.2011
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.soundbus;

import java.util.Collections;
import java.util.List;

/**
 * <p>
 * This class represents a parsed tempo node script. If parsing
 * failed due to syntax errors, this class wraps the parser error
 * information.
 * </p>
 * 
 * @author jonas.reese
 */
public class TempoScript {

    private String script;
    private int errorLine;
    private int errorColumn;
    private List<TempoScriptExpression> expressions;

    /**
     * Creates a new <code>ProgrammableTempoParserResult</code>.
     * @param script The source script.
     * @param errorLine Pass value for line number if parsing failed. If parsing was successful,
     * pass 0 or less.
     * @param errorColumn Pass value for column number if parsing failed. If parsing was successful,
     * pass 0 or less.
     * @param expressions The top-level tempo script expressions.
     */
    public TempoScript(String script, int errorLine, int errorColumn, List<TempoScriptExpression> expressions) {
        this.script = script;
        this.errorLine = errorLine;
        this.errorColumn = errorColumn;
        if (expressions == null) {
            this.expressions = Collections.emptyList();
        } else {
            this.expressions = expressions;
        }
    }
    
    public String getScript() {
        return script;
    }
    
    public boolean isParserError() {
        return (errorLine > 0 || errorColumn > 0);
    }
    
    public int getErrorLine() {
        return errorLine;
    }
    
    public int getErrorColumn() {
        return errorColumn;
    }
    
    /**
     * Gets the total tempo track length, or -1 if the track is infinite.
     * @return The tempo track length in tacts.
     */
    public int getTotalLengh() {
        int l = 0;
        for (TempoScriptExpression expr : expressions) {
            int exprL = expr.getTotalLength();
            if (exprL < 0) {
                return -1;
            }
            l += exprL;
        }
        return l;
    }
}
