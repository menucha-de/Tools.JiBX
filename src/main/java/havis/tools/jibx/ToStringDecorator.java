package havis.tools.jibx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.jibx.binding.model.ElementBase;
import org.jibx.schema.codegen.IClassHolder;
import org.jibx.schema.codegen.StructureClassHolder;
import org.jibx.schema.codegen.extend.NameMatchDecoratorBase;

/**
 * <p>
 * Decorates the class by overriding <i>toString()</i>.
 * </p>
 * <p>
 * <p>
 * Value of <b>includes</b> and <b>excludes</b> could be a comma separated list
 * of internal fields.
 * </p>
 * <p>
 * If neither <b>includes</b> nor <b>excludes</b> is given, all fields will be considered.
 * </p>
 * <p>
 * For fields with complex types the toString() method will be called.
 * </p>
 * <p>
 * If one element of the field set is of type {@link List}, the toString() method of all entries will be called.
 * </p>
 */
public class ToStringDecorator extends NameMatchDecoratorBase {

    private static Map<String, Map<String, Field>> globalFieldList = Collections.synchronizedMap(new LinkedHashMap<String, Map<String, Field>>());

    private static class Field {
        private Type type;
        private String getter;

        public Field(Type type, String getter) {
            this.type = type;
            this.getter = getter;
        }

        public Type getType() {
            return type;
        }
        public String getGetter() {
            return getter;
        }
    }

	private final AST ast = new AST();

	private Map<String, Field> fields;
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

	@Override
    public void start(IClassHolder holder) {
		fields = new LinkedHashMap<>();
	}

	@Override
    public void valueAdded(String basename, boolean collect, String type,
			FieldDeclaration field, MethodDeclaration getmeth,
			MethodDeclaration setmeth, String descript, IClassHolder holder) {
		fields.put(basename, new Field(field.getType(), getmeth.getName().getIdentifier()));
	}

    private boolean isConsidered(String name) {
        if ((includes != null && !includes.contains(name)) || (excludes != null && excludes.contains(name))) {
            return false;
        }
        return true;
    }

	@Override
    @SuppressWarnings("unchecked")
	public void finish(ElementBase binding, IClassHolder holder) {
		if (matchName(holder.getName())) {

		    globalFieldList.put(holder.getFullName(), fields);
		    Map<String, Field> allFields = new LinkedHashMap<>();
		    // add super fields
		    if (holder instanceof StructureClassHolder) {
		        StructureClassHolder current = ((StructureClassHolder) holder);
		        while (current.getSuperClass() != null) {
                    if (!(current.getSuperClass() instanceof StructureClassHolder)) break;
                    Map<String, Field> superFields = globalFieldList.get(current.getSuperClassName());
                    if (superFields != null) allFields.putAll(superFields);
                    current = (StructureClassHolder) current.getSuperClass();
                }
		    }
		    // add current fields
		    allFields.putAll(fields);

			holder.addImport(StringBuilder.class.getName());

			Block block = ast.newBlock();
			block.statements()
					.add(ast.newVariableDeclarationStatement("result", ast
							.newClassInstanceCreation(ast.newType(StringBuilder.class.getSimpleName())), ast.newType(StringBuilder.class.getSimpleName())));
			block.statements().add(ast.newExpressionStatement(ast.newMethodInvocation(ast.newSimpleName("result"), "append", ast.newCharacterLiteral('{'))));
			boolean first = true;
			for (Entry<String, Field> field : allFields.entrySet()) {
				String name = field.getKey();
				if (isConsidered(name)) {
    				Type type = field.getValue().getType();
    				String getter = field.getValue().getGetter();

    				String keyPart;
                    if (first) {
                        first = false;
                        keyPart = "\"" + name + "\":";
                    } else
                        keyPart = ",\"" + name + "\":";

    				block.statements().add(ast.newExpressionStatement(ast.newMethodInvocation(ast.newSimpleName("result"), "append", ast.newStringLiteral(keyPart))));

    				if (type.isParameterizedType()) {
    					ParameterizedType parameterizedType = (ParameterizedType) type;
    					if (ast.equals(parameterizedType.getType(), "List")) {
    						for (Object t : parameterizedType.typeArguments()) {
    							FieldDeclaration fieldDeclaration = (FieldDeclaration) type.getParent();

    							for (Object f : fieldDeclaration.fragments()) {
    								VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) f;
    								if (name.equals(variableDeclarationFragment
    										.getName().getIdentifier())) {
    								    boolean isString = isString((Type) t);
    									Block ifBlock = ast.newBlock();
    									ifBlock.statements().add(ast.newExpressionStatement(ast.newMethodInvocation(ast.newSimpleName("result"), "append", ast.newCharacterLiteral('['))));
                                        ifBlock.statements().add(ast.newVariableDeclarationStatement("first", ast.newBooleanLiteral(true), ast.newPrimitiveType(PrimitiveType.BOOLEAN)));

    									Block forBlock = ast.newBlock();
    									forBlock.statements().add(ast.newIfStatement(ast.newSimpleName("first"),
                                                        ast.newExpressionStatement(ast.newAssignment(ast.newSimpleName("first"), ast.newBooleanLiteral(false))),
                                                        ast.newExpressionStatement(ast.newMethodInvocation(ast.newSimpleName("result"), "append", ast.newCharacterLiteral(',')))));
    									if (isString)
    									    forBlock.statements().add(ast.newExpressionStatement(ast.newMethodInvocation(ast.newSimpleName("result"), "append", ast.newCharacterLiteral('"'))));
    									forBlock.statements().add(ast.newExpressionStatement(ast.newMethodInvocation(ast.newSimpleName("result"), "append", ast.newMethodInvocation(ast.newSimpleName("entry"), "toString"))));
                                        if (isString)
                                            forBlock.statements().add(ast.newExpressionStatement(ast.newMethodInvocation(ast.newSimpleName("result"), "append", ast.newCharacterLiteral('"'))));

                                        ifBlock.statements().add(
                                                                ast.newEnhancedForStatement(
                                                                        ast.newSingleVariableDeclaration(
                                                                                ast.newType((Type) t),
                                                                                "entry"),
                                                                        ast.newMethodInvocation(null, getter),
                                                                        forBlock));
    									ifBlock.statements().add(ast.newExpressionStatement(ast.newMethodInvocation(ast.newSimpleName("result"), "append", ast.newCharacterLiteral(']'))));
    									block.statements()
    											.add(ast.newIfStatement(
    													ast.newInfixExpression(ast.newMethodInvocation(null, getter),
    															ast.newNullLiteral(),
    															InfixExpression.Operator.NOT_EQUALS),
    													ifBlock, ast.newExpressionStatement(ast.newMethodInvocation(ast.newSimpleName("result"), "append", ast.newStringLiteral("null")))));
    									break;
    								}
    							}
    							break;
    						}
    					}
    				} else if (type.isPrimitiveType()) {
    				    block.statements().add(ast.newExpressionStatement(ast.newMethodInvocation(ast.newSimpleName("result"), "append", ast.newMethodInvocation(null, getter))));
    				} else {
    				    boolean isString = isString(type);
    				    Block ifBlock = ast.newBlock();
    				    if (isString)
    				        ifBlock.statements().add(ast.newExpressionStatement(ast.newMethodInvocation(ast.newSimpleName("result"), "append", ast.newCharacterLiteral('"'))));
    				    ifBlock.statements().add(ast.newExpressionStatement(ast.newMethodInvocation(ast.newSimpleName("result"), "append", ast.newMethodInvocation(ast.newMethodInvocation(null, getter), "toString"))));
    				    if (isString)
    				        ifBlock.statements().add(ast.newExpressionStatement(ast.newMethodInvocation(ast.newSimpleName("result"), "append", ast.newCharacterLiteral('"'))));
    					block.statements()
    							.add(ast.newIfStatement(
    									ast.newInfixExpression(
    									        ast.newMethodInvocation(null, getter),
    											ast.newNullLiteral(),
    											InfixExpression.Operator.NOT_EQUALS),
    									ifBlock, ast.newExpressionStatement(ast.newMethodInvocation(ast.newSimpleName("result"), "append", ast.newStringLiteral("null")))));
    				}
				}
			}
            block.statements().add(ast.newExpressionStatement(ast.newMethodInvocation(ast.newSimpleName("result"), "append", ast.newCharacterLiteral('}'))));
			block.statements()
					.add(ast.newReturnStatement(ast.newMethodInvocation(ast.newSimpleName("result"), "toString")));
			holder.addMethod(ast.newMethodDeclaration("toString",
					"String", Modifier.PUBLIC, block));
		}
	}

    private boolean isString(Type type) {
        if (type != null && type.isSimpleType()) {
            return String.class.getSimpleName().equals(((SimpleType) type).getName().toString());
        }
        return false;
    }
}