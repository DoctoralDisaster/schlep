package com.netflix.schlep.kafka

import org.scalatest.junit.JUnit3Suite
import kafka.server.{KafkaConfig, KafkaServer}
import kafka.consumer.ConsumerConfig
import org.I0Itec.zkclient.{IDefaultNameSpace, ZkClient, ZkServer}
import kafka.utils._
import kafka.admin.CreateTopicCommand
import org.junit.Assert._
import java.io.File
import org.mockito.Mockito._
import java.net.ServerSocket
import java.util.{Random, Properties, Map}
import java.util.concurrent.locks.ReentrantLock
import scala.Some
import java.util.concurrent.{CountDownLatch, TimeUnit}
import junit.framework.Assert
import kafka.common.TopicAndPartition
import org.codehaus.jackson.map.ObjectMapper
import org.codehaus.jackson.`type`.TypeReference
import kafka.producer.{KeyedMessage, Producer, ProducerConfig}
import com.netflix.schlep.reader.IncomingMessage
import rx.Observer
import org.apache.commons.io.FileUtils
import java.util.concurrent.atomic.AtomicInteger

class TestKafkaMessageReader extends JUnit3Suite {
  private val brokerId1 = 0
  private val brokerId2 = 1
  private val ports = choosePorts(2)
  private val (port1, port2) = (ports(0), ports(1))
  private var server1: KafkaServer = null
  private var server2: KafkaServer = null
  private var zkServer: ZkServer = null
  private var zkClient: ZkClient = null

  private var servers = List.empty[KafkaServer]

  private val props1 = createBrokerConfig(brokerId1, port1)
  private val config1 = new KafkaConfig(props1) {
    override val hostName = "localhost"
    override val numPartitions = 1
  }
  private val props2 = createBrokerConfig(brokerId2, port2)
  private val config2 = new KafkaConfig(props2) {
    override val hostName = "localhost"
    override val numPartitions = 1
  }

  private val timeout = 10000

  override def setUp() {
    zkServer = startZkServer("testkafkamessagereader", 2181)
    zkClient = new ZkClient("localhost:2181", 20000, 20000, ZKStringSerializer)
    // set up 2 brokers with 4 partitions each
    server1 = createServer(config1)
    server2 = createServer(config2)
    servers = List(server1,server2)
  }

  override def tearDown() {
    server1.shutdown
    server1.awaitShutdown()
    server2.shutdown
    server2.awaitShutdown()
    Utils.rm(server1.config.logDirs)
    Utils.rm(server2.config.logDirs)
    zkServer.shutdown()
  }

  val jsonMapper = new ObjectMapper()
  val typeRef = new TypeReference[Map[Object, Object]](){}

  def test() {
    val topic = "routingKey"
    // create topic with 1 partition and await leadership
    CreateTopicCommand.createTopic(zkClient, topic, 1, 2)
    waitUntilMetadataIsPropagated(servers, topic, 0, 10000)
    waitUntilLeaderIsElectedOrChanged(zkClient, topic, 0, 500)

    val props = new Properties()
    props.put("group.id", "kafkamessagereadertest")
    props.put("zookeeper.connect", "localhost:2181")
    props.put("auto.offset.reset", "smallest")
    props.put("auto.commit.enable", "false")

    val kafkaReader = new KafkaMessageReader[Map[Object, Object]](
      topic,
      new ConsumerConfig(props),
      10,
      100000,
      new JsonMapDecoder())

    val producerProps = new Properties()
    producerProps.put("metadata.broker.list", getBrokerListStrFromConfigs(Seq(config1, config2)))
    producerProps.put("request.required.acks", "1")
    val producer = new Producer[String, Array[Byte]](new ProducerConfig(producerProps))
    for (a <- 0 until 25) {
      producer.send(new KeyedMessage[String, Array[Byte]](topic, createMessage(a)))
    }

    val latch = new CountDownLatch(25);
    kafkaReader.call(new Observer[IncomingMessage](){
      override def onCompleted() {}
      override def onError(e: Throwable) {}
      override def onNext(m : IncomingMessage) {
        val c: Map[Object, Object] = m.getContents(classOf[Map[Object, Object]]);
        assertEquals(c.get("id").toString, (25 - latch.getCount).toString)
        latch.countDown()
        m.ack();
      }
    })
    latch.await(timeout, TimeUnit.MILLISECONDS)
    assertEquals(latch.getCount, 0)
  }

  def testTimeout() {
    val topic = "routingKey"
    // create topic with 1 partition and await leadership
    CreateTopicCommand.createTopic(zkClient, topic, 1, 2)
    waitUntilMetadataIsPropagated(servers, topic, 0, 10000)
    waitUntilLeaderIsElectedOrChanged(zkClient, topic, 0, 500)

    val props = new Properties()
    props.put("group.id", "kafkamessagereadertest")
    props.put("zookeeper.connect", "localhost:2181")
    props.put("auto.offset.reset", "smallest")
    props.put("auto.commit.enable", "false")

    val kafkaReader = new KafkaMessageReader[Map[Object, Object]](
      topic,
      new ConsumerConfig(props),
      10,
      1000,
      new JsonMapDecoder())

    val producerProps = new Properties()
    producerProps.put("metadata.broker.list", getBrokerListStrFromConfigs(Seq(config1, config2)))
    producerProps.put("request.required.acks", "1")
    val producer = new Producer[String, Array[Byte]](new ProducerConfig(producerProps))
    for (a <- 0 until 25) {
      producer.send(new KeyedMessage[String, Array[Byte]](topic, createMessage(a)))
    }

    val count = new AtomicInteger()
    val latch = new CountDownLatch(25);
    kafkaReader.call(new Observer[IncomingMessage](){
      override def onCompleted() {}
      override def onError(e: Throwable) {}
      override def onNext(m : IncomingMessage) {
        val c: Map[Object, Object] = m.getContents(classOf[Map[Object, Object]]);
        assertEquals(c.get("id").toString, (25 - latch.getCount).toString)
        latch.countDown()
        if (latch.getCount() % 10 != 0) {
          m.ack(); // if the id is something, don't ack to test timeout
        } else {
          count.incrementAndGet();
        }
      }
    })
    latch.await(timeout, TimeUnit.MILLISECONDS)
    assertEquals(latch.getCount, 0)
    assertEquals(kafkaReader.getUnackedMessageCommitted, count.get - 1) // -1 needs because last batch is not awaited
  }


  def startZkServer(testName: String, port: Int): ZkServer = {
    val dataPath = "./build/test/" + testName + "/data"
    val logPath = "./build/test/" + testName + "/log"
    FileUtils.deleteDirectory(new File(dataPath))
    FileUtils.deleteDirectory(new File(logPath))
    val zkServer = new ZkServer(dataPath, logPath, mock(classOf[IDefaultNameSpace]), port, ZkServer.DEFAULT_TICK_TIME, 100)
    zkServer.start()
    return zkServer
  }

  def createMessage(id: Int): Array[Byte] = {
    val m = new java.util.HashMap[Object, Object]()
    m.put("id", id.asInstanceOf[AnyRef])
    jsonMapper.writeValueAsBytes(m)
  }

  def choosePorts(count: Int): List[Int] = {
    val sockets =
      for(i <- 0 until count)
      yield new ServerSocket(0)
    val socketList = sockets.toList
    val ports = socketList.map(_.getLocalPort)
    socketList.map(_.close)
    ports
  }

  /**
   * Create a test config for the given node id
   */
  def createBrokerConfigs(numConfigs: Int): List[Properties] = {
    for((port, node) <- choosePorts(numConfigs).zipWithIndex)
    yield createBrokerConfig(node, port)
  }

  def getBrokerListStrFromConfigs(configs: Seq[KafkaConfig]): String = {
    configs.map(c => c.hostName + ":" + c.port).mkString(",")
  }

  /**
   * Create a test config for the given node id
   */
  def createBrokerConfig(nodeId: Int, port: Int): Properties = {
    val props = new Properties
    props.put("broker.id", nodeId.toString)
    props.put("host.name", "localhost")
    props.put("port", port.toString)
    props.put("log.dir", tempDir().getAbsolutePath)
    props.put("log.flush.interval.messages", "1")
    props.put("zookeeper.connect", "localhost:2181")
    props.put("replica.socket.timeout.ms", "1500")
    props
  }

    /**
   * Create a kafka server instance with appropriate test settings
   * USING THIS IS A SIGN YOU ARE NOT WRITING A REAL UNIT TEST
   * @param config The configuration of the server
   */
  def createServer(config: KafkaConfig, time: Time = SystemTime): KafkaServer = {
    val server = new KafkaServer(config, time)
    server.startup()
    server
  }

  def tempDir(): File = {
    val ioDir = System.getProperty("java.io.tmpdir")
    val f = new File(ioDir, "kafka-" + new Random().nextInt(1000000))
    f.mkdirs()
    f.deleteOnExit()
    f
  }

  def waitUntilLeaderIsElectedOrChanged(zkClient: ZkClient, topic: String, partition: Int, timeoutMs: Long, oldLeaderOpt: Option[Int] = None): Option[Int] = {
    val leaderLock = new ReentrantLock()
    val leaderExistsOrChanged = leaderLock.newCondition()

    if(oldLeaderOpt == None)
      println("Waiting for leader to be elected for partition [%s,%d]".format(topic, partition))
    else
      println("Waiting for leader for partition [%s,%d] to be changed from old leader %d".format(topic, partition, oldLeaderOpt.get))

    leaderLock.lock()
    try {
      zkClient.subscribeDataChanges(ZkUtils.getTopicPartitionLeaderAndIsrPath(topic, partition), new LeaderExistsOrChangedListener(topic, partition, leaderLock, leaderExistsOrChanged, oldLeaderOpt, zkClient))
      leaderExistsOrChanged.await(timeoutMs, TimeUnit.MILLISECONDS)
      // check if leader is elected
      val leader = ZkUtils.getLeaderForPartition(zkClient, topic, partition)
      leader match {
        case Some(l) =>
          if(oldLeaderOpt == None)
            println("Leader %d is elected for partition [%s,%d]".format(l, topic, partition))
          else
            println("Leader for partition [%s,%d] is changed from %d to %d".format(topic, partition, oldLeaderOpt.get, l))
        case None => error("Timing out after %d ms since leader is not elected for partition [%s,%d]"
          .format(timeoutMs, topic, partition))
      }
      leader
    } finally {
      leaderLock.unlock()
    }
  }

  def waitUntilMetadataIsPropagated(servers: Seq[KafkaServer], topic: String, partition: Int, timeout: Long) = {
    Assert.assertTrue("Partition [%s,%d] metadata not propagated after timeout".format(topic, partition),
      waitUntilTrue(() =>
        servers.foldLeft(true)(_ && _.apis.leaderCache.keySet.contains(TopicAndPartition(topic, partition))), timeout))
  }

  /**
   * Wait until the given condition is true or the given wait time ellapses
   */
  def waitUntilTrue(condition: () => Boolean, waitTime: Long): Boolean = {
    val startTime = System.currentTimeMillis()
    while (true) {
      if (condition())
        return true
      if (System.currentTimeMillis() > startTime + waitTime)
        return false
      Thread.sleep(waitTime.min(100L))
    }
    // should never hit here
    throw new RuntimeException("unexpected error")
  }
}
