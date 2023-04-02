package javavalueobjectparser;

import java.io.*;
import javax.tools.*;
import javax.tools.JavaCompiler.CompilationTask;

import java.util.*;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.util.*;
import com.sun.source.util.*;
import com.sun.source.tree.*;
import com.sun.source.doctree.*;

public class CommentParser {
  public static void main(String[] args) throws Exception {
    File file = new File("MyClass.java");
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
    Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(file));
    CompilationTask task = compiler.getTask(null, fileManager, null, null, null, compilationUnits);
    task.setProcessors(Collections.singleton(new CommentProcessor()));
    task.call();
    fileManager.close();
  }
  
  public static class CommentProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
      for (Element element : roundEnv.getRootElements()) {
        Trees trees = Trees.instance(processingEnv);
        TreePathScanner<Void, Void> scanner = new TreePathScanner<Void, Void>() {
          @Override
          public Void visitVariable(VariableTree variableTree, Void aVoid) {
            processComments(trees.getDocCommentTree(trees.getPath(element, variableTree)));
            return super.visitVariable(variableTree, aVoid);
          }
          
          @Override
          public Void visitMethod(MethodTree methodTree, Void aVoid) {
            processComments(trees.getDocCommentTree(trees.getPath(element, methodTree)));
            return super.visitMethod(methodTree, aVoid);
          }
          
          @Override
          public Void visitClass(ClassTree classTree, Void aVoid) {
            processComments(trees.getDocCommentTree(trees.getPath(element, classTree)));
            return super.visitClass(classTree, aVoid);
          }
          
          private void processComments(DocCommentTree commentTree) {
            if (commentTree != null) {
              String comment = commentTree.getFullBody().trim();
              System.out.println(comment);
            }
          }
        };
        scanner.scan(trees.getPath(element));
      }
      return true;
    }
  }
}
