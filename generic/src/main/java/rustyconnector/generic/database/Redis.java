package rustyconnector.generic.database;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import javax.security.auth.callback.Callback;

public class Redis {
    private String host;
    private int port;
    private String password;
    private String dataChannel;
    private Jedis client;
    private Jedis jedisSubscriber;
    private Subscriber subscriber;
    private JedisPool pool;

    public Redis() {}

    /**
     * Sets the connection
     */
    public void setConnection(String host, int port, String password, String dataChannel) {
        this.host = host;
        this.port = port;
        this.password = password;
        this.dataChannel = dataChannel;
    }

    /**
     * Tests the connection to the provided Redis server
     */
    public void connect(Callback callback) throws ExceptionInInitializerError{
        try{
            if(!(this.client == null)) return;

            this.client = new Jedis(this.host, this.port);
            this.client.auth(this.password);
            this.client.connect();

            final JedisPoolConfig poolConfig = new JedisPoolConfig();
            this.pool = new JedisPool(poolConfig, this.host, this.port, 0);
            this.jedisSubscriber = this.pool.getResource();
            this.jedisSubscriber.auth(this.password);
            this.subscriber = new Subscriber();

            Subscriber.setCallback(callback);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        jedisSubscriber.subscribe(subscriber, dataChannel);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ExceptionInInitializerError();
        }
    }

    public void publish(String message) {
        this.client.publish(this.dataChannel, message);
    }

    /**
     * Disconnects
     */
    public void disconnect(Callback callback) throws ExceptionInInitializerError {
        try {
            this.subscriber.unsubscribe();
            this.jedisSubscriber.close();
            this.client.close();
            this.client.disconnect();
            callback.call();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public class Subscriber extends JedisPubSub {
        private static Callback callback;

        public static void setCallback(Callback callback) {
            Subscriber.callback = callback;
        }

        @Override
        public void onMessage(String channel, String message) {
            try {
                Subscriber.callback.call(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
