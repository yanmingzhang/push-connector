package io.sunfly.push;

import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;

public interface Message {
    public static final int MAX_BYTES_PER_CHAR_UTF8 =
            (int)CharsetUtil.encoder(CharsetUtil.UTF_8).maxBytesPerChar();

    // MUST equal to or larger than the required size
    public int estimateSize();

    public void encode(ByteBuf out);

    public void decode(ByteBuf in);
}
