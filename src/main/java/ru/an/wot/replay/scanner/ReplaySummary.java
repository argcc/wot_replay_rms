package ru.an.wot.replay.scanner;

import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;

import ru.an.wot.replay.ByteArraySlice;
import ru.an.wot.replay.PacketIteratorCallback;
import ru.an.wot.replay.Replay;

public class ReplaySummary implements PacketIteratorCallback {
	
	protected static class PacketCount{
		public int count;
		public ByteArraySlice packet;
		public PacketCount(Integer count, ByteArraySlice packet) {
			this.count = count;
			this.packet = packet;
		}
	}
	
	protected Map<Integer, Map<Integer, PacketCount>> packetCounter = new HashMap<Integer, Map<Integer, PacketCount>>();
	protected Replay replay;

	public ReplaySummary(Replay replay) throws Throwable {
		super();
		this.replay = replay;
		this.replay.iteratePackets(this);
	}
	
	@Override
	public boolean doPacket(ByteArraySlice packet) {
		int payload_size = packet.getInt(0);
		int packet_type = packet.getInt(4);
		
		float clock = packet.getFloat(8);
		
		Map<Integer, PacketCount> submap  = packetCounter.get(packet_type);
		if(submap == null){
			submap = new HashMap<Integer, PacketCount>();
			packetCounter.put(packet_type, submap);
		}
		
		PacketCount pc = submap.get(payload_size);
		if(pc == null) {
			pc = new PacketCount(0, packet.copy());
			submap.put(payload_size, pc);
		}
		++pc.count;
		return true;
	}
	
	protected static int getShotsCount(JSONArray end) {
		try {
			int shots = 0;
			JSONObject v = end.getJSONObject(0).getJSONObject("vehicles");
			Iterator<String> vIds = v.keys();
			while (vIds.hasNext()){
				JSONArray vi = v.getJSONArray(vIds.next());
				for (int i=0; i< vi.length(); ++i) {
					shots += vi.getJSONObject(i).getInt("shots");
				}
			}
			return shots;
		}catch(Throwable r) {}
		return -1;
	}

	@Override
	public String toString() {
		String sep = System.lineSeparator();
		
		StringBuffer sb = new StringBuffer();

		sb.append("Packets: " + sep);
		
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
				
				sb.append(String.format("%-20s", ptype + ":" + psize + " - " + pc.count + " "));
				
				for(int i = 0; i < pc.packet.getLength(); ++i) {
					if(i == 12) sb.append("> ");
					if((ptype == 8 || ptype == 7) && i == 16) sb.append("[ ");
					if((ptype == 8 || ptype == 7) && i == 20) sb.append("] ");
					sb.append(String.format("%02X ", pc.packet.getByte(i)));
				}
				
				sb.append(sep);
			}
					
		}
		
		return sb.toString();
	}

}
