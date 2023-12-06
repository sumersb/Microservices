import com.rabbitmq.client.Connection;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import com.rabbitmq.client.Channel;


import org.apache.commons.pool2.impl.GenericObjectPool;

public class RabbitMQChannelPool {
    private final GenericObjectPool<Channel> channelPool;

    public RabbitMQChannelPool(Connection connection) {
        RabbitMQChannelFactory channelFactory = new RabbitMQChannelFactory(connection);
        // Configure pool parameters (e.g., maxTotal, maxIdle, etc.)
        GenericObjectPoolConfig<Channel> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(10); // Maximum number of objects that can be allocated by the pool
        poolConfig.setMaxIdle(5); // Maximum number of objects that can be sitting idle in the pool
        poolConfig.setMinIdle(2); // Minimum number of objects that can be sitting idle in the pool
        poolConfig.setMaxWaitMillis(3000); // Maximum time (in milliseconds) the borrowObject method should block before throwing an exception
        this.channelPool = new GenericObjectPool<>(channelFactory, poolConfig);
    }

    public Channel borrowChannel() throws Exception {
        return channelPool.borrowObject();
    }

    public void returnChannel(Channel channel) {
        channelPool.returnObject(channel);
    }

    public void close() {
        channelPool.close();
    }
}
