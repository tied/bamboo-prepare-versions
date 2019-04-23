package ut.com.github.bayaro;

import org.junit.Test;
import com.github.bayaro.api.MyPluginComponent;
import com.github.bayaro.impl.MyPluginComponentImpl;

import static org.junit.Assert.assertEquals;

public class MyComponentUnitTest
{
    @Test
    public void testMyName()
    {
        MyPluginComponent component = new MyPluginComponentImpl(null);
        assertEquals("names do not match!", "myComponent",component.getName());
    }
}