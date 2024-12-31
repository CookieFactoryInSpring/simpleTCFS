package fr.univcotedazur.simpletcfs.dto;

import fr.univcotedazur.simpletcfs.entities.OrderStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderDTO (
    @NotNull Long id,
    @NotNull Long customerId,
    @Positive double price,
    @NotBlank String payReceiptId,
    @NotNull OrderStatus status) {
}
