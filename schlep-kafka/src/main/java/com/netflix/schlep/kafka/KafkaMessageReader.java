package com.netflix.schlep.kafka;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.netflix.schlep.reader.AbstractIncomingMessage;
import com.netflix.schlep.reader.IncomingMessage;
import com.netflix.schlep.reader.MessageReader;
import com.netflix.servo.annotations.DataSourceType;
import com.netflix.servo.annotations.Monitor;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class KafkaMessageReader<V> implements MessageReader {
    private static final Logger LOG = LoggerFactory.getLogger(KafkaMessageReader.class);

    private final String topic;
    private final ConsumerConfig config;
    private final Decoder<V> decoder;
    private final int batchSize;
    private final long timeoutMs;
    private ConsumerConnector consumer;

    private final AtomicLong    poolId     = new AtomicLong();
    private final AtomicLong    counter    = new AtomicLong();
    private final AtomicLong    ackCounter = new AtomicLong();
    private final AtomicBoolean paused     = new AtomicBoolean(false);
    private final ResettableCountDownLatch latch = new ResettableCountDownLatch();
    final ExecutorService committer  = Executors.newSingleThreadExecutor(
            new ThreadFactoryBuilder()
                    .setNameFormat("KafkaMessageReader-ConsumerCommitter")
                    .build());

    public KafkaMessageReader(
            String topic,
            ConsumerConfig config,
            int batchSize,
            long timeoutMs,
            Decoder<V> decoder) {
        Preconditions.checkArgument(config.autoCommitEnable() == false, "autocommit should be disabled");

        this.topic = topic;
        this.config = config;
        this.batchSize = batchSize;
        this.timeoutMs = timeoutMs;
        this.decoder = decoder;
    }

    @Monitor(type = DataSourceType.COUNTER, name = "unackedMessageCommitted")
    private long unackedMessageCommitted = 0;

    public long getUnackedMessageCommitted() { return unackedMessageCommitted; }

    @Override
    public Subscription call(final Observer<IncomingMessage> observer) {
        consumer = kafka.consumer.Consumer.createJavaConsumerConnector(config);

        Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
        topicCountMap.put(topic, 1); // only one thread
        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);
        final ConsumerIterator<byte[], byte[]> it = consumerMap.get(topic).get(0).iterator();

        final ExecutorService executor  = Executors.newSingleThreadExecutor(
                new ThreadFactoryBuilder()
                        .setNameFormat("KafkaMessageReader-" + poolId.incrementAndGet() + "-" + getId() + "-%d")
                        .build());

        latch.reset(batchSize);

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
                        final MessageAndMetadata<byte[], byte[]> m = it.next();

                        observer.onNext(new AbstractIncomingMessage<MessageAndMetadata<byte[], byte[]>>(m) {
                            @Override
                            public void ack() {
                                latch.countDown();
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

                        if (counter.incrementAndGet() % batchSize == 0) {
                            latch.await(timeoutMs, TimeUnit.MILLISECONDS);
                            if (latch.getCount() > 0) {
                                LOG.error("unacked messages skipped: " + latch.getCount());
                                unackedMessageCommitted += latch.getCount();
                            }
                            committer.submit(new Runnable() {
                                @Override
                                public void run() {
                                    consumer.commitOffsets();
                                }
                            });

                            latch.reset(batchSize);
                        }
                    } catch (Exception e) {
                        LOG.error("Interrupted", e);
                        //observer.onError(e);    // Not sure we actually want to do this since it'll stop all consumers.
                        return;
                    }
                }

                if (counter.get() - ackCounter.get() > 0) {
                    try {
                        Thread.sleep(timeoutMs);
                    } catch (InterruptedException e) {
                        LOG.error("Interrupted", e);
                    }
                    if (counter.get() - ackCounter.get() > 0) {
                        LOG.error("unacked messages skipped: " + (counter.get() - ackCounter.get()));
                        unackedMessageCommitted += latch.getCount();
                    }
                    committer.submit(new Runnable() {
                        @Override
                        public void run() {
                            consumer.commitOffsets();
                        }
                    });
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
