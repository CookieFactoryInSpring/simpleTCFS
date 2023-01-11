import { IsNotEmpty, IsPositive, IsString } from 'class-validator';

export class PaymentReceiptDto {
  constructor(payReceiptId: string, amount: number) {
    this.payReceiptId = payReceiptId;
    this.amount = amount;
  }

  @IsNotEmpty()
  @IsString()
  payReceiptId: string;

  @IsNotEmpty()
  @IsPositive()
  amount: number;
}
