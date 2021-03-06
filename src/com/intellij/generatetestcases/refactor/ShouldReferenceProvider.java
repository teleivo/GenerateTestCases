package com.intellij.generatetestcases.refactor;

import com.intellij.generatetestcases.model.BDDCore;
import com.intellij.generatetestcases.model.TestClass;
import com.intellij.generatetestcases.TestFrameworkNotConfigured;
import com.intellij.generatetestcases.model.TestMethod;
import com.intellij.generatetestcases.model.TestMethodImpl;
import com.intellij.generatetestcases.util.BddUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.*;
import com.intellij.psi.javadoc.PsiDocTag;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public class ShouldReferenceProvider extends PsiReferenceProvider {

    private static final PsiReference[] NO_REFERENCES = new PsiReference[0];

    public static final ShouldReferenceProvider INSTANCE = new ShouldReferenceProvider();

    private ShouldReferenceProvider() {
    }

    /**
     * Static factory that returns singleton effective java item 3
     *
     * @return
     */
    public static ShouldReferenceProvider getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean acceptsTarget(@NotNull PsiElement target) {
        throw new UnsupportedOperationException();
    }

    private final Logger logger = Logger.getInstance(getClass());

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {


        // TODO it doesn't get called as often as the methods in ShouldReference so it would be convenient if some static
        // information could be passed to it or created in its constructor

        // TODO it should only create references for @should PsiDocTags

        PsiDocTag psiDocTag = (PsiDocTag) element;
        if (BddUtil.isValidShouldTag(psiDocTag)) {


            // TODO create TestClass and TestMethod

            //  find the target test method
            PsiElement parentPsiClass = psiDocTag;
            do {
                parentPsiClass = parentPsiClass.getParent();
            } while (!(parentPsiClass instanceof PsiClass));

            TestClass testClass;
            try {
                testClass = BDDCore.createTestClass((PsiClass) parentPsiClass);
            } catch (TestFrameworkNotConfigured testFrameworkNotConfigured) {
                logger.warn("Trying to resolve test methods but no framework is configured");
                return NO_REFERENCES;
            }
            List<TestMethod> allMethods = testClass.getAllMethods();

            TestMethod testMethod = null;
            for (TestMethod item : allMethods) {
                PsiDocTag backingTag = ((TestMethodImpl) item).getBackingTag();
                if (backingTag.equals(psiDocTag)) {
                    testMethod = item;
                }
            }
            ShouldReference shouldReference = new ShouldReference(psiDocTag, testMethod);
            return new PsiReference[]{shouldReference};
        } else {
            return NO_REFERENCES;
        }
    }
}
