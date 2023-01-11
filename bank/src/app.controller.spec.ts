import { Test, TestingModule } from '@nestjs/testing';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { PaymentRequestDto } from './dto/paymentRequest.dto';
import { HttpException } from '@nestjs/common';

describe('AppController', () => {
  let appController: AppController;

  const goodPaymentDto: PaymentRequestDto = {
    creditCard: '1230896983',
    amount: 43.7,
  };

  const badPaymentDto: PaymentRequestDto = {
    creditCard: '1234567890',
    amount: 43.7,
  };

  beforeEach(async () => {
    const app: TestingModule = await Test.createTestingModule({
      controllers: [AppController],
      providers: [AppService],
    }).compile();

    appController = app.get<AppController>(AppController);
  });

  describe('root', () => {
    it('should return no transactions at startup', () => {
      expect(appController.getAllTransactions().length).toBe(0);
    });
  });

  describe('payByCredit()', () => {
    it('should return a PaymentReceiptDto (generated UUID and input amount) with transaction success', () => {
      const paymentReceiptDto = appController.payByCreditCard(goodPaymentDto);
      expect(paymentReceiptDto.amount).toBe(goodPaymentDto.amount);
      expect(paymentReceiptDto.payReceiptId.substring(0, 8)).toBe('RECEIPT:');
      expect(paymentReceiptDto.payReceiptId.length).toBe(44);
      expect(appController.getAllTransactions().length).toBe(1);
    });
  });

  describe('payByCredit()', () => {
    it('should throw exception transaction failure', () => {
      expect(() => appController.payByCreditCard(badPaymentDto)).toThrow(
        HttpException,
      );
      expect(appController.getAllTransactions().length).toBe(0);
    });
  });
});
