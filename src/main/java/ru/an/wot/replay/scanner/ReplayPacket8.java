package ru.an.wot.replay.scanner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ru.an.wot.replay.ByteArraySlice;
import ru.an.wot.replay.PacketIteratorCallback;
import ru.an.wot.replay.Replay;
import ru.an.wot.replay.scanner.ReplaySummary.PacketCount;

public class ReplayPacket8 implements PacketIteratorCallback {
	
	protected Map<Integer, Map<Integer, PacketCount>> packetCounter = new HashMap<Integer, Map<Integer, PacketCount>>();
	protected String sep;

	public ReplayPacket8(Replay replay) throws Throwable {
		super();
		replay.iteratePackets(this);
		sep = System.lineSeparator();
	}

	@Override
	public boolean doPacket(ByteArraySlice packet) {
		int packet_type = packet.getInt(4);
		if(packet_type == 8) {
			int payload_size = packet.getInt(20);
			//float clock = packet.getFloat(8);
			int subtype = packet.getInt(16);
			
			Map<Integer, PacketCount> submap  = packetCounter.get(subtype);
			if(submap == null){
				submap = new HashMap<Integer, PacketCount>();
				packetCounter.put(subtype, submap);
			}
			
			PacketCount pc = submap.get(payload_size);
			if(pc == null) {
				pc = new PacketCount(0, packet.copy());
				submap.put(payload_size, pc);
			}
			++pc.count;
		}
		return true;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		Iterator<Integer> iterPType = packetCounter.keySet().iterator();
		List<Integer> ptypes = new ArrayList<Integer>();
		while(iterPType.hasNext()) ptypes.add(iterPType.next());
		Collections.sort(ptypes);
		
		for(Integer ptype : ptypes) {
			Iterator<Integer> iterPSizes = packetCounter.get(ptype).keySet().iterator();
			List<Integer> psizes = new ArrayList<Integer>();
			while(iterPSizes.hasNext()) psizes.add(iterPSizes.next());
			Collections.sort(psizes);
			for(Integer psize : psizes) {
				PacketCount pc = packetCounter.get(ptype).get(psize);
				sb.append(String.format("%4s[%16s]", ptype.toString(), Integer.toBinaryString(ptype)).replaceAll(" ", "0"));
				sb.append(String.format("%-14s", ":" + psize + " - " + pc.count + " "));
				
				for(int i = 0; i < pc.packet.getLength(); ++i) {
					if(i == 12) sb.append("> ");
					if(i == 24) sb.append("> ");
					sb.append(String.format("%02X ", pc.packet.getByte(i)));
				}
				
				sb.append(sep);
			}
					
		}
		return sb.toString();
	}

}
