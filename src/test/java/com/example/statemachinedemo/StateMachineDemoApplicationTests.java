package com.example.statemachinedemo;

import com.example.statemachinedemo.domain.Payment;
import com.example.statemachinedemo.domain.PaymentEvent;
import com.example.statemachinedemo.domain.PaymentState;
import com.example.statemachinedemo.repository.PaymentRepository;
import com.example.statemachinedemo.services.PaymentService;
import java.math.BigDecimal;
import javax.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;

@SpringBootTest
class StateMachineDemoApplicationTests {

  @Autowired
  StateMachineFactory<PaymentState, PaymentEvent> factory;
  @Autowired
  PaymentService paymentService;

  @Autowired
  PaymentRepository paymentRepository;
  Payment payment;
  @Test
  void contextLoads() {
  }
  @Test
  void testNewStateMachine() {
    StateMachine<PaymentState, PaymentEvent> sm = factory.getStateMachine();

    sm.start();
    System.out.println(sm.getState().toString());
    sm.sendEvent(PaymentEvent.PRE_AUTHORIZE);
    System.out.println(sm.getState().toString());
    sm.sendEvent(PaymentEvent.PRE_AUTH_APPROVED);
    System.out.println(sm.getState().toString());
    sm.sendEvent(PaymentEvent.PRE_AUTH_DECLINED);
    System.out.println(sm.getState().toString());

  }

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
