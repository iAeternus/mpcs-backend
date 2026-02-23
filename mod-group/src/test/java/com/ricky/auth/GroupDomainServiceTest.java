package com.ricky.auth;

import com.ricky.common.permission.Permission;
import org.junit.jupiter.api.Test;

import static com.ricky.auth.AuthScenario.FolderPath.path;
import static com.ricky.auth.AuthTestKit.scenario;
import static com.ricky.auth.FakeGroupBuilder.group;
import static com.ricky.common.permission.Permission.*;

public class GroupDomainServiceTest {

    @Test
    void full_inheritance_should_inherit_from_parent() {
        scenario()
                .givenUser("u1", "g1")
                .givenGroup(
                        group("g1")
                                .member("u1")
                                .fullInheritance()
                                .permission("parent", READ)
                )
                .givenHierarchy("c1", path("parent", "child"))
                .whenResolve("u1", "c1", "child")
                .thenPermissionsAre(READ);
    }

    @Test
    void none_policy_should_not_inherit_from_parent() {
        scenario()
                .givenUser("u1", "g1")
                .givenGroup(
                        group("g1")
                                .member("u1")
                                .noInheritance()
                                .permission("parent", READ)
                )
                .givenHierarchy("c1", path("parent", "child"))
                .whenResolve("u1", "c1", "child")
                .thenEmpty();
    }

    @Test
    void overridable_should_use_nearest_permission() {
        scenario()
                .givenUser("u1", "g1")
                .givenGroup(
                        group("g1")
                                .member("u1")
                                .overridable()
                                .permission("parent", READ)
                                .permission("child", WRITE)
                )
                .givenHierarchy("c1", path("parent", "child"))
                .whenResolve("u1", "c1", "child")
                .thenPermissionsAre(WRITE);
    }

    @Test
    void overridable_should_fallback_to_parent() {
        scenario()
                .givenUser("u1", "g1")
                .givenGroup(
                        group("g1")
                                .member("u1")
                                .overridable()
                                .permission("parent", READ)
                )
                .givenHierarchy("c1", path("parent", "child"))
                .whenResolve("u1", "c1", "child")
                .thenPermissionsAre(READ);
    }

    @Test
    void permissions_should_merge_from_multiple_groups() {
        scenario()
                .givenUser("u1", "g1", "g2")
                .givenGroup(
                        group("g1")
                                .member("u1")
                                .permission("f1", READ)
                )
                .givenGroup(
                        group("g2")
                                .member("u1")
                                .permission("f1", WRITE)
                )
                .givenHierarchy("c1", path("f1"))
                .whenResolve("u1", "c1", "f1")
                .thenPermissionsAre(READ, WRITE);
    }

    @Test
    void inactive_group_should_be_ignored() {
        scenario()
                .givenUser("u1", "g1")
                .givenGroup(
                        group("g1")
                                .inactive()
                                .member("u1")
                                .permission("f1", DELETE)
                )
                .givenHierarchy("c1", path("f1"))
                .whenResolve("u1", "c1", "f1")
                .thenEmpty();
    }

    @Test
    void non_member_should_have_no_permissions() {
        scenario()
                .givenUser("u1") // no groups
                .givenGroup(
                        group("g1")
                                .member("u2")
                                .permission("f1", READ)
                )
                .givenHierarchy("c1", path("f1"))
                .whenResolve("u1", "c1", "f1")
                .thenEmpty();
    }

    @Test
    void admin_should_have_all_permissions_when_no_grants() {
        scenario()
                .givenUser("u1", "g1")
                .givenGroup(
                        group("g1")
                                .manager("u1")
                )
                .givenHierarchy("c1", path("f1"))
                .whenResolve("u1", "c1", "f1")
                .thenPermissionsAre(Permission.all().toArray(new Permission[0]));
    }

    @Test
    void member_should_have_read_permission_when_no_grants() {
        scenario()
                .givenUser("u1", "g1")
                .givenGroup(
                        group("g1")
                                .member("u1")
                )
                .givenHierarchy("c1", path("f1"))
                .whenResolve("u1", "c1", "f1")
                .thenPermissionsAre(READ);
    }

}
