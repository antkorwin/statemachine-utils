package com.antkorwin.statemachineutils.errorinfo;

import com.antkorwin.commonutils.validation.ErrorInfoCollisionDetector;
import org.junit.Test;

/**
 * Created on 18.07.2018.
 *
 * @author Korovin Anatoliy
 */
public class IntersectionAvoidTest {

    @Test
    public void testErrorInfoUnique() {
        ErrorInfoCollisionDetector.assertInPackage("com.antkorwin.statemachineutils");
    }
}
