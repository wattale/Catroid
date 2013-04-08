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
package org.catrobat.catroid.formulaeditor;

import java.util.LinkedList;
import java.util.List;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.content.Sprite;

public class InternFormulaParser {

	private class InternFormulaParserException extends Exception {

		private static final long serialVersionUID = 1L;
		private boolean calculateErrorOffsetInBrackets = false;

		public InternFormulaParserException(String errorMessage) {
			super(errorMessage);
		}

		public InternFormulaParserException(String errorMessage, boolean calculateErrorOffsetInBrackets) {
			super(errorMessage);
			this.calculateErrorOffsetInBrackets = calculateErrorOffsetInBrackets;
		}

		public boolean isCalculationErrorOffsetInBrackets() {
			return calculateErrorOffsetInBrackets;
		}
	}

	public static final int PARSER_OK = -1;
	public static final int PARSER_STACK_OVERFLOW = -2;
	public static final int PARSER_INPUT_SYNTAX_ERROR = -3;
	public static final int PARSER_NO_INPUT = -4;
	private static final int MAXIMUM_TOKENS_TO_PARSE = 1000;

	private List<InternToken> internTokensToParse;
	private int currentTokenParseIndex;
	private int errorTokenIndex;
	private InternToken currentToken;

	public InternFormulaParser(List<InternToken> internTokensToParse) {

		this.internTokensToParse = new LinkedList<InternToken>();

		for (InternToken internToken : internTokensToParse) {
			this.internTokensToParse.add(internToken);
		}

	}

	private void getNextToken() throws InternFormulaParserException {
		currentTokenParseIndex++;
		currentToken = internTokensToParse.get(currentTokenParseIndex);

	}

	public int getErrorTokenIndex() {
		return errorTokenIndex;

	}

	private FormulaElement findLowerOrEqualPriorityOperatorElement(Operators currentOperator,
			FormulaElement currentElement) {
		FormulaElement returnElement = currentElement.getParent();
		FormulaElement notNullElement = currentElement;
		boolean goOn = true;

		while (goOn) {
			if (returnElement == null) {
				goOn = false;
				returnElement = notNullElement;
			} else {
				Operators parentOperator = Operators.getOperatorByValue(returnElement.getValue());
				int compareOperator = parentOperator.compareOperatorTo(currentOperator);
				if (compareOperator < 0) {
					goOn = false;
					returnElement = notNullElement;
				} else {
					notNullElement = returnElement;
					returnElement = returnElement.getParent();
				}
			}
		}
		return returnElement;
	}

	public void handleOperator(String operator, FormulaElement currentElement, FormulaElement newElement)
			throws InternFormulaParserException {

		if (currentElement.getParent() == null) {
			new FormulaElement(FormulaElement.ElementType.OPERATOR, operator, null, currentElement, newElement);
			return;
		}

		Operators parentOperator = Operators.getOperatorByValue(currentElement.getParent().getValue());
		Operators currentOperator = Operators.getOperatorByValue(operator);

		int compareOp = parentOperator.compareOperatorTo(currentOperator);

		if (compareOp >= 0) {

			FormulaElement newLeftChild = findLowerOrEqualPriorityOperatorElement(currentOperator, currentElement);
			FormulaElement newLeftChildParent = newLeftChild.getParent();

			if (newLeftChildParent != null) {
				newLeftChild.replaceWithSubElement(operator, newElement);
			} else {
				new FormulaElement(FormulaElement.ElementType.OPERATOR, operator, null, newLeftChild, newElement);
			}
		} else {
			currentElement.replaceWithSubElement(operator, newElement);
		}

	}

	private void addEndOfFileToken() {
		InternToken endOfFileParserToken = new InternToken(InternTokenType.PARSER_END_OF_FILE);
		internTokensToParse.add(endOfFileParserToken);
	}

	public FormulaElement parseFormula() {
		errorTokenIndex = PARSER_OK;
		currentTokenParseIndex = 0;

		if (internTokensToParse == null) {
			errorTokenIndex = PARSER_NO_INPUT;
			return null;
		}
		if (internTokensToParse.size() == 0) {
			errorTokenIndex = PARSER_NO_INPUT;
			return null;
		}
		if (internTokensToParse.size() > MAXIMUM_TOKENS_TO_PARSE) {
			errorTokenIndex = PARSER_STACK_OVERFLOW;
			errorTokenIndex = 0;
			return null;
		}

		addEndOfFileToken();

		currentToken = internTokensToParse.get(0);

		FormulaElement formulaParseTree = null;

		try {
			formulaParseTree = formula();
		} catch (InternFormulaParserException parseExeption) {

			errorTokenIndex = currentTokenParseIndex;
		}

		return formulaParseTree;

	}

	private FormulaElement formula() throws InternFormulaParserException {

		FormulaElement termListTree = termList();

		if (!currentToken.isEndOfFileToken()) {
			throw new InternFormulaParserException("Parse Error");
		}

		FormulaElement errorElement = termListTree.checkTypes();

		if (errorElement != null) {
			currentTokenParseIndex = termListTree.getInternTokenList(errorElement).size();
			throw new InternFormulaParserException("Parse Error - Type Checking");
		}

		return termListTree;
	}

	private FormulaElement termList() throws InternFormulaParserException {
		FormulaElement currentElement = term();

		FormulaElement loopTermTree = null;
		String operatorStringValue;
		while (currentToken.isOperator() && !currentToken.getTokenStringValue().equals(Operators.LOGICAL_NOT.name())) {

			operatorStringValue = currentToken.getTokenStringValue();
			getNextToken();

			try {
				loopTermTree = term();
			} catch (InternFormulaParserException parserException) {
				if (parserException.isCalculationErrorOffsetInBrackets()) {
					currentTokenParseIndex += currentElement.getRoot().getInternTokenList().size() + 1;
				}
				throw parserException;
			}

			handleOperator(operatorStringValue, currentElement, loopTermTree);
			currentElement = loopTermTree;
		}

		return currentElement.getRoot();
	}

	private FormulaElement term() throws InternFormulaParserException {

		FormulaElement termTree = new FormulaElement(FormulaElement.ElementType.NUMBER, null, null);

		FormulaElement currentElement = termTree;
		boolean isLogicalChildExpected = false;

		if (currentToken.isOperator() && currentToken.getTokenStringValue().equals(Operators.MINUS.name())) {

			currentElement = new FormulaElement(FormulaElement.ElementType.NUMBER, null, termTree, null, null);
			termTree.replaceElement(new FormulaElement(FormulaElement.ElementType.OPERATOR, Operators.MINUS.name(),
					null, null, currentElement));

			getNextToken();
		} else if (currentToken.isOperator() && currentToken.getTokenStringValue().equals(Operators.LOGICAL_NOT.name())) {

			isLogicalChildExpected = true;
			currentElement = new FormulaElement(FormulaElement.ElementType.NUMBER, null, termTree, null, null);
			termTree.replaceElement(new FormulaElement(FormulaElement.ElementType.OPERATOR, Operators.LOGICAL_NOT
					.name(), null, null, currentElement));

			getNextToken();
		}

		if (currentToken.isNumber()) {

			currentElement.replaceElement(FormulaElement.ElementType.NUMBER, number());

		} else if (currentToken.isBracketOpen()) {

			getNextToken();

			FormulaElement bracketTreeRoot = null;
			try {
				bracketTreeRoot = termList();
			} catch (InternFormulaParserException parserException) {
				if (parserException.isCalculationErrorOffsetInBrackets()) {
					currentTokenParseIndex++;
				}
				throw parserException;
			}

			currentElement.replaceElement(new FormulaElement(FormulaElement.ElementType.BRACKET, null, null, null,
					bracketTreeRoot));

			if (!currentToken.isBracketClose()) {
				throw new InternFormulaParserException("Parse Error");
			}
			getNextToken();

		} else if (currentToken.isFunctionName()) {
			currentElement.replaceElement(function());

		} else if (currentToken.isSensor()) {
			currentElement.replaceElement(sensor());

		} else if (currentToken.isUserVariable()) {

			currentElement.replaceElement(userVariable());

		} else {
			throw new InternFormulaParserException("Parse Error");
		}

		if (isLogicalChildExpected && !currentElement.isLogicalOperator()) {
			currentTokenParseIndex = termTree.getRoot().getInternTokenList().size();
			throw new InternFormulaParserException("Parse Error - Wrong logical/mathematical operators nesting", true);
		}

		return termTree;

	}

	private FormulaElement userVariable() throws InternFormulaParserException {
		UserVariablesContainer userVariables = ProjectManager.getInstance().getCurrentProject().getUserVariables();

		Sprite currentSprite = ProjectManager.getInstance().getCurrentSprite();

		if (userVariables.getUserVariable(currentToken.getTokenStringValue(), currentSprite) == null) {
			throw new InternFormulaParserException("Parse Error");
		}

		FormulaElement lookTree = new FormulaElement(FormulaElement.ElementType.USER_VARIABLE,
				currentToken.getTokenStringValue(), null);

		getNextToken();
		return lookTree;
	}

	private FormulaElement sensor() throws InternFormulaParserException {

		if (!Sensors.isSensor(currentToken.getTokenStringValue())) {
			throw new InternFormulaParserException("Parse Error");
		}

		FormulaElement sensorTree = new FormulaElement(FormulaElement.ElementType.SENSOR,
				currentToken.getTokenStringValue(), null);

		getNextToken();

		return sensorTree;
	}

	private FormulaElement function() throws InternFormulaParserException {
		FormulaElement functionTree = new FormulaElement(FormulaElement.ElementType.FUNCTION, null, null);

		if (!Functions.isFunction(currentToken.getTokenStringValue())) {
			throw new InternFormulaParserException("Parse Error");
		}

		functionTree = new FormulaElement(FormulaElement.ElementType.FUNCTION, currentToken.getTokenStringValue(), null);
		getNextToken();

		if (currentToken.isFunctionParameterBracketOpen()) {
			getNextToken();
			functionTree.setLeftChild(termList());

			if (currentToken.isFunctionParameterDelimiter()) {
				getNextToken();
				functionTree.setRightChild(termList());
			}

			if (!currentToken.isFunctionParameterBracketClose()) {
				throw new InternFormulaParserException("Parse Error");
			}
			getNextToken();

		}

		return functionTree;
	}

	private String number() throws InternFormulaParserException {

		String numberToCheck = currentToken.getTokenStringValue();

		if (!numberToCheck.matches("(\\d)+(\\.(\\d)+)?")) {
			throw new InternFormulaParserException("Parse Error");
		}

		getNextToken();

		return numberToCheck;
	}

}
