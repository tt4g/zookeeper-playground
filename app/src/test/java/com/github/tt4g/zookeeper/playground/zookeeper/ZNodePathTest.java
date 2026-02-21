package com.github.tt4g.zookeeper.playground.zookeeper;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ZNodePathTest {

    @Test
    void testRoot() {
        var root = ZNodePath.root();
        assertThat(root.getPath()).isEqualTo("/");
        assertThat(root.getName()).isEqualTo("");
        assertThat(root).hasToString("ZNodePath(/)");
    }

    @Test
    void testJoin() {
        var root = ZNodePath.root();

        var child = root.join("child");
        assertThat(child.getPath()).isEqualTo("/child");
        assertThat(child.getName()).isEqualTo("child");
        assertThat(child).hasToString("ZNodePath(/child)");

        var grandchild = child.join("grandchild");
        assertThat(grandchild.getPath()).isEqualTo("/child/grandchild");
        assertThat(grandchild.getName()).isEqualTo("grandchild");
        assertThat(grandchild).hasToString("ZNodePath(/child/grandchild)");
    }

}
