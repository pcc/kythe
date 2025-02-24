/*
 * Copyright 2014 The Kythe Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.devtools.kythe.analyzers.java;

import com.google.common.base.Ascii;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import com.google.common.flogger.FluentLogger;
import com.google.common.io.ByteStreams;
import com.google.devtools.kythe.analyzers.base.EdgeKind;
import com.google.devtools.kythe.analyzers.base.EntrySet;
import com.google.devtools.kythe.analyzers.java.KytheDocTreeScanner.DocCommentVisitResult;
import com.google.devtools.kythe.analyzers.java.SourceText.Comment;
import com.google.devtools.kythe.analyzers.java.SourceText.Keyword;
import com.google.devtools.kythe.analyzers.java.SourceText.Positions;
import com.google.devtools.kythe.analyzers.jvm.JvmGraph;
import com.google.devtools.kythe.analyzers.jvm.JvmGraph.Type.ReferenceType;
import com.google.devtools.kythe.platform.java.filemanager.JavaFileStoreBasedFileManager;
import com.google.devtools.kythe.platform.java.helpers.JCTreeScanner;
import com.google.devtools.kythe.platform.java.helpers.JavacUtil;
import com.google.devtools.kythe.platform.java.helpers.SignatureGenerator;
import com.google.devtools.kythe.platform.shared.Metadata;
import com.google.devtools.kythe.platform.shared.MetadataLoaders;
import com.google.devtools.kythe.platform.shared.StatisticsCollector;
import com.google.devtools.kythe.proto.Diagnostic;
import com.google.devtools.kythe.proto.MarkedSource;
import com.google.devtools.kythe.proto.Storage.VName;
import com.google.devtools.kythe.util.Span;
import com.sun.source.tree.MemberReferenceTree.ReferenceMode;
import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree.Kind;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.PackageSymbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCArrayTypeTree;
import com.sun.tools.javac.tree.JCTree.JCAssert;
import com.sun.tools.javac.tree.JCTree.JCAssign;
import com.sun.tools.javac.tree.JCTree.JCAssignOp;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCExpressionStatement;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCFunctionalExpression;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCImport;
import com.sun.tools.javac.tree.JCTree.JCLambda;
import com.sun.tools.javac.tree.JCTree.JCLiteral;
import com.sun.tools.javac.tree.JCTree.JCMemberReference;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.tree.JCTree.JCPackageDecl;
import com.sun.tools.javac.tree.JCTree.JCPrimitiveTypeTree;
import com.sun.tools.javac.tree.JCTree.JCReturn;
import com.sun.tools.javac.tree.JCTree.JCThrow;
import com.sun.tools.javac.tree.JCTree.JCTypeApply;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.JCTree.JCWildcard;
import com.sun.tools.javac.util.Context;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.NestingKind;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;

/** {@link JCTreeScanner} that emits Kythe nodes and edges. */
public class KytheTreeScanner extends JCTreeScanner<JavaNode, TreeContext> {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  /** Maximum allowed text size for variable {@link MarkedSource.Kind.INITIALIZER}s */
  private static final int MAX_INITIALIZER_LENGTH = 80;

  /** Name for special source file containing package annotations and documentation. */
  private static final String PACKAGE_INFO_NAME = "package-info";

  private final JavaIndexerConfig config;

  private final JavaEntrySets entrySets;
  private final StatisticsCollector statistics;
  // TODO(schroederc): refactor SignatureGenerator for new schema names
  private final SignatureGenerator signatureGenerator;
  private final Positions filePositions;
  private final Map<Integer, List<Comment>> comments = new HashMap<>();
  private final Map<Integer, Integer> commentClaims = new HashMap<>();
  private final BiConsumer<JCTree, VName> nodeConsumer;
  private final Context javaContext;
  private final JavaFileStoreBasedFileManager fileManager;
  private final MetadataLoaders metadataLoaders;
  private final JvmGraph jvmGraph;
  private List<Metadata> metadata;

  private KytheDocTreeScanner docScanner;

  private KytheTreeScanner(
      JavaEntrySets entrySets,
      StatisticsCollector statistics,
      SignatureGenerator signatureGenerator,
      SourceText src,
      Context javaContext,
      BiConsumer<JCTree, VName> nodeConsumer,
      JavaFileStoreBasedFileManager fileManager,
      MetadataLoaders metadataLoaders,
      JvmGraph jvmGraph,
      JavaIndexerConfig config) {
    this.entrySets = entrySets;
    this.statistics = statistics;
    this.signatureGenerator = signatureGenerator;
    this.filePositions = src.getPositions();
    this.javaContext = javaContext;
    this.nodeConsumer = nodeConsumer;
    this.fileManager = fileManager;
    this.metadataLoaders = metadataLoaders;
    this.jvmGraph = jvmGraph;
    this.config = config;

    for (Comment comment : src.getComments()) {
      for (int line = comment.lineSpan.getStart(); line <= comment.lineSpan.getEnd(); line++) {
        if (comments.containsKey(line)) {
          comments.get(line).add(comment);
        } else {
          comments.put(line, Lists.newArrayList(comment));
        }
      }
    }
  }

  public static void emitEntries(
      Context javaContext,
      StatisticsCollector statistics,
      JavaEntrySets entrySets,
      SignatureGenerator signatureGenerator,
      JCCompilationUnit compilation,
      BiConsumer<JCTree, VName> nodeConsumer,
      SourceText src,
      JavaFileStoreBasedFileManager fileManager,
      MetadataLoaders metadataLoaders,
      JavaIndexerConfig config)
      throws IOException {
    new KytheTreeScanner(
            entrySets,
            statistics,
            signatureGenerator,
            src,
            javaContext,
            nodeConsumer,
            fileManager,
            metadataLoaders,
            config.getJvmMode() == JavaIndexerConfig.JvmMode.SEMANTIC
                ? new JvmGraph(statistics, entrySets.getEmitter())
                : null,
            config)
        .scan(compilation, null);
  }

  /** Returns the {@link Symtab} (symbol table) for the compilation currently being processed. */
  public Symtab getSymbols() {
    return Symtab.instance(javaContext);
  }

  @Override
  public JavaNode scan(JCTree tree, TreeContext owner) {
    JavaNode node = super.scan(tree, owner);
    if (node != null && nodeConsumer != null) {
      nodeConsumer.accept(tree, node.getVName());
    }
    return node;
  }

  @Override
  public JavaNode visitTopLevel(JCCompilationUnit compilation, TreeContext owner) {
    if (compilation.docComments != null) {
      docScanner = new KytheDocTreeScanner(this, javaContext);
    }
    TreeContext ctx = new TreeContext(filePositions, compilation);
    metadata = new ArrayList<>();

    EntrySet fileNode = entrySets.newFileNodeAndEmit(filePositions);

    List<JavaNode> decls = scanList(compilation.getTypeDecls(), ctx);
    decls.removeAll(Collections.singleton(null));

    JavaNode pkgNode = scan(compilation.getPackage(), ctx);
    if (pkgNode != null) {
      for (JavaNode n : decls) {
        entrySets.emitEdge(n.getVName(), EdgeKind.CHILDOF, pkgNode.getVName());
      }
    }

    scan(compilation.getImports(), ctx);
    return new JavaNode(fileNode);
  }

  @Override
  public JavaNode visitPackage(JCPackageDecl pkg, TreeContext owner) {
    TreeContext ctx = owner.down(pkg.pid);

    VName pkgNode = entrySets.newPackageNodeAndEmit(pkg.packge).getVName();

    boolean isPkgInfo =
        filePositions
            .getSourceFile()
            .isNameCompatible(PACKAGE_INFO_NAME, JavaFileObject.Kind.SOURCE);
    EdgeKind anchorKind = isPkgInfo ? EdgeKind.DEFINES_BINDING : EdgeKind.REF;
    emitAnchor(ctx, anchorKind, pkgNode);

    visitDocComment(pkgNode, null, /* modifiers= */ null);
    visitAnnotations(pkgNode, pkg.getAnnotations(), ctx);

    return new JavaNode(pkgNode);
  }

  @Override
  public JavaNode visitImport(JCImport imprt, TreeContext owner) {
    return scan(imprt.qualid, owner.downAsSnippet(imprt));
  }

  @Override
  public JavaNode visitIdent(JCIdent ident, TreeContext owner) {
    TreeContext ctx = owner.down(ident);
    if (ident.sym == null) {
      return emitDiagnostic(ctx, "missing identifier symbol", null, null);
    }
    return emitSymUsage(ctx, ident.sym);
  }

  @Override
  public JavaNode visitClassDef(JCClassDecl classDef, TreeContext owner) {
    loadAnnotationsFromClassDecl(classDef);
    TreeContext ctx = owner.down(classDef);

    Optional<String> signature = signatureGenerator.getSignature(classDef.sym);
    if (!signature.isPresent()) {
      // TODO(schroederc): details
      return emitDiagnostic(ctx, "missing class signature", null, null);
    }

    MarkedSource.Builder markedSource = MarkedSource.newBuilder();
    VName classNode =
        entrySets.getNode(signatureGenerator, classDef.sym, signature.get(), markedSource, null);

    // Emit the fact that the class is a child of its containing class or method.
    // Note that for a nested/inner class, we already emitted the fact that it's a
    // child of the containing class when we scanned the containing class's members.
    // However we can't restrict ourselves to just classes contained in methods here,
    // because that would miss the case of local/anonymous classes in static/member
    // initializers. But there's no harm in emitting the same fact twice!
    getScope(ctx).ifPresent(scope -> entrySets.emitEdge(classNode, EdgeKind.CHILDOF, scope));

    NestingKind nestingKind = classDef.sym.getNestingKind();
    if (nestingKind != NestingKind.LOCAL && nestingKind != NestingKind.ANONYMOUS) {
      if (jvmGraph != null) {
        // Emit corresponding JVM node
        JvmGraph.Type.ReferenceType referenceType = referenceType(classDef.sym.type);
        VName jvmNode = jvmGraph.emitClassNode(referenceType);
        entrySets.emitEdge(classNode, EdgeKind.GENERATES, jvmNode);
      } else {
        // Emit NAME nodes for the jvm binary name of classes.
        VName nameNode = entrySets.getJvmNameAndEmit(classDef.sym.flatname.toString()).getVName();
        entrySets.emitEdge(classNode, EdgeKind.NAMED, nameNode);
      }
    }

    Span classIdent = filePositions.findIdentifier(classDef.name, classDef.getPreferredPosition());
    if (!classDef.name.isEmpty() && classIdent == null) {
      logger.atWarning().log("Missing span for class identifier: %s", classDef.sym);
    }

    // Generic classes record the source range of the class name for the abs node, regular
    // classes record the source range of the class name for the record node.
    EntrySet absNode =
        defineTypeParameters(
            ctx,
            classNode,
            classDef.getTypeParameters(),
            ImmutableList.<VName>of(), /* There are no wildcards in class definitions */
            markedSource.build());

    boolean documented = visitDocComment(classNode, absNode, classDef.getModifiers());

    if (absNode != null) {
      if (classIdent != null) {
        EntrySet absAnchor =
            entrySets.newAnchorAndEmit(filePositions, classIdent, ctx.getSnippet());
        emitDefinesBindingEdge(classIdent, absAnchor, absNode.getVName(), getScope(ctx));
      }
      if (!documented) {
        emitComment(classDef, absNode.getVName());
      }
    }
    if (absNode == null && classIdent != null) {
      EntrySet anchor = entrySets.newAnchorAndEmit(filePositions, classIdent, ctx.getSnippet());
      emitDefinesBindingEdge(classIdent, anchor, classNode, getScope(ctx));
    }
    emitAnchor(ctx, EdgeKind.DEFINES, classNode);
    if (!documented) {
      emitComment(classDef, classNode);
    }

    visitAnnotations(classNode, classDef.getModifiers().getAnnotations(), ctx);

    JavaNode superClassNode = scan(classDef.getExtendsClause(), ctx);
    if (superClassNode == null) {
      // Use the implicit superclass.
      switch (classDef.getKind()) {
        case CLASS:
          superClassNode = getJavaLangObjectNode();
          break;
        case ENUM:
          superClassNode = getJavaLangEnumNode(classNode);
          break;
        case ANNOTATION_TYPE:
          // TODO(schroederc): handle annotation superclass
          break;
        case INTERFACE:
          break; // Interfaces have no implicit superclass.
        default:
          logger.atWarning().log("Unexpected JCClassDecl kind: %s", classDef.getKind());
          break;
      }
    }

    if (superClassNode != null) {
      entrySets.emitEdge(classNode, EdgeKind.EXTENDS, superClassNode.getVName());
    }

    for (JCExpression implClass : classDef.getImplementsClause()) {
      JavaNode implNode = scan(implClass, ctx);
      if (implNode == null) {
        statistics.incrementCounter("warning-missing-implements-node");
        logger.atWarning().log(
            "Missing 'implements' node for %s: %s", implClass.getClass(), implClass);
        continue;
      }
      entrySets.emitEdge(classNode, EdgeKind.EXTENDS, implNode.getVName());
    }

    // Set the resulting node for the class before recursing through its members.  Setting the node
    // first is necessary to correctly add childof edges from local/anonymous classes defined
    // directly in the class body (in static initializers or member initializers).
    JavaNode node = ctx.setNode(new JavaNode(classNode));

    for (JCTree member : classDef.getMembers()) {
      JavaNode n = scan(member, ctx);
      if (n != null) {
        entrySets.emitEdge(n.getVName(), EdgeKind.CHILDOF, classNode);
      }
    }

    return node;
  }

  @Override
  public JavaNode visitMethodDef(JCMethodDecl methodDef, TreeContext owner) {
    TreeContext ctx = owner.down(methodDef);

    scan(methodDef.getThrows(), ctx);
    scan(methodDef.getDefaultValue(), ctx);
    scan(methodDef.getReceiverParameter(), ctx);

    JavaNode returnType = scan(methodDef.getReturnType(), ctx);
    List<JavaNode> params = new ArrayList<>();
    List<JavaNode> paramTypes = new ArrayList<>();
    List<VName> wildcards = new ArrayList<>();
    for (JCVariableDecl param : methodDef.getParameters()) {
      JavaNode n = scan(param, ctx);
      params.add(n);

      JavaNode typeNode = n.getType();
      if (typeNode == null) {
        logger.atWarning().log(
            "Missing parameter type (method: %s; parameter: %s)", methodDef.getName(), param);
        wildcards.addAll(n.childWildcards);
        continue;
      }
      wildcards.addAll(typeNode.childWildcards);
      paramTypes.add(typeNode);
    }

    Optional<String> signature = signatureGenerator.getSignature(methodDef.sym);
    if (!signature.isPresent()) {
      // Try to scan method body even if signature could not be generated.
      scan(methodDef.getBody(), ctx);

      // TODO(schroederc): details
      return emitDiagnostic(ctx, "missing method signature", null, null);
    }

    MarkedSource.Builder markedSource = MarkedSource.newBuilder();
    VName methodNode =
        entrySets.getNode(signatureGenerator, methodDef.sym, signature.get(), markedSource, null);
    visitAnnotations(methodNode, methodDef.getModifiers().getAnnotations(), ctx);

    EntrySet absNode =
        defineTypeParameters(
            ctx, methodNode, methodDef.getTypeParameters(), wildcards, markedSource.build());
    boolean documented = visitDocComment(methodNode, absNode, methodDef.getModifiers());

    // Emit corresponding JVM node
    if (jvmGraph != null) {
      JvmGraph.Type.MethodType methodJvmType =
          toMethodJvmType((Type.MethodType) externalType(methodDef.sym));
      ReferenceType parentClass = referenceType(externalType(owner.getTree().type.tsym));
      String methodName = methodDef.name.toString();
      VName jvmNode = jvmGraph.emitMethodNode(parentClass, methodName, methodJvmType);
      entrySets.emitEdge(methodNode, EdgeKind.GENERATES, jvmNode);

      for (int i = 0; i < params.size(); i++) {
        JavaNode param = params.get(i);
        VName paramJvmNode = jvmGraph.emitParameterNode(parentClass, methodName, methodJvmType, i);
        entrySets.emitEdge(param.getVName(), EdgeKind.GENERATES, paramJvmNode);
        entrySets.emitEdge(jvmNode, EdgeKind.PARAM, paramJvmNode, i);
      }
    }

    VName ret = null;
    EntrySet bindingAnchor = null;
    if (methodDef.sym.isConstructor()) {
      // Implicit constructors (those without syntactic definition locations) share the same
      // preferred position as their owned class.  We can differentiate them from other constructors
      // by checking if its position is ahead of the owner's position.
      if (methodDef.getPreferredPosition() > owner.getTree().getPreferredPosition()) {
        // Explicit constructor: use the owner's name (the class name) to find the definition
        // anchor's location because constructors are internally named "<init>".
        bindingAnchor =
            emitDefinesBindingAnchorEdge(
                ctx, methodDef.sym.owner.name, methodDef.getPreferredPosition(), methodNode);
      } else {
        // Implicit constructor: generate a zero-length implicit anchor
        emitAnchor(ctx, EdgeKind.DEFINES, methodNode);
      }
      // Likewise, constructors don't have return types in the Java AST, but
      // Kythe models all functions with return types.  As a solution, we use
      // the class type as the return type for all constructors.
      ret = getNode(methodDef.sym.owner);
    } else {
      bindingAnchor =
          emitDefinesBindingAnchorEdge(
              ctx, methodDef.name, methodDef.getPreferredPosition(), methodNode);
      ret = returnType.getVName();
    }

    if (bindingAnchor != null) {
      if (!documented) {
        emitComment(methodDef, methodNode);
      }
      if (absNode != null) {
        emitAnchor(bindingAnchor, EdgeKind.DEFINES_BINDING, absNode.getVName(), getScope(ctx));
        Span span = filePositions.findIdentifier(methodDef.name, methodDef.getPreferredPosition());
        if (span != null) {
          emitMetadata(span, absNode.getVName());
        }
        if (!documented) {
          emitComment(methodDef, absNode.getVName());
        }
      }
      emitAnchor(ctx, EdgeKind.DEFINES, methodNode);
    }

    emitOrdinalEdges(methodNode, EdgeKind.PARAM, params);

    VName recv = null;
    if (!methodDef.getModifiers().getFlags().contains(Modifier.STATIC)) {
      recv = owner.getNode().getVName();
    }
    EntrySet fnTypeNode =
        entrySets.newFunctionTypeAndEmit(
            ret,
            recv == null ? entrySets.newBuiltinAndEmit("void").getVName() : recv,
            toVNames(paramTypes),
            recv == null ? MarkedSources.FN_TAPP : MarkedSources.METHOD_TAPP);
    entrySets.emitEdge(methodNode, EdgeKind.TYPED, fnTypeNode.getVName());

    JavacUtil.visitSuperMethods(
        javaContext,
        methodDef.sym,
        (sym, kind) ->
            entrySets.emitEdge(
                methodNode,
                kind == JavacUtil.OverrideKind.DIRECT
                    ? EdgeKind.OVERRIDES
                    : EdgeKind.OVERRIDES_TRANSITIVE,
                getNode(sym)));

    // Set the resulting node for the method and then recurse through its body.  Setting the node
    // first is necessary to correctly add childof edges in the callgraph.
    JavaNode node = ctx.setNode(new JavaNode(methodNode));
    scan(methodDef.getBody(), ctx);

    for (JavaNode param : params) {
      entrySets.emitEdge(param.getVName(), EdgeKind.CHILDOF, node.getVName());
    }

    return node;
  }

  @Override
  public JavaNode visitLambda(JCLambda lambda, TreeContext owner) {
    TreeContext ctx = owner.down(lambda);
    VName lambdaNode = entrySets.newLambdaAndEmit(filePositions, lambda).getVName();
    emitAnchor(ctx, EdgeKind.DEFINES, lambdaNode);

    for (Type target : getTargets(lambda)) {
      VName targetNode = getNode(target.asElement());
      entrySets.emitEdge(lambdaNode, EdgeKind.EXTENDS, targetNode);
    }

    scan(lambda.body, ctx);
    scanList(lambda.params, ctx);
    return new JavaNode(lambdaNode);
  }

  private static Iterable<Type> getTargets(JCFunctionalExpression node) {
    try {
      @SuppressWarnings("unchecked")
      Iterable<Type> targets =
          (Iterable<Type>) JCFunctionalExpression.class.getField("targets").get(node);
      return targets != null ? targets : ImmutableList.of();
    } catch (ReflectiveOperationException e) {
      // continue below
    }
    try {
      // Work with the field rename in JDK 11: http://hg.openjdk.java.net/jdk/jdk11/rev/f854b76b6a0c
      return com.sun.tools.javac.util.List.of(
          (Type) JCFunctionalExpression.class.getField("target").get(node));
    } catch (ReflectiveOperationException e) {
      throw new LinkageError(e.getMessage(), e);
    }
  }

  @Override
  public JavaNode visitVarDef(JCVariableDecl varDef, TreeContext owner) {
    TreeContext ctx = owner.downAsSnippet(varDef);

    Optional<String> signature = signatureGenerator.getSignature(varDef.sym);
    if (!signature.isPresent()) {
      // TODO(schroederc): details
      return emitDiagnostic(ctx, "missing variable signature", null, null);
    }

    List<MarkedSource> markedSourceChildren = new ArrayList<>();
    if (varDef.getInitializer() != null) {
      String initializer = varDef.getInitializer().toString();
      if (initializer.length() <= MAX_INITIALIZER_LENGTH) {
        markedSourceChildren.add(
            MarkedSource.newBuilder()
                .setKind(MarkedSource.Kind.INITIALIZER)
                .setPreText(initializer)
                .build());
      }
    }
    scan(varDef.getInitializer(), ctx);

    VName varNode =
        entrySets.getNode(
            signatureGenerator, varDef.sym, signature.get(), null, markedSourceChildren);
    boolean documented = visitDocComment(varNode, null, varDef.getModifiers());
    emitDefinesBindingAnchorEdge(ctx, varDef.name, varDef.getStartPosition(), varNode);
    emitAnchor(ctx, EdgeKind.DEFINES, varNode);
    if (varDef.sym.getKind().isField() && !documented) {
      // emit comments for fields and enumeration constants
      emitComment(varDef, varNode);
    }

    // Emit corresponding JVM node
    if (jvmGraph != null && varDef.sym.getKind().isField()) {
      VName jvmNode =
          jvmGraph.emitFieldNode(
              referenceType(externalType(owner.getTree().type.tsym)), varDef.name.toString());
      entrySets.emitEdge(varNode, EdgeKind.GENERATES, jvmNode);
    }

    getScope(ctx).ifPresent(scope -> entrySets.emitEdge(varNode, EdgeKind.CHILDOF, scope));
    visitAnnotations(varNode, varDef.getModifiers().getAnnotations(), ctx);

    if (varDef.getModifiers().getFlags().contains(Modifier.STATIC)) {
      entrySets.getEmitter().emitFact(varNode, "/kythe/tag/static", "");
    }

    JavaNode typeNode = scan(varDef.getType(), ctx);
    if (typeNode != null) {
      entrySets.emitEdge(varNode, EdgeKind.TYPED, typeNode.getVName());
      return new JavaNode(varNode, typeNode.childWildcards).setType(typeNode);
    }

    return new JavaNode(varNode);
  }

  @Override
  public JavaNode visitTypeApply(JCTypeApply tApply, TreeContext owner) {
    TreeContext ctx = owner.down(tApply);

    JavaNode typeCtorNode = scan(tApply.getType(), ctx);
    if (typeCtorNode == null) {
      logger.atWarning().log("Missing type constructor: %s", tApply.getType());
      return emitDiagnostic(ctx, "missing type constructor", null, null);
    }

    List<JavaNode> arguments = scanList(tApply.getTypeArguments(), ctx);
    List<VName> argVNames = new ArrayList<>();
    Builder<VName> childWildcards = ImmutableList.builder();
    for (JavaNode n : arguments) {
      argVNames.add(n.getVName());
      childWildcards.addAll(n.childWildcards);
    }

    EntrySet typeNode =
        entrySets.newTApplyAndEmit(typeCtorNode.getVName(), argVNames, MarkedSources.GENERIC_TAPP);
    // TODO(salguarnieri) Think about removing this since it isn't something that we have a use for.
    emitAnchor(ctx, EdgeKind.REF, typeNode.getVName());

    return new JavaNode(typeNode, childWildcards.build());
  }

  @Override
  public JavaNode visitSelect(JCFieldAccess field, TreeContext owner) {
    TreeContext ctx = owner.down(field);

    JCImport imprt = null;
    if (owner.getTree() instanceof JCImport) {
      imprt = (JCImport) owner.getTree();
    }

    Symbol sym = field.sym;
    if (sym == null && imprt != null && imprt.isStatic()) {
      // Static imports don't have their symbol populated so we search for the symbol.

      ClassSymbol cls = JavacUtil.getClassSymbol(javaContext, field.selected + "." + field.name);
      if (cls != null) {
        // Import was a inner class import
        sym = cls;
      } else {
        cls = JavacUtil.getClassSymbol(javaContext, field.selected.toString());
        if (cls != null) {
          // Import is a class member; emit usages for all matching (by name) class members.
          ctx = ctx.down(field);

          JavacTrees trees = JavacTrees.instance(javaContext);
          Type.ClassType classType = (Type.ClassType) cls.asType();
          Scope scope = trees.getScope(treePath);

          JavaNode lastMember = null;
          for (Symbol member : cls.members().getSymbolsByName(field.name)) {
            try {
              if (!member.isStatic() || !trees.isAccessible(scope, member, classType)) {
                continue;
              }

              // Ensure member symbol's type is complete.  If the extractor finds that a static
              // member isn't used (due to overloads), the symbol's dependent type classes won't
              // be saved in the CompilationUnit and this will throw an exception.
              if (member.type != null) {
                member.type.tsym.complete();
                member.type.getParameterTypes().forEach(t -> t.tsym.complete());
                Type returnType = member.type.getReturnType();
                if (returnType != null) {
                  returnType.tsym.complete();
                }
              }

              lastMember = emitNameUsage(ctx, member, field.name, EdgeKind.REF_IMPORTS);
            } catch (Symbol.CompletionFailure e) {
              // Symbol resolution failed (see above comment).  Ignore and continue with other
              // class members matching static import.
            }
          }
          scan(field.getExpression(), ctx);
          return lastMember;
        }
      }
    }

    if (sym == null) {
      scan(field.getExpression(), ctx);
      if (!field.name.toString().equals("*")) {
        String msg = "Could not determine selected Symbol for " + field;
        if (config.getVerboseLogging()) {
          logger.atWarning().log(msg);
        }
        return emitDiagnostic(ctx, msg, null, null);
      }
      return null;
    } else if (sym.getKind() == ElementKind.PACKAGE) {
      EntrySet pkgNode = entrySets.newPackageNodeAndEmit((PackageSymbol) sym);
      emitAnchor(ctx, EdgeKind.REF, pkgNode.getVName());
      return new JavaNode(pkgNode);
    } else {
      scan(field.getExpression(), ctx);
      return emitNameUsage(
          ctx, sym, field.name, imprt != null ? EdgeKind.REF_IMPORTS : EdgeKind.REF);
    }
  }

  @Override
  public JavaNode visitReference(JCMemberReference reference, TreeContext owner) {
    TreeContext ctx = owner.down(reference);
    scan(reference.getQualifierExpression(), ctx);
    return emitNameUsage(
        ctx,
        reference.sym,
        reference.getMode() == ReferenceMode.NEW ? Keyword.of("new") : reference.name);
  }

  @Override
  public JavaNode visitApply(JCMethodInvocation invoke, TreeContext owner) {
    TreeContext ctx = owner.down(invoke);
    scan(invoke.getArguments(), ctx);
    scan(invoke.getTypeArguments(), ctx);

    JavaNode method = scan(invoke.getMethodSelect(), ctx);
    if (method == null) {
      // TODO details
      return emitDiagnostic(ctx, "error analyzing method", null, null);
    }

    emitAnchor(ctx, EdgeKind.REF_CALL, method.getVName());
    return method;
  }

  @Override
  public JavaNode visitNewClass(JCNewClass newClass, TreeContext owner) {
    TreeContext ctx = owner.down(newClass);

    VName ctorNode = getNode(newClass.constructor);
    if (ctorNode == null) {
      return emitDiagnostic(ctx, "error analyzing class", null, null);
    }

    // Span over "new Class"
    Span refSpan =
        new Span(filePositions.getStart(newClass), filePositions.getEnd(newClass.getIdentifier()));
    // Span over "new Class(...)"
    Span callSpan = new Span(refSpan.getStart(), filePositions.getEnd(newClass));

    if (owner.getTree().getTag() == JCTree.Tag.VARDEF) {
      JCVariableDecl varDef = (JCVariableDecl) owner.getTree();
      if (varDef.sym.getKind() == ElementKind.ENUM_CONSTANT) {
        // Handle enum constructors specially.
        // Span over "EnumValueName"
        refSpan = filePositions.findIdentifier(varDef.name, varDef.getStartPosition());
        // Span over "EnumValueName(...)"
        callSpan = new Span(refSpan.getStart(), filePositions.getEnd(varDef));
      }
    }

    EntrySet anchor = entrySets.newAnchorAndEmit(filePositions, refSpan, ctx.getSnippet());
    emitAnchor(anchor, EdgeKind.REF, ctorNode, getScope(ctx));

    EntrySet callAnchor = entrySets.newAnchorAndEmit(filePositions, callSpan, ctx.getSnippet());
    emitAnchor(callAnchor, EdgeKind.REF_CALL, ctorNode, getScope(ctx));

    scanList(newClass.getTypeArguments(), ctx);
    scanList(newClass.getArguments(), ctx);
    scan(newClass.getEnclosingExpression(), ctx);
    scan(newClass.getClassBody(), ctx);
    return scan(newClass.getIdentifier(), ctx);
  }

  @Override
  public JavaNode visitTypeIdent(JCPrimitiveTypeTree primitiveType, TreeContext owner) {
    TreeContext ctx = owner.down(primitiveType);
    if (config.getVerboseLogging() && primitiveType.typetag == TypeTag.ERROR) {
      logger.atWarning().log("found primitive ERROR type: %s", ctx);
    }
    String name = Ascii.toLowerCase(primitiveType.typetag.toString());
    EntrySet node = entrySets.newBuiltinAndEmit(name);
    emitAnchor(ctx, EdgeKind.REF, node.getVName());
    return new JavaNode(node);
  }

  @Override
  public JavaNode visitTypeArray(JCArrayTypeTree arrayType, TreeContext owner) {
    TreeContext ctx = owner.down(arrayType);

    JavaNode typeNode = scan(arrayType.getType(), ctx);
    EntrySet node =
        entrySets.newTApplyAndEmit(
            entrySets.newBuiltinAndEmit("array").getVName(),
            Arrays.asList(typeNode.getVName()),
            MarkedSources.ARRAY_TAPP);
    emitAnchor(ctx, EdgeKind.REF, node.getVName());
    return new JavaNode(node);
  }

  @Override
  public JavaNode visitAnnotation(JCAnnotation annotation, TreeContext owner) {
    TreeContext ctx = owner.down(annotation);
    scanList(annotation.getArguments(), ctx);
    return scan(annotation.getAnnotationType(), ctx);
  }

  @Override
  public JavaNode visitWildcard(JCWildcard wild, TreeContext owner) {
    TreeContext ctx = owner.down(wild);

    EntrySet node = entrySets.newWildcardNodeAndEmit(wild, owner.getSourcePath());
    Builder<VName> wildcards = ImmutableList.builder();
    wildcards.add(node.getVName());

    if (wild.getKind() != Kind.UNBOUNDED_WILDCARD) {
      JavaNode bound = scan(wild.getBound(), ctx);
      emitEdge(
          node,
          wild.getKind() == Kind.EXTENDS_WILDCARD ? EdgeKind.BOUNDED_UPPER : EdgeKind.BOUNDED_LOWER,
          bound);
      wildcards.addAll(bound.childWildcards);
    }
    return new JavaNode(node, wildcards.build());
  }

  @Override
  public JavaNode visitExec(JCExpressionStatement stmt, TreeContext owner) {
    return scan(stmt.expr, owner.downAsSnippet(stmt));
  }

  @Override
  public JavaNode visitReturn(JCReturn ret, TreeContext owner) {
    return scan(ret.expr, owner.downAsSnippet(ret));
  }

  @Override
  public JavaNode visitThrow(JCThrow thr, TreeContext owner) {
    return scan(thr.expr, owner.downAsSnippet(thr));
  }

  @Override
  public JavaNode visitAssert(JCAssert azzert, TreeContext owner) {
    return scanAll(owner.downAsSnippet(azzert), azzert.cond, azzert.detail);
  }

  @Override
  public JavaNode visitAssign(JCAssign assgn, TreeContext owner) {
    return scanAll(owner.downAsSnippet(assgn), assgn.lhs, assgn.rhs);
  }

  @Override
  public JavaNode visitAssignOp(JCAssignOp assgnOp, TreeContext owner) {
    return scanAll(owner.downAsSnippet(assgnOp), assgnOp.lhs, assgnOp.rhs);
  }

  private boolean visitDocComment(VName node, EntrySet absNode, JCModifiers modifiers) {
    // TODO(#1501): always use absNode
    Optional<String> deprecation = Optional.empty();
    boolean documented = false;
    if (docScanner != null) {
      DocCommentVisitResult result = docScanner.visitDocComment(treePath, node, absNode);
      documented = result.documented();
      deprecation = result.deprecation();
    }
    if (!deprecation.isPresent() && modifiers != null) {
      // emit tags/deprecated if a @Deprecated annotation is present even if there isn't @deprecated
      // javadoc
      if (modifiers.getAnnotations().stream()
          .map(a -> a.annotationType.type.tsym.getQualifiedName())
          .anyMatch(n -> n.contentEquals("java.lang.Deprecated"))) {
        deprecation = Optional.of("");
      }
    }
    emitDeprecated(deprecation, node);
    if (absNode != null) {
      emitDeprecated(deprecation, absNode.getVName());
    }
    return documented;
  }

  // // Utility methods ////

  void emitDocReference(Symbol sym, int startChar, int endChar) {
    VName node = getNode(sym);
    if (node == null) {
      if (config.getVerboseLogging()) {
        logger.atWarning().log("failed to emit documentation reference to %s", sym);
      }
      return;
    }

    Span loc =
        new Span(
            filePositions.charToByteOffset(startChar), filePositions.charToByteOffset(endChar));
    EntrySet anchor = entrySets.newAnchorAndEmit(filePositions, loc);
    if (anchor != null) {
      emitAnchor(anchor, EdgeKind.REF_DOC, node, Optional.empty());
    }
  }

  int charToLine(int charPosition) {
    return filePositions.charToLine(charPosition);
  }

  boolean emitCommentsOnLine(int line, VName node, int defLine) {
    List<Comment> lst = comments.get(line);
    if (lst == null || commentClaims.computeIfAbsent(line, l -> defLine) != defLine) {
      return false;
    }
    for (Comment comment : lst) {
      String bracketed =
          MiniAnchor.bracket(
              comment.text.replaceFirst("^(//|/\\*) ?", "").replaceFirst(" ?\\*/$", ""),
              pos -> pos,
              new ArrayList<>());
      emitDoc(DocKind.LINE, bracketed, new ArrayList<>(), node, null);
    }
    return !lst.isEmpty();
  }

  private static List<VName> toVNames(Iterable<JavaNode> nodes) {
    return Streams.stream(nodes).map(JavaNode::getVName).collect(Collectors.toList());
  }

  // TODO When we want to refer to a type or method that is generic, we need to point to the abs
  // node. The code currently does not have an easy way to access that node but this method might
  // offer a way to change that.
  // See #1501 for more discussion and detail.
  /** Create an abs node if we have type variables or if we have wildcards. */
  private EntrySet defineTypeParameters(
      TreeContext ownerContext,
      VName owner,
      List<JCTypeParameter> params,
      List<VName> wildcards,
      MarkedSource markedSource) {
    if (params.isEmpty() && wildcards.isEmpty()) {
      return null;
    }

    List<VName> typeParams = new ArrayList<>();
    for (JCTypeParameter tParam : params) {
      TreeContext ctx = ownerContext.down(tParam);
      Symbol sym = tParam.type.asElement();
      VName node =
          signatureGenerator
              .getSignature(sym)
              .map(sig -> entrySets.getNode(signatureGenerator, sym, sig, null, null))
              .orElse(null);
      if (node == null) {
        logger.atWarning().log("Could not get type parameter VName: %s", tParam);
        continue;
      }
      emitDefinesBindingAnchorEdge(ctx, tParam.name, tParam.getStartPosition(), node);
      visitAnnotations(node, tParam.getAnnotations(), ctx);
      typeParams.add(node);

      List<JCExpression> bounds = tParam.getBounds();
      List<JavaNode> boundNodes =
          bounds.stream().map(expr -> scan(expr, ctx)).collect(Collectors.toList());
      if (boundNodes.isEmpty()) {
        boundNodes.add(getJavaLangObjectNode());
      }
      emitOrdinalEdges(node, EdgeKind.BOUNDED_UPPER, boundNodes);
    }
    // Add all of the wildcards that roll up to this node. For example:
    // public static <T> void foo(Ty<?> a, Obj<?, ?> b, Obj<Ty<?>, Ty<?>> c) should declare an abs
    // node that has 1 named absvar (T) and 5 unnamed absvars.
    typeParams.addAll(wildcards);
    return entrySets.newAbstractAndEmit(owner, typeParams, markedSource);
  }

  /** Returns the node associated with a {@link Symbol} or {@code null}. */
  private VName getNode(Symbol sym) {
    JavaNode node = getJavaNode(sym);
    return node == null ? null : node.getVName();
  }

  /** Returns the {@link JavaNode} associated with a {@link Symbol} or {@code null}. */
  private JavaNode getJavaNode(Symbol sym) {
    if (sym.getKind() == ElementKind.PACKAGE) {
      return new JavaNode(entrySets.newPackageNodeAndEmit((PackageSymbol) sym).getVName());
    }

    if (jvmGraph != null && config.getEmitJvmReferences() && isExternal(sym)) {
      // Symbol is external to the analyzed compilation and may not be defined in Java.  Return the
      // related JVM node to accommodate cross-language references.
      Type type = externalType(sym);
      if (type instanceof Type.MethodType) {
        JvmGraph.Type.MethodType methodJvmType = toMethodJvmType((Type.MethodType) type);
        ReferenceType parentClass = referenceType(externalType(sym.enclClass()));
        String methodName = sym.getQualifiedName().toString();
        return new JavaNode(JvmGraph.getMethodVName(parentClass, methodName, methodJvmType));
      } else if (type instanceof Type.ClassType) {
        return new JavaNode(JvmGraph.getReferenceVName(referenceType(sym.type)));
      } else if (sym instanceof Symbol.VarSymbol
          && ((Symbol.VarSymbol) sym).getKind() == ElementKind.FIELD) {
        ReferenceType parentClass = referenceType(externalType(sym.enclClass()));
        String fieldName = sym.getSimpleName().toString();
        return new JavaNode(JvmGraph.getFieldVName(parentClass, fieldName));
      }
    }

    return signatureGenerator
        .getSignature(sym)
        .map(sig -> new JavaNode(entrySets.getNode(signatureGenerator, sym, sig, null)))
        .orElse(null);
  }

  private boolean isExternal(Symbol sym) {
    // TODO(schroederc): check if Symbol comes from any source file in compilation
    // TODO(schroederc): research other methods to hueristically determine if a Symbol is defined in
    //                   a Java compilation (vs. some other JVM language)
    ClassSymbol cls = sym.enclClass();
    return cls != null
        && cls.sourcefile != filePositions.getSourceFile()
        && !JavaEntrySets.fromJDK(sym);
  }

  private void visitAnnotations(
      VName owner, List<JCAnnotation> annotations, TreeContext ownerContext) {
    for (JCAnnotation annotation : annotations) {
      int defPosition = annotation.getPreferredPosition();
      int defLine = filePositions.charToLine(defPosition);
      // Claim trailing annotation comments, which isn't always right, but
      // avoids some confusing comments for method annotations.
      // TODO(danielmoy): don't do this for inline field annotations.
      commentClaims.put(defLine, defLine);
    }
    for (JavaNode node : scanList(annotations, ownerContext)) {
      entrySets.emitEdge(owner, EdgeKind.ANNOTATED_BY, node.getVName());
    }
  }

  // Emits a node for the given sym, an anchor encompassing the TreeContext, and a REF edge
  private JavaNode emitSymUsage(TreeContext ctx, Symbol sym) {
    JavaNode node = getRefNode(ctx, sym);
    if (node == null) {
      // TODO(schroederc): details
      return emitDiagnostic(ctx, "failed to resolve symbol reference", null, null);
    }

    // TODO(schroederc): emit reference to JVM node if `sym.outermostClass()` is not defined in a
    //                   .java source file
    emitAnchor(ctx, EdgeKind.REF, node.getVName());
    statistics.incrementCounter("symbol-usages-emitted");
    return node;
  }

  // Emits a node for the given sym, an anchor encompassing the name, and a REF edge
  private JavaNode emitNameUsage(TreeContext ctx, Symbol sym, Name name) {
    return emitNameUsage(ctx, sym, name, EdgeKind.REF);
  }

  // Emits a node for the given sym, an anchor encompassing the name, and a given edge kind
  private JavaNode emitNameUsage(TreeContext ctx, Symbol sym, Name name, EdgeKind edgeKind) {
    JavaNode node = getRefNode(ctx, sym);
    if (node == null) {
      // TODO(schroederc): details
      return emitDiagnostic(ctx, "failed to resolve symbol name", null, null);
    }

    // Ensure the context has a valid source span before searching for the Name.  Otherwise, anchors
    // may accidentily be emitted for Names that happen to appear after the tree context (e.g.
    // lambdas with type-inferred parameters that use the parameter type in the lambda body).
    if (filePositions.getSpan(ctx.getTree()).isValidAndNonZero()) {
      emitAnchor(
          name,
          ctx.getTree().getPreferredPosition(),
          edgeKind,
          node.getVName(),
          ctx.getSnippet(),
          getScope(ctx));
      statistics.incrementCounter("name-usages-emitted");
    }
    return node;
  }

  private static Optional<VName> getScope(TreeContext ctx) {
    return Optional.ofNullable(ctx.getClassOrMethodParent())
        .map(TreeContext::getNode)
        .map(JavaNode::getVName);
  }

  // Returns the reference node for the given symbol.
  private JavaNode getRefNode(TreeContext ctx, Symbol sym) {
    // If referencing a generic class, distinguish between generic vs. raw use
    // (e.g., `List` is in generic context in `List<String> x` but not in `List x`).
    boolean inGenericContext = ctx.up().getTree() instanceof JCTypeApply;
    try {
      if (sym != null
          && SignatureGenerator.isArrayHelperClass(sym.enclClass())
          && ctx.getTree() instanceof JCFieldAccess) {
        signatureGenerator.setArrayTypeContext(((JCFieldAccess) ctx.getTree()).selected.type);
      }
      JavaNode node = getJavaNode(sym);
      if (node != null
          && sym instanceof ClassSymbol
          && inGenericContext
          && !sym.getTypeParameters().isEmpty()) {
        // Always reference the abs node of a generic class, unless used as a raw type.
        node = new JavaNode(entrySets.newAbstractAndEmit(node.getVName()));
      }
      return node;
    } finally {
      signatureGenerator.setArrayTypeContext(null);
    }
  }

  // Cached common java.lang.* nodes.
  private JavaNode javaLangObjectNode, javaLangEnumNode;

  // Returns a JavaNode representing java.lang.Object.
  private JavaNode getJavaLangObjectNode() {
    if (javaLangObjectNode == null) {
      javaLangObjectNode = resolveJavaLangSymbol(getSymbols().objectType.asElement());
    }
    return javaLangObjectNode;
  }

  // Returns a JavaNode representing java.lang.Enum<E> where E is a given enum type.
  private JavaNode getJavaLangEnumNode(VName enumVName) {
    if (javaLangEnumNode == null) {
      javaLangEnumNode =
          new JavaNode(
              entrySets
                  .newAbstractAndEmit(resolveJavaLangSymbol(getSymbols().enumSym).getVName())
                  .getVName());
    }
    EntrySet typeNode =
        entrySets.newTApplyAndEmit(
            javaLangEnumNode.getVName(),
            Collections.singletonList(enumVName),
            MarkedSources.GENERIC_TAPP);
    return new JavaNode(typeNode);
  }

  private JavaNode resolveJavaLangSymbol(Symbol sym) {
    Optional<String> signature = signatureGenerator.getSignature(sym);
    if (!signature.isPresent()) {
      // This usually indicates a problem with the compilation's bootclasspath.
      return emitDiagnostic(null, "failed to resolve " + sym, null, null);
    }
    return new JavaNode(entrySets.getNode(signatureGenerator, sym, signature.get(), null, null));
  }

  // Creates/emits an anchor and an associated edge
  private EntrySet emitAnchor(TreeContext anchorContext, EdgeKind kind, VName node) {
    return emitAnchor(
        entrySets.newAnchorAndEmit(
            filePositions, anchorContext.getTreeSpan(), anchorContext.getSnippet()),
        kind,
        node,
        getScope(anchorContext));
  }

  // Creates/emits an anchor (for an identifier) and an associated edge
  private EntrySet emitAnchor(
      Name name, int startOffset, EdgeKind kind, VName node, Span snippet, Optional<VName> scope) {
    EntrySet anchor = entrySets.newAnchorAndEmit(filePositions, name, startOffset, snippet);
    if (anchor == null) {
      // TODO(schroederc): Special-case these anchors (most come from visitSelect)
      return null;
    }
    return emitAnchor(anchor, kind, node, scope);
  }

  private void emitMetadata(Span span, VName node) {
    for (Metadata data : metadata) {
      for (Metadata.Rule rule : data.getRulesForLocation(span.getStart())) {
        if (rule.end == span.getEnd()) {
          if (rule.reverseEdge) {
            entrySets.emitEdge(rule.vname, rule.edgeOut, node);
          } else {
            entrySets.emitEdge(node, rule.edgeOut, rule.vname);
          }
        }
      }
    }
  }

  private EntrySet emitDefinesBindingAnchorEdge(
      TreeContext ctx, Name name, int startOffset, VName node) {
    EntrySet anchor =
        emitAnchor(
            name, startOffset, EdgeKind.DEFINES_BINDING, node, ctx.getSnippet(), getScope(ctx));
    Span span = filePositions.findIdentifier(name, startOffset);
    if (span != null) {
      emitMetadata(span, node);
    }
    return anchor;
  }

  private void emitDefinesBindingEdge(
      Span span, EntrySet anchor, VName node, Optional<VName> scope) {
    emitMetadata(span, node);
    emitAnchor(anchor, EdgeKind.DEFINES_BINDING, node, scope);
  }

  // Creates/emits an anchor and an associated edge
  private EntrySet emitAnchor(EntrySet anchor, EdgeKind kind, VName node, Optional<VName> scope) {
    Preconditions.checkArgument(
        kind.isAnchorEdge(), "EdgeKind was not intended for ANCHORs: %s", kind);
    if (anchor == null) {
      return null;
    }
    entrySets.emitEdge(anchor.getVName(), kind, node);
    if (kind == EdgeKind.REF_CALL || config.getEmitAnchorScopes()) {
      scope.ifPresent(s -> entrySets.emitEdge(anchor.getVName(), EdgeKind.CHILDOF, s));
    }
    return anchor;
  }

  private void emitComment(JCTree defTree, VName node) {
    int defPosition = defTree.getPreferredPosition();
    int defLine = filePositions.charToLine(defPosition);
    emitCommentsOnLine(defLine, node, defLine);
    emitCommentsOnLine(defLine - 1, node, defLine);
  }

  void emitDoc(
      DocKind kind, String bracketedText, Iterable<Symbol> params, VName node, VName absNode) {
    List<VName> paramNodes = new ArrayList<>();
    for (Symbol s : params) {
      VName paramNode = getNode(s);
      if (paramNode == null) {
        return;
      }
      paramNodes.add(paramNode);
    }
    EntrySet doc =
        entrySets.newDocAndEmit(kind.getDocSubkind(), filePositions, bracketedText, paramNodes);
    // TODO(#1501): always use absNode
    entrySets.emitEdge(doc.getVName(), EdgeKind.DOCUMENTS, node);
    if (absNode != null) {
      entrySets.emitEdge(doc.getVName(), EdgeKind.DOCUMENTS, absNode);
    }
  }

  private void emitDeprecated(Optional<String> deprecation, VName node) {
    deprecation.ifPresent(d -> entrySets.getEmitter().emitFact(node, "/kythe/tag/deprecated", d));
  }

  // Unwraps the target EntrySet and emits an edge to it from the sourceNode
  private void emitEdge(EntrySet sourceNode, EdgeKind kind, JavaNode target) {
    entrySets.emitEdge(sourceNode.getVName(), kind, target.getVName());
  }

  // Unwraps each target JavaNode and emits an ordinal edge to each from the given source node
  private void emitOrdinalEdges(VName node, EdgeKind kind, List<JavaNode> targets) {
    entrySets.emitOrdinalEdges(node, kind, toVNames(targets));
  }

  private JavaNode emitDiagnostic(TreeContext ctx, String message, String details, String context) {
    Diagnostic.Builder d = Diagnostic.newBuilder().setMessage(message);
    if (details != null) {
      d.setDetails(details);
    }
    if (context != null) {
      d.setContextUrl(context);
    }
    if (ctx != null) {
      Span s = ctx.getTreeSpan();
      if (s.isValid()) {
        d.getSpanBuilder().getStartBuilder().setByteOffset(s.getStart());
        d.getSpanBuilder().getEndBuilder().setByteOffset(s.getEnd());
      } else if (s.getStart() >= 0) {
        // If the span isn't valid but we have a valid start, use the start for a zero-width span.
        d.getSpanBuilder().getStartBuilder().setByteOffset(s.getStart());
        d.getSpanBuilder().getEndBuilder().setByteOffset(s.getStart());
      }
    }
    EntrySet node = entrySets.emitDiagnostic(filePositions, d.build());
    // TODO(schroederc): don't allow any edges to a diagnostic node
    return new JavaNode(node);
  }

  private <T extends JCTree> List<JavaNode> scanList(List<T> trees, TreeContext owner) {
    List<JavaNode> nodes = new ArrayList<>();
    for (T t : trees) {
      nodes.add(scan(t, owner));
    }
    return nodes;
  }

  private void loadAnnotationsFile(String path) {
    URI uri = filePositions.getSourceFile().toUri();
    try {
      String fullPath = uri.resolve(path).getPath();
      if (fullPath.startsWith("/")) {
        fullPath = fullPath.substring(1);
      }
      FileObject file = fileManager.getJavaFileFromPath(fullPath, JavaFileObject.Kind.OTHER);
      if (file == null) {
        logger.atWarning().log("Can't find metadata %s for %s at %s", path, uri, fullPath);
        return;
      }
      InputStream stream = file.openInputStream();
      Metadata newMetadata = metadataLoaders.parseFile(fullPath, ByteStreams.toByteArray(stream));
      if (newMetadata == null) {
        logger.atWarning().log("Can't load metadata %s for %s", path, uri);
        return;
      }
      metadata.add(newMetadata);
    } catch (IOException | IllegalArgumentException ex) {
      logger.atWarning().log("Can't read metadata %s for %s", path, uri);
    }
  }

  private void loadAnnotationsFromClassDecl(JCClassDecl decl) {
    for (JCAnnotation annotation : decl.getModifiers().getAnnotations()) {
      Symbol annotationSymbol = null;
      if (annotation.getAnnotationType() instanceof JCFieldAccess) {
        annotationSymbol = ((JCFieldAccess) annotation.getAnnotationType()).sym;
      } else if (annotation.getAnnotationType() instanceof JCIdent) {
        annotationSymbol = ((JCIdent) annotation.getAnnotationType()).sym;
      }
      if (annotationSymbol == null
          || !annotationSymbol.toString().equals("javax.annotation.Generated")) {
        continue;
      }
      for (JCExpression arg : annotation.getArguments()) {
        if (!(arg instanceof JCAssign)) {
          continue;
        }
        JCAssign assignArg = (JCAssign) arg;
        if (!(assignArg.lhs instanceof JCIdent) || !(assignArg.rhs instanceof JCLiteral)) {
          continue;
        }
        JCIdent lhs = (JCIdent) assignArg.lhs;
        JCLiteral rhs = (JCLiteral) assignArg.rhs;
        if (!lhs.name.contentEquals("comments") || !(rhs.getValue() instanceof String)) {
          continue;
        }
        String comments = (String) rhs.getValue();
        if (comments.startsWith(Metadata.ANNOTATION_COMMENT_PREFIX)) {
          loadAnnotationsFile(comments.substring(Metadata.ANNOTATION_COMMENT_PREFIX.length()));
        }
      }
    }
  }

  private Type externalType(Symbol sym) {
    return sym.externalType(Types.instance(javaContext));
  }

  static JvmGraph.Type toJvmType(Type type) {
    switch (type.getTag()) {
      case ARRAY:
        return JvmGraph.Type.arrayType(toJvmType(((Type.ArrayType) type).getComponentType()));
      case CLASS:
        return referenceType(type);
      case METHOD:
        return toMethodJvmType(type.asMethodType());
      case TYPEVAR:
        return referenceType(type);

      case BOOLEAN:
        return JvmGraph.Type.booleanType();
      case BYTE:
        return JvmGraph.Type.byteType();
      case CHAR:
        return JvmGraph.Type.charType();
      case DOUBLE:
        return JvmGraph.Type.doubleType();
      case FLOAT:
        return JvmGraph.Type.floatType();
      case INT:
        return JvmGraph.Type.intType();
      case LONG:
        return JvmGraph.Type.longType();
      case SHORT:
        return JvmGraph.Type.shortType();

      default:
        throw new IllegalStateException("unhandled Java Type: " + type.getTag());
    }
  }

  /** Returns a new JVM class/enum/interface type descriptor to the specified source type. */
  private static ReferenceType referenceType(Type referent) {
    String qualifiedName = referent.tsym.flatName().toString();
    return JvmGraph.Type.referenceType(qualifiedName);
  }

  private static JvmGraph.VoidableType toJvmReturnType(Type type) {
    switch (type.getTag()) {
      case VOID:
        return JvmGraph.Type.voidType();
      default:
        return toJvmType(type);
    }
  }

  static JvmGraph.Type.MethodType toMethodJvmType(Type.MethodType type) {
    return JvmGraph.Type.methodType(
        type.getParameterTypes().stream()
            .map(KytheTreeScanner::toJvmType)
            .collect(Collectors.toList()),
        toJvmReturnType(type.getReturnType()));
  }

  static enum DocKind {
    JAVADOC(Optional.of("javadoc")),
    LINE;

    private final Optional<String> subkind;

    private DocKind(Optional<String> subkind) {
      this.subkind = subkind;
    }

    private DocKind() {
      this(Optional.empty());
    }

    /** Returns the Kythe subkind for this type of document. */
    public Optional<String> getDocSubkind() {
      return subkind;
    }
  }
}
