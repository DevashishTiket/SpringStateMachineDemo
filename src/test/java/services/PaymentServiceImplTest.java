package services;

import com.example.statemachinedemo.domain.Payment;
import com.example.statemachinedemo.domain.PaymentEvent;
import com.example.statemachinedemo.domain.PaymentState;
import com.example.statemachinedemo.repository.PaymentRepository;
import com.example.statemachinedemo.services.PaymentService;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import jakarta.transaction.Transactional;

@SpringBootTest
class PaymentServiceImplTest {

  @Autowired
  PaymentService paymentService;

  @Autowired
  PaymentRepository paymentRepository;

  Payment payment;

  @BeforeEach
  void setUp() {
    payment = Payment.builder().amount(new BigDecimal("15.75")).build();
  }

  @Transactional
  @Test
  void preAuth() {
    Payment savedPayment = paymentService.newPayment(payment);
    StateMachine<PaymentState, PaymentEvent> sm = paymentService.preAuth(savedPayment.getId());
    Payment preAuthPayment = paymentRepository.getOne(savedPayment.getId());
    System.out.println(sm.getState().getId());
    System.out.println(preAuthPayment);
  }
}
