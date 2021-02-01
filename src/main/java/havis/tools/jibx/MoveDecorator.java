package havis.tools.jibx;

import java.util.List;

import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.jibx.binding.model.ElementBase;
import org.jibx.schema.codegen.IClassHolder;
import org.jibx.schema.codegen.extend.NameMatchDecoratorBase;

/**
 * <p>
 * Decorates the {@link List} based class by adding a <i>move</i> method.<br />
 * This method moves an existing entry in the list to a given position.
 * </p>
 */
public class MoveDecorator extends NameMatchDecoratorBase {

	private final AST ast = new AST();

	private String name;
	private String type;

	public void start(IClassHolder holder) {
	}

	public void valueAdded(String basename, boolean collect, String type,
			FieldDeclaration field, MethodDeclaration getmeth,
			MethodDeclaration setmeth, String descript, IClassHolder holder) {
		if (matchName(holder.getName()) && basename.endsWith("List")) {
			this.name = basename;
			this.type = holder.getTypeName(type);
		}
	}

	public void finish(ElementBase binding, IClassHolder holder) {
		if (matchName(holder.getName()) && name != null) {
			holder.addMethod(ast.newMethodDeclaration(
					"move",
					ast.newSimpleType(ast.newSimpleName(type)),
					Modifier.PUBLIC,
					ast.newBlock(
							ast.newVariableDeclarationStatement(
									PrimitiveType.INT, "index", ast
											.newMethodInvocation(
													ast.newSimpleName(name),
													"indexOf",
													ast.newSimpleName("entry"))),
							ast.newIfStatement(
									ast.newInfixExpression(
											ast.newSimpleName("index"),
											ast.newNumberLiteral("0"),
											InfixExpression.Operator.LESS),
									ast.newReturnStatement(ast.newNullLiteral()),
									ast.newIfStatement(
											ast.newInfixExpression(
													ast.newSimpleName("index"),
													ast.newSimpleName("pos"),
													InfixExpression.Operator.EQUALS),
											ast.newReturnStatement(ast.newMethodInvocation(
													ast.newSimpleName(name),
													"get",
													ast.newSimpleName("index"))),
											ast.newExpressionStatement(ast.newMethodInvocation(
													ast.newSimpleName(name),
													"add",
													ast.newSimpleName("pos"),
													ast.newAssignment(
															ast.newSimpleName("entry"),
															ast.newMethodInvocation(
																	ast.newSimpleName(name),
																	"remove",
																	ast.newSimpleName("index"))))))),
							ast.newReturnStatement(ast.newSimpleName("entry"))),
					ast.newJavadoc(
							ast.newTagElement(ast
									.newTextElement("Moves the entry in list, which is equal to the given one, to a new position")),
							ast.newTagElement(ast.newTextElement()),
							ast.newTagElement("@param",
									ast.newSimpleName("entry"),
									ast.newTextElement(" The entry")),
							ast.newTagElement(
									"@param",
									ast.newSimpleName("pos"),
									ast.newTextElement(" The new position of the existing entry")),
							ast.newTagElement(ast.newTextElement()),
							ast.newTagElement(
									"@return",
									ast.newTextElement(" The exiting entry or null if entry does not exists"))),
					ast.newSingleVariableDeclaration(
							ast.newSimpleType(ast.newSimpleName(type)),
							ast.newSimpleName("entry")), ast
							.newSingleVariableDeclaration(
									ast.newPrimitiveType(PrimitiveType.INT),
									ast.newSimpleName("pos"))));
		}
	}
}