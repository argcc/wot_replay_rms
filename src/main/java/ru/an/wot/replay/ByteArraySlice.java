package ru.an.wot.replay;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ByteArraySlice {

	private byte[] data;
	private int start, length;
	
	public ByteArraySlice(byte[] data, int start, int length) {
		init(data, start, length);
	}
	
	public void init(byte[] data, int start, int length)
	{
		if(start < 0  || length < 0 || start+length > data.length)
			throw new IllegalArgumentException();
		this.data = data;
		this.start = start;
		this.length = length;
	}
	
	public ByteArraySlice(byte[] data) {
		this(data, 0, data.length);
	}
	
	public void init(byte[] data){
		init(data, 0, data.length);
	}
	
	public void getBytes(int pos, byte[] out, int dest_start, int dest_length){
		checkBounds(pos, dest_length);
		System.arraycopy(data, start+pos, out, dest_start, dest_length);
	}
	
	public void getBytes(int pos, byte[] out){
		checkBounds(pos, out.length);
		System.arraycopy(data, start+pos, out, 0, out.length);
	}
	
	public byte getByte(int pos) {
		checkBounds(pos, 1);
		return data[start + pos];
	}
	
	public int getLength() {return length;}
	
	private void checkBounds(int pos, int length){
		if(pos < 0 || start + pos + length > data.length)
			throw new IndexOutOfBoundsException(
					String.format("array length: %d\npos: %d\nlength: %d",
							this.length, pos, length));
	}
	
	public ByteArraySlice copy() {
		return getSlice(0);
	}
	
	public void copyTo(ByteArraySlice out) {
		getSlice(0, out);
	}
	
	public ByteArraySlice getSlice(int pos){
		return getSlice(pos, length - pos);
	}
	
	public ByteArraySlice getSlice(int pos, int length){
		checkBounds(pos, length);
		return new ByteArraySlice(data, start + pos, length);
	}
	
	public void getSlice(int pos, ByteArraySlice out){
		getSlice(pos, length - pos, out);
	}
	
	public void getSlice(int pos, int length, ByteArraySlice out){
		checkBounds(pos, length);
		out.data = data;
		out.start = start + pos;
		out.length = length;
	}
	
	public String getString() {
		return getString(0, length);
	}
	
	public String getString(int pos){
		return getString(pos, length - pos);
	}
	
	public String getString(int pos, int len){
		checkBounds(pos, len);
		StringBuffer sb = new StringBuffer(len);
		for(int i=0; i<len; ++i)
			sb.append((char)data[start+pos+i]);
		return sb.toString();
	}
	
	public int getInt(int pos){
		checkBounds(pos, 4);
		return ByteBuffer.wrap(data, start + pos, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
	}
	
	public short getShort(int pos) {
		checkBounds(pos, 2);
		return ByteBuffer.wrap(data, start + pos, 2).order(ByteOrder.LITTLE_ENDIAN).getShort();
	}
	
	public float getFloat(int pos) {
		checkBounds(pos, 4);
		return ByteBuffer.wrap(data, start + pos, 4).order(ByteOrder.LITTLE_ENDIAN).getFloat();
	}
}
