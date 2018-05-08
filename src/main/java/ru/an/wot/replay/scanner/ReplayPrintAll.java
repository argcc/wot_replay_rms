package ru.an.wot.replay.scanner;

import java.io.FileWriter;
import java.io.IOException;

import ru.an.wot.replay.ByteArraySlice;
import ru.an.wot.replay.PacketIteratorCallback;
import ru.an.wot.replay.Replay;


public class ReplayPrintAll implements PacketIteratorCallback {

	protected Replay replay;
	protected int counter = 0;
	protected FileWriter out;
	protected String sep = System.lineSeparator();

	public ReplayPrintAll(Replay replay, FileWriter out) throws Throwable {
		super();
		this.replay = replay;
		this.out = out;
		
		this.replay.iteratePackets(this);
	}
	
	@Override
	public boolean doPacket(ByteArraySlice packet) {
		int type = packet.getInt(4);
		float time = packet.getFloat(8);
		
		//if(type != 8 || packet.getInt(16) != 14) return true;
		
		if(type == 8 && packet.getInt(12) == replay.vehicleId && packet.getInt(16) == 0)
			try {
				out.write(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" + sep);
			} catch (IOException e1) {
			}
		
		try {
			if(counter % 20 == 0){
				out.write("                ");
				for(int i=0; i <= 80; i+=4) {
					if(i == 12 
							//|| i == 24
							) out.write("  ");
					out.write(String.format("%9s", String.format("%02d", i)));
				}
				out.write(sep);
			}
			++counter;
			
			if(type == 8 && packet.getInt(16) == 0)
				out.write(String.format("%16s", type + " SHOT " + time));
			else
				out.write(String.format("%16s", type + " ---- " + time));
			
			out.write("  HEX:");
			for(int i = 0; i < packet.getLength(); ++i) {
				if(i == 12) out.write(" >");
				//if(/*type == 8 &&*/ i == 24) out.write(" >");
				out.write(String.format("%s%02X", (i%4==0)?" ":"", packet.getByte(i)));
			}
			out.write(sep);
		}
		catch(IOException e) {throw new RuntimeException(e);}
		return true;
	}

}
