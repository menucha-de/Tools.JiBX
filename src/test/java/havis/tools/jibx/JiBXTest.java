package havis.tools.jibx;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.jibx.schema.codegen.CodeGen;
import org.junit.Assert;
import org.junit.Test;

public class JiBXTest {

	JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

	final String target = "target/test";

	@Test
	public void test() throws Exception {
		CodeGen.main(new String[] { "-t", target, "-c",
				"src/test/java/havis/tools/jibx/jibx-conf.xml",
				"src/test/java/havis/tools/jibx/Test.xsd" });

		final MessageDigest md = MessageDigest.getInstance("SHA1");

		Files.walkFileTree(Paths.get(target), new SimpleFileVisitor<Path>() {
			@Override
            public FileVisitResult visitFile(Path file,
					BasicFileAttributes attrs) throws IOException {
				String hash;
				switch (file.getFileName().toString()) {
				case "ConstructorType.java":
					hash = "8f361e59b538cfce269dcdfa1f9061f271c310ad";
					break;
				case "EnumType.java":
					hash = "eda56e68e5b2e63cceaadcb9f2164d8b3a225e08";
					break;
				case "EqualsType.java":
					hash = "a3d034afbb26cdafc0a733092b45d9aaf8d4bbb1";
					break;
				case "MoveType.java":
					hash = "8a771d4423be3615550e4527f75d94dd521c47fe";
					break;
				case "SuperConstructorType.java":
					hash = "87ca452a0535f133955f53080af155f30db53e0a";
					break;
				case "CloneType.java":
					hash = "195fcce228dfafe7ee2854ed040ff5fb75e14a45";
					break;
				case "EntryType.java":
					hash = "28fa0e9a16c15473c418211b9f0956e8cf5e0869";
					break;
				case "ListType.java":
					hash = "b1e9e4e10b85b8431d92a5ed52785423ed1e92df";
					break;
                case "ToStringType.java":
                    hash = "2ca1b29d9fe5bd045ae28217a2106a60e37eaeeb";
                    break;
                case "ToStringExtendType.java":
                    hash = "e2d67c03c7f736a68e2110d47698462e76f6af4f";
                    break;
                case "StringListType.java":
                    hash = "3d10ce52ab259f04d131d123ad87148bc03bc9fb";
                    break;
				default:
					hash = null;
				}
				if (hash != null) {
					md.update(Files.readAllBytes(file));

					Assert.assertEquals("Excpected " + hash + " for '" + file
							+ "'", hash,
							new BigInteger(1, md.digest()).toString(16));
				}
				Files.delete(file);
				return super.visitFile(file, attrs);
			}

			@Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc)
					throws IOException {
				Files.delete(dir);
				return super.postVisitDirectory(dir, exc);
			}
		});
	}

	public void testTest() throws Exception {
		CodeGen.main(new String[] { "-t", target, "-c",
				"src/test/java/havis/tools/jibx/jibx-conf.xml",
				"src/test/java/havis/tools/jibx/Test.xsd" });
	}
}