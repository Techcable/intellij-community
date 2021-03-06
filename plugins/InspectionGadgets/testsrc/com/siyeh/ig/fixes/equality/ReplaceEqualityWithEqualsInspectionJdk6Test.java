/*
 * Copyright 2000-2016 JetBrains s.r.o.
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
package com.siyeh.ig.fixes.equality;

import com.intellij.pom.java.LanguageLevel;
import com.intellij.testFramework.IdeaTestUtil;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;

/**
 * @author Pavel.Dolgov
 */
public class ReplaceEqualityWithEqualsInspectionJdk6Test extends ReplaceEqualityWithEqualsInspectionTestBase {

  public void testSimpleObjectOldSafeComparison() { doTest(true, true); }
  public void testNegatedObjectOldSafeComparison() { doTest(false, true); }

  @Override
  protected void tuneFixture(JavaModuleFixtureBuilder builder) throws Exception {
    builder.addJdk(IdeaTestUtil.getMockJdk17Path().getPath())
      .setLanguageLevel(LanguageLevel.JDK_1_6);
  }
}
