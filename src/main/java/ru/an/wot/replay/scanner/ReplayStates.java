package ru.an.wot.replay.scanner;

import ru.an.wot.replay.ByteArraySlice;
import ru.an.wot.replay.PacketIteratorCallback;

public class ReplayStates implements PacketIteratorCallback {

	@Override
	public boolean doPacket(ByteArraySlice packet) {
		int type = packet.getInt(4);
		float time = packet.getFloat(8);

		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < packet.getLength(); ++i) {
			if(i == 12) sb.append(" >");
			sb.append(String.format("%s%02X", (i%4==0)?" ":"", packet.getByte(i)));
		}
		
		if(type == 22)
			System.out.println("-STATE-> "+time+" >"+sb.toString());
		//-STATE-> 56.105 > 04000000 16000000 856B6042 > 03000000

		
		return true;
	}

}
