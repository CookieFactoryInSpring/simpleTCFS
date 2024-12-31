package fr.univcotedazur.simpletcfs.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc // start the FULL Web+Back stack (all controllers + all components) in an embedded server -> Integration test
class RecipeWebAutoConfigureIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void recipesFullStackTest() throws Exception {
        mockMvc.perform(get(RecipeController.BASE_URI)
                        .contentType(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$", hasItem("CHOCOLALALA")))
                .andExpect(jsonPath("$", hasItem("DARK_TEMPTATION")))
                .andExpect(jsonPath("$", hasItem("SOO_CHOCOLATE")));
    }

}
