package com.adroll.cantor;

import java.io.*;
import java.util.Random;

import static org.junit.Assert.*;

import com.google.common.hash.Hashing;
import org.junit.Test;

import com.adroll.cantor.HMNCounter;

public class TestHMNCounter {

  @Test
  public void test_serialization() throws IOException, ClassNotFoundException {
    HMNCounter h = new HMNCounter();
    h.put("a");
    h.put("b");
    h.put("c");
    assertTrue(h.size() == 3L);

    File f = new File("hmn.ser");
    FileOutputStream fos = new FileOutputStream(f);
    ObjectOutputStream oos = new ObjectOutputStream(fos);
    oos.writeObject(h);
    oos.close();
    
    FileInputStream fis = new FileInputStream(f);
    ObjectInputStream ois = new ObjectInputStream(fis);
    HMNCounter hi = (HMNCounter)ois.readObject();
    assertTrue(hi.size() == 3L);
    hi.put("d");
    assertTrue(hi.size() == 4L);

    assertTrue(f.delete());
  }

  @Test
  public void test_combination() throws Exception {
    HMNCounter h1 = new HMNCounter();
    h1.put("a");
    h1.put("b");
    h1.put("c");
    assertTrue(h1.size() == 3L);

    HMNCounter h2 = new HMNCounter();
    h2.put("d");
    h2.put("e");
    h2.put("f");
    assertTrue(h2.size() == 3L);

    HMNCounter h3 = new HMNCounter();
    h3.put("d");
    h3.put("e");
    h3.put("f");
    assertTrue(h3.size() == 3L);

    h1.combine(h2);
    assertTrue(h1.size() == 6L);

    h1.combine(h3);
    assertTrue(h1.size() == 6L);

    h2.combine(h3);
    assertTrue(h2.size() == 3L);

    h1.clear();
    h2.clear();
    h3.clear();

    //h1 and h3 are the same, h2 is a subset
    for(int i = 0; i < 1000000; i++) {
      String s = String.valueOf(Math.random());
      h1.put(s);
      h3.put(s);
      if(i > 500000) {
        h2.put(s);
      }
    }

    //Add more uniques to h2, same to h3
    for(int i = 0; i < 1000000; i++) {
      String s = String.valueOf(Math.random());
      h2.put(s);
      h3.put(s);
    }
    
    //So now the union of h1 and h2 should
    //be h3.
    h1.combine(h2);
    assertTrue(h3.size() == h1.size());
  }

	@Test
	public void test_basic() {
    Random r = new Random(4618201L);
    HMNCounter h = new HMNCounter((byte)8);
    fillHMNCounter(h, r, 25851093);
    assertTrue(h.size() == 22787413L);

    r = new Random(8315542L); 
    h = new HMNCounter((byte)9); 
    fillHMNCounter(h, r, 4954434); 
    assertTrue(h.size() == 5013953L);

    //default precision of HMNCounter.DEFAULT_P = 18
    h = new HMNCounter();
    r = new Random(73919566L); 
    fillHMNCounter(h, r, 17078033); 
    assertTrue(h.size() == 17034653L);

    h.clear();
    r = new Random(57189216L); 
    fillHMNCounter(h, r, 18592874); 
    assertTrue(h.size() == 18526241L);

    h.clear();
    r = new Random(10821894L);
    fillHMNCounter(h, r, 3777716); 
    assertTrue(h.size() == 3760602L);
	}

  @Test
  public void test_intersection() {
    HMNCounter h0 = new HMNCounter((byte)10);
    HMNCounter h1 = new HMNCounter((byte)10);
    HMNCounter h2 = new HMNCounter((byte)10);
    HMNCounter h3 = new HMNCounter((byte)10);
    for(int i = 0; i < 10000; i++) {
      h0.put(String.valueOf(i));
    }
    for(int i = 5000; i < 15000; i++) {
      h1.put(String.valueOf(i));
    }
    for(int i = 8000; i < 11000; i++) {
      h2.put(String.valueOf(i));
    }
    for(int i = 8000; i < 9000; i++) {
      h3.put(String.valueOf(i));
    }

    assertEquals(5235, HMNCounter.intersect(h0, h1)); //about 5000
    assertEquals(2085, HMNCounter.intersect(h0, h2)); //about 2000
    assertEquals(1011, HMNCounter.intersect(h0, h3)); //about 1000
    assertEquals(3076, HMNCounter.intersect(h1, h2)); //about 3000
    assertEquals(1074, HMNCounter.intersect(h1, h3)); //about 1000
    assertEquals(981, HMNCounter.intersect(h2, h3)); //about 1000
    assertEquals(2233, HMNCounter.intersect(h0, h1, h2)); //about 2000
    assertEquals(1207, HMNCounter.intersect(h0, h1, h3)); //about 1000
    assertEquals(1098, HMNCounter.intersect(h0, h2, h3)); //about 1000
    assertEquals(1084, HMNCounter.intersect(h1, h2, h3)); //about 1000
    assertEquals(1207, HMNCounter.intersect(h0, h1, h2, h3)); //about 1000
    assertEquals(0, HMNCounter.intersect());
    assertEquals(0, HMNCounter.intersect(new HMNCounter((byte)10), h0));
    
  }

  private void fillHMNCounter(HMNCounter h, Random r, int n) {
    for(int i = 0; i < n; i++) {
      h.put(String.valueOf(r.nextDouble()));
    }
  }
}
