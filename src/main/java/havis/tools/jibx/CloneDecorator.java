package havis.tools.jibx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.jibx.binding.model.ElementBase;
import org.jibx.schema.codegen.IClassHolder;
import org.jibx.schema.codegen.extend.NameMatchDecoratorBase;

/**
 * <p>
 * Decorates the class by overriding <i>clone</i>.
 * </p>
 * <p>
 * <p>
 * Value of <b>includes</b> and <b>excludes</b> could be a comma separated list
 * of internal fields.
 * </p>
 * <p>
 * If neither <b>includes</b> nor <b>excludes</b> is given, cloning is flat.
 * </p>
 * <p>
 * Even, if <b>includes</b> or <b>excludes</b> is given, the resulting set of
 * fields will be cloned separately , too. The <i>class</i> definition of those
 * field set should override <i>clone</i>, too.
 * </p>
 * <p>
 * If one element of the field set is of type {@link List}, each element of
 * those list will be cloned, too.
 * </p>
 * <p>
 * 
 * </p>
 */
public class CloneDecorator extends NameMatchDecoratorBase {

	private final AST ast = new AST();

	private Map<String, Type> fields;
	private List<String> includes, excludes;

	public void setIncludes(String includes) {
		this.includes = new ArrayList<>();
		for (String include : includes.split(",")) {
			this.includes.add(include.trim());
		}
	}

	public void setExcludes(String excludes) {
		this.excludes = new ArrayList<>();
		for (String exclude : excludes.split(",")) {
			this.excludes.add(exclude.trim());
		}
	}

	public void start(IClassHolder holder) {
		fields = new HashMap<>();
	}

	public void valueAdded(String basename, boolean collect, String type,
			FieldDeclaration field, MethodDeclaration getmeth,
			MethodDeclaration setmeth, String descript, IClassHolder holder) {
		fields.put(basename, field.getType());
	}

	private Expression clone(String name, Expression expression) {
		if ((includes != null && includes.contains(name))
				|| (excludes != null && !excludes.contains(name))) {
			return ast.newMethodInvocation(expression, "clone");
		} else {
			return expression;
		}
	}

	@SuppressWarnings("unchecked")
	public void finish(ElementBase binding, IClassHolder holder) {
		if (matchName(holder.getName())) {
			holder.addInterface(Cloneable.class.getName());

			Block block = ast.newBlock();
			block.statements()
					.add(ast.newVariableDeclarationStatement("clone", ast
							.newClassInstanceCreation(ast.newType(holder
									.getName())), ast.newType(holder.getName())));
			for (Entry<String, Type> field : fields.entrySet()) {
				String name = field.getKey();
				Type type = field.getValue();
				if (type.isParameterizedType()) {
					ParameterizedType parameterizedType = (ParameterizedType) type;
					if (ast.equals(parameterizedType.getType(), "List")) {
						for (Object t : parameterizedType.typeArguments()) {
							FieldDeclaration fieldDeclaration = (FieldDeclaration) field
									.getValue().getParent();

							for (Object f : fieldDeclaration.fragments()) {
								VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) f;
								if (name.equals(variableDeclarationFragment
										.getName().getIdentifier())) {
									ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) variableDeclarationFragment
											.getInitializer();
									block.statements()
											.add(ast.newIfStatement(
													ast.newInfixExpression(
															ast.newSimpleName(field
																	.getKey()),
															ast.newNullLiteral(),
															InfixExpression.Operator.NOT_EQUALS),
													ast.newBlock(
															ast.newExpressionStatement(ast
																	.newAssignment(
																			ast.newQualifiedName(
																					"clone",
																					name),
																			ast.newClassInstanceCreation(classInstanceCreation
																					.getType()))),
															ast.newEnhancedForStatement(
																	ast.newSingleVariableDeclaration(
																			ast.newType((Type) t),
																			"entry"),
																	ast.newSimpleName(name),
																	ast.newBlock(ast
																			.newExpressionStatement(ast
																					.newMethodInvocation(
																							ast.newQualifiedName(
																									"clone",
																									name),
																							"add",
																							clone(name,
																									ast.newSimpleName("entry")))))))));
									break;
								}
							}
							break;
						}
					}
				} else if (type.isPrimitiveType()) {
					block.statements().add(
							ast.newExpressionStatement(ast.newAssignment(
									ast.newFieldAccess(ast.newName("clone"),
											name),
									clone(name, ast.newSimpleName(name)))));
				} else {
					block.statements()
							.add(ast.newIfStatement(
									ast.newInfixExpression(
											ast.newSimpleName(name),
											ast.newNullLiteral(),
											InfixExpression.Operator.NOT_EQUALS),
									ast.newExpressionStatement(ast.newAssignment(
											ast.newFieldAccess(
													ast.newName("clone"), name),
											clone(name, ast.newSimpleName(name))))));
				}
			}
			block.statements()
					.add(ast.newReturnStatement(ast.newName("clone")));
			holder.addMethod(ast.newMethodDeclaration("clone",
					holder.getName(), Modifier.PUBLIC, block));
		}
	}
}