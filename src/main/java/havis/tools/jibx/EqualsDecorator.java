package havis.tools.jibx;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.PrimitiveType.Code;
import org.jibx.binding.model.ElementBase;
import org.jibx.schema.codegen.IClassHolder;
import org.jibx.schema.codegen.extend.NameMatchDecoratorBase;

/**
 * <p>
 * Decorates the class by overriding <i>equals</i> and <i>hashCode</i>.
 * </p>
 * <p>
 * Value of <b>params</b> should be a comma separated list of internal fields <br />
 * <br />
 * <code>
 * i.e. <i>id</i>, <i>name</i> etc.
 * </code>
 * </p>
 */
public class EqualsDecorator extends NameMatchDecoratorBase {

	private final AST ast = new AST();

	private Map<String, Code> map = new LinkedHashMap<String, Code>();

	public void setParams(String params) {
		for (String param : params.split(",")) {
			String name = param.trim();
			map.put(name, null);
		}
	}

	public void start(IClassHolder holder) {
	}

	public void valueAdded(String basename, boolean collect, String type,
			FieldDeclaration field, MethodDeclaration getmeth,
			MethodDeclaration setmeth, String descript, IClassHolder holder) {
		if (map.containsKey(basename)) {
			map.put(basename, PrimitiveType.toCode(type));
		}
	}

	@SuppressWarnings("unchecked")
	private void addEquals(IClassHolder holder) {
		Block block = ast.newBlock();

		// check same reference
		block.statements().add(
				ast.newIfStatement(
						ast.newInfixExpression(ast.newThisExpression(),
								ast.newSimpleName("obj"), Operator.EQUALS),
						ast.newReturnStatement(ast.newBooleanLiteral(true))));

		// check null reference
		block.statements().add(
				ast.newIfStatement(
						ast.newInfixExpression(ast.newSimpleName("obj"),
								ast.newNullLiteral(), Operator.EQUALS),
						ast.newReturnStatement(ast.newBooleanLiteral(false))));

		// check same class
		block.statements().add(
				ast.newIfStatement(ast.newInfixExpression(ast
						.newMethodInvocation(null, "getClass"), ast
						.newMethodInvocation(ast.newSimpleName("obj"),
								"getClass"), Operator.NOT_EQUALS), ast
						.newReturnStatement(ast.newBooleanLiteral(false))));

		// cast to concrete class
		block.statements().add(
				ast.newVariableDeclarationStatement("other", ast
						.newCastExpression(ast.newSimpleName("obj"), ast
								.newSimpleType(ast.newSimpleName(holder
										.getName()))), ast.newSimpleType(ast
						.newSimpleName(holder.getName()))));

		// adds expression for each parameter
		for (Entry<String, Code> entry : map.entrySet()) {
			String name = entry.getKey();
			Code code = entry.getValue();
			if (code != null) {
				// check primitive type equivalence
				block.statements().add(
						ast.newIfStatement(ast.newInfixExpression(
								ast.newSimpleName(name),
								ast.newQualifiedName("other", name),
								Operator.NOT_EQUALS), ast
								.newReturnStatement(ast
										.newBooleanLiteral(false))));
			} else {
				// check simple type equivalence
				block.statements().add(
						ast.newIfStatement(ast.newInfixExpression(
								ast.newSimpleName(name), ast.newNullLiteral(),
								Operator.EQUALS),

						ast.newBlock(ast.newIfStatement(ast.newInfixExpression(
								ast.newQualifiedName("other", name),
								ast.newNullLiteral(), Operator.NOT_EQUALS), ast
								.newReturnStatement(ast
										.newBooleanLiteral(false)))

						), ast.newIfStatement(ast.newPrefixExpression(ast
								.newMethodInvocation(ast.newSimpleName(name),
										"equals",
										ast.newQualifiedName("other", name)),
								PrefixExpression.Operator.NOT), ast
								.newReturnStatement(ast
										.newBooleanLiteral(false)))));
			}
		}

		block.statements().add(
				ast.newReturnStatement(ast.newBooleanLiteral(true)));

		// create method
		holder.addMethod(ast.newMethodDeclaration("equals",
				PrimitiveType.BOOLEAN, Modifier.PUBLIC, block,
				ast.newSingleVariableDeclaration("Object", "obj")));
	}

	@SuppressWarnings("unchecked")
	private void addHashCode(IClassHolder holder) {
		Block block = ast.newBlock();

		// initialize prime
		block.statements().add(
				ast.newVariableDeclarationStatement("prime", "31",
						PrimitiveType.INT, Modifier.FINAL));

		// initialize result
		block.statements().add(
				ast.newVariableDeclarationStatement("result", "1",
						PrimitiveType.INT));

		// adds expression for each parameter
		for (Entry<String, Code> entry : map.entrySet()) {
			String name = entry.getKey();
			Code code = entry.getValue();
			if (code != null) {
				// adds primitive type calculation
				block.statements().add(
						ast.newExpressionStatement(ast.newAssignment(ast
								.newSimpleName("result"), ast
								.newInfixExpression(ast.newInfixExpression(
										ast.newSimpleName("prime"),
										ast.newSimpleName("result"),
										Operator.TIMES), ast
										.newSimpleName(name)))));
			} else {
				// adds simple type calculation
				block.statements()
						.add(ast.newExpressionStatement(ast.newAssignment(
								ast.newSimpleName("result"),
								ast.newInfixExpression(
										ast.newInfixExpression(
												ast.newSimpleName("prime"),
												ast.newSimpleName("result"),
												Operator.TIMES),
										ast.newParenthesizedExpression(ast.newConditionalExpression(
												ast.newParenthesizedExpression(ast.newInfixExpression(
														ast.newSimpleName(name),
														ast.newNullLiteral(),
														Operator.EQUALS)),
												ast.newNumberLiteral("0"),
												ast.newMethodInvocation(
														ast.newSimpleName(name),
														"hashCode")))))));
			}
		}

		block.statements().add(
				ast.newReturnStatement(ast.newSimpleName("result")));

		// create method
		holder.addMethod(ast.newMethodDeclaration("hashCode",
				PrimitiveType.INT, Modifier.PUBLIC, block));
	}

	public void finish(ElementBase binding, IClassHolder holder) {
		if (matchName(holder.getName())) {
			addEquals(holder);
			addHashCode(holder);
		}
	}
}