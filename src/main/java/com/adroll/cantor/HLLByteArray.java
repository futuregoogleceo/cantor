package com.adroll.cantor;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by ifishbein on 1/12/17.
 */
public class HLLByteArray implements Serializable {

    public int length = 0;
    private int shards = 0;
    private int shard_size = 0;

    private byte[][] b_data;

    public HLLByteArray(int length) {
        this.length = length;
        int p = Integer.numberOfTrailingZeros(length);
        this.shards = 1 << (p / 2 + p % 2);
        this.shard_size = 1 << (p / 2);
        b_data = new byte[shards][];
    }

    public HLLByteArray(byte[] src) {
        this.length = src.length;
        int p = Integer.numberOfTrailingZeros(src.length);
        this.shards = 1 << (p / 2 + p % 2);
        this.shard_size = 1 << (p / 2);
        b_data = new byte[shards][];

        for (int i = 0; i < src.length;) {
            if (src[i] != 0) {
                b_data[i / this.shard_size] = Arrays.copyOfRange(src, i / shard_size * shard_size, (i / shard_size + 1) * shard_size);
                i = (i / shard_size + 1) * shard_size;
            } else {
                i++;
            }
        }
    }

    public void put(int index, byte value) {
        if (value == 0)
            return;

        int shard_idx = index / shard_size;
        int value_idx = index % shard_size;

        if (b_data[shard_idx] == null)
            b_data[shard_idx] = new byte[shard_size];

        b_data[shard_idx][value_idx] = value;
    }

    public byte get(int index) {
        int shard_idx = index / shard_size;
        int value_idx = index % shard_size;

        if (b_data[shard_idx] == null)
            return 0;

        return b_data[shard_idx][value_idx];
    }

    /* Merges b into this HLLByteArray */
    public HLLByteArray merge(HLLByteArray b) {
        HLLByteArray a = this;
        byte[][] a_data = a.getBData();
        byte[][] b_data = b.getBData();
        for (int i = 0; i < b_data.length; i++) {
            if (a_data[i] != null && b_data[i] != null) {
                if (a_data[i] == null) {
                    a_data[i] = new byte[b_data[i].length];
                }
                for (int j = 0; j < b_data[i].length; j++) {
                    a_data[i][j] = a_data[i][j] > b_data[i][j] ? a_data[i][j] : b_data[i][j];
                }
            } else if (b_data[i] != null) {
                a_data[i] = b_data[i].clone();
            }
        }

        return a;
    }

    public byte[] getShard(int shard_index) {
        return b_data[shard_index];
    }

    public int getNumShards() {
        return shards;
    }

    public byte[][] getBData() {
        return b_data;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof HLLByteArray))
            return false;
        if (obj == this)
            return true;

        HLLByteArray rhs = (HLLByteArray) obj;
        if (this.length != rhs.length)
            return false;

        for (int i = 0; i < this.shards; i ++) {
            if (!Arrays.equals(b_data[i], rhs.getShard(i)))
                return false;
        }
        return true;
    }
}
