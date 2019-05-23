package com.javaexcel.automation.alm;

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import javax.annotation.Priority;
import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;

@Priority(Integer.MIN_VALUE)
@ConstrainedTo(RuntimeType.CLIENT)
class LoggingStream extends FilterOutputStream {

    private final StringBuilder b = new StringBuilder();
    private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private static final int MAX_ENTITY_SIZE = 1024 * 8;

    LoggingStream(final OutputStream inner) {
        super(inner);
    }

    StringBuilder getStringBuilder(final Charset charset) {
        // write entity to the builder
        final byte[] entity = baos.toByteArray();

        b.append(new String(entity, 0, 
                            Math.min(entity.length, MAX_ENTITY_SIZE), charset));
        if (entity.length > MAX_ENTITY_SIZE) {
            b.append("...more...");
        }
        b.append('\n');

        return b;
    }

    @Override
    public void write(final int i) throws IOException {
        if (baos.size() <= MAX_ENTITY_SIZE) {
            baos.write(i);
        }
        out.write(i);
    }
}
