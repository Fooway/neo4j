/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.ndp.transport.socket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import org.junit.Test;

import org.neo4j.collection.primitive.PrimitiveLongObjectMap;
import org.neo4j.function.Factory;
import org.neo4j.logging.NullLog;
import org.neo4j.ndp.runtime.Session;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.neo4j.collection.primitive.Primitive.longObjectMap;

public class SocketTransportHandlerTest
{

    @Test
    public void shouldCloseSessionOnChannelClose() throws Throwable
    {
        // Given
        Session session = mock(Session.class);
        ChannelHandlerContext ctx = mock( ChannelHandlerContext.class );

        when(ctx.alloc()).thenReturn( UnpooledByteBufAllocator.DEFAULT );

        SocketTransportHandler handler = new SocketTransportHandler( protocolChooser( session ) );

        // And Given a session has been established
        handler.channelRead( ctx, handshake() );

        // When
        handler.channelInactive( ctx );

        // Then
        verify(session).close();
    }

    private SocketTransportHandler.ProtocolChooser protocolChooser( final Session session )
    {
        PrimitiveLongObjectMap<Factory<SocketProtocol>> availableVersions = longObjectMap();
        availableVersions.put( SocketProtocolV1.VERSION, new Factory<SocketProtocol>()
        {
            @Override
            public SocketProtocol newInstance()
            {
                return new SocketProtocolV1( NullLog.getInstance(), session );
            }
        } );

        return new SocketTransportHandler.ProtocolChooser( availableVersions );
    }

    private ByteBuf handshake()
    {
        ByteBuf buf = UnpooledByteBufAllocator.DEFAULT.buffer();
        buf.writeInt( 1 );
        buf.writeInt( 0 );
        buf.writeInt( 0 );
        buf.writeInt( 0 );
        return buf;
    }

}