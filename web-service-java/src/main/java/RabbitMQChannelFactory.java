import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.DestroyMode;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;


public class RabbitMQChannelFactory extends BasePooledObjectFactory<Channel> {

    private final Connection connection;

    public RabbitMQChannelFactory(Connection connection) {
        this.connection = connection;
    }
    @Override
    public Channel create() throws Exception {
        return connection.createChannel();
    }

    @Override
    public PooledObject<Channel> wrap(Channel channel) {
        return new DefaultPooledObject<>(channel);
    }

    @Override
    public void destroyObject(PooledObject<Channel> pooledObject) throws Exception {
        Channel channel = pooledObject.getObject();
        channel.close();
    }

    @Override
    public boolean validateObject(PooledObject<Channel> pooledObject) {
        Channel channel = pooledObject.getObject();
        return channel.isOpen();
    }
}
