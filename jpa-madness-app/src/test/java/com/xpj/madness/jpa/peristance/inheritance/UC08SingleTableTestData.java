package com.xpj.madness.jpa.peristance.inheritance;

import com.xpj.madness.jpa.peristance.inheritance.entity.*;

import java.util.ArrayList;
import java.util.List;

public class UC08SingleTableTestData {

    public static UC08SingleAdamWithLazyChildren createUC08SingleAdamWithLazyChildren(String testId, String surName) {
        UC08SingleAdamWithLazyChildren entity = UC08SingleAdamWithLazyChildren.builder()
                .testId(testId)
                .adamSurname(surName)
                .build();

        // commonLazyChildren
        List<UC08SingleCommonLazyChild> commonLazyChildren = new ArrayList<>();

        commonLazyChildren.add(createUC08SingleCommonLazyChild("com-adam-1", 1));
        commonLazyChildren.add(createUC08SingleCommonLazyChild("com-adam-2", 2));

        commonLazyChildren.forEach(child -> child.setParent(entity));

        entity.setCommonLazyChildren(commonLazyChildren);

        // adamChildren
        List<UC08SingleAdamChild> adamChildren = new ArrayList<>();

        adamChildren.add(createUC08SingleAdamChild(surName + "-child-1", 1));
        adamChildren.add(createUC08SingleAdamChild(surName + "-child-2", 2));

        adamChildren.forEach(child -> child.setParent(entity));

        entity.setAdamsChildren(adamChildren);

        return entity;
    }

    public static UC08SingleCommonLazyChild createUC08SingleCommonLazyChild(String name, int numbOfSubChildren) {
        List<UC08SingleCommonLazyChildEagerSubChild> subChildren = new ArrayList<>();

        for (int i = 0; i < numbOfSubChildren; i++) {
            subChildren.add(createUC08SingleCommonLazyChildEagerSubChild(name + "-sub-" + i));
        }
        UC08SingleCommonLazyChild entity = UC08SingleCommonLazyChild.builder()
                .name(name)
                .eagerSubChildren(subChildren)
                .build();

        subChildren.forEach(subChild -> subChild.setParent(entity));

        return entity;
    }

    public static UC08SingleCommonLazyChildEagerSubChild createUC08SingleCommonLazyChildEagerSubChild(String name) {
        return UC08SingleCommonLazyChildEagerSubChild.builder()
                .name(name)
                .build();
    }

    public static UC08SingleAdamChild createUC08SingleAdamChild(String name, int numbOfSubChildren) {
        List<UC08SingleAdamSubChild> subChildren = new ArrayList<>();

        for (int i = 0; i < numbOfSubChildren; i++) {
            subChildren.add(createUC08SingleAdamSubChild(name + "-sub-" + i));
        }
        UC08SingleAdamChild entity = UC08SingleAdamChild.builder()
                .name(name)
                .adamsSubChildren(subChildren)
                .build();
        subChildren.forEach(subChild -> subChild.setParent(entity));

        return entity;
    }

    public static UC08SingleAdamSubChild createUC08SingleAdamSubChild(String name) {
        return UC08SingleAdamSubChild.builder()
                .name(name)
                .build();
    }

}
