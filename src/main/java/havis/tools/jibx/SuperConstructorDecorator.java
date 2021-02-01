package havis.tools.jibx;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.jibx.binding.model.ElementBase;
import org.jibx.schema.codegen.IClassHolder;
import org.jibx.schema.codegen.extend.NameMatchDecoratorBase;

/**
 * <p>
 * Decorates the class by adding a additional super constructor. Value of
 * <b>params</b> should be a comma separated list of type and name pairs. <br />
 * </p>
 * <p>
 * i.e. set <b>params</b> to <br />
 * <br />
 * <code>
 * {@link String} message, {@link Throwable} cause
 * </code> <br />
 * <br />
 * to add a the super constructor to a class which extends {@link Exception}
 * </p>
 */
public class SuperConstructorDecorator extends NameMatchDecoratorBase {

	private final AST ast = new AST();

	private String params;

	public void setParams(String params) {
		this.params = params;
	}

	public void finish(ElementBase binding, IClassHolder holder) {
	}

	@SuppressWarnings("unchecked")
	private void addConstructor(IClassHolder holder) {
		// create constructor

		MethodDeclaration method = ast.newConstructorDeclaration(
				holder.getName(), Modifier.PUBLIC);

		SuperConstructorInvocation invocation = ast
				.newSuperConstructorInvocation();
		if (params != null) {
			for (String param : params.trim().split(",")) {
				String[] pair = param.trim().split(" ", 2);
				String type = pair[0].trim();
				String name = pair[1].trim();

				method.parameters().add(
						ast.newSingleVariableDeclaration(type, name));
				invocation.arguments().add(ast.newSimpleName(name));
			}
		}
		Block block = ast.newBlock(invocation);

		method.setBody(block);
		holder.addMethod(method);
	}

	public void start(IClassHolder holder) {
		if (matchName(holder.getName())) {
			addConstructor(holder);
		}
	}

	public void valueAdded(String basename, boolean collect, String type,
			FieldDeclaration field, MethodDeclaration getmeth,
			MethodDeclaration setmeth, String descript, IClassHolder holder) {
	}
}