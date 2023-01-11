package fr.univcotedazur.simpletcfs.cli.commands;

import fr.univcotedazur.simpletcfs.cli.model.CookieEnum;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(RecipeCommands.class)
@Import(RestTestConfig.class)
class RecipeCommandsTest {

    @Autowired
    private RecipeCommands client;

    @Autowired
    private MockRestServiceServer mockServer;

    @Test
    void recipesSetTest() {
        // Given
        mockServer.expect(requestTo(RestTestConfig.TEST_BASE_URL + "/recipes"))
                .andRespond(withSuccess("[\"CHOCOLALALA\",\"DARK_TEMPTATION\",\"SOO_CHOCOLATE\"]", MediaType.APPLICATION_JSON));

        // When-Then
        assertEquals(EnumSet.allOf(CookieEnum.class), client.recipes());
        mockServer.verify();
    }

}
