import com.rabbitmq.client.Connection;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import com.rabbitmq.client.Channel;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeoutException;


import org.apache.commons.pool2.impl.GenericObjectPool;

public class RabbitMQChannelPool {
    private final LinkedBlockingQueue<Channel> channelPool;
    Connection connection;

    public RabbitMQChannelPool(Connection connection, int poolSize) throws IOException {
        this.connection = connection;
        channelPool = new LinkedBlockingQueue<>();
        for (int i = 0; i < poolSize; i++) {
            channelPool.add(connection.createChannel());
        }
        System.out.println("lemon face");
    }

    public Channel borrowChannel() throws Exception {
        return channelPool.poll();
    }

    public void releaseChannel(Channel channel) {
        if (channel != null && channel.isOpen()) {
            channelPool.offer(channel);
        }
    }

    public void close () throws IOException, TimeoutException {
        for (Channel channel : channelPool) {
            channel.close();
        }
        connection.close();
    }


}
