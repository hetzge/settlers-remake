package jsettlers.network.infrastructure.channel.listeners;

import java.io.IOException;
import java.util.function.Consumer;

import jsettlers.network.NetworkConstants.ENetworkKey;
import jsettlers.network.infrastructure.channel.GenericDeserializer;
import jsettlers.network.infrastructure.channel.packet.Packet;

public class SimpleListener<T extends Packet> extends PacketChannelListener<T> {

	private final Consumer<T> consumer;

	public SimpleListener(ENetworkKey key, Class<T> packetClass, Consumer<T> consumer) {
		super(key, new GenericDeserializer<T>(packetClass));
		this.consumer = consumer;
	}

	@Override
	protected void receivePacket(ENetworkKey key, T packet) throws IOException {
		consumer.accept(packet);
	}
}