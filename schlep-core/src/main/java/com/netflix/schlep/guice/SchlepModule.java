package com.netflix.schlep.guice;

import com.google.inject.AbstractModule;
import com.netflix.schlep.consumer.MessageConsumerManager;
import com.netflix.schlep.producer.MessageProducerManager;

public class SchlepModule extends AbstractModule {
    @Override
    protected void configure() {

        bind(MessageConsumerManager.class);
        bind(MessageProducerManager.class);
//        // Binding of a consumer via the @Consumer annotation.  Note that
//        // the consumer is gotten from the MessageConsumerRegistry, which means
//        // the consumer must have been registered prior to @Consumer being processed
//        final MessageConsumerRegistry consumerManager = new MessageConsumerRegistry();
//        bindListener(Matchers.any(), new TypeListener() {
//            @Override
//            public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
//                for (final Method method : type.getRawType().getDeclaredMethods()) {
//                    if (method.isAnnotationPresent(Consumer.class)) {
//                        encounter.register(new InjectionListener<I>() {
//                            @Override
//                            public void afterInjection(final I injectee) {
//                                Consumer annot = method.getAnnotation(Consumer.class);
//                                
//                                MessageConsumer consumer;
//                                try {
//                                    consumer = consumerManager.get(annot.name());
//                                    
//                                    Subscription sub = Observable.create(consumer)
//                                            .subscribe(new Action1<IncomingMessage>() {
//                                                @Override
//                                                public void call(IncomingMessage message) {
//                                                    try {
//                                                        method.invoke(injectee, message);
//                                                    } catch (Exception e) {
//                                                    }
//                                                    message.ack();
//                                                }
//                                            });
//                                    
//                                    if (annot.autoStart()) {
//                                        consumer.start();
//                                    }
//                                    
//                                    // TODO: Tie sub.unsubscribe to PreDestroy
//                                } catch (Exception e) {
//                                    throw new RuntimeException("Faild to get consumer '" + annot.name() + "'", e);
//                                }
//                            }
//                        });
//                    }
//                }
//            }
//        });
    }
}
