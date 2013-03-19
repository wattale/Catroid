/**
 *  Catroid: An on-device visual programming system for Android devices
 *  Copyright (C) 2010-2013 The Catrobat Team
 *  (<http://developer.catrobat.org/credits>)
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *  
 *  An additional term exception under section 7 of the GNU Affero
 *  General Public License, version 3, is available at
 *  http://developer.catrobat.org/license_additional_term
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *  
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.catrobat.catroid.test.formulaeditor;

import java.util.LinkedList;
import java.util.List;

import org.catrobat.catroid.formulaeditor.FormulaElement;
import org.catrobat.catroid.formulaeditor.Functions;
import org.catrobat.catroid.formulaeditor.InternFormulaParser;
import org.catrobat.catroid.formulaeditor.InternToken;
import org.catrobat.catroid.formulaeditor.InternTokenType;
import org.catrobat.catroid.formulaeditor.Operators;

import android.test.AndroidTestCase;

public class ParserTestErrorDedection extends AndroidTestCase {

	public void testLogicalMathematicalOperatorsNesting() {
		List<InternToken> internTokenList = new LinkedList<InternToken>();

		internTokenList.add(new InternToken(InternTokenType.BRACKET_OPEN));
		internTokenList.add(new InternToken(InternTokenType.BRACKET_OPEN));
		internTokenList.add(new InternToken(InternTokenType.NUMBER, "3"));
		internTokenList.add(new InternToken(InternTokenType.OPERATOR, Operators.MULT.name()));
		internTokenList.add(new InternToken(InternTokenType.NUMBER, "4"));
		internTokenList.add(new InternToken(InternTokenType.BRACKET_CLOSE));
		internTokenList.add(new InternToken(InternTokenType.OPERATOR, Operators.PLUS.name()));
		internTokenList.add(new InternToken(InternTokenType.BRACKET_OPEN));
		internTokenList.add(new InternToken(InternTokenType.NUMBER, "5"));
		internTokenList.add(new InternToken(InternTokenType.OPERATOR, Operators.PLUS.name()));
		internTokenList.add(new InternToken(InternTokenType.BRACKET_OPEN));
		internTokenList.add(new InternToken(InternTokenType.NUMBER, "0"));
		internTokenList.add(new InternToken(InternTokenType.OPERATOR, Operators.SMALLER_THAN.name()));
		internTokenList.add(new InternToken(InternTokenType.NUMBER, "1"));
		internTokenList.add(new InternToken(InternTokenType.BRACKET_CLOSE));
		internTokenList.add(new InternToken(InternTokenType.BRACKET_CLOSE));
		internTokenList.add(new InternToken(InternTokenType.BRACKET_CLOSE));

		InternFormulaParser internParser = new InternFormulaParser(internTokenList);
		FormulaElement parseTree = internParser.parseFormula();

		assertNull("Invalid formula parsed: ( ( 3 * 4 ) + ( 5 + ( 0 < 1 ) ) ) ", parseTree);
		int errorTokenIndex = internParser.getErrorTokenIndex();
		assertEquals("Error Token Index is not as expected", 9, errorTokenIndex);
		internTokenList.clear();

		internTokenList.add(new InternToken(InternTokenType.BRACKET_OPEN));
		internTokenList.add(new InternToken(InternTokenType.BRACKET_OPEN));
		internTokenList.add(new InternToken(InternTokenType.FUNCTION_NAME, Functions.RAND.name()));
		internTokenList.add(new InternToken(InternTokenType.FUNCTION_PARAMETERS_BRACKET_OPEN));
		internTokenList.add(new InternToken(InternTokenType.NUMBER, "0"));
		internTokenList.add(new InternToken(InternTokenType.FUNCTION_PARAMETER_DELIMITER));
		internTokenList.add(new InternToken(InternTokenType.NUMBER, "1"));
		internTokenList.add(new InternToken(InternTokenType.FUNCTION_PARAMETERS_BRACKET_CLOSE));
		internTokenList.add(new InternToken(InternTokenType.OPERATOR, Operators.MULT.name()));
		internTokenList.add(new InternToken(InternTokenType.NUMBER, "4"));
		internTokenList.add(new InternToken(InternTokenType.BRACKET_CLOSE));
		internTokenList.add(new InternToken(InternTokenType.OPERATOR, Operators.PLUS.name()));
		internTokenList.add(new InternToken(InternTokenType.BRACKET_OPEN));
		internTokenList.add(new InternToken(InternTokenType.NUMBER, "5"));
		internTokenList.add(new InternToken(InternTokenType.OPERATOR, Operators.PLUS.name()));
		internTokenList.add(new InternToken(InternTokenType.BRACKET_OPEN));
		internTokenList.add(new InternToken(InternTokenType.NUMBER, "0"));
		internTokenList.add(new InternToken(InternTokenType.OPERATOR, Operators.SMALLER_THAN.name()));
		internTokenList.add(new InternToken(InternTokenType.NUMBER, "1"));
		internTokenList.add(new InternToken(InternTokenType.BRACKET_CLOSE));
		internTokenList.add(new InternToken(InternTokenType.BRACKET_CLOSE));
		internTokenList.add(new InternToken(InternTokenType.BRACKET_CLOSE));

		internParser = new InternFormulaParser(internTokenList);
		parseTree = internParser.parseFormula();

		assertNull("Invalid formula parsed: ( ( random( 0 , 1 ) * 4 ) + ( 5 + ( 0 < 1 ) ) ) ", parseTree);
		errorTokenIndex = internParser.getErrorTokenIndex();
		assertEquals("Error Token Index is not as expected", 14, errorTokenIndex);
		internTokenList.clear();

		internTokenList.add(new InternToken(InternTokenType.NUMBER, "5"));
		internTokenList.add(new InternToken(InternTokenType.OPERATOR, Operators.MINUS.name()));
		internTokenList.add(new InternToken(InternTokenType.BRACKET_OPEN));
		internTokenList.add(new InternToken(InternTokenType.NUMBER, "0"));
		internTokenList.add(new InternToken(InternTokenType.OPERATOR, Operators.SMALLER_THAN.name()));
		internTokenList.add(new InternToken(InternTokenType.NUMBER, "1"));
		internTokenList.add(new InternToken(InternTokenType.BRACKET_CLOSE));

		internParser = new InternFormulaParser(internTokenList);
		parseTree = internParser.parseFormula();

		assertNull("Invalid formula parsed: 5 - ( 0 < 1 ) ", parseTree);
		errorTokenIndex = internParser.getErrorTokenIndex();
		assertEquals("Error Token Index is not as expected", 1, errorTokenIndex);
		internTokenList.clear();

		internTokenList.add(new InternToken(InternTokenType.BRACKET_OPEN));
		internTokenList.add(new InternToken(InternTokenType.NUMBER, "0"));
		internTokenList.add(new InternToken(InternTokenType.OPERATOR, Operators.SMALLER_THAN.name()));
		internTokenList.add(new InternToken(InternTokenType.NUMBER, "1"));
		internTokenList.add(new InternToken(InternTokenType.BRACKET_CLOSE));
		internTokenList.add(new InternToken(InternTokenType.OPERATOR, Operators.MINUS.name()));
		internTokenList.add(new InternToken(InternTokenType.NUMBER, "5"));

		internParser = new InternFormulaParser(internTokenList);
		parseTree = internParser.parseFormula();

		assertNull("Invalid formula parsed: ( 0 < 1 ) - 5 ", parseTree);
		errorTokenIndex = internParser.getErrorTokenIndex();
		assertEquals("Error Token Index is not as expected", 5, errorTokenIndex);
		internTokenList.clear();

		internTokenList.add(new InternToken(InternTokenType.NUMBER, "1"));
		internTokenList.add(new InternToken(InternTokenType.OPERATOR, Operators.GREATER_THAN.name()));
		internTokenList.add(new InternToken(InternTokenType.BRACKET_OPEN));
		internTokenList.add(new InternToken(InternTokenType.NUMBER, "0"));
		internTokenList.add(new InternToken(InternTokenType.OPERATOR, Operators.SMALLER_THAN.name()));
		internTokenList.add(new InternToken(InternTokenType.NUMBER, "1"));
		internTokenList.add(new InternToken(InternTokenType.BRACKET_CLOSE));
		internTokenList.add(new InternToken(InternTokenType.OPERATOR, Operators.MULT.name()));
		internTokenList.add(new InternToken(InternTokenType.NUMBER, "6"));

		internParser = new InternFormulaParser(internTokenList);
		parseTree = internParser.parseFormula();

		assertNull("Invalid formula parsed: 1 > ( 0 < 1 ) * 6 ", parseTree);
		errorTokenIndex = internParser.getErrorTokenIndex();
		assertEquals("Error Token Index is not as expected", 7, errorTokenIndex);
		internTokenList.clear();

	}

	public void testTooManyOperators() {
		List<InternToken> internTokenList = new LinkedList<InternToken>();

		internTokenList.add(new InternToken(InternTokenType.OPERATOR, Operators.MINUS.name()));
		internTokenList.add(new InternToken(InternTokenType.OPERATOR, Operators.MINUS.name()));
		internTokenList.add(new InternToken(InternTokenType.NUMBER, "42.42"));
		InternFormulaParser internParser = new InternFormulaParser(internTokenList);
		FormulaElement parseTree = internParser.parseFormula();
		assertNull("Invalid formula parsed: - - 42.42", parseTree);
		int errorTokenIndex = internParser.getErrorTokenIndex();
		assertEquals("Error Token Index is not as expected", 1, errorTokenIndex);

		internTokenList = new LinkedList<InternToken>();
		internTokenList.add(new InternToken(InternTokenType.OPERATOR, Operators.PLUS.name()));
		internParser = new InternFormulaParser(internTokenList);
		parseTree = internParser.parseFormula();
		assertNull("Invalid formula parsed: +", parseTree);
		errorTokenIndex = internParser.getErrorTokenIndex();
		assertEquals("Error Token Index is not as expected", 0, errorTokenIndex);

		internTokenList = new LinkedList<InternToken>();
		internTokenList.add(new InternToken(InternTokenType.OPERATOR, Operators.MINUS.name()));
		internTokenList.add(new InternToken(InternTokenType.OPERATOR, Operators.PLUS.name()));
		internParser = new InternFormulaParser(internTokenList);
		parseTree = internParser.parseFormula();
		assertNull("Invalid formula parsed: + -", parseTree);
		errorTokenIndex = internParser.getErrorTokenIndex();
		assertEquals("Error Token Index is not as expected", 1, errorTokenIndex);

		internTokenList = new LinkedList<InternToken>();
		internTokenList.add(new InternToken(InternTokenType.OPERATOR, Operators.MULT.name()));
		internTokenList.add(new InternToken(InternTokenType.NUMBER, "42.53"));
		internParser = new InternFormulaParser(internTokenList);
		parseTree = internParser.parseFormula();
		assertNull("Invalid formula parsed:  * 42.53", parseTree);
		errorTokenIndex = internParser.getErrorTokenIndex();
		assertEquals("Error Token Index is not as expected", 0, errorTokenIndex);

		internTokenList = new LinkedList<InternToken>();
		internTokenList.add(new InternToken(InternTokenType.OPERATOR, Operators.MINUS.name()));
		internTokenList.add(new InternToken(InternTokenType.NUMBER, "42.42"));
		internTokenList.add(new InternToken(InternTokenType.OPERATOR, Operators.MINUS.name()));
		internTokenList.add(new InternToken(InternTokenType.NUMBER, "42.42"));
		internTokenList.add(new InternToken(InternTokenType.OPERATOR, Operators.MINUS.name()));
		internParser = new InternFormulaParser(internTokenList);
		parseTree = internParser.parseFormula();
		assertNull("Invalid formula parsed: - 42.42 - 42.42 -", parseTree);
		errorTokenIndex = internParser.getErrorTokenIndex();
		assertEquals("Error Token Index is not as expected", 5, errorTokenIndex);
	}

	public void testOperatorMissing() {
		List<InternToken> internTokenList = new LinkedList<InternToken>();

		internTokenList.add(new InternToken(InternTokenType.NUMBER, "42.53"));
		internTokenList.add(new InternToken(InternTokenType.NUMBER, "42.42"));
		InternFormulaParser internParser = new InternFormulaParser(internTokenList);
		FormulaElement parseTree = internParser.parseFormula();
		assertNull("Invalid formula parsed:  42.53 42.42", parseTree);
		int errorTokenIndex = internParser.getErrorTokenIndex();
		assertEquals("Error Token Index is not as expected", 1, errorTokenIndex);
	}

	public void testNumberMissing() {
		List<InternToken> internTokenList = new LinkedList<InternToken>();

		internTokenList.add(new InternToken(InternTokenType.OPERATOR, Operators.MULT.name()));
		internTokenList.add(new InternToken(InternTokenType.NUMBER, "42.53"));
		InternFormulaParser internParser = new InternFormulaParser(internTokenList);
		FormulaElement parseTree = internParser.parseFormula();
		assertNull("Invalid formula parsed:  * 42.53", parseTree);
		int errorTokenIndex = internParser.getErrorTokenIndex();
		assertEquals("Error Token Index is not as expected", 0, errorTokenIndex);
	}

	public void testRightBracketMissing() {
		List<InternToken> internTokenList = new LinkedList<InternToken>();

		internTokenList.add(new InternToken(InternTokenType.BRACKET_OPEN));
		internTokenList.add(new InternToken(InternTokenType.NUMBER, "42.53"));

		InternFormulaParser internParser = new InternFormulaParser(internTokenList);
		FormulaElement parseTree = internParser.parseFormula();
		assertNull("Invalid formula parsed:   (42.53", parseTree);
		int errorTokenIndex = internParser.getErrorTokenIndex();
		assertEquals("Error Token Index is not as expected", 2, errorTokenIndex);
		internTokenList.clear();

		internTokenList.add(new InternToken(InternTokenType.BRACKET_OPEN));

		internParser = new InternFormulaParser(internTokenList);
		parseTree = internParser.parseFormula();
		assertNull("Invalid formula parsed: ( ", parseTree);
		errorTokenIndex = internParser.getErrorTokenIndex();
		assertEquals("Error Token Index is not as expected", 0, errorTokenIndex);
		internTokenList.clear();
	}

	public void testLefttBracketMissing() {
		List<InternToken> internTokenList = new LinkedList<InternToken>();

		internTokenList.add(new InternToken(InternTokenType.NUMBER, "42.53"));
		internTokenList.add(new InternToken(InternTokenType.BRACKET_CLOSE));

		InternFormulaParser internParser = new InternFormulaParser(internTokenList);
		FormulaElement parseTree = internParser.parseFormula();
		assertNull("Invalid formula parsed:   42.53)", parseTree);
		int errorTokenIndex = internParser.getErrorTokenIndex();
		assertEquals("Error Token Index is not as expected", 1, errorTokenIndex);
	}

	public void testOutOfBound() {
		List<InternToken> internTokenList = new LinkedList<InternToken>();

		internTokenList.add(new InternToken(InternTokenType.NUMBER, "42.53"));
		internTokenList.add(new InternToken(InternTokenType.BRACKET_CLOSE));

		InternFormulaParser internParser = new InternFormulaParser(internTokenList);
		FormulaElement parseTree = internParser.parseFormula();
		assertNull("Invalid formula parsed:   42.53)", parseTree);
		int errorTokenIndex = internParser.getErrorTokenIndex();
		assertEquals("Error Token Index is not as expected", 1, errorTokenIndex);
	}

}
