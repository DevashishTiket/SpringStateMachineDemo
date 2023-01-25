package com.example.statemachinedemo.services;

import com.example.statemachinedemo.domain.Payment;
import com.example.statemachinedemo.domain.PaymentEvent;
import com.example.statemachinedemo.domain.PaymentState;
import com.example.statemachinedemo.repository.PaymentRepository;

import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service

public class PaymentServiceImpl implements PaymentService {

  public static final String PAYMENT_ID_HEADER = "payment_id";
  private final PaymentRepository repository;
  private final StateMachineFactory<PaymentState, PaymentEvent> stateMachineFactory;
  private final PaymentStateChangeInterceptor paymentStateChangeInterceptor;


  @Override
  public Payment newPayment(Payment payment) {
    payment.setState(PaymentState.NEW);
    return repository.save(payment);
  }

  @Transactional
  @Override
  public StateMachine<PaymentState, PaymentEvent> preAuth(Long paymentId) {
    StateMachine<PaymentState, PaymentEvent> sm = build(paymentId);
    sendEvent(paymentId, sm, PaymentEvent.PRE_AUTH_APPROVED);

    return sm;
  }

  @Transactional
  @Override
  public StateMachine<PaymentState, PaymentEvent> authorizePayment(Long paymentId) {
    StateMachine<PaymentState, PaymentEvent> sm = build(paymentId);
    sendEvent(paymentId, sm, PaymentEvent.AUTH_APPROVED);

    return sm;
  }

  @Transactional
  @Override
  public StateMachine<PaymentState, PaymentEvent> declineAuth(Long paymentId) {
    StateMachine<PaymentState, PaymentEvent> sm = build(paymentId);
    sendEvent(paymentId, sm, PaymentEvent.AUTH_DECLINED);

    return sm;
  }

  private void sendEvent(Long paymentId, StateMachine<PaymentState, PaymentEvent> sm, PaymentEvent event){
    Message msg = MessageBuilder.withPayload(event)
            .setHeader(PAYMENT_ID_HEADER, paymentId)
            .build();

    sm.sendEvent(msg);
  }

  private StateMachine<PaymentState, PaymentEvent> build(Long paymentId){
    Payment payment = repository.getOne(paymentId);

    StateMachine<PaymentState, PaymentEvent> sm = stateMachineFactory.getStateMachine();

    sm.stop();

    sm.getStateMachineAccessor()
            .doWithAllRegions(sma -> {
              sma.addStateMachineInterceptor(paymentStateChangeInterceptor);
              sma.resetStateMachine(new DefaultStateMachineContext<>(payment.getState(), null, null, null));
            });

    sm.start();

    return sm;
  }
}
