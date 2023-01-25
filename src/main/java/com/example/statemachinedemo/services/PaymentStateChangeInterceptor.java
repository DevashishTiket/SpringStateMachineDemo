package com.example.statemachinedemo.services;

import com.example.statemachinedemo.domain.Payment;
import com.example.statemachinedemo.domain.PaymentEvent;
import com.example.statemachinedemo.domain.PaymentState;
import com.example.statemachinedemo.repository.PaymentRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PaymentStateChangeInterceptor extends
    StateMachineInterceptorAdapter<PaymentState, PaymentEvent> {
private final PaymentRepository paymentRepository;

@Override
  public void preStateChanges(State<PaymentState,PaymentEvent>state, Message<PaymentEvent> message,
    Transition<PaymentState,PaymentEvent>transition,
    StateMachine<PaymentState,PaymentEvent> stateMachine){
  Optional.ofNullable(message).ifPresent(msg -> {
    Optional.ofNullable(Long.class.cast(msg.getHeaders().getOrDefault(PaymentServiceImpl.PAYMENT_ID_HEADER, -1L)))
        .ifPresent(paymentId -> {
          Payment payment = paymentRepository.getOne(paymentId);
          payment.setState(state.getId());
          paymentRepository.save(payment);
        });
  });
}
}
