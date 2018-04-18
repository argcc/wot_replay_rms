package ru.an.wot.replay;

public interface PacketIteratorCallback {
	/**
	 * 
	 * @param packet
	 * @return false - прекратить итерацию, true - продолжить
	 */
	public abstract boolean doPacket(ByteArraySlice packet) throws Throwable;
}
