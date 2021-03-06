package com.github.tfaga.lynx.test;

import com.github.tfaga.lynx.beans.QueryParameters;
import com.github.tfaga.lynx.exceptions.NoSuchEntityFieldException;
import com.github.tfaga.lynx.test.entities.User;
import com.github.tfaga.lynx.test.rules.JpaRule;
import com.github.tfaga.lynx.utils.JPAUtils;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.List;

import javax.persistence.EntityManager;

/**
 * @author Tilen Faganel
 * @version 1.0.0
 * @since 1.0.0
 */
public class JPAUtilsFieldsTest {

    @ClassRule
    public static JpaRule server = new JpaRule();

    private EntityManager em = server.getEntityManager();

    @Test
    public void testSingleField() {

        QueryParameters q = new QueryParameters();
        q.getFields().add("firstname");

        List<User> users = JPAUtils.queryEntities(em, User.class, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(100, users.size());
        Assert.assertNotNull(users.get(0).getFirstname());
        Assert.assertNull(users.get(0).getLastname());
        Assert.assertEquals("Jason", users.get(0).getFirstname());
    }

    @Test
    public void testMultipleFields() {

        QueryParameters q = new QueryParameters();
        q.getFields().add("firstname");
        q.getFields().add("country");
        q.getFields().add("role");

        List<User> users = JPAUtils.queryEntities(em, User.class, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(100, users.size());
        Assert.assertNotNull(users.get(0).getFirstname());
        Assert.assertNotNull(users.get(0).getCountry());
        Assert.assertNotNull(users.get(0).getRole());
        Assert.assertNull(users.get(0).getLastname());
        Assert.assertEquals("Jason", users.get(0).getFirstname());
        Assert.assertEquals("China", users.get(0).getCountry());
        Assert.assertEquals(0, users.get(0).getRole().intValue());
    }

    @Test
    public void testIdAvailable() {

        QueryParameters q = new QueryParameters();
        q.getFields().add("lastname");

        List<User> users = JPAUtils.queryEntities(em, User.class, q);

        Assert.assertNotNull(users);
        Assert.assertEquals(100, users.size());
        Assert.assertNotNull(users.get(0).getLastname());
        Assert.assertNotNull(users.get(0).getId());
        Assert.assertNull(users.get(0).getFirstname());
        Assert.assertEquals("Ramos", users.get(0).getLastname());
        Assert.assertEquals(1, users.get(0).getId().intValue());
    }

    @Test(expected = NoSuchEntityFieldException.class)
    public void testNonExistingField() {

        QueryParameters q = new QueryParameters();
        q.getFields().add("something");

        JPAUtils.queryEntities(em, User.class, q);
    }
}
