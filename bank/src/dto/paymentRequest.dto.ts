import { IsNotEmpty, IsPositive, IsString } from 'class-validator';

export class PaymentRequestDto {
  @IsNotEmpty()
  @IsString()
  creditCard: string;

  @IsNotEmpty()
  @IsPositive()
  amount: number;
}
