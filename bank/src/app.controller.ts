import {
  Body,
  Controller,
  Get,
  HttpException,
  HttpStatus,
  Post,
} from '@nestjs/common';

import { AppService } from './app.service';
import { PaymentRequestDto } from './dto/paymentRequest.dto';
import { PaymentReceiptDto} from "./dto/paymentReceipt.dto";

@Controller('cctransactions')
export class AppController {
  constructor(private readonly appService: AppService) {}

  @Get()
  getAllTransactions(): PaymentReceiptDto[] {
    return this.appService.findAll();
  }

  @Post()
  payByCreditCard(@Body() paymentRequestDto: PaymentRequestDto): PaymentReceiptDto {
    try {
      return this.appService.pay(paymentRequestDto);
    } catch (e) {
      throw new HttpException(
        'business error: ' + e.message,
        HttpStatus.BAD_REQUEST,
      );
    }
  }
}
