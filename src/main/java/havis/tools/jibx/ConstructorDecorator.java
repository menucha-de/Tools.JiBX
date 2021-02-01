package havis.tools.jibx;

import havis.tools.jibx.AST.Pair;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.jibx.binding.model.ElementBase;
import org.jibx.schema.codegen.IClassHolder;
import org.jibx.schema.codegen.extend.NameMatchDecoratorBase;

/**
 * <p>
 * Decorates the class by adding an additional constructor to set the initial
 * value of fields. Value of <b>params</b> should be a comma separated list of
 * <i>type</i> and <i>name</i> pairs. <br />
 * </p>
 * <p>
 * i.e. set <b>params</b> to <br />
 * <br />
 * <code>
 * <i>int</i> id, {@link String} name
 * </code> <br />
 * <br />
 * to add a constructor which set the initial value of <i>id</i> and <i>name</i>
 * for a new instance.<br />
 * The implementation will add the default constructor, too. Set <b>empty</b> to
 * <i>false</i> avoid the behavior.
 * </p>
 */
public class ConstructorDecorator extends NameMatchDecoratorBase {

	private final AST ast = new AST();

	private boolean empty = true;
	private String params;

	public void setEmpty(boolean empty) {
		this.empty = empty;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public void finish(ElementBase binding, IClassHolder holder) {
	}

	private void addEmptyConstructor(IClassHolder holder) {
		holder.addMethod(ast.newConstructorDeclaration(holder.getName(),
				Modifier.PUBLIC, ast.newBlock()));
	}

	@SuppressWarnings("unchecked")
	private void addConstructor(IClassHolder holder) {
		MethodDeclaration method = ast.newConstructorDeclaration(
				holder.getName(), Modifier.PUBLIC, ast.newBlock());
		Block block = ast.newBlock();
		if (params != null) {
			for (String param : params.trim().split(",")) {
				Pair pair = ast.new Pair(param);

				method.parameters().add(
						ast.newSingleVariableDeclaration(pair.getType(),
								pair.getName()));

				block.statements().add(
						ast.newExpressionStatement(ast.newAssignment(ast
								.newFieldAccess(ast.newThisExpression(),
										pair.getName()), ast.newSimpleName(pair
								.getName()))));
			}
		}
		method.setBody(block);
		holder.addMethod(method);
	}

	public void start(IClassHolder holder) {
		if (matchName(holder.getName())) {
			if (empty)
				addEmptyConstructor(holder);
			addConstructor(holder);
		}
	}

	public void valueAdded(String basename, boolean collect, String type,
			FieldDeclaration field, MethodDeclaration getmeth,
			MethodDeclaration setmeth, String descript, IClassHolder holder) {
	}
}