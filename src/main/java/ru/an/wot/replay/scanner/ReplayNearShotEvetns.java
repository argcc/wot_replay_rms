package ru.an.wot.replay.scanner;

import java.util.*;

import ru.an.wot.replay.ByteArraySlice;
import ru.an.wot.replay.PacketIteratorCallback;
import ru.an.wot.replay.Replay;
import ru.an.wot.replay.scanner.ReplaySummary.PacketCount;

public class ReplayNearShotEvetns implements PacketIteratorCallback {

	protected String sep;
	protected float timeDelta;
	protected LinkedList<ByteArraySlice> packetQueue;
	protected float lastShotTime;
	protected StringBuffer sb = new StringBuffer();
	protected int counter = 0;

	public ReplayNearShotEvetns(Replay replay, float timeDelta) throws Throwable {
		super();
		sep = System.lineSeparator();
		if(timeDelta < 0.0001f) timeDelta = 0.0001f;
		this.timeDelta = timeDelta * 0.5f;
		packetQueue = new LinkedList<ByteArraySlice>();
		lastShotTime = -1000.0f;
		replay.iteratePackets(this);
	}
	
	protected void printPacket(ByteArraySlice packet) {
		int type = packet.getInt(4);
		float time = packet.getFloat(8);
		
		if(counter % 20 == 0){
			sb.append("                ");
			
			for(int i=0; i <= 80; i+=4) {
				if(i == 12 
						//|| i == 24
						) sb.append("  ");
				sb.append(String.format("%9s", String.format("%02d", i)));
			}
			
			sb.append(sep);
		}
			
		++counter;
		
		if(type == 8 && packet.getInt(16) == 0)
			sb.append(String.format("%16s", type + " SHOT " + time));
		else
			sb.append(String.format("%16s", type + " ---- " + time));
		
		sb.append("  HEX:");
		for(int i = 0; i < packet.getLength(); ++i) {
			if(i == 12) sb.append(" >");
			//if(type == 8 && i == 24) sb.append(" >");
			sb.append(String.format("%s%02X", (i%4==0)?" ":"", packet.getByte(i)));
		}
		
		//if(type == 8 && packet.getInt(16) == 22)
		//	sb.append(" #" + packet.getFloat(52));
		
		sb.append(sep);
	}
	
	@Override
	public boolean doPacket(ByteArraySlice packet) {
		float time = packet.getFloat(8);
		int type = packet.getInt(4);
		
		if (type == 7 || type == 10 || type == 26 ||
				type == 28 || type == 29 || type == 31 || 
				type == 38 || type == 32 || type == 5 ||
				type == 50
				)
			return true;
		
		if(type == 8 && packet.getInt(16) == 0)
			lastShotTime = time;
		
		if(time - lastShotTime > timeDelta){
			while(packetQueue.size() > 0 && packetQueue.getFirst().getFloat(8) < time - timeDelta)
				packetQueue.removeFirst();
			
			packetQueue.add(packet.copy());
		}
		else {
			while(packetQueue.size() > 0 && packetQueue.getFirst().getFloat(8) < time - timeDelta)
				packetQueue.removeFirst();
			
			while(packetQueue.size() > 0)
				printPacket(packetQueue.removeFirst());
			
			printPacket(packet);
		}
			
		return true;
	}

	@Override
	public String toString() {
		
		return sb.toString();
	}
	
	

}
