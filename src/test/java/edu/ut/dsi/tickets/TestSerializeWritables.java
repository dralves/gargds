package edu.ut.dsi.tickets;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.junit.Test;

import edu.ut.dsi.tickets.Message.MsgType;
import edu.ut.dsi.tickets.mutex.Clock.Timestamp;
import edu.ut.dsi.tickets.mutex.MutexReq;

public class TestSerializeWritables {

  @SuppressWarnings("unchecked")
  public static <T extends Writable> T testSerializeWritable(T object) throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(baos);
    object.write(dos);
    baos.flush();
    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    DataInputStream dis = new DataInputStream(bais);
    T serialized = (T) object.getClass().newInstance();
    serialized.read(dis);
    return serialized;
  }

  @Test
  public void testSerializeMutexReqMessage() throws Exception {
    Message<MutexReq> msg = new Message<MutexReq>(MsgType.CS_REQ, new Timestamp(1), 10, new MutexReq(20));
    System.out.println(msg);
    Message<MutexReq> serialized = testSerializeWritable(msg);
    System.out.println(serialized);
  }

}
