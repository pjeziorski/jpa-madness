package com.xpj.madness.jpa.peristance.inheritance;

import com.xpj.madness.jpa.peristance.inheritance.entity.*;

import java.util.ArrayList;
import java.util.List;

public class UC08JoinedColumnTableTestData {

    public static UC08JoinedColumnAdamWithLazyChildren createUC08JoinedColumnAdamWithLazyChildren(String testId, String surName) {
        UC08JoinedColumnAdamWithLazyChildren entity = UC08JoinedColumnAdamWithLazyChildren.builder()
                .testId(testId)
                .adamSurname(surName)
                .build();

        // commonLazyChildren
        List<UC08JoinedColumnCommonLazyChild> commonLazyChildren = new ArrayList<>();

        commonLazyChildren.add(createUC08JoinedColumnCommonLazyChild("com-adam-1", 1));
        commonLazyChildren.add(createUC08JoinedColumnCommonLazyChild("com-adam-2", 2));

        commonLazyChildren.forEach(child -> child.setParent(entity));

        entity.setCommonLazyChildren(commonLazyChildren);

        // adamChildren
        List<UC08JoinedColumnAdamChild> adamChildren = new ArrayList<>();

        adamChildren.add(createUC08JoinedColumnAdamChild(surName + "-child-1", 1));
        adamChildren.add(createUC08JoinedColumnAdamChild(surName + "-child-2", 2));

        adamChildren.forEach(child -> child.setParent(entity));

        entity.setAdamsChildren(adamChildren);

        return entity;
    }

    public static UC08JoinedColumnBethWithEagerChildren createUC08JoinedColumnBethWithEagerChildren(String testId, String surName) {
        UC08JoinedColumnBethWithEagerChildren entity = UC08JoinedColumnBethWithEagerChildren.builder()
                .testId(testId)
                .bethSurname(surName)
                .build();

        // commonLazyChildren
        List<UC08JoinedColumnCommonLazyChild> commonLazyChildren = new ArrayList<>();

        commonLazyChildren.add(createUC08JoinedColumnCommonLazyChild("com-beth-1", 1));
        commonLazyChildren.add(createUC08JoinedColumnCommonLazyChild("com-beth-2", 2));

        commonLazyChildren.forEach(child -> child.setParent(entity));

        entity.setCommonLazyChildren(commonLazyChildren);

        // bethChildren
        List<UC08JoinedColumnBethChild> bethChildren = new ArrayList<>();

        bethChildren.add(createUC08JoinedColumnBethChild(surName + "-child-1", 1));
        bethChildren.add(createUC08JoinedColumnBethChild(surName + "-child-2", 2));

        bethChildren.forEach(child -> child.setParent(entity));

        entity.setBethChildren(bethChildren);

        return entity;
    }

    public static UC08JoinedColumnCommonLazyChild createUC08JoinedColumnCommonLazyChild(String name, int numbOfSubChildren) {
        List<UC08JoinedColumnCommonLazyChildEagerSubChild> subChildren = new ArrayList<>();

        for (int i = 0; i < numbOfSubChildren; i++) {
            subChildren.add(createUC08JoinedColumnCommonLazyChildEagerSubChild(name + "-sub-" + i));
        }
        UC08JoinedColumnCommonLazyChild entity = UC08JoinedColumnCommonLazyChild.builder()
                .name(name)
                .eagerSubChildren(subChildren)
                .build();

        subChildren.forEach(subChild -> subChild.setParent(entity));

        return entity;
    }

    public static UC08JoinedColumnCommonLazyChildEagerSubChild createUC08JoinedColumnCommonLazyChildEagerSubChild(String name) {
        return UC08JoinedColumnCommonLazyChildEagerSubChild.builder()
                .name(name)
                .build();
    }

    public static UC08JoinedColumnAdamChild createUC08JoinedColumnAdamChild(String name, int numbOfSubChildren) {
        List<UC08JoinedColumnAdamSubChild> subChildren = new ArrayList<>();

        for (int i = 0; i < numbOfSubChildren; i++) {
            subChildren.add(createUC08JoinedColumnAdamSubChild(name + "-sub-" + i));
        }
        UC08JoinedColumnAdamChild entity = UC08JoinedColumnAdamChild.builder()
                .name(name)
                .adamsSubChildren(subChildren)
                .build();
        subChildren.forEach(subChild -> subChild.setParent(entity));

        return entity;
    }

    public static UC08JoinedColumnAdamSubChild createUC08JoinedColumnAdamSubChild(String name) {
        return UC08JoinedColumnAdamSubChild.builder()
                .name(name)
                .build();
    }

    public static UC08JoinedColumnBethChild createUC08JoinedColumnBethChild(String name, int numbOfSubChildren) {
        List<UC08JoinedColumnBethSubChild> subChildren = new ArrayList<>();

        for (int i = 0; i < numbOfSubChildren; i++) {
            subChildren.add(createUC08JoinedColumnBethSubChild(name + "-sub-" + i));
        }
        UC08JoinedColumnBethChild entity = UC08JoinedColumnBethChild.builder()
                .name(name)
                .bethSubChildren(subChildren)
                .build();
        subChildren.forEach(subChild -> subChild.setParent(entity));

        return entity;
    }

    public static UC08JoinedColumnBethSubChild createUC08JoinedColumnBethSubChild(String name) {
        return UC08JoinedColumnBethSubChild.builder()
                .name(name)
                .build();
    }

}
