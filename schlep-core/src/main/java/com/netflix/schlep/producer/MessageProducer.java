package com.netflix.schlep.producer;

import com.netflix.schlep.Completion;
import com.netflix.schlep.component.Component;

import rx.Observable;
import rx.Observer;

/**
 * Interface to implement for each message protocol.  The concrete protocol will receive messages
 * and will handle it's own batching and threading.  Completion notification is provided to the
 * caller either by returning an Observable or calling a provided observer
 * 
 * @author elandau
 *
 */
public interface MessageProducer extends Component {
    /**
     * Write a message and notify completion on the provided observer
     * 
     * @param message
     * @param observer
     */
    public void send(OutgoingMessage message, Observer<Completion<OutgoingMessage>> observer);

    /**
     * Write a message and return an observable on which a Completion will be emitted
     * once the message is written successfully
     * @param message
     * @return
     */
    public Observable<Completion<OutgoingMessage>> send(OutgoingMessage message);
    
    /**
     * Return a unique id for this message writer
     * @return
     */
    public String getId();
}
