import junit.framework.TestCase;
import ru.bahusoff.net.NetHelpers;

import java.util.LinkedHashMap;
import java.util.Map;

public class VkApiClientTest extends TestCase {

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {

    }

    public void testBuildUrlWithParams() throws Exception {
        Map<String, Object> params = new LinkedHashMap<String, Object>();

        params.put("access_token", "abcdef");
        params.put("owner_id", 123);
        params.put("audio_id", 456);

        assertEquals(
            "access_token=abcdef&owner_id=123&audio_id=456",
            NetHelpers.buildURLParams(params)
        );
    }
}