package com.example.statemachinedemo.config;

import com.example.statemachinedemo.domain.PaymentEvent;
import com.example.statemachinedemo.domain.PaymentState;
import com.example.statemachinedemo.services.PaymentServiceImpl;
import java.util.EnumSet;
import java.util.Random;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;

public class StateMachineConfig extends StateMachineConfigurerAdapter<PaymentState, PaymentEvent> {
@Override
  public void configure(StateMachineStateConfigurer<PaymentState,PaymentEvent>states)throws Exception{
  states.withStates()
      .initial(PaymentState.NEW)
      .states(EnumSet.allOf(PaymentState.class))
      .end(PaymentState.AUTH)
      .end(PaymentState.PRE_AUTH_ERROR)
      .end(PaymentState.AUTH_ERROR);
}
@Override
public void configure(StateMachineTransitionConfigurer<PaymentState, PaymentEvent> transitions) throws Exception {
  transitions.withExternal().source(PaymentState.NEW).target(PaymentState.NEW).event(PaymentEvent.PRE_AUTHORIZE)
      .action(preAuthAction()).guard(paymentIdGuard())
      .and()
      .withExternal().source(PaymentState.NEW).target(PaymentState.PRE_AUTH).event(PaymentEvent.PRE_AUTH_APPROVED)
      .and()
      .withExternal().source(PaymentState.NEW).target(PaymentState.PRE_AUTH_ERROR).event(PaymentEvent.PRE_AUTH_DECLINED);
}

  private Action<PaymentState, PaymentEvent> preAuthAction() {
  return  context ->{
    System.out.println("Pre Auth called");

    if (new Random().nextInt(10) < 8) {
      System.out.println("Approved");
      context.getStateMachine().sendEvent(MessageBuilder.withPayload(PaymentEvent.PRE_AUTH_APPROVED)
          .setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER, context.getMessageHeader(
              PaymentServiceImpl.PAYMENT_ID_HEADER))
          .build());

    } else {
      System.out.println("Declined! No Credit!!!!!!");
      context.getStateMachine().sendEvent(MessageBuilder.withPayload(PaymentEvent.PRE_AUTH_DECLINED)
          .setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER, context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
          .build());
    }
  };
  }
  public Guard<PaymentState, PaymentEvent> paymentIdGuard(){
    return context -> {
      return context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER) != null;
    };
  }
}
