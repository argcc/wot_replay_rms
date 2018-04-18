package ru.an.wot.replay;

import java.util.Iterator;
import java.util.zip.Inflater;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONArray;
import org.json.JSONObject;



public class Replay {
	
	public int infoBlockCount;
	public int headerBlockSize;
	public int endBlockSize;
	public JSONObject header;
	public String playerName;
	public String clientVersionExe;
	public String clientVersionXml;
	public int playerId;
	public int vehicleId;
	public String vehicleName;
	public String gameplayId;
	public int battleType;
	public String mapName;
	
	protected ByteArraySlice replay;
	
	private static final byte[] key = new byte[]{
			(byte)0xDE, (byte)0x72, (byte)0xBE, (byte)0xA0, 
			(byte)0xDE, (byte)0x04, (byte)0xBE, (byte)0xB1, 
			(byte)0xDE, (byte)0xFE, (byte)0xBE, (byte)0xEF,
			(byte)0xDE, (byte)0xAD, (byte)0xBE, (byte)0xEF};
	
	private static void decrupt(byte[] input) throws Exception{
		Cipher cipher = Cipher.getInstance("Blowfish/ECB/NoPadding");
		cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "Blowfish"));
		
		byte[] previous = new byte[8];
		byte[] chunk = new byte[8];
		
		for(int i=0; i < input.length; i += 8){
			cipher.doFinal(input, i, 8, chunk, 0);
			for(int j=0; j<8; ++j){
				previous[j] ^= chunk[j];
				input[i+j] = previous[j];
			}
		}
	}

	public Replay(ByteArraySlice filedata, CheckHeader checker) throws Exception {
		
		int offset = 0;
		
		{
			//File header
			int file_signature = filedata.getInt(offset); offset+=4;
			if (file_signature != 288633362)
				throw new ReplayException("File signature check fail");
	
			infoBlockCount = filedata.getInt(offset); offset+=4;
			if(infoBlockCount != 3 && infoBlockCount != 2 && infoBlockCount != 1)
				throw new ReplayException("Data block count fail: " + infoBlockCount);
		}
		
		{
			//Replay header
			headerBlockSize = filedata.getInt(offset); offset+=4;
			
			header = new JSONObject(filedata.getString(offset, headerBlockSize));
			offset+=headerBlockSize;
			
			playerName = header.getString("playerName");
			
			if(header.has("clientVersionFromExe"))
				clientVersionExe =  header.getString("clientVersionFromExe");
			else
				clientVersionExe = "-unknown-";
			
			if(header.has("clientVersionFromXml"))
				clientVersionXml =  header.getString("clientVersionFromXml");
			else
				clientVersionXml = "-unknown-";
				
			playerId = header.getInt("playerID");
			vehicleName = header.getString("playerVehicle");
			gameplayId = header.getString("gameplayID");
			
			if(header.has("battleType"))
				battleType = header.getInt("battleType");
			else
				battleType = -1;
			
			mapName = header.getString("mapName");
			
			JSONObject vehicles = header.getJSONObject("vehicles");
			Iterator<String> i = vehicles.keys();
			vehicleId = -1;
			while (i.hasNext())
			{
				String key = i.next();
				if(vehicles.getJSONObject(key).getString("name").equals(playerName)){
					vehicleId = Integer.parseInt(key);
					break;
				}
			}
			
			if (vehicleId < 0) throw new ReplayException("Player ID not found");
		}
		
		checker.check(this);
		
		//Skip replay end block
		if(infoBlockCount >= 2){
			endBlockSize = filedata.getInt(offset); offset+=4;
			//end = new JSONArray(filedata.getString(offset, endBlockSize));
			offset += endBlockSize;
		}
//		else
//			end = null;
		
		if(infoBlockCount >= 3){
			int ubsize = filedata.getInt(offset); offset+=4;
			offset += ubsize;
		}
		
		
		
		//Decrupt&Unzip replay data
		{
			
			int encrupted_data_size = filedata.getInt(offset); offset+=4;
//			int crupted_data_size = 
					filedata.getInt(offset); offset+=4;
					

			//align size
			//crupted_data_size = ((int)(crupted_data_size/8))*8 + 8;

			ByteArraySlice zip_slice = filedata.getSlice(offset);
			int zip_slice_length = zip_slice.getLength();
			zip_slice_length += 8- (zip_slice_length % 8);
			byte[] zip_data = new byte[zip_slice_length];
			zip_slice.getBytes(0, zip_data, 0, zip_slice.getLength());
			
			if(zip_data.length % 8 != 0)
				throw new ReplayException("Replay data size is not aligned: " + zip_data.length);

			
			decrupt(zip_data);
			
			//unzip
			Inflater decompresser = new Inflater();
			decompresser.setInput(zip_data);
			byte[] replay_data = new byte[encrupted_data_size];
//			int resultLength = 
					decompresser.inflate(replay_data);
			decompresser.end();
			
			replay = new ByteArraySlice(replay_data);
		}
	}
	
	public void iteratePackets(PacketIteratorCallback pic) throws Throwable {
		ByteArraySlice packet = replay.copy();
		int offset = 0;
		
		while(true)
		{
			if(offset + 12 > replay.getLength() || offset >= replay.getLength()) break;
			int payload_size = replay.getInt(offset);
			
			replay.getSlice(offset, 12 + payload_size, packet);
			offset += 12 + payload_size;
			
			if(!pic.doPacket(packet)) break;
		}
	}
}
