package com.adroll.cantor;

import java.io.DataOutput;
import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by ifishbein on 1/12/17.
 */
public class HMNPartitionedArray implements Serializable {

    public int length = 0;
    private int shards = 0;
    private int shard_size = 0;
    private int partition_bits = 6;

    private short[][] b_data;

    public HMNPartitionedArray(int length) {
        if (Integer.bitCount(length) != 1 || length <= 0) {
            throw new IllegalArgumentException("HLLByteArray length must be a power of 2");
        }
        this.length = length;

        // Number of bits required to address all the values stored
        // in this byte array
        int p = Integer.numberOfTrailingZeros(length);

        this.shards = 1 << (p / 2 + p % 2);
        this.shard_size = 1 << (p / 2);
        b_data = new short[shards][];
    }

    public HMNPartitionedArray(short[] src) {
        if (Integer.bitCount(src.length) != 1 || src.length <= 0) {
            throw new IllegalArgumentException("HLLByteArray length must be a power of 2");
        }
        this.length = src.length;
        int p = Integer.numberOfTrailingZeros(src.length);
        this.shards = 1 << (p / 2 + p % 2);
        this.shard_size = 1 << (p / 2);
        b_data = new short[shards][];

        // Break the source array into shards and store them in the right spots
        for (int i = 0; i < src.length;) {
            if (src[i] != 0) {
                b_data[i / this.shard_size] = Arrays.copyOfRange(src, i / shard_size * shard_size, (i / shard_size + 1) * shard_size);
                i = (i / shard_size + 1) * shard_size;
            } else {
                i++;
            }
        }
    }

    public HMNPartitionedArray(HMNPartitionedArray src) {
        this.length = src.length;
        this.shards = src.shards;
        this.shard_size = src.shard_size;
        b_data = new short[shards][];
        for (int i = 0; i < shards; i++) {
            if (src.b_data[i] != null) {
                b_data[i] = Arrays.copyOf(src.b_data[i], shard_size);
            }
        }
    }

    public void put(int index, short value) {
        if (value == 0)
            return;

        int shard_idx = index / shard_size;
        int value_idx = index % shard_size;

        if (b_data[shard_idx] == null)
            b_data[shard_idx] = new short[shard_size];

        b_data[shard_idx][value_idx] = value;
    }

    public short get(int index) {
        int shard_idx = index / shard_size;
        int value_idx = index % shard_size;

        if (b_data[shard_idx] == null)
            return 0;

        return b_data[shard_idx][value_idx];
    }

    /* Merges b into this HLLByteArray */
    public HMNPartitionedArray merge(HMNPartitionedArray b) {
        HMNPartitionedArray a = this;
        short[][] a_data = a.getBData();
        short[][] b_data = b.getBData();
        for (int i = 0; i < b_data.length; i++) {
            if (a_data[i] != null && b_data[i] != null) {
                for (int j = 0; j < b_data[i].length; j++) {
                    int lz =
                    a_data[i][j] = a_data[i][j] > b_data[i][j] ? a_data[i][j] : b_data[i][j];
                }
            } else if (b_data[i] != null) {
                a_data[i] = b_data[i].clone();
            }
        }

        return a;
    }

    public void write(DataOutput out) throws Exception {
        try {
            for (short[] bd : b_data) {
                if (bd == null) {
                    for (int i = 0; i < shard_size; i++) {
                        out.writeShort(0);
                    }
                } else {
                    for (int i = 0; i < shard_size; i++) {
                        out.writeShort(bd[i]);
                    }
                }
            }
        } catch (Exception e) {
            throw e;
        }
    }

    private short[] getShard(int shard_index) {
        return b_data[shard_index];
    }

    public int getNumShards() {
        return shards;
    }

    private short[][] getBData() {
        return b_data;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof HMNPartitionedArray))
            return false;
        if (obj == this)
            return true;

        HMNPartitionedArray rhs = (HMNPartitionedArray) obj;
        if (this.length != rhs.length)
            return false;

        for (int i = 0; i < this.shards; i ++) {
            if (!Arrays.equals(b_data[i], rhs.getShard(i)))
                return false;
        }
        return true;
    }
}
