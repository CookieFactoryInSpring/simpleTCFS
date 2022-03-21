package fr.univcotedazur.simpletcfs.controllers;

import fr.univcotedazur.simpletcfs.Tracker;
import fr.univcotedazur.simpletcfs.entities.Item;
import fr.univcotedazur.simpletcfs.entities.OrderStatus;
import fr.univcotedazur.simpletcfs.exceptions.CustomerIdNotFoundException;
import fr.univcotedazur.simpletcfs.exceptions.UnknownOrderId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = TrackingController.BASE_URI, produces = APPLICATION_JSON_VALUE)
public class TrackingController {

    public static final String BASE_URI = "/orders";

    @Autowired
    private Tracker tracker;

    @GetMapping("/{orderId}")
    public ResponseEntity<String> getOrderStatus(@PathVariable("orderId") long orderId)  {
        OrderStatus status = null;
        try {
            status = tracker.retrieveStatus(orderId);
        } catch (UnknownOrderId e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("orderId " + orderId + " unknown");
        }
        return ResponseEntity.ok().body(status.name());
    }

}
