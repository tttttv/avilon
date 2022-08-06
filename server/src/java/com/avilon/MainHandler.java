package com.avilon;

import com.github.jasync.sql.db.Configuration;
import com.github.jasync.sql.db.Connection;
import com.github.jasync.sql.db.ConnectionPoolConfiguration;
import com.github.jasync.sql.db.QueryResult;
import com.github.jasync.sql.db.general.ArrayRowData;
import com.github.jasync.sql.db.mysql.MySQLConnectionBuilder;
import com.github.jasync.sql.db.pool.ConnectionPool;
import com.github.jasync.sql.db.pool.PoolConfiguration;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.util.CharsetUtil;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class MainHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Подключение");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        while (buf.readableBytes() > 0){
            System.out.print((char) buf.readByte());
        }
        buf.release(); //Освобождает память, иначе буффер не освободится
        System.out.println("Чтенние завершено");

        ByteBuf in = (ByteBuf) msg;

        String st = "HTTP/1.1 200 OK\n" +
                "Server: Apache-Coyote/1.1 \n" +
                "Content-Encoding: gzip\n" +
                "Content-Type: text/xml;charset=UTF-8 \n" +
                "Content-Length: 20\n" +
                "Date: Mon, 14 Dec 2015 10:02:01 GMT";
        ctx.writeAndFlush(Unpooled.copiedBuffer(st, CharsetUtil.UTF_8));
        System.out.println("Отправка завершена");
        ctx.close();


        Connection connection = MySQLConnectionBuilder.createConnectionPool("jdbc:mysql://0.0.0.0:3306/avilon?user=root&password=123456");

        CompletableFuture<QueryResult> future = connection.sendPreparedStatement("INSERT INTO locations (imei, test) VALUES ('1234567989', 4)");
        // work with result ...
        // Close the connection pool
        connection.disconnect().get();

        /*
        Channel ch = ctx.channel();
        ChannelFuture f = ch.write(msg);
        f.addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture future) {
                Channel ch = ctx.channel();
                ch.close();
            }
        });

         */
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Отключение");
    }
}
