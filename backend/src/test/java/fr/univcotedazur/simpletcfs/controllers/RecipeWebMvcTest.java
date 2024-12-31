package fr.univcotedazur.simpletcfs.controllers;

import fr.univcotedazur.simpletcfs.entities.Cookies;
import fr.univcotedazur.simpletcfs.interfaces.CatalogExplorator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RecipeController.class)
// start only the specified MVC front controller and no other Spring components nor the server -> Unit test of the controller
@AutoConfigureWebClient // Added to avoir error on RestTemplateBuilder missing
class RecipeWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CatalogExplorator mockedCat; // the real Catalog component is not created, we have to mock it

    @Test
    void recipesRestTest() throws Exception {
        when(mockedCat.listPreMadeRecipes())
                .thenReturn(Set.of(Cookies.CHOCOLALALA, Cookies.DARK_TEMPTATION)); // only 2 of the 3 enum values

        mockMvc.perform(get(RecipeController.BASE_URI)
                        .contentType(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$", hasItem("CHOCOLALALA")))
                .andExpect(jsonPath("$", hasItem("DARK_TEMPTATION")));
    }

}
