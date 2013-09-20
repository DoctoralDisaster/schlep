package com.netflix.schlep.kafka;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.netflix.schlep.reader.AbstractIncomingMessage;
import com.netflix.schlep.reader.IncomingMessage;
import com.netflix.schlep.reader.MessageReader;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;
import kafka.serializer.Decoder;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observer;
import rx.Subscription;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class KafkaMessageReader<V> implements MessageReader {
    private static final Logger LOG = LoggerFactory.getLogger(KafkaMessageReader.class);

    private final String topic;
    private final ConsumerConfig config;
    private final int batchSize;
    private final Decoder<V> decoder;
    private final ConsumerConnector consumer;

    private final AtomicLong poolId     = new AtomicLong();
    private final AtomicLong    counter    = new AtomicLong();
    private final AtomicLong    ackCounter = new AtomicLong();
    private final AtomicBoolean paused = new AtomicBoolean(false);
    private final CountDownLatch latch;

    public static class Builder<V> {
        private String topic;
        private int batchSize;
        private ConsumerConfig config;
        private Decoder<V> decoder;

        public Builder withTopic(String topic) {
            this.topic = topic;
            return this;
        }

        public Builder withBatchSize(int batchSize) {
            this.batchSize = batchSize;
            return this;
        }

        public Builder withConsumerConfig(ConsumerConfig config) {
            this.config = config;
            return this;
        }

        public Builder withDecoder(Decoder<V> decoder) {
            this.decoder = decoder;
            return this;
        }

        public KafkaMessageReader build() {
            return new KafkaMessageReader(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public KafkaMessageReader(Builder builder) {
        this.topic = builder.topic;
        this.config = builder.config;
        this.batchSize = builder.batchSize;
        this.decoder = builder.decoder;

        latch = new CountDownLatch(batchSize);
        Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
        topicCountMap.put(topic, new Integer(1));
        consumer = kafka.consumer.Consumer.createJavaConsumerConnector(config);
    }

    @Override
    public Subscription call(final Observer<IncomingMessage> observer) {
        Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
        topicCountMap.put(topic, 1);
        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);
        final ConsumerIterator<byte[], byte[]> it = consumerMap.get(topic).get(0).iterator();

        final ExecutorService executor  = Executors.newSingleThreadExecutor(
                new ThreadFactoryBuilder()
                        .setNameFormat("KafkaMessageReader-" + poolId.incrementAndGet() + "-" + getId() + "-%d")
                        .build());

        executor.submit(new Runnable() {
            @Override
            public void run() {
                while (it.hasNext()) {
                    if (paused.get()) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        continue;
                    }

                    try {
                        counter.incrementAndGet();

                        observer.onNext(new AbstractIncomingMessage<MessageAndMetadata<byte[], byte[]>>(it.next()) {
                            @Override
                            public void ack() {
                                ackCounter.incrementAndGet();
                            }
                            @Override
                            public void nak() {
                                //
                            }

                            public String toString() {
                                return "Kafka[" + StringUtils.abbreviate(this.getContents(String.class), 32) + "]";
                            }

                            @Override
                            public <T> T getContents(Class<T> clazz) {
                                return (T) decoder.fromBytes(entity.message());
                            }
                        });
                    } catch (Exception e) {
                        LOG.error("Interrupted", e);
//                        observer.onError(e);    // Not sure we actually want to do this since it'll stop all consumers.
                        return;
                    }
                }
            }
        });

        return new Subscription() {
            @Override
            public void unsubscribe() {
                consumer.shutdown();
                observer.onCompleted();
                executor.shutdown();
            }
        };
    }

    @Override
    public String getId() {
        return "kafka-" + topic;
    }

    @Override
    public void start() throws Exception {
    }

    @Override
    public void stop() throws Exception {
    }

    @Override
    public void pause() throws Exception {
        this.paused.set(true);
    }

    @Override
    public void resume() throws Exception {
        this.paused.set(false);
    }
}
