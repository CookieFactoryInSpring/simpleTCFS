package fr.univcotedazur.simpletcfs.cli.model;

public record CliOrder(
   Long id,
   Long customerId,
   double price,
   String payReceiptId,
   String status) {
}
