package com.xpj.madness.jpa.peristance.inheritance;

import com.xpj.madness.jpa.peristance.inheritance.entity.*;

import java.util.ArrayList;
import java.util.List;

public class UC08SeparateTableTestData {

    public static UC08SeparateAdamWithLazyChildren createUC08SeparateAdamWithLazyChildren(String testId, String surName) {
        UC08SeparateAdamWithLazyChildren entity = UC08SeparateAdamWithLazyChildren.builder()
                .testId(testId)
                .adamSurname(surName)
                .build();

        // commonLazyChildren
        List<UC08SeparateCommonLazyChild> commonLazyChildren = new ArrayList<>();

        commonLazyChildren.add(createUC08SeparateCommonLazyChild("com-adam-1", 1));
        commonLazyChildren.add(createUC08SeparateCommonLazyChild("com-adam-2", 2));

        commonLazyChildren.forEach(child -> child.setParent(entity));

        entity.setCommonLazyChildren(commonLazyChildren);

        // adamChildren
        List<UC08SeparateAdamChild> adamChildren = new ArrayList<>();

        adamChildren.add(createUC08SeparateAdamChild(surName + "-child-1", 1));
        adamChildren.add(createUC08SeparateAdamChild(surName + "-child-2", 2));

        adamChildren.forEach(child -> child.setParent(entity));

        entity.setAdamsChildren(adamChildren);

        return entity;
    }

    public static UC08SeparateBethWithEagerChildren createUC08SeparateBethWithEagerChildren(String testId, String surName) {
        UC08SeparateBethWithEagerChildren entity = UC08SeparateBethWithEagerChildren.builder()
                .testId(testId)
                .bethSurname(surName)
                .build();

        // commonLazyChildren
        List<UC08SeparateCommonLazyChild> commonLazyChildren = new ArrayList<>();

        commonLazyChildren.add(createUC08SeparateCommonLazyChild("com-beth-1", 1));
        commonLazyChildren.add(createUC08SeparateCommonLazyChild("com-beth-2", 2));

        commonLazyChildren.forEach(child -> child.setParent(entity));

        entity.setCommonLazyChildren(commonLazyChildren);

        // bethChildren
        List<UC08SeparateBethChild> bethChildren = new ArrayList<>();

        bethChildren.add(createUC08SeparateBethChild(surName + "-child-1", 1));
        bethChildren.add(createUC08SeparateBethChild(surName + "-child-2", 2));

        bethChildren.forEach(child -> child.setParent(entity));

        entity.setBethChildren(bethChildren);

        return entity;
    }

    public static UC08SeparateCommonLazyChild createUC08SeparateCommonLazyChild(String name, int numbOfSubChildren) {
        List<UC08SeparateCommonLazyChildEagerSubChild> subChildren = new ArrayList<>();

        for (int i = 0; i < numbOfSubChildren; i++) {
            subChildren.add(createUC08SeparateCommonLazyChildEagerSubChild(name + "-sub-" + i));
        }
        UC08SeparateCommonLazyChild entity = UC08SeparateCommonLazyChild.builder()
                .name(name)
                .eagerSubChildren(subChildren)
                .build();

        subChildren.forEach(subChild -> subChild.setParent(entity));

        return entity;
    }

    public static UC08SeparateCommonLazyChildEagerSubChild createUC08SeparateCommonLazyChildEagerSubChild(String name) {
        return UC08SeparateCommonLazyChildEagerSubChild.builder()
                .name(name)
                .build();
    }

    public static UC08SeparateAdamChild createUC08SeparateAdamChild(String name, int numbOfSubChildren) {
        List<UC08SeparateAdamSubChild> subChildren = new ArrayList<>();

        for (int i = 0; i < numbOfSubChildren; i++) {
            subChildren.add(createUC08SeparateAdamSubChild(name + "-sub-" + i));
        }
        UC08SeparateAdamChild entity = UC08SeparateAdamChild.builder()
                .name(name)
                .adamsSubChildren(subChildren)
                .build();
        subChildren.forEach(subChild -> subChild.setParent(entity));

        return entity;
    }

    public static UC08SeparateAdamSubChild createUC08SeparateAdamSubChild(String name) {
        return UC08SeparateAdamSubChild.builder()
                .name(name)
                .build();
    }

    public static UC08SeparateBethChild createUC08SeparateBethChild(String name, int numbOfSubChildren) {
        List<UC08SeparateBethSubChild> subChildren = new ArrayList<>();

        for (int i = 0; i < numbOfSubChildren; i++) {
            subChildren.add(createUC08SeparateBethSubChild(name + "-sub-" + i));
        }
        UC08SeparateBethChild entity = UC08SeparateBethChild.builder()
                .name(name)
                .bethSubChildren(subChildren)
                .build();
        subChildren.forEach(subChild -> subChild.setParent(entity));

        return entity;
    }

    public static UC08SeparateBethSubChild createUC08SeparateBethSubChild(String name) {
        return UC08SeparateBethSubChild.builder()
                .name(name)
                .build();
    }

}
