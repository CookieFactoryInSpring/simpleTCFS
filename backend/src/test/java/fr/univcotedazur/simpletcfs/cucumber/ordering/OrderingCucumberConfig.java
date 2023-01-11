package fr.univcotedazur.simpletcfs.cucumber.ordering;

import fr.univcotedazur.simpletcfs.cashier.connectors.interfaces.Bank;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@CucumberContextConfiguration
@SpringBootTest
public class OrderingCucumberConfig {

    @MockitoBean // Spring/Cucumber setup: declare here the mocks to be used (and autowired) in the step definition classes
    private Bank bankMock;
}
