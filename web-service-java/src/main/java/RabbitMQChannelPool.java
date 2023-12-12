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
        //Set connection of pool
        this.connection = connection;
        //Set storage for channel pool
        channelPool = new LinkedBlockingQueue<>();
        //Create channels for given pool Size
        for (int i = 0; i < poolSize; i++) {
            channelPool.add(connection.createChannel());
        }
    }

    public Channel borrowChannel() throws Exception {
        //Waits until channel can be taken
        return channelPool.poll();
    }

    public void releaseChannel(Channel channel) {
        //Waits until channel can be returned to pool
        if (channel != null && channel.isOpen()) {
            channelPool.offer(channel);
        }
    }

    public void close () throws IOException, TimeoutException {
        //Close everything
        for (Channel channel : channelPool) {
            channel.close();
        }
        connection.close();
    }


}
