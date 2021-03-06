/*
 * Copyright 2000-2014 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.codeInsight.daemon.lambda;

import com.intellij.JavaTestUtil;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.FunctionalExpressionSearch;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.function.Predicate;

public class FindFunctionalInterfaceTest extends LightCodeInsightFixtureTestCase {
  public void testMethodArgument() throws Exception {
    doTestOneExpression();
  }

  public void testMethodArgumentByTypeParameter() throws Exception {
    doTestOneExpression();
  }

  public void testFieldDeclaredInFileWithoutFunctionalInterfaces() throws Exception {
    myFixture.addClass("class B {" +
                       "  void f(A a) {" +
                       "    a.r = () -> {};" +
                       "  }" +
                       "}");
    myFixture.addClass("public class A {" +
                       "  public I r;" +
                       "}");

    doTestOneExpression();
  }

  public void testVarargPosition() throws Exception {
    myFixture.addClass("\n" +
                       "class A {  \n" +
                       "  <T> void foo(T... r) {}\n" +
                       "  void bar(J i){foo(i, i, () -> {});}\n" +
                       "}");

    doTestOneExpression();
  }

  private void doTestOneExpression() {
    myFixture.configureByFile(getTestName(false) + ".java");
    final PsiClass psiClass = findClassAtCaret();
    final Collection<PsiFunctionalExpression> expressions = FunctionalExpressionSearch.search(psiClass).findAll();
    int size = expressions.size();
    assertEquals(1, size);
    final PsiFunctionalExpression next = expressions.iterator().next();
    assertNotNull(next);
    assertEquals("() -> {}", next.getText());
  }

  @NotNull
  private PsiClass findClassAtCaret() {
    final PsiElement elementAtCaret = myFixture.getElementAtCaret();
    assertNotNull(elementAtCaret);
    final PsiClass psiClass = PsiTreeUtil.getParentOfType(elementAtCaret, PsiClass.class, false);
    assertTrue(psiClass != null && psiClass.isInterface());
    return psiClass;
  }

  public void testFieldFromAnonymousClassScope() throws Exception {
    myFixture.configureByFile(getTestName(false) + ".java");
    final PsiElement elementAtCaret = myFixture.getElementAtCaret();
    assertNotNull(elementAtCaret);
    final PsiField field = PsiTreeUtil.getParentOfType(elementAtCaret, PsiField.class, false);
    assertNotNull(field);
    final PsiClass aClass = field.getContainingClass();
    assertTrue(aClass instanceof PsiAnonymousClass);
    final Collection<PsiReference> references = ReferencesSearch.search(field).findAll();
    assertFalse(references.isEmpty());
    assertEquals(1, references.size());
  }

  public void testMethodWithClassTypeParameter() {
    myFixture.configureByFile(getTestName(false) + ".java");

    PsiClass runnable = JavaPsiFacade.getInstance(getProject()).findClass("I", GlobalSearchScope.allScope(getProject()));
    assertSize(1, FunctionalExpressionSearch.search(runnable).findAll());
  }

  public void testClassFromJdk() {
    doTestIndexSearch("(e) -> true");
  }

  public void testClassFromJdkMethodRef() {
    doTestIndexSearch("this::bar");
  }

  public void doTestIndexSearch(String expected) {
    myFixture.configureByFile(getTestName(false) + ".java");

    PsiClass predicate = JavaPsiFacade.getInstance(getProject()).findClass(Predicate.class.getName(), GlobalSearchScope.allScope(getProject()));
    assert predicate != null;
    final PsiFunctionalExpression next = assertOneElement(FunctionalExpressionSearch.search(predicate).findAll());
    assertEquals(expected, next.getText());
  }

  public void testConstructorReferences() {
    myFixture.configureByFile(getTestName(false) + ".java");

    myFixture.addClass("class Bar extends Foo {\n" +
                       "  public Bar() { super(() -> 1); }\n" +
                       "\n" +
                       "  {\n" +
                       "    new Foo(() -> 2) { };\n" +
                       "    new Foo(() -> 3);\n" +
                       "  }\n" +
                       "}");

    assertSize(5, FunctionalExpressionSearch.search(findClassAtCaret()).findAll());
  }

  @Override
  protected String getBasePath() {
    return JavaTestUtil.getRelativeJavaTestDataPath() + "/codeInsight/daemonCodeAnalyzer/lambda/findUsages/";
  }

  @NotNull
  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return JAVA_8;
  }
}
