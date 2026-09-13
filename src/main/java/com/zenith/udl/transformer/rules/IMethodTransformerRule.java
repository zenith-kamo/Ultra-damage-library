package com.zenith.udl.transformer.rules;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public interface IMethodTransformerRule {
    /**
     * 対象のクラス・メソッドがこのルールの適用対象かどうかを判定します。
     */
    boolean matches(ClassNode classNode, MethodNode methodNode);

    /**
     * 実際のバイトコード変換処理を行います。
     */
    void apply(ClassNode classNode, MethodNode methodNode);
}