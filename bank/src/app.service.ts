import { Injectable } from '@nestjs/common';

import { PaymentRequestDto } from './dto/paymentRequest.dto';
import { PaymentReceiptDto } from './dto/paymentReceipt.dto';
import { PaymentRejectedException } from './exceptions/payment-rejected-exception';
import { randomUUID } from 'crypto';

@Injectable()
export class AppService {
  private static readonly magicKey: string = '896983'; // ASCII code for 'YES'

  private transactions: Array<PaymentReceiptDto>;

  constructor() {
    this.transactions = [];
  }

  findAll(): PaymentReceiptDto[] {
    return this.transactions;
  }

  pay(paymentRequestDto: PaymentRequestDto): PaymentReceiptDto {
    let paymentReceiptDto: PaymentReceiptDto;
    if (paymentRequestDto.creditCard.includes(AppService.magicKey)) {
      paymentReceiptDto = new PaymentReceiptDto(
        'RECEIPT:' + randomUUID(),
        paymentRequestDto.amount,
      );
      this.transactions.push(paymentReceiptDto);
      console.log(
        'Payment accepted(' +
          paymentReceiptDto.payReceiptId +
          '): ' +
          paymentReceiptDto.amount,
      );
      return paymentReceiptDto;
    } else {
      console.log('Payment rejected: ' + paymentRequestDto.amount);
      throw new PaymentRejectedException(paymentRequestDto.amount);
    }
  }
}
