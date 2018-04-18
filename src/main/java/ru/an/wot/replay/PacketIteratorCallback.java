package ru.an.wot.replay;

public interface PacketIteratorCallback {
	/**
	 * 
	 * @param packet
	 * @return false - ���������� ��������, true - ����������
	 */
	public abstract boolean doPacket(ByteArraySlice packet) throws Throwable;
}
