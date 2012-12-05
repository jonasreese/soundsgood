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
 * This class represents a single tempo script expression.
 * A tempo script expression can contain more expressions.
 * </p>
 * 
 * @author jonas.reese
 */
public class TempoScriptExpression {

    private int loopCount;
    private int numerator;
    private int denominator;
    private List<TempoScriptExpression> subExpressions;
    
    /**
     * Creates a tact <code>TempoScriptExpression</code>.
     * @param The number of times this script shall be looped. Pass 1 for "no loop", 0 for
     * "infinite loop".
     * @param numerator The tact numerator, e.g. 6.
     * @param denominator The tact denominator, e.g. 8.
     */
    public TempoScriptExpression(int loopCount, int numerator, int denominator) {
        this.loopCount = loopCount;
        this.numerator = numerator;
        this.denominator = denominator;
        this.subExpressions = Collections.emptyList();
    }
    
    /**
     * Creates a <code>TempoScriptExpression</code> with sub-expressions.
     * @param The number of times this script shall be looped. Pass 1 for "no loop", 0 for
     * "infinite loop".
     * @param 
     * @param denominator The tact denominator, e.g. 8.
     */
    public TempoScriptExpression(int loopCount, List<TempoScriptExpression> subExpressions) {
        this.loopCount = loopCount;
        if (subExpressions == null) {
            this.subExpressions = Collections.emptyList();
        } else {
            this.subExpressions = subExpressions;
        }
    }

    public boolean isTactExpression() {
        return (numerator > 0 && denominator > 0);
    }
    
    public boolean hasSubExpressions() {
        return !subExpressions.isEmpty();
    }
    
    public int getNumerator() {
        return numerator;
    }
    
    public int getDenominator() {
        return denominator;
    }
    
    public List<TempoScriptExpression> getSubExpressions() {
        return subExpressions;
    }
    
    /**
     * Gets the loop count for this expression.
     * @return The number of times this script shall be looped. 1 means "no loop",
     * 0 means "infinite loop". 
     */
    public int getLoopCount() {
        return loopCount;
    }
    
    /**
     * Gets the total tact length of this expression (including sub-expressions).
     * @return The total tact length, or -1 if expression is infinite.
     */
    public int getTotalLength() {
        if (loopCount == 0) {
            return -1;
        }

        if (isTactExpression()) {
            return loopCount;
        }
        int l = 0;
        for (TempoScriptExpression expr : subExpressions) {
            if (expr.isTactExpression()) {
                l += expr.getLoopCount();
            } else {
                int exprL = expr.getTotalLength();
                if (exprL < 0) {
                    return -1;
                } else {
                    l += exprL;
                }
            }
        }
        return loopCount * l;
    }
}
