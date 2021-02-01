package havis.tools.jibx;

import java.util.Arrays;

import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.PrimitiveType.Code;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

/**
 * Base class for decorators
 *
 */
class AST {

	private final org.eclipse.jdt.core.dom.AST ast = org.eclipse.jdt.core.dom.AST
			.newAST(org.eclipse.jdt.core.dom.AST.JLS3);

	class Pair {

		String type;
		String name;

		Pair(String string) {
			String[] pair = string.trim().split(" ", 2);
			type = pair[0].trim();
			name = pair[1].trim();
		}

		String getType() {
			return type;
		}

		String getName() {
			return name;
		}
	}

	InfixExpression newInfixExpression(Expression leftOperand,
			Expression rightOperand) {
		InfixExpression infix = ast.newInfixExpression();
		infix.setLeftOperand(leftOperand);
		infix.setRightOperand(rightOperand);
		return infix;
	}

	InfixExpression newInfixExpression(Expression leftOperand,
			Expression rightOperand, Operator operator) {
		InfixExpression infix = newInfixExpression(leftOperand, rightOperand);
		infix.setOperator(operator);
		return infix;
	}

	@SuppressWarnings("unchecked")
	MethodDeclaration newMethodDeclaration(String identifier, String type,
			int flags, String[] thrownExceptions, Block body,
			SingleVariableDeclaration... parameters) {
		MethodDeclaration methodDeclaration = newMethodDeclaration(identifier,
				newType(type), flags, body, parameters);
		for (String thrownException : thrownExceptions)
			methodDeclaration.thrownExceptions().add(
					newSimpleName(thrownException));
		return methodDeclaration;
	}

	MethodDeclaration newMethodDeclaration(String identifier, String type,
			int flags, Block body, SingleVariableDeclaration... parameters) {
		return newMethodDeclaration(identifier, newType(type), flags, body,
				parameters);
	}

	MethodDeclaration newMethodDeclaration(String identifier, Code code,
			int flags, Block body, SingleVariableDeclaration... parameters) {
		return newMethodDeclaration(identifier, ast.newPrimitiveType(code),
				flags, body, parameters);
	}

	MethodDeclaration newMethodDeclaration(String identifier, Type type,
			int flags, Block body, Javadoc docComment,
			SingleVariableDeclaration... parameters) {
		MethodDeclaration methodDeclaration = newMethodDeclaration(identifier,
				type, flags, body, parameters);
		methodDeclaration.setJavadoc(docComment);
		return methodDeclaration;
	}

	MethodDeclaration newMethodDeclaration(String identifier, Type type,
			int flags, Block body, SingleVariableDeclaration... parameters) {
		MethodDeclaration methodDeclaration = newMethodDeclaration(identifier,
				type, flags, parameters);
		methodDeclaration.setBody(body);
		return methodDeclaration;
	}

	MethodDeclaration newMethodDeclaration(String identifier, Code code,
			int flags, SingleVariableDeclaration... parameters) {
		return newMethodDeclaration(identifier, ast.newPrimitiveType(code),
				flags, parameters);
	}

	MethodDeclaration newMethodDeclaration(String identifier, String type,
			int flags, SingleVariableDeclaration... parameters) {
		return newMethodDeclaration(identifier, newType(type), flags,
				parameters);
	}

	MethodDeclaration newMethodDeclaration(String name, Type type, int flags,
			SingleVariableDeclaration... parameters) {
		MethodDeclaration methodDeclaration = newMethodDeclaration(name, flags,
				parameters);
		methodDeclaration.setReturnType2(type);
		return methodDeclaration;
	}

	MethodDeclaration newConstructorDeclaration(String name, int flags,
			Block body, SingleVariableDeclaration... parameters) {
		MethodDeclaration methodDeclaration = newMethodDeclaration(name, flags,
				body, parameters);
		methodDeclaration.setConstructor(true);
		return methodDeclaration;
	}

	MethodDeclaration newConstructorDeclaration(String name, int flags,
			SingleVariableDeclaration... parameters) {
		MethodDeclaration methodDeclaration = newMethodDeclaration(name, flags,
				parameters);
		methodDeclaration.setConstructor(true);
		return methodDeclaration;
	}

	MethodDeclaration newMethodDeclaration(String name, int flags, Block body,
			SingleVariableDeclaration... parameters) {
		MethodDeclaration methodDeclaration = newMethodDeclaration(name, flags,
				parameters);
		methodDeclaration.setBody(body);
		return methodDeclaration;
	}

	@SuppressWarnings("unchecked")
	MethodDeclaration newMethodDeclaration(String name, int flags,
			SingleVariableDeclaration... parameters) {
		MethodDeclaration methodDeclaration = ast.newMethodDeclaration();
		methodDeclaration.setName(newSimpleName(name));
		methodDeclaration.modifiers().addAll(ast.newModifiers(flags));
		for (SingleVariableDeclaration parameter : parameters)
			methodDeclaration.parameters().add(parameter);
		return methodDeclaration;
	}

	VariableDeclarationFragment newVariableDeclarationFragment(
			String identifier, Expression expression) {
		VariableDeclarationFragment fragment = newVariableDeclarationFragment(identifier);
		fragment.setInitializer(expression);
		return fragment;
	}

	VariableDeclarationFragment newVariableDeclarationFragment(String identifier) {
		VariableDeclarationFragment fragment = ast
				.newVariableDeclarationFragment();
		fragment.setName(ast.newSimpleName(identifier));
		return fragment;
	}

	@SuppressWarnings("unchecked")
	VariableDeclarationStatement newVariableDeclarationStatement(
			String identifier, String literal, Code code, int flags) {
		VariableDeclarationStatement statement = newVariableDeclarationStatement(
				identifier, literal, code);
		statement.modifiers().addAll(ast.newModifiers(flags));
		return statement;
	}

	VariableDeclarationStatement newVariableDeclarationStatement(
			String identifier, String literal, Code code) {
		return newVariableDeclarationStatement(identifier, literal,
				ast.newPrimitiveType(code));
	}

	VariableDeclarationStatement newVariableDeclarationStatement(
			String identifier, String literal, Type type) {
		VariableDeclarationStatement statement = ast
				.newVariableDeclarationStatement(newVariableDeclarationFragment(
						identifier, ast.newNumberLiteral(literal)));
		statement.setType(type);
		return statement;
	}

	VariableDeclarationStatement newVariableDeclarationStatement(
			String identifier, Expression expression, Type type) {
		VariableDeclarationStatement statement = ast
				.newVariableDeclarationStatement(newVariableDeclarationFragment(
						identifier, expression));
		statement.setType(type);
		return statement;
	}

	Assignment newAssignment(Expression left, Expression right) {
		Assignment assignment = ast.newAssignment();
		assignment.setLeftHandSide(left);
		assignment.setRightHandSide(right);
		return assignment;
	}

	ConditionalExpression newConditionalExpression(Expression expression,
			Expression thenExpression, Expression elseExpression) {
		ConditionalExpression conditionalExpression = ast
				.newConditionalExpression();
		conditionalExpression.setExpression(expression);
		conditionalExpression.setThenExpression(thenExpression);
		conditionalExpression.setElseExpression(elseExpression);
		return conditionalExpression;
	}

	ParenthesizedExpression newParenthesizedExpression(Expression expression) {
		ParenthesizedExpression parenthesizedExpression = ast
				.newParenthesizedExpression();
		parenthesizedExpression.setExpression(expression);
		return parenthesizedExpression;
	}

	MethodInvocation newMethodInvocation(Expression expression, String name) {
		MethodInvocation methodInvocation = ast.newMethodInvocation();
		methodInvocation.setExpression(expression);
		methodInvocation.setName(ast.newSimpleName(name));
		return methodInvocation;
	}

	// Return statement

	ReturnStatement newReturnStatement(Expression expression) {
		ReturnStatement returnStatement = ast.newReturnStatement();
		returnStatement.setExpression(expression);
		return returnStatement;
	}

	IfStatement newIfStatement(Expression expression, Statement thenStatement,
			Statement elseStatement) {
		IfStatement ifStatement = newIfStatement(expression, thenStatement);
		ifStatement.setElseStatement(elseStatement);
		return ifStatement;
	}

	IfStatement newIfStatement(Expression expression, Statement thenStatement) {
		IfStatement ifStatement = ast.newIfStatement();
		ifStatement.setExpression(expression);
		ifStatement.setThenStatement(thenStatement);
		return ifStatement;
	}

	CastExpression newCastExpression(Expression expression, Type type) {
		CastExpression castExpression = ast.newCastExpression();
		castExpression.setExpression(expression);
		castExpression.setType(type);
		return castExpression;
	}

	Block newBlock() {
		return ast.newBlock();
	}

	@SuppressWarnings("unchecked")
	Block newBlock(Statement... statements) {
		Block block = newBlock();
		for (Statement statement : statements)
			block.statements().add(statement);
		return block;
	}

	PrefixExpression newPrefixExpression(Expression expression,
			PrefixExpression.Operator operator) {
		PrefixExpression prefixExpression = ast.newPrefixExpression();
		prefixExpression.setOperand(expression);
		prefixExpression.setOperator(operator);
		return prefixExpression;
	}

	SingleVariableDeclaration newSingleVariableDeclaration(Code code,
			String name) {
		return newSingleVariableDeclaration(code, ast.newSimpleName(name));
	}

	SingleVariableDeclaration newSingleVariableDeclaration(Code code,
			SimpleName name) {
		return newSingleVariableDeclaration(ast.newPrimitiveType(code), name);
	}

	SingleVariableDeclaration newSingleVariableDeclaration(String type,
			String name, boolean variableArity) {
		SingleVariableDeclaration variable = newSingleVariableDeclaration(
				newType(type), name);
		variable.setVarargs(variableArity);
		return variable;
	}

	SingleVariableDeclaration newSingleVariableDeclaration(String type,
			String name) {
		return newSingleVariableDeclaration(newType(type), name);
	}

	SingleVariableDeclaration newSingleVariableDeclaration(Type type,
			String name) {
		return newSingleVariableDeclaration(type, ast.newSimpleName(name));
	}

	SingleVariableDeclaration newSingleVariableDeclaration(Type type,
			SimpleName name) {
		SingleVariableDeclaration variable = ast.newSingleVariableDeclaration();
		variable.setType(type);
		variable.setName(name);
		return variable;
	}

	VariableDeclarationStatement newVariableDeclarationStatement(Type type,
			String identifier, Expression expression) {
		VariableDeclarationStatement statement = ast
				.newVariableDeclarationStatement(newVariableDeclarationFragment(
						identifier, expression));
		statement.setType(type);
		return statement;
	}

	VariableDeclarationStatement newVariableDeclarationStatement(Code code,
			String identifier, Expression expression) {
		return newVariableDeclarationStatement(ast.newPrimitiveType(code),
				identifier, expression);
	}

	@SuppressWarnings("unchecked")
	MethodInvocation newMethodInvocation(Expression expression, String name,
			Expression... arguments) {
		MethodInvocation methodInvocation = newMethodInvocation(expression,
				name);
		for (Expression argument : arguments)
			methodInvocation.arguments().add(argument);
		return methodInvocation;
	}

	public SuperMethodInvocation newSuperMethodInvocation(String name) {
		SuperMethodInvocation superMethodInvocation = ast
				.newSuperMethodInvocation();
		superMethodInvocation.setName(ast.newSimpleName(name));
		return superMethodInvocation;
	}

	TextElement newTextElement() {
		return ast.newTextElement();
	}

	TextElement newTextElement(String text) {
		TextElement textElement = newTextElement();
		textElement.setText(text);
		return textElement;
	}

	@SuppressWarnings("unchecked")
	TagElement newTagElement(Object... fragments) {
		TagElement tagElement = ast.newTagElement();
		for (Object fragment : fragments)
			tagElement.fragments().add(fragment);
		return tagElement;
	}

	TagElement newTagElement(String tagName, Object... fragments) {
		TagElement tagElement = newTagElement(fragments);
		tagElement.setTagName(tagName);
		return tagElement;
	}

	@SuppressWarnings("unchecked")
	Javadoc newJavadoc(TagElement... tagElements) {
		Javadoc javaDoc = ast.newJavadoc();
		for (TagElement tagElement : tagElements)
			javaDoc.tags().add(tagElement);
		return javaDoc;
	}

	SwitchStatement newSwitchStatement(String identifier) {
		return newSwitchStatement(ast.newSimpleName(identifier));
	}

	SwitchStatement newSwitchStatement(Expression expression) {
		SwitchStatement switchStatement = ast.newSwitchStatement();
		switchStatement.setExpression(expression);
		return switchStatement;
	}

	SwitchCase newSwitchCase(Expression expression) {
		SwitchCase switchCase = ast.newSwitchCase();
		switchCase.setExpression(expression);
		return switchCase;
	}

	Name newName(String qualifiedName) {
		return ast.newName(qualifiedName);
	}

	NumberLiteral newNumberLiteral(String literal) {
		return ast.newNumberLiteral(literal);
	}

	SimpleName newSimpleName(String identifier) {
		return ast.newSimpleName(identifier);
	}

	NullLiteral newNullLiteral() {
		return ast.newNullLiteral();
	}

	SimpleType newSimpleType(String type) {
		return newSimpleType(newSimpleName(type));
	}

	SimpleType newSimpleType(Name name) {
		return ast.newSimpleType(name);
	}

	QualifiedName newQualifiedName(String qualifier, String name) {
		return ast.newQualifiedName(ast.newSimpleName(qualifier),
				ast.newSimpleName(name));
	}

	QualifiedName newQualifiedName(Name qualifier, SimpleName name) {
		return ast.newQualifiedName(qualifier, name);
	}

	BooleanLiteral newBooleanLiteral(boolean value) {
		return ast.newBooleanLiteral(value);
    }

    StringLiteral newStringLiteral(String value) {
        StringLiteral stringLiteral = ast.newStringLiteral();
        stringLiteral.setLiteralValue(value);
        return stringLiteral;
    }

    CharacterLiteral newCharacterLiteral(char value) {
        CharacterLiteral characterLiteral = ast.newCharacterLiteral();
        characterLiteral.setCharValue(value);
        return characterLiteral;
    }

    ThisExpression newThisExpression() {
		return ast.newThisExpression();
	}

	ExpressionStatement newExpressionStatement(Expression expression) {
		ExpressionStatement expressionStatement = ast
				.newExpressionStatement(expression);
		return expressionStatement;
	}

	@SuppressWarnings("unchecked")
	ParameterizedType newParameterizedType(Type type, Type... arguments) {
		ParameterizedType parameterizedType = newParameterizedType(type);
		for (Type argument : arguments)
			parameterizedType.typeArguments().add(argument);
		return parameterizedType;
	}

	ParameterizedType newParameterizedType(Type type) {
		return ast.newParameterizedType(type);
	}

	PrimitiveType newPrimitiveType(Code code) {
		return ast.newPrimitiveType(code);
	}

	SuperConstructorInvocation newSuperConstructorInvocation() {
		return ast.newSuperConstructorInvocation();
	}

	Type newType(String name) {
		Code code = PrimitiveType.toCode(name);
		if (code == null) {
			return ast.newSimpleType(ast.newName(name));
		} else {
			return ast.newPrimitiveType(code);
		}
	}

	FieldAccess newFieldAccess(Expression expression, String name) {
		FieldAccess fieldAccess = ast.newFieldAccess();
		fieldAccess.setExpression(expression);
		fieldAccess.setName(ast.newSimpleName(name));
		return fieldAccess;
	}

	public ClassInstanceCreation newClassInstanceCreation() {
		ClassInstanceCreation classInstanceCreation = ast
				.newClassInstanceCreation();
		return classInstanceCreation;
	}

	SimpleType newSimpleType(SimpleType type) {
		if (type.getName().isSimpleName()) {
			SimpleName simpleName = (SimpleName) type.getName();
			return newSimpleType(simpleName.getIdentifier());
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	ParameterizedType newParameterizedType(ParameterizedType type) {
		ParameterizedType parameterizedType = ast
				.newParameterizedType(newType(type.getType()));

		for (Object typeArgument : type.typeArguments()) {
			if (typeArgument instanceof Type) {
				parameterizedType.typeArguments().add(
						newType((Type) typeArgument));
			}
		}
		return parameterizedType;
	}

	Type newType(Type type) {
		if (type.isSimpleType()) {
			return newSimpleType((SimpleType) type);
		} else if (type.isParameterizedType()) {
			return newParameterizedType((ParameterizedType) type);
		}
		return null;
	}

	ClassInstanceCreation newClassInstanceCreation(Type type) {
		ClassInstanceCreation classInstanceCreation = ast
				.newClassInstanceCreation();
		classInstanceCreation.setType(newType(type));
		return classInstanceCreation;
	}

	EnhancedForStatement newEnhancedForStatement(
			SingleVariableDeclaration parameter, Expression expression,
			Statement statement) {
		EnhancedForStatement enhancedForStatement = ast
				.newEnhancedForStatement();
		enhancedForStatement.setParameter(parameter);
		enhancedForStatement.setExpression(expression);
		enhancedForStatement.setBody(statement);
		return enhancedForStatement;
    }

	boolean equals(Name name, String identifier) {
		if (name.isSimpleName()) {
			SimpleName simpleName = (SimpleName) name;
			return simpleName.getIdentifier().equals(identifier);
		} else if (name.isQualifiedName()) {
			QualifiedName qualifiedName = (QualifiedName) name;
			equals(qualifiedName.getName());
		}
		return false;
	}

    boolean equals(Type type, String identifier) {
        if (type.isSimpleType()) {
            SimpleType simpleType = (SimpleType) type;
            return equals(simpleType.getName(), identifier);
        } else if (type.isQualifiedType()) {
            QualifiedType qualifiedType = (QualifiedType) type;
            return equals(qualifiedType.getName(), identifier);
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    TryStatement newTryStatement(Block body, CatchClause ... catchClauses) {
        TryStatement tryStatement = ast.newTryStatement();
        tryStatement.setBody(body);
        tryStatement.catchClauses().addAll(Arrays.asList(catchClauses));
        return tryStatement;
    }

    CatchClause newCatchClause(SingleVariableDeclaration exception, Block body) {
        CatchClause catchClause = ast.newCatchClause();
        catchClause.setException(exception);
        catchClause.setBody(body);
        return catchClause;
    }

	@SuppressWarnings("unchecked")
	public FieldDeclaration newFieldDeclaration(
			VariableDeclarationFragment fragment, String type, int flags) {
		FieldDeclaration fieldDeclaration = ast.newFieldDeclaration(fragment);
		fieldDeclaration.modifiers().addAll(ast.newModifiers(flags));
		fieldDeclaration.setType(newType(type));
		return fieldDeclaration;
	}
}
