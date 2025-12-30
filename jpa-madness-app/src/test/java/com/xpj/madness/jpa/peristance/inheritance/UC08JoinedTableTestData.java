package com.xpj.madness.jpa.peristance.inheritance;

import com.xpj.madness.jpa.peristance.inheritance.entity.*;

import java.util.ArrayList;
import java.util.List;

public class UC08JoinedTableTestData {

    public static UC08JoinedAdamWithLazyChildren createUC08JoinedAdamWithLazyChildren(String testId, String surName) {
        UC08JoinedAdamWithLazyChildren entity = UC08JoinedAdamWithLazyChildren.builder()
                .testId(testId)
                .adamSurname(surName)
                .build();

        // commonLazyChildren
        List<UC08JoinedCommonLazyChild> commonLazyChildren = new ArrayList<>();

        commonLazyChildren.add(createUC08JoinedCommonLazyChild("com-adam-1", 1));
        commonLazyChildren.add(createUC08JoinedCommonLazyChild("com-adam-2", 2));

        commonLazyChildren.forEach(child -> child.setParent(entity));

        entity.setCommonLazyChildren(commonLazyChildren);

        // adamChildren
        List<UC08JoinedAdamChild> adamChildren = new ArrayList<>();

        adamChildren.add(createUC08JoinedAdamChild(surName + "-child-1", 1));
        adamChildren.add(createUC08JoinedAdamChild(surName + "-child-2", 2));

        adamChildren.forEach(child -> child.setParent(entity));

        entity.setAdamsChildren(adamChildren);

        return entity;
    }

    public static UC08JoinedBethWithEagerChildren createUC08JoinedBethWithEagerChildren(String testId, String surName) {
        UC08JoinedBethWithEagerChildren entity = UC08JoinedBethWithEagerChildren.builder()
                .testId(testId)
                .bethSurname(surName)
                .build();

        // commonLazyChildren
        List<UC08JoinedCommonLazyChild> commonLazyChildren = new ArrayList<>();

        commonLazyChildren.add(createUC08JoinedCommonLazyChild("com-beth-1", 1));
        commonLazyChildren.add(createUC08JoinedCommonLazyChild("com-beth-2", 2));

        commonLazyChildren.forEach(child -> child.setParent(entity));

        entity.setCommonLazyChildren(commonLazyChildren);

        // bethChildren
        List<UC08JoinedBethChild> bethChildren = new ArrayList<>();

        bethChildren.add(createUC08JoinedBethChild(surName + "-child-1", 1));
        bethChildren.add(createUC08JoinedBethChild(surName + "-child-2", 2));

        bethChildren.forEach(child -> child.setParent(entity));

        entity.setBethChildren(bethChildren);

        return entity;
    }

    public static UC08JoinedCommonLazyChild createUC08JoinedCommonLazyChild(String name, int numbOfSubChildren) {
        List<UC08JoinedCommonLazyChildEagerSubChild> subChildren = new ArrayList<>();

        for (int i = 0; i < numbOfSubChildren; i++) {
            subChildren.add(createUC08JoinedCommonLazyChildEagerSubChild(name + "-sub-" + i));
        }
        UC08JoinedCommonLazyChild entity = UC08JoinedCommonLazyChild.builder()
                .name(name)
                .eagerSubChildren(subChildren)
                .build();

        subChildren.forEach(subChild -> subChild.setParent(entity));

        return entity;
    }

    public static UC08JoinedCommonLazyChildEagerSubChild createUC08JoinedCommonLazyChildEagerSubChild(String name) {
        return UC08JoinedCommonLazyChildEagerSubChild.builder()
                .name(name)
                .build();
    }

    public static UC08JoinedAdamChild createUC08JoinedAdamChild(String name, int numbOfSubChildren) {
        List<UC08JoinedAdamSubChild> subChildren = new ArrayList<>();

        for (int i = 0; i < numbOfSubChildren; i++) {
            subChildren.add(createUC08JoinedAdamSubChild(name + "-sub-" + i));
        }
        UC08JoinedAdamChild entity = UC08JoinedAdamChild.builder()
                .name(name)
                .adamsSubChildren(subChildren)
                .build();
        subChildren.forEach(subChild -> subChild.setParent(entity));

        return entity;
    }

    public static UC08JoinedAdamSubChild createUC08JoinedAdamSubChild(String name) {
        return UC08JoinedAdamSubChild.builder()
                .name(name)
                .build();
    }

    public static UC08JoinedBethChild createUC08JoinedBethChild(String name, int numbOfSubChildren) {
        List<UC08JoinedBethSubChild> subChildren = new ArrayList<>();

        for (int i = 0; i < numbOfSubChildren; i++) {
            subChildren.add(createUC08JoinedBethSubChild(name + "-sub-" + i));
        }
        UC08JoinedBethChild entity = UC08JoinedBethChild.builder()
                .name(name)
                .bethSubChildren(subChildren)
                .build();
        subChildren.forEach(subChild -> subChild.setParent(entity));

        return entity;
    }

    public static UC08JoinedBethSubChild createUC08JoinedBethSubChild(String name) {
        return UC08JoinedBethSubChild.builder()
                .name(name)
                .build();
    }

}
