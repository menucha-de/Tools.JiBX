package havis.tools.jibx;

import java.lang.reflect.Modifier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.jibx.binding.model.ElementBase;
import org.jibx.schema.codegen.IClassHolder;
import org.jibx.schema.codegen.extend.NameMatchDecoratorBase;

/**
 * <p>
 * Decorates enumerations and adding a <i>parseInt</i> and a <i>toInt</i>
 * method. Value of <b>params</b>
 * </p>
 * should be in the following form: <br />
 * <code>
 * ONE_INT_VALUE(47), ANOTHER_INT_VALUE(34)
 * </code> </p>
 */
public class EnumDecorator extends NameMatchDecoratorBase {

	private final AST ast = new AST();

	private final static Pattern pattern = Pattern.compile("(.*)\\((.*)\\)");

	private String params;

	public void setParams(String params) {
		this.params = params;
	}

	@Override
	public void start(IClassHolder holder) {
	}

	@Override
	public void valueAdded(String basename, boolean collect, String type,
			FieldDeclaration field, MethodDeclaration getmeth,
			MethodDeclaration setmeth, String descript, IClassHolder holder) {
	}

	@Override
	@SuppressWarnings("unchecked")
	public void finish(ElementBase binding, IClassHolder holder) {
		if (params != null) {
			if (matchName(holder.getName())) {
				SwitchStatement parseStatement = ast
						.newSwitchStatement("value");

				SwitchStatement toStatement = ast.newSwitchStatement("value");

				for (String s : params.split(",")) {
					Matcher matcher = pattern.matcher(s.trim());
					if (matcher.matches()) {
						if (matcher.groupCount() > 1) {
							String name = matcher.group(1);
							String value = matcher.group(2);

							// creates switch case for parse method
							parseStatement.statements().add(
									ast.newSwitchCase(ast
											.newNumberLiteral(value)));

							parseStatement.statements().add(
									ast.newReturnStatement(ast.newName(name)));

							// creates switch case for to method
							toStatement.statements().add(
									ast.newSwitchCase(ast.newName(name)));

							toStatement.statements().add(
									ast.newReturnStatement(ast
											.newNumberLiteral(value)));
						}
					}
				}

				// creates default case for parse method
				parseStatement.statements().add(ast.newSwitchCase(null));
				parseStatement.statements().add(
						ast.newReturnStatement(ast.newNullLiteral()));

				// creates default case for to method
				toStatement.statements().add(ast.newSwitchCase(null));
				toStatement.statements().add(
						ast.newReturnStatement(ast.newNumberLiteral("-1")));

				// creates parse method
				holder.addMethod(ast.newMethodDeclaration("parseInt", holder
						.getName(), Modifier.PUBLIC | Modifier.STATIC, ast
						.newBlock(parseStatement), ast
						.newSingleVariableDeclaration(PrimitiveType.INT,
								"value")));

				// creates to method
				holder.addMethod(ast.newMethodDeclaration("toInt",
						PrimitiveType.INT, Modifier.PUBLIC | Modifier.STATIC,
						ast.newBlock(toStatement), ast
								.newSingleVariableDeclaration(holder.getName(),
										"value")));
			}
		}
	}
}